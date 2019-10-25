package me.ele.napos.evalon.gitlab.exceptions

import me.ele.napos.evalon.gitlab.structs.GitlabException

class GitlabUnauthorizedException extends GitlabException {
    GitlabUnauthorizedException() {

    }

    GitlabUnauthorizedException(String message) {
        super(message)
    }

    GitlabUnauthorizedException(Throwable throwable) {
        super(throwable)
    }
}
