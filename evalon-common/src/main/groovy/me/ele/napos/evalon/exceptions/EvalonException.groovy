package me.ele.napos.evalon.exceptions

import com.alibaba.fastjson.JSON


class EvalonException extends Exception {
    String errorCode

    EvalonException(String errorCode, String message) {
        super(message)

        this.errorCode = errorCode
    }

    EvalonException(String message) {
        super(message)
    }

    EvalonException(Object message) {
        super(JSON.toJSONString(message))
    }

    EvalonException(Throwable throwable) {
        super(throwable)
    }
}
