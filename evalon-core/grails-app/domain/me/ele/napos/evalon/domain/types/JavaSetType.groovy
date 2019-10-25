package me.ele.napos.evalon.domain.types

class JavaSetType extends JavaAbstractType {
    String simpleName = "Set"

    List<JavaAbstractType> typeArgument = []

    static mapWith = "mongo"

    static constraints = {

    }

    String getSimpleName() {
        return "Set<${typeArgument.first().getSimpleName()}>"
    }
}
