package me.ele.napos.evalon.domain

class FavoriteApiDomain {
    String token

    String repositoryName

    Integer projectId

    String projectName

    String appName

    String branchName

    String serviceName

    String serviceQualifiedName

    String methodName

    String methodComment

    static mapWith = "mongo"

    static constraints = {

    }
}
