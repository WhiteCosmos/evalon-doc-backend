package me.ele.napos.evalon.http

import com.fasterxml.jackson.databind.ObjectMapper


class EvalonAbstractClient {
    protected static final int TIME_OUT_IN_MILLISECONDS = 3000 // 请求超时时间
    private static ObjectMapper mapper = new ObjectMapper()

    protected String host
    protected int port


    protected static OBJECT_MAP = [
        "boolean": "Boolean",
        "short"  : "Short",
        "int"    : "Integer",
        "long"   : "Long",
    ]

    protected read() {

    }

    protected getResult() {}
}
