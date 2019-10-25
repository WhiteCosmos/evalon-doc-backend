package me.ele.napos.evalon.exceptions


class AuthenticationFailedException extends EvalonException {
    static String ERROR_CODE = "AUTHENTICATION_FAILED"

    AuthenticationFailedException(String message) {
        super(ERROR_CODE, message)
    }
}
