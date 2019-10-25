package me.ele.napos.evalon.domain.types

import me.ele.napos.evalon.domain.fields.JavaFieldDomain
import me.ele.napos.evalon.visitor.VisitorHelper

/**
 * 泛型是一种复合类型
 *
 * simpleName: T/S/R 类型占位符
 */
@SuppressWarnings("GroovyAssignabilityCheck")
class JavaGenericType extends JavaAbstractType {
    JavaAbstractType genericType

    List<JavaArgumentType> argumentTypes = []

    static mapWith = "mongo"

    static constraints = {
        genericType nullable: true
    }

    /**
     * 把容器类型和泛型类型重建为实际看到的类型
     */
    JavaAbstractType build() {
        // 得到泛型容器
        JavaPojoType containerType = (this.genericType as JavaPojoType)

        // 得到泛型参数 S T etc.
        List<String> typeArguments = containerType.typeArguments

        // 存放泛型参数和对应的实际类型
        Map<String, JavaAbstractType> map = [:]

        // 替换子 进行实际的替换操作
        Closure replaceGenericPlaceholder = { JavaAbstractType evalonAbstractType ->
            if (!evalonAbstractType.instanceOf(JavaGenericType)) {
                return evalonAbstractType
            }

            def g = evalonAbstractType as JavaGenericType

            if (g.genericType) {
                return g.build()
            } else {
                def r = map[g.simpleName]

                if (r.instanceOf(JavaGenericType)) {
                    return r.build()
                }

                return r
            }
        }

        Closure recurseExtensions // 不分开写的话无法递归

        recurseExtensions = { List<JavaPojoType> extensions ->
            extensions.each { ext ->
                ext.fields.each { field ->
                    if (checkField(field)) {
                        field.fieldTypes = VisitorHelper.replaceEvalonAbstractType(field.fieldTypes.first(), replaceGenericPlaceholder)

                        field.fieldTypeName = field.fieldTypes.first().getSimpleName()
                    }
                }

                recurseExtensions(ext.extensions)
            }
        }

        // 查询泛型参数对应的实际类型
        (0..typeArguments.size()).each { index ->
            typeArguments[index] && (map[typeArguments[index]] = argumentTypes[index].types.first())
        }

        containerType.fields.each { field ->
            if (checkField(field)) {
                field.fieldTypes = [VisitorHelper.replaceEvalonAbstractType(field.fieldTypes.first(), replaceGenericPlaceholder)]

                field.fieldTypeName = field.fieldTypes.first().getSimpleName()
            }
        }

        recurseExtensions(containerType.extensions)

        return containerType
    }

    boolean checkField(JavaFieldDomain field) {
        if (!field) {
            return false
        }

        if (!field.fieldTypes) {
            return false
        }

        return true
    }
}
