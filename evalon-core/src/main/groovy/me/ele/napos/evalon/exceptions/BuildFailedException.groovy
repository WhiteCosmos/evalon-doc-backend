package me.ele.napos.evalon.exceptions

class BuildFailedException extends EvalonException {
    static String ERROR_CODE = "BUILD_FAILED"

    BuildFailedException(String message) {
        super(ERROR_CODE, message)
    }
}
