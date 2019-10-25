package me.ele.napos.evalon.gitlab.structs

enum EvalonGitlabCode {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    BAD_REQUEST(400),

    int code
    EvalonGitlabCode(int code) {
        this.code = code
    }
}