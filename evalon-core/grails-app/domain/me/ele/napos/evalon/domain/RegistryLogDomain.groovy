package me.ele.napos.evalon.domain

class RegistryLogDomain {
    String repositoryName

    String projectName

    String branchName

    String message

    static mapWith = "mongo"

    static constraints = {

    }
}
