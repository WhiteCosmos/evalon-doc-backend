package me.ele.napos.evalon.gitlab.structs

import java.time.LocalDateTime


class GitlabBranchQuery {
    LocalDateTime after
    int size = 100
}
