package me.ele.napos.evalon.domain

class RepositoryDomain { // 1 -> N ProjectDomain
    String repositoryName = ""

    static mapWith = "mongo"

    static constraints = {
        repositoryName unique: true
    }
}
