package me.ele.napos.evalon.http

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import me.ele.napos.evalon.exceptions.EvalonParamErrorException
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.DecompressingEntity
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.*
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.FileEntity
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager

class EvalonHttpUtil {
    private static final HttpClient client = HttpClientBuilder.create()
            .disableRedirectHandling()
            .disableContentCompression()
            .build()

    private static final logger = LogManager.getLogger()

    private static final Map<String, Map<String, String>> COMMON_HEADER = [:]

    private static final Map<String, Closure> COMMON_ERROR_HANDLER = [:]

    private static final Map<String, Integer> RETRY = [:] // 重试次数

    private static final Map<String, Integer> TIMEOUT = [:] // 超时时间 单位/秒

    static setRetry(List<String> urls, int retry) {
        urls.each {
            setRetry(it, retry)
        }
    }

    static setRetry(String baseUrl, int retry) {
        if (retry <= 0) {
            throw new EvalonParamErrorException("重试次数必须大于0次")
        }

        RETRY[baseUrl] = retry
    }

    static setTimeout(String baseUrl, int timeoutInSeconds) {
        if (timeoutInSeconds <= 0) {
            throw new EvalonParamErrorException("超时时间必须大于0秒")
        }

        TIMEOUT[baseUrl] = timeoutInSeconds
    }

    static setHeaders(String baseUrl, Map headers = [:]) {
        if (!COMMON_HEADER[baseUrl]) {
            COMMON_HEADER[baseUrl] = [:]
        }

        COMMON_HEADER[baseUrl] = headers
    }

    static setErrorHandler(String baseUrl, Closure errorHandler) {
        COMMON_ERROR_HANDLER[baseUrl] = errorHandler
    }

    private static Closure DEFAULT_ERROR_HANDLER = { HttpResponse response ->
        throw new EvalonHttpException("发生Http异常: " +
                " Code: ${response.statusLine.statusCode}" +
                " Reason: ${response.statusLine.reasonPhrase}")
    }

    static httpGet(EvalonHttpOperation condition) {
        return http(EvalonHttpMethod.GET,
                condition.toUrl(),
                condition.parameters,
                condition.headers,
                condition.returnType,
                condition.errorHandler)
    }

    static <T> T httpPost(EvalonHttpOperation<T> condition) {
        return http(EvalonHttpMethod.POST,
                condition.toUrl(),
                condition.parameters,
                condition.headers,
                condition.returnType,
                condition.errorHandler)
    }

    static httpPut(EvalonHttpOperation condition) {
        return http(EvalonHttpMethod.PUT,
                condition.toUrl(),
                condition.parameters,
                condition.headers,
                condition.returnType,
                condition.errorHandler)
    }

    static httpDelete(EvalonHttpOperation condition) {
        return http(EvalonHttpMethod.DELETE,
                condition.toUrl(),
                condition.parameters,
                condition.headers,
                condition.returnType,
                condition.errorHandler)
    }

    static httpGet(String url, Object parameters = null,
                   Class<?> returnType = Object,
                   Closure errorHandler = null) {
        return http(EvalonHttpMethod.GET, url, parameters, [:], returnType, errorHandler)
    }

    static httpPost(String url, Object parameters = null,
                    Class<?> returnType = Object,
                    Closure errorHandler = null) {
        return http(EvalonHttpMethod.POST, url, parameters, [:], returnType, errorHandler)
    }

    static httpPut(String url, Object parameters = null,
                   Class<?> returnType = Object,
                   Closure errorHandler = null) {
        return http(EvalonHttpMethod.PUT, url, parameters, [:], returnType, errorHandler)
    }

    static httpDelete(String url, Object parameters = null,
                      Class<?> returnType = Object,
                      Closure errorHandler = null) {
        return http(EvalonHttpMethod.DELETE, url, parameters, [:], returnType, errorHandler)
    }

    private static addHeaders(Map<String, String> headers, String host, HttpRequestBase base) {
        headers.each { String key, String value ->
            base.setHeader(key, value)
        }

        COMMON_HEADER[host].each { String key, String value ->
            base.setHeader(key, value)
        }
    }

    static httpGet(String url, TypeReference typeReference) {
        return httpGet(url, null, [:], typeReference)
    }

    static httpGet(String url,
                   Object parameters,
                   Map<String, String> headers = [:],
                   TypeReference typeReference,
                   Closure errorHandler = null) {
        String host = new URI(url).host

        url = setParametersToURL(url, parameters) // http get 不能有body

        HttpRequestBase base = new HttpGet(url)

        addHeaders(headers, host, base)

        HttpResponse response = executeRequestWithRetry(host, base)

        assert response != null

        return consumeHttpResponseWithErrorHandler(response, url, typeReference, errorHandler)
    }

    private static HttpResponse executeRequestWithRetry(String host, HttpRequestBase base) {
        if (RETRY[host]) {
            Exception exp = null
            for (int i = 0; i < RETRY[host]; i++) {
                try {
                    return client.execute(base)
                } catch (Exception e) {
                    exp = e
                    logger.warn("调用${base}失败... 重试次数: ${i}")
                }
            }
            throw new EvalonHttpException("发生Http异常: ${exp.getMessage()}")
        } else {
            try {
                return client.execute(base)
            } catch (IOException e) {
                throw new EvalonHttpException("发生Http异常: ${e.getMessage()}")
            }
        }
    }

    private
    static consumeHttpResponseWithErrorHandler(HttpResponse response, String url, TypeReference typeReference, Closure errorHandler) {
        int code = response.statusLine.statusCode

        if (code != HttpURLConnection.HTTP_OK) {
            if (errorHandler) {
                return errorHandler(response)
            } else if (COMMON_ERROR_HANDLER.containsKey(getBaseUrl(url))) {
                return COMMON_ERROR_HANDLER[getBaseUrl(url)](response)
            } else {
                return DEFAULT_ERROR_HANDLER(response)
            }
        }

        try {
            if (isApplicationJsonResponse(response)) {
                return JSON.parseObject(EntityUtils.toString(response.entity), typeReference.getType())
            }
        } finally {
            EntityUtils.consume(response.entity)
        }

        return null
    }

    private static boolean isApplicationJsonResponse(HttpResponse response) {
        return response.getAllHeaders().any {
            it.name == "Content-Type" && it.value == "application/json"
        }
    }

    private static http(EvalonHttpMethod,
                        String url,
                        Object parameters,
                        Map<String, String> headers = [:],
                        TypeReference typeReference,
                        Closure errorHander) {
    }

    private static http(EvalonHttpMethod method,
                        String url,
                        Object parameters,
                        Map<String, String> headers = [:],
                        Class<?> returnType,
                        Closure errorHandler) {
        String host = new URI(url).host

        HttpRequestBase base = null

        switch (method) {
            case EvalonHttpMethod.GET:
                url = setParametersToURL(url, parameters)
                base = new HttpGet(url)
                addHeaders(headers, host, base)
                break
            case EvalonHttpMethod.POST:
                base = new HttpPost(url)
                addHeaders(headers, host, base)
                setParametersToBody(base, parameters)
                break
            case EvalonHttpMethod.PUT:
                base = new HttpPut(url)
                addHeaders(headers, host, base)
                setParametersToBody(base, parameters)
                break
            case EvalonHttpMethod.DELETE:
                base = new HttpDelete(url)
                addHeaders(headers, host, base)
                break
        }

        HttpResponse response = null

        int timeout = 10 * 1000

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build()

        base.setConfig(requestConfig)

        if (RETRY[host]) {
            for (int i = 0; i < RETRY[host]; i++) {
                try {
                    response = client.execute(base)
                    break
                } catch (Exception ignored) {
                    logger.warn("调用${base}失败... 重试次数: ${i}")
                }
            }
        } else {
            try {
                response = client.execute(base)
            } catch (IOException e) {
                throw new EvalonHttpException("发生Http异常: ${e.getMessage()}")
            }
        }


        assert response != null

        try {
            if (response.statusLine.statusCode == HttpURLConnection.HTTP_OK) {
                if (!returnType) {
                    return JSON.parseObject(EntityUtils.toString(response.entity, "UTF-8"))
                }

                if (InputStream.isAssignableFrom(returnType)) {
                    return new ByteArrayInputStream(EntityUtils.toByteArray(response.entity))
                }

                String content

                if (response.entity instanceof DecompressingEntity) {
                    content = response.entity.getContent().getText("UTF-8")
                } else {
                    content = EntityUtils.toString(response.entity, "UTF-8")
                }

                if (String.isAssignableFrom(returnType)) {
                    return content
                }

                if (List.isAssignableFrom(returnType)) {
                    return JSON.parseArray(content)
                }

                return JSON.parseObject(content, returnType)
            } else {
                if (errorHandler) {
                    return errorHandler(response)
                } else if (COMMON_ERROR_HANDLER.containsKey(getBaseUrl(url))) {
                    return COMMON_ERROR_HANDLER[getBaseUrl(url)](response)
                } else {
                    return DEFAULT_ERROR_HANDLER(response)
                }
            }
        } finally {
            EntityUtils.consume(response.entity)
        }
    }

    private static getBaseUrl(String url) {
        return new URI(url).host
    }

    private static setParametersToURL(String url, Object p) {
        if (!p) {
            return url
        }

        def map = null

        if (p instanceof Map) {
            map = p
        } else {
            map = p.properties
            map.remove("class")
        }

        def builder = new URIBuilder(url)

        map.each { k, v ->
            builder.addParameter(k as String, v as String)
        }

        return builder.toString()
    }

    private static setParametersToBody(HttpEntityEnclosingRequestBase base,
                                       Object p) {
        if (!p) {
            return
        }

        if (base.getAllHeaders().any {
            it.name == "Content-Type" && it.value.contains("application/json")
        }) {
            base.setEntity(new StringEntity(JSON.toJSONString(p), ContentType.APPLICATION_JSON))
            return
        }

        if (p instanceof String) {
            base.setEntity(new StringEntity(p, ContentType.APPLICATION_JSON))
            return
        }

        if (p instanceof Map) {
            List<NameValuePair> pairs = p.collect { it ->
                new BasicNameValuePair(it.key as String, it.value as String)
            }

            base.setEntity(new UrlEncodedFormEntity(pairs))
            base.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            return
        }

        if (p instanceof File) {
            base.setEntity(new FileEntity(p, ContentType.MULTIPART_FORM_DATA))
            return
        }

        if (p instanceof InputStream) {
            base.setEntity(new ByteArrayEntity(p.bytes, ContentType.APPLICATION_OCTET_STREAM))
            return
        }

        if (p instanceof Object) {
            List<NameValuePair> pairs = p.properties.findAll { it.key != "class" }.collect { it ->
                return it.value ? new BasicNameValuePair(it.key as String, it.value as String) : null
            }.findAll(Objects.&nonNull)

            base.setEntity(new UrlEncodedFormEntity(pairs))
            base.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            return
        }
    }
}
