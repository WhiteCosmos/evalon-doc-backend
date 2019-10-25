package me.ele.napos.evalon.domain

class ModuleDomain {
    long projectId

    String appName // aka appId

    String teamName

    String moduleName

    static mapWith = "mongo"

    static constraints = {
        teamName nullable: true

        projectId unique: ['moduleName'] // projectId + moduleName is unique
    }
}
