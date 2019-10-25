package me.ele.napos.evalon.structs

/**
 * Java Method
 */
class JavaMethod {
    String serviceName

    String serviceQualifiedName

    String methodName

    String commentTitle

    String commentBody

    List<JavaField> parameters = []

    List<JavaField> response = []

    List<JavaField> exceptions = []

    String restfulMethod = "GET"

    String restfulUrl = ""

    boolean isDeprecated = false

    String parametersAsJson = ""

    String responseAsJson = ""
}
