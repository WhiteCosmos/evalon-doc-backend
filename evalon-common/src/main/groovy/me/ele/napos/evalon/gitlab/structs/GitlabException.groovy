package me.ele.napos.evalon.gitlab.structs

import me.ele.napos.evalon.http.EvalonHttpException

class GitlabException extends EvalonHttpException {
    GitlabException() {

    }

    GitlabException(String message) {
        super(message)
    }

    GitlabException(Throwable throwable) {
        super(throwable)
    }
}
