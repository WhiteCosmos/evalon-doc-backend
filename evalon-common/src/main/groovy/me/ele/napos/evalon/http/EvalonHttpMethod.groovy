package me.ele.napos.evalon.http

enum EvalonHttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE")

    String name

    EvalonHttpMethod(String name) {
        this.name = name
    }
}