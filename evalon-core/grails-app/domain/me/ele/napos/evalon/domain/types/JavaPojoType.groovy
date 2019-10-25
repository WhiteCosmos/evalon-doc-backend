package me.ele.napos.evalon.domain.types


import me.ele.napos.evalon.domain.fields.JavaFieldDomain

class JavaPojoType extends JavaAbstractType {
    String qualifiedName = ""

    String commentTitle = ""

    String commentBody = ""

    String packageName = ""

    List<String> imports = []

    List<String> typeArguments = []

    List<JavaPojoType> extensions = []

    List<JavaFieldDomain> fields = []

    boolean isRecurse

    boolean isCycle

    static transients = ['isRecurse', 'isCycle']

//    static hasMany = [
//            fields    : JavaFieldDomain,
//
//            extensions: JavaPojoType,
//    ]

    static mapWith = "mongo"

    static constraints = {
        qualifiedName nullable: true

        commentTitle nullable: true
    }
}
