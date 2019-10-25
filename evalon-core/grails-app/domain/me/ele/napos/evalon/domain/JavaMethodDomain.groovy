package me.ele.napos.evalon.domain


import me.ele.napos.evalon.domain.fields.JavaFieldDomain
import me.ele.napos.evalon.domain.types.JavaPojoType

class JavaMethodDomain {
    String methodName = ""

    String commentTitle = ""

    String commentBody = ""

    boolean isDeprecated = false

    List<JavaFieldDomain> parameters = []

    List<JavaFieldDomain> responses = []

    List<JavaPojoType> exceptions = []

    String restfulMethod = "GET"

    String restfulUrl = ""

    static mapWith = "mongo"

//    static hasMany = [
//            parameters: JavaFieldDomain,
//
//            responses : JavaFieldDomain,
//
//            exceptions: JavaPojoType
//    ]

    static constraints = {

    }
}
