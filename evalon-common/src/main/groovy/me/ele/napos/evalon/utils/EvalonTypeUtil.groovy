package me.ele.napos.evalon.utils

class EvalonTypeUtil {
    static PRIMITIVE_TYPES = [
            Byte,
            byte,
            Boolean,
            boolean,
            Short,
            short,
            Integer,
            int,
            Long,
            long,
            Double,
            double,
            Float,
            float,
            String,
            "byte",
            "boolean",
            "short",
            "integer",
            "long",
            "double",
            "float",
            "string",
    ]

    static JAVA_NUMBER_TYPES = [
            "byte",
            "Byte",
            "short",
            "Short",
            "int",
            "Integer",
            "long",
            "Long",
            "double",
            "Double",
            "float",
            "Float",
            "BigDecimal",
    ]

    static JAVA_DATETIME_TYPES = [
            "Date",
            "LocalDate",
            "LocalTime",
            "LocalDateTime",
    ]

    static JAVA_PRIMITIVE_TYPES = [
            "byte",
            "Byte",
            "short",
            "Short",
            "int",
            "Integer",
            "long",
            "Long",
            "double",
            "Double",
            "float",
            "Float",
            "BigDecimal",
            "Date",
            "LocalDate",
            "LocalTime",
            "LocalDateTime",
            "char",
            "Character",
            "String",
            "boolean",
            "Boolean",
    ]

    static isPrimitive(Object obj) {
        return obj.class in PRIMITIVE_TYPES
    }

    static isCollection(Object obj) {
        return obj.class instanceof Collection
    }

    static isMap(Object obj) {
        return obj.class instanceof Map
    }

    static isEnum(Object obj) {
        return obj.class.isEnum()
    }

    static isObject(Object obj) {
        return !isPrimitive(obj) &&
                !isCollection(obj) &&
                !isMap(obj) &&
                !isEnum(obj)
    }
}
