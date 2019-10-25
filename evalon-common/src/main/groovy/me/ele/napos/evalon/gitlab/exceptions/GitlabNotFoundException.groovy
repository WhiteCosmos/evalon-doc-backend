package me.ele.napos.evalon.gitlab.exceptions

import me.ele.napos.evalon.gitlab.structs.GitlabException

class GitlabNotFoundException extends GitlabException {
    GitlabNotFoundException() {
    }

    GitlabNotFoundException(String message) {
        super(message)
    }
}
