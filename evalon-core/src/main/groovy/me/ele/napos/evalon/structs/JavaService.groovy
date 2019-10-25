package me.ele.napos.evalon.structs

/**
 * Java Service
 */
class JavaService {
    String serviceName // service methodName

    String serviceQualifiedName // service full methodName

    String serviceType

    String commentTitle

    String commentBody

    String restfulUrl

    boolean deprecated = false // is isDeprecated

    List<JavaMethod> methods = []
}
