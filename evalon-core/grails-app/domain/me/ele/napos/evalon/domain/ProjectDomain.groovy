package me.ele.napos.evalon.domain

class ProjectDomain { // 1 -> N ModuleDomain
    Long repositoryId

    Long projectId

    String projectName

    String groupId = ""

    String versionId = "" // version is occupied

    static mapWith = "mongo"

    static constraints = {
        projectId unique: true

        groupId nullable: true

        versionId nullable: true
    }
}
