package me.ele.napos.evalon.gitlab.exceptions

import me.ele.napos.evalon.gitlab.structs.GitlabException

class GitlabForbiddenException extends GitlabException {
    GitlabForbiddenException() {
    }

    GitlabForbiddenException(String message) {
        super(message)
    }

    GitlabForbiddenException(Throwable throwable) {
        super(throwable)
    }
}
