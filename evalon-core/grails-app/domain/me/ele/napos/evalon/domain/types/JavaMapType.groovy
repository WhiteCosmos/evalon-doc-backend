package me.ele.napos.evalon.domain.types

class JavaMapType extends JavaAbstractType {
    String simpleName = "Map"

    List<JavaAbstractType> keyTypeArgument

    List<JavaAbstractType> valueTypeArgument

    static mapWith = "mongo"

    static constraints = {

    }

    String getSimpleName() {
        return "Map<${keyTypeArgument.first().getSimpleName()}, ${valueTypeArgument.first().simpleName}>"
    }
}
