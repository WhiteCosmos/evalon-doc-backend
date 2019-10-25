package me.ele.napos.evalon.domain.types

class JavaListType extends JavaAbstractType {
    String simpleName = "List"

    List<JavaAbstractType> typeArgument = []

    boolean isArray = false

    static mapWith = "mongo"

    static constraints = {

    }

    String getSimpleName() {
        return "List<${typeArgument.first().getSimpleName()}>"
    }
}