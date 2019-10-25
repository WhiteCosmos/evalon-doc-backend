package me.ele.napos.evalon.exceptions


class OperationFailedException extends EvalonException {
    static String ERROR_CODE = "OPERATION_FAILED"

    OperationFailedException(String message) {
        super(message)
    }
}
