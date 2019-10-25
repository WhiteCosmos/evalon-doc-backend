package me.ele.napos.evalon.utils

class CurlBuilder {
    String curl = "curl"

    CurlBuilder buildMethod(String method = "POST") {
        curl += " -X \"${method}\""

        return this
    }

    CurlBuilder buildURL(String url) {
        curl += " \"${url}\" "

        return this
    }


    CurlBuilder buildHeader(String header) {
        curl += " -H \"${header}\" "

        return this
    }

    CurlBuilder buildHeaders(Map<String, String> headers) {
        if (!headers) {
            return this
        }

        headers.each { k, v ->
            curl += " -H \"${k}:${v}\" "
        }

        return this
    }

    CurlBuilder buildData(String data) {
        curl += " -d \'${data}\' "

        return this
    }

    @Override
    String toString() {
        return curl
    }
}
