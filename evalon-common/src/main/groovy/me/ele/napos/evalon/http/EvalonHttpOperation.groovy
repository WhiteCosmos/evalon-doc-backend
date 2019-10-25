package me.ele.napos.evalon.http

class EvalonHttpOperation<T> {
    String baseUrl = null
    String url = null
    List<String> urls = []
    Object parameters = null
    Map<String, String> headers = [:]
    Class<T> returnType = Object as Class<T>
    Closure errorHandler = null
    int timeout = 3000

    String toUrl() {
        if (url) {
            return url
        } else {
            return baseUrl + urls.join("")
        }
    }
}
