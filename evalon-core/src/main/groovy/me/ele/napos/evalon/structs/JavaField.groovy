package me.ele.napos.evalon.structs

import groovy.transform.AutoClone
import me.ele.napos.evalon.transformer.JavaFieldHelper

/**
 * Java pojo and field in pojo
 */
@AutoClone
class JavaField {
    String fieldName

    String fieldQualifyName

    String fieldType

    String fieldCommentTitle // javadoc title

    String fieldCommentBody // javadoc body

    String fieldEnumParent

    Object fieldValue

    boolean isRequired = false // if has  @NotNull validation

    boolean isRecursion = false // if self-dependency pojo

    boolean isCycle = false // if cycle dependency with other pojos

    List<JavaField> fields = []

    String toJsonStr(int layer = 0, boolean underContainer = false) {
        if (JavaFieldHelper.isString(this)) {
            return "${space(layer)}\"${fieldName}\" : \"\", ${comment()}"
        }

        if (JavaFieldHelper.isNumber(this)) {
            return "${space(layer)}\"${fieldName}\" : 0, ${comment()}"
        }

        if (JavaFieldHelper.isBoolean(this)) {
            return "${space(layer)}\"${fieldName}\" : false, ${comment()}"
        }

        if (JavaFieldHelper.isDateTime(this)) {
            return "${space(layer)}\"${fieldName}\" : \"\", ${comment()}"
        }

        if (JavaFieldHelper.isEnum(this)) {
            return "${space(layer)}\"${fieldName}\" : \"${this.fields.first().fieldName}\", ${comment()}"
        }

        if (JavaFieldHelper.isObject(this)) {
            def sb = new StringBuilder()

            if (fieldName.capitalize() == fieldName) {
                sb.append("${space(layer)}{\n")
            } else {
                sb.append("${space(layer)}\"${fieldName}\" : {\n")
            }

            this.fields.each {
                sb.append(it.toJsonStr(layer + 1))
            }

            if (fieldName.capitalize() == fieldName) {
                sb.append("${space(layer)}},\n")
            } else {
                sb.append("${space(layer)}}\n")
            }

            return sb.toString()
        }

        if (JavaFieldHelper.isList(this) || JavaFieldHelper.isSet(this)) {
            def sb = new StringBuffer()

            def child = fields.first()

            if (JavaFieldHelper.isObject(child)) {
                if (!underContainer) {
                    sb.append("${space(layer)}\"${fieldName}\" : [\n")
                }

                sb.append(child.toJsonStr(layer + 1))

                sb.append("${space(layer)}],\n")

                return sb.toString()
            }

//            if (JavaFieldHelper.isMap(child)) {
//
//            }

            sb.append("${space(layer)}\"${fieldName}\" : [],\n")

            return sb.toString()
        }

        if (JavaFieldHelper.isMap(this)) {
//            def keyField = this.fields[0]
//
//            def valueField = this.fields[1]
//
//            def key = ""
//
//            def value = ""
//
//            if (JavaFieldHelper.isString(keyField)) {
//                key = "key"
//            }
//
//            if (JavaFieldHelper.isNumber(keyField)) {
//                key = -1
//            }
//
//            if (JavaFieldHelper.isEnum(keyField)) {
//                key = keyField.fields.first().fieldName
//            }


//            if (JavaFieldHelper.isString(valueField)) {
//                value = "value"
//            }
//
//            if (JavaFieldHelper.isNumber(valueField)) {
//                value = -1
//            }
//
//            if (JavaFieldHelper.isBoolean(valueField)) {
//                value = false
//            }


            return "${space(layer)}${fieldName} : { },\n"
        }

        return "${space(layer)}${fieldName} : null,\n"
    }

    String comment() {
        if (fieldCommentTitle) {
            return "// ${fieldCommentTitle} \n"
        }

        return "\n"
    }

    String space(layer) {
        return " " * 4 * layer
    }
}
