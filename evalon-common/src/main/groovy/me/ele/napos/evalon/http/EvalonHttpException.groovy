package me.ele.napos.evalon.http

import me.ele.napos.evalon.exceptions.EvalonException


class EvalonHttpException extends EvalonException {
    EvalonHttpException() {
    }

    EvalonHttpException(String message) {
        super(message)
    }

    EvalonHttpException(Throwable throwable) {
        super(throwable)
    }
}
