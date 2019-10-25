package me.ele.napos.evalon.web

import me.ele.napos.evalon.exceptions.EvalonException


class EvalonWebResponse {
    def result
    String errorCode
    String errorMessage
    boolean hasError = false

    static success(result) {
        if (result == null) {
            return new EvalonWebResponse(result: null)
        }

        return new EvalonWebResponse(result: result)
    }

    static fail(EvalonException e) {
        return new EvalonWebResponse(errorMessage: e.getMessage(), errorCode: e.errorCode, hasError: true)
    }
}
