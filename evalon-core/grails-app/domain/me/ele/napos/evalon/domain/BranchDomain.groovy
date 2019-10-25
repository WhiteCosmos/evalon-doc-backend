package me.ele.napos.evalon.domain


import me.ele.napos.evalon.domain.types.JavaPojoType

class BranchDomain {
    Long moduleId

    String moduleName

    String branchName

    boolean isDefault = false

    List<JavaServiceDomain> services = []

    List<JavaPojoType> pojos = []

    static mapWith = "mongo"

    static constraints = {
        moduleId unique: ['branchName'] // moduleId + branchName is unique
    }

    static mapping = {

    }

//    static hasMany = [
//            services: JavaServiceDomain,
//
//            pojos   : JavaPojoType
//    ]
}
