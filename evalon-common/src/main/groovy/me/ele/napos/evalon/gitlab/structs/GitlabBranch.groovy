package me.ele.napos.evalon.gitlab.structs

class GitlabBranch {
    String name
    boolean merged
//    boolean 'protected'
    boolean developers_can_push
    boolean developers_can_merge
    GitlabCommit commit
}
