package me.ele.napos.evalon.domain

class JavaServiceDomain {
    String serviceName = ""

    String packageName = ""

    String serviceQualifiedName = ""

    String serviceType = ""

    String commentTitle = ""

    String commentBody = ""

    String moduleName = ""

    boolean isDeprecated = false

    String restfulUrl = ""

    static mapWith = "mongo"

    List<JavaMethodDomain> methods = []

//    static hasMany = [
//            methods: JavaMethodDomain
//    ]

    static mapping = {
        serviceQualifiedName index: true
    }

    static constraints = {
        packageName nullable: true
    }
}
