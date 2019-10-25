package me.ele.napos.evalon.utils

import com.thoughtworks.paranamer.*
import me.ele.napos.evalon.exceptions.EvalonException

import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class EvalonReflectionUtil {
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

    static isPrimitive(type) {
        return type in PRIMITIVE_TYPES
    }

    static isList(Type type) {
        return (type instanceof ParameterizedType) && type.rawType == List
    }

    static getActualListType(Type type) {
        return ((ParameterizedType) type).actualTypeArguments.first()
    }

    static isSet(Type type) {
        return (type instanceof ParameterizedType) && type.rawType == Set
    }

    static getActualSetType(Type type) {
        return ((ParameterizedType) type).actualTypeArguments.first()
    }

    static isMap(Type type) {
        return (type instanceof ParameterizedType) && type.rawType == Map
    }

    static getActualMapKeyType(Type type) {
        return ((ParameterizedType) type).actualTypeArguments[0]
    }

    static getActualMapValueType(Type type) {
        return ((ParameterizedType) type).actualTypeArguments[1]
    }

    static isPojo(Type type) {
        return !isPrimitive(type) &&
                !isList(type) &&
                !isSet(type) &&
                !isMap(type)
    }

    static String[] getParameterNames(Method method) {
        Paranamer info = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()))
        try {
            return info.lookupParameterNames(method)
        } catch (ParameterNamesNotFoundException ignored) {
            return method.parameters.collect {
                it.name
            }
        }
    }

    static getGenericClassFromType(Type genericType) {
        if (!(genericType instanceof ParameterizedType)) {
            throw new EvalonException("不是泛型类型")
        }


    }
}
