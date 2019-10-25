package me.ele.napos.evalon.domain.fields


import me.ele.napos.evalon.domain.types.JavaAbstractType

class JavaFieldDomain {
    String fieldName = ""

    String fieldTypeName = ""

    List<JavaAbstractType> fieldTypes

    String fieldCommentTitle = ""

    String fieldCommentBody = ""

    boolean isRequired = false // 是否必填

    static mapWith = "mongo"

    static constraints = {

    }
}
