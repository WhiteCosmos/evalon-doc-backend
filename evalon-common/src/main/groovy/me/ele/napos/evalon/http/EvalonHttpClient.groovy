package me.ele.napos.evalon.http

import com.alibaba.fastjson.JSON
import com.google.common.reflect.TypeToken
import me.ele.napos.evalon.exceptions.OperationFailedException
import me.ele.napos.evalon.utils.EvalonReflectionUtil
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.*
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

class EvalonHttpClient {
    private static final HttpClient client = HttpClientBuilder.create()
            .disableRedirectHandling()
            .build()

    private static final String PAYLOAD_TYPE_JSON = "json"

    private static final String PAYLOAD_TYPE_FORM = "form"

    private static final String PAYLOAD_TYPE_MULTIPART = "multipart"

    private String baseApi = ""

    private String url = ""

    private Map parameters = [:]

    private Map headers = [:]

    Map<Integer, Closure> errorHandlers = [:] // http code -> handler

    private String payloadType = PAYLOAD_TYPE_JSON

    Closure errorHandler = { HttpResponse response ->
        if (response.statusLine.statusCode != HttpURLConnection.HTTP_OK) {
            throw new EvalonHttpException(EntityUtils.toString(response.entity))
        }
    } // 默认异常处理

    EvalonHttpClient(String baseApi) {
        if (baseApi.endsWith("/")) {
            baseApi = baseApi.drop(1)
        }

        this.baseApi = baseApi

        this.url = baseApi

        registerResponseHandler()
    }

    EvalonHttpClient(String baseApi, Map<Integer, Closure> errorHandlers) {
        this(baseApi)

        this.errorHandlers = errorHandlers
    }

    def addHeader(String key, String value) {
        headers[key] = value
    }

    def addHeaders(Map<String, String> headers) {
        headers.putAll(headers)
    }

    @Override
    Object invokeMethod(String name, Object args) {
        this.url += "/${name}"

        args = args as Object[]

        if (!args) {
            return this
        }

        if (args.size() > 1) {
            throw new OperationFailedException("方法参数只能有一个")
        }

        Object arg = args.first()

        if (arg.class in EvalonReflectionUtil.PRIMITIVE_TYPES) {
            if ((arg as String).startsWith("/")) {
                this.url += "${arg}"
            } else {
                this.url += "/${arg}"
            }
            return this
        }

        if (arg instanceof Map) {
            this.parameters = arg
            return this
        }

        if (arg instanceof Object) {
            arg = JSON.parseObject(JSON.toJSONString(arg), Map)
            this.parameters = arg
            return this
        }

        return this
    }

    HttpResponse get(Map args) {
        try {
            URIBuilder uri = new URIBuilder(this.url)

            args.each { k, v ->
                uri.addParameter(k as String, v as String)
            }

            def base = new HttpGet(uri.toString())

            headers.each { k, v ->
                base.addHeader(k as String, v as String)
            }

            return execute(base)
        } finally {
            this.url = baseApi
        }
    }

    HttpResponse post(Object args) {
        def map = args.properties

        map.remove("class")

        return post(map)
    }

    HttpResponse post(Map args) {
        URIBuilder uri = new URIBuilder(this.url)

        def base = new HttpPost(uri.toString())

        if (payloadType == PAYLOAD_TYPE_JSON) {
            base.setEntity(new StringEntity(JSON.toJSONString(args), ContentType.APPLICATION_JSON))
        }

        if (payloadType == PAYLOAD_TYPE_FORM) {
            base.setEntity(new UrlEncodedFormEntity(
                    args.collect { arg ->
                        new BasicNameValuePair(arg.key as String, arg.value as String)
                    }
            ))
        }

        if (payloadType == PAYLOAD_TYPE_MULTIPART) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()

            args.each { k, v ->
                builder.addTextBody(k as String, v as String)
            }

            base.setEntity(builder.build())
        }

        headers.each { k, v ->
            base.addHeader(k as String, v as String)
        }

        return execute(base)
    }

    HttpResponse put(Map args) {
        URIBuilder uri = new URIBuilder(this.url)

        def base = new HttpPut(uri.toString())

        if (payloadType == PAYLOAD_TYPE_JSON) {
            base.setEntity(new StringEntity(JSON.toJSONString(args), ContentType.APPLICATION_JSON))
        }

        if (payloadType == PAYLOAD_TYPE_FORM) {
            base.setEntity(new UrlEncodedFormEntity(
                    args.collect { arg ->
                        new BasicNameValuePair(arg.key as String, arg.value as String)
                    }
            ))
        }

        if (payloadType == PAYLOAD_TYPE_MULTIPART) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()

            args.each { k, v ->
                builder.addTextBody(k as String, v as String)
            }

            base.setEntity(builder.build())
        }

        headers.each { k, v ->
            base.addHeader(k as String, v as String)
        }

        return execute(base)
    }

    HttpResponse delete(Map args = [:]) {
        URIBuilder uri = new URIBuilder(this.url)

        args.each { k, v ->
            uri.addParameter(k as String, v as String)
        }

        def base = new HttpDelete(uri.toString())

        headers.each { k, v ->
            base.addHeader(k as String, v as String)
        }

        return execute(base)
    }

    def execute(HttpRequestBase base) {
        println base.getURI()

        HttpResponse response = client.execute(base)

        assert response != null

        int code = response.statusLine.statusCode

        errorHandlers[code] ? errorHandlers[code](response) : errorHandler(response)

        return response
    }

    def isJsonResponse(HttpResponse response) {
        response.getFirstHeader("content-type").value.contains(ContentType.APPLICATION_JSON.mimeType)
    }

    def isTextResponse(HttpResponse response) {
        response.getFirstHeader("content-type").value.contains(ContentType.TEXT_HTML.mimeType) ||
                response.getFirstHeader("content-type").value.contains(ContentType.TEXT_PLAIN.mimeType) ||
                response.getFirstHeader("content-type").value.contains(ContentType.TEXT_XML.mimeType)
    }

    def isStreamResponse(HttpResponse response) {
        return true
    }

    def registerResponseHandler() {
        HttpResponse.metaClass."as" { TypeToken typeToken ->
            if (!delegate.entity) {
                return null
            }

            return JSON.parseObject(EntityUtils.toString(delegate.entity), typeToken.type)
        }

        HttpResponse.metaClass."as" { Class clazz ->
            if (!delegate.entity) {
                return null
            }

            return JSON.parseObject(EntityUtils.toString(delegate.entity), clazz)
        }

        HttpResponse.metaClass.asJson {
            if (!delegate.entity) {
                return {}
            }

            return JSON.parse(EntityUtils.toString(delegate.entity))
        }

        HttpResponse.metaClass.asString {
            if (!delegate.entity) {
                return ""
            }

            return EntityUtils.toString(delegate.entity)
        }

        HttpResponse.metaClass.asByte {
            if (!delegate.entity) {
                return byte[]
            }

            return EntityUtils.toByteArray(delegate.entity)
        }
    }
}
