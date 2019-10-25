package me.ele.napos.evalon.domain

class ProjectDomain { // 1 -> N ModuleDomain
    Long repositoryId

    Long projectId

    String projectName

    static mapWith = "mongo"

    static constraints = {
        projectId unique: true
    }
}
