package me.ele.napos.evalon.visitor

import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc
import com.github.javaparser.ast.type.ArrayType
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.type.WildcardType
import me.ele.napos.evalon.domain.fields.JavaFieldDomain
import me.ele.napos.evalon.domain.types.*
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream

import java.util.zip.ZipInputStream

class VisitorHelper {
    /**
     * 类是否存在@Deprecated注解
     */
    static boolean isDeprecated(ClassOrInterfaceDeclaration n) {
        def annotations = n.annotations

        if (annotations.any { it.nameAsString == "Deprecated" }) {
            return true
        }

        return false
    }

    /**
     * 方法是否存在@Deprecated注解
     */
    static boolean isDeprecated(MethodDeclaration n) {
        def annotations = n.annotations

        if (annotations.any { it.nameAsString == "Deprecated" }) {
            return true
        }

        return false
    }

    private static final String SPRING_CONTROLLER = "Controller"

    private static final String SPRING_REST_CONTROLLER = "RestController"

    /**
     * 是否为Spring服务
     */
    static boolean isSpringController(ClassOrInterfaceDeclaration n) {
        return n.annotations.any {
            it.nameAsString == SPRING_CONTROLLER || it.nameAsString == SPRING_REST_CONTROLLER
        }
    }

    private static Type awayFromShell(Type type) {
        if (isListType(type) && hasGenericType(type)) {
            return awayFromShell(getFirstGenericType(type))
        }

        if (isMapType(type) && hasGenericType(type)) {
            return awayFromShell(getSecondGenericType(type))
        }

        return type
    }

    private static getTypeNameFromType(Type type) {
        if (type instanceof ClassOrInterfaceType) {
            return type.name
        } else {
            return type.toString()
        }
    }

    static replaceEvalonAbstractType(JavaAbstractType javaType, Closure action) {
        if (javaType.instanceOf(JavaPrimitiveType)) {
            return javaType
        }

        if (javaType.instanceOf(JavaListType)) {
            javaType.typeArgument = [replaceEvalonAbstractType(javaType.typeArgument.first(), action)]

            return javaType
        }

        if (javaType.instanceOf(JavaSetType)) {
            javaType.typeArgument = [replaceEvalonAbstractType(javaType.typeArgument.first(), action)]

            return javaType
        }

        if (javaType.instanceOf(JavaMapType)) {
            javaType.keyTypeArgument = [replaceEvalonAbstractType(javaType.keyTypeArgument.first(), action)]

            javaType.valueTypeArgument = [replaceEvalonAbstractType(javaType.valueTypeArgument.first(), action)]

            return javaType
        }

        if (javaType.instanceOf(JavaGenericType)) {
            return action(javaType)
        }

        if (javaType.instanceOf(JavaPojoType)) {
            return action(javaType)
        }
    }

    static visitEvalonAbstractType(JavaAbstractType javaType, Closure action) {
        if (javaType.instanceOf(JavaPrimitiveType)) {
            return
        }

        if (javaType.instanceOf(JavaListType)) {
            visitEvalonAbstractType(javaType.typeArgument.first(), action)

            action(javaType)

            return
        }

        if (javaType.instanceOf(JavaSetType)) {
            visitEvalonAbstractType(javaType.typeArgument.first(), action)

            action(javaType)

            return
        }

        if (javaType.instanceOf(JavaMapType)) {
            visitEvalonAbstractType(javaType.keyTypeArgument.first(), action)

            visitEvalonAbstractType(javaType.valueTypeArgument.first(), action)

            action(javaType)

            return
        }

        if (javaType.instanceOf(JavaGenericType)) {
            if (javaType.genericType) {
                visitEvalonAbstractType(javaType.genericType, action)
            }

            javaType.argumentTypes.each {
                visitEvalonAbstractType(it.types.first(), action)
            }

            action(javaType)

            return
        }

        if (javaType.instanceOf(JavaPojoType)) {
            action(javaType)
            return
        }
    }

    static iterateEvalonType(JavaAbstractType evalonType, Closure action, Set<String> set = []) {
        if (evalonType.instanceOf(JavaPrimitiveType)) {
            return
        }

        if (evalonType.instanceOf(JavaListType)) {
            iterateEvalonType(evalonType.typeArgument, action, set)

            return
        }

        if (evalonType.instanceOf(JavaMapType)) {
            iterateEvalonType(evalonType.keyTypeArgument, action, set)

            iterateEvalonType(evalonType.valueTypeArgument, action, set)

            return
        }

        if (evalonType.instanceOf(JavaGenericType)) {
            if (evalonType.genericType) {
                action(evalonType.genericType)
            }

            evalonType.argumentTypes.each {
                iterateEvalonType(it.types.first(), action, set)
            }

            return
        }

        if (evalonType.instanceOf(JavaPojoType)) {
            if (!(set.add(evalonType.simpleName))) { // 排除已经遍历过的类
                return
            }

            action(evalonType)

            //递归执行
            (evalonType as JavaPojoType).fields.each { field ->
                if (field.fieldTypes) {
                    iterateEvalonType(field.fieldTypes.first(), action, set)
                }
            }
        }
    }

//    static getEvalonPrimitiveType(String fieldType) {
//        def primitiveType = JavaPrimitiveType.findBySimpleName(fieldType)
//
//        if (!primitiveType) {
//            primitiveType = new JavaPrimitiveType(simpleName: fieldType).save()
//        }
//
//        return primitiveType
//    }

//    static JavaAbstractType buildFieldTypeRecurse(List<String> typeArguments = [], Type fieldType) {
//        if (isPrimitiveType(fieldType)) {
//            return getEvalonPrimitiveType(fieldType.toString())
//        }
//
//        if (isGenericType(typeArguments, fieldType)) {
//            return new JavaGenericType(simpleName: fieldType.toString())
//        }
//
//        if (isSetType(fieldType)) {
//            def evalonType = new JavaSetType()
//
//            if ((fieldType as ClassOrInterfaceType).typeArguments.present) {
//                def type = (fieldType as ClassOrInterfaceType).typeArguments.get()[0]
//
//                evalonType.typeArgument = buildFieldTypeRecurse(typeArguments, type)
//            } else {
//                evalonType.typeArgument = getEvalonPrimitiveType("Object")
//            }
//
//            return evalonType
//        }
//
//        if (isListType(fieldType)) {
//            def listType = new JavaListType()
//
//            if (fieldType instanceof ArrayType) {
//                listType.typeArgument = [getEvalonPrimitiveType((fieldType.toString()))]
//            }
//
//            if (fieldType instanceof ClassOrInterfaceType) {
//                if ((fieldType as ClassOrInterfaceType).typeArguments.present) {
//                    def type = (fieldType as ClassOrInterfaceType).typeArguments.get()[0]
//                    listType.typeArgument = buildFieldTypeRecurse(typeArguments, type)
//                } else {
//                    listType.typeArgument = getEvalonPrimitiveType("Object")
//                }
//            }
//
//            return listType
//        }
//
//        if (isMapType(fieldType)) {
//            def evalonType = new JavaMapType()
//
//            if ((fieldType as ClassOrInterfaceType).typeArguments.present) {
//                def keyType = (fieldType as ClassOrInterfaceType).typeArguments.get()[0]
//
//                def valueType = (fieldType as ClassOrInterfaceType).typeArguments.get()[1]
//
//                evalonType.keyTypeArgument = [buildFieldTypeRecurse(typeArguments, keyType)]
//
//                evalonType.valueTypeArgument = [buildFieldTypeRecurse(typeArguments, valueType)]
//            } else {
//                evalonType.keyTypeArgument = [getEvalonPrimitiveType("Object")]
//
//                evalonType.valueTypeArgument = [getEvalonPrimitiveType("Object")]
//            }
//
//            return evalonType
//        }
//
//        if (isGenericObjectType(fieldType)) {
//            fieldType = fieldType as ClassOrInterfaceType
//
//            def evalonType = new JavaGenericType(simpleName: fieldType.toString())
//
//            def arguments = fieldType.typeArguments.get()
//
//            arguments.each {
//                evalonType.argumentTypes << new JavaArgumentType(types: [buildFieldTypeRecurse(it)])
//            }
//
//            def args = fieldType.typeArguments
//
//            fieldType.setTypeArguments([] as NodeList<Type>) // 否则无限递归
//
//            evalonType.genericType = buildFieldTypeRecurse(fieldType)
//
//            fieldType.setTypeArguments(args.get()) // 还原
//
//            return evalonType
//        }
//
//        if (isObjectType(fieldType)) {
//            fieldType = fieldType as ClassOrInterfaceType
//
//            return new JavaPojoType(simpleName: fieldType.name)
//        }
//
//        return null
//    }

    static boolean isGenericType(List<String> typeArguments, Type fieldType) {
        return typeArguments.contains(fieldType as String)
    }

    //======================
    // 类型判断
    //======================

    static PRIMITIVE_TYPES = [
            "Object",
            "String",
            "Boolean",
            "Long",
            "Double",
            "Float",
            "Integer",
            "Short",
            "Date",
            "LocalDate",
            "LocalTime",
            "LocalDateTime",
            "BigDecimal",
            "long",
            "int",
            "boolean",
            "double",
            "float",
            "short",
            "char",
            "byte",
            "?", // 问号相当于Object
    ]

    static boolean isPrimitiveType(Type type) {
        return PRIMITIVE_TYPES.contains(type as String)
    }

    static boolean isPrimitiveType(Class type) {
        return isPrimitiveType(type.simpleName)
    }

    static boolean isPrimitiveType(String type) {
        return PRIMITIVE_TYPES.contains(type)
    }

    static boolean isObjectType(Type type) {
        return !isPrimitiveType(type) &&
                !isListType(type) &&
                !isMapType(type) &&
                type instanceof ClassOrInterfaceType
    }

    static boolean isObjectType(Class type) {
        return !isPrimitiveType(type) &&
                !isListType(type) &&
                !isMapType(type) &&
                !type.isInterface()
    }

    static boolean isEnumType(Class type) {
        return type.isEnum()
    }

    static boolean isGenericObjectType(Type type) {
        return isObjectType(type) &&
                type instanceof ClassOrInterfaceType &&
                type.typeArguments.present &&
                !type.typeArguments.get().empty
    }

    static boolean isGenericObjectType(Class cls) {
        return isObjectType(cls) && cls.typeParameters.length != 0
    }

    static boolean isVoidType(Type type) {
        return (type as String) == "void"
    }

    static boolean isListType(Type type) {
        return (type =~ /List<.*>|List/).matches() ||
                type instanceof ArrayType
    }

    static boolean isSetType(Type type) {
        return (type =~ /Set<.*>|Set/).matches()
    }

    static boolean isMapType(Type type) {
        return (type =~ /Map<.*,.*>|Map/).matches()
    }

    static boolean hasGenericType(Type type) {
        if (!(type instanceof ClassOrInterfaceType)) {
            return false
        }

        return (type as ClassOrInterfaceType).typeArguments.isPresent()
    }

    static Type getFirstGenericType(Type type) {
        if (!hasGenericType(type)) {
            return null
        }

        def argumentType = (type as ClassOrInterfaceType).typeArguments.get()[0]

        if (argumentType instanceof WildcardType && argumentType.extendedType.present) {
            return argumentType.extendedType.get()
        } else {
            return argumentType
        }
    }

    static Type getSecondGenericType(Type type) {
        if (!hasGenericType(type)) {
            return null
        }

        def argumentType = (type as ClassOrInterfaceType).typeArguments.get()[1]

        if (argumentType instanceof WildcardType && argumentType.extendedType.present) {
            return argumentType.extendedType.get()
        } else {
            return argumentType
        }
    }

    /**
     * 取第一行有效的注释
     */
    static String getFirstValidComment(com.github.javaparser.ast.Node n) {
        if (n.comment.isPresent()) {
            def comment = n.comment.get()

            // 忽略行注释
            if (comment.isLineComment()) {
                return ""
            }

            List<String> lines = comment.content.readLines().findAll { !it.empty }

            if (lines.isEmpty()) {
                return ""
            }

            return lines.first().replaceAll("\\*", "").trim()
        } else {
            return ""
        }
    }

    /**
     * 获取字段上的JavaDoc文本
     */
    static String getJavaDocText(NodeWithJavadoc n) {
        if (!n.javadoc.present) {
            return ""
        }

        return n.javadoc.get().description.toText()
    }
}
