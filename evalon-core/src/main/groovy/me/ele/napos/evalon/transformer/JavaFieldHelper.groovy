package me.ele.napos.evalon.transformer

import me.ele.napos.evalon.structs.JavaField
import me.ele.napos.evalon.utils.EvalonTypeUtil

class JavaFieldHelper {
    static boolean isString(JavaField template) {
        return template.fieldType == "String"
    }

    static boolean isNumber(JavaField template) {
        return template.fieldType in EvalonTypeUtil.JAVA_NUMBER_TYPES
    }

    static boolean isBoolean(JavaField template) {
        return template.fieldType == "Boolean" || template.fieldType == "boolean"
    }

    static boolean isDateTime(JavaField template) {
        return template.fieldType in EvalonTypeUtil.JAVA_DATETIME_TYPES
    }

    static boolean isPrimitive(JavaField template) {
        return template.fieldType in EvalonTypeUtil.JAVA_PRIMITIVE_TYPES
    }

    static boolean isEnum(JavaField template) {
        return template.fields.any {
            it.fieldType == "STRING"
        }
    }

    static boolean isArray(JavaField template) {
        return isList(template) || isSet(template)
    }

    static boolean isList(JavaField template) {
        return template.fieldType.startsWith("List") && (template.fieldType =~ /List<.*>/ || template.fieldType == "List")
    }

    static boolean isSet(JavaField template) {
        return template.fieldType.startsWith("Set") && (template.fieldType =~ /Set<.*>/ || template.fieldType == "Set")
    }

    static boolean isMap(JavaField template) {
        return template.fieldType.startsWith("Map") && (template.fieldType =~ /Map<.*,.*>/ || template.fieldType == "Map")
    }

    static boolean isObject(JavaField template) {
        return !isPrimitive(template) &&
                !isArray(template) &&
                !isMap(template) &&
                !isEnum(template)
    }
}
