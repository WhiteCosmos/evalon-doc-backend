package me.ele.napos.evalon.resolver


import me.ele.napos.evalon.domain.JavaMethodDomain
import me.ele.napos.evalon.domain.JavaServiceDomain
import me.ele.napos.evalon.domain.fields.JavaFieldDomain
import me.ele.napos.evalon.domain.types.*
import me.ele.napos.evalon.structs.ProjectRegistryReport

import javax.validation.constraints.NotNull
import java.lang.reflect.*

abstract class AbstractResolver {
    List<JavaPojoType> pojos = []

    ProjectRegistryReport report

    ResolverConfiguration configuration

    AbstractResolver() {
        configuration = new ResolverConfiguration()
    }

    AbstractResolver(ProjectRegistryReport report) {
        this.report = report
    }

    /**
     * Is class match this resolver
     * @param clazz java class
     * @return if match
     */
    abstract boolean match(Class clazz)

    JavaServiceDomain resolve(Class clazz) {
        def serviceDomain = resolveService(clazz)

        clazz.declaredMethods.each { method ->
            try {
                def methodDomain = resolveMethod(method)

                resolveParameters(method).each {
                    methodDomain.parameters << it
                }

                resolveResponse(method).each {
                    methodDomain.responses << it
                }

                serviceDomain.methods << methodDomain
            } catch (Exception e) {
                e.printStackTrace()

                report.addException("something wrong", null, e)
            }
        }

        return serviceDomain
    }

    abstract JavaServiceDomain resolveService(Class clazz)

    abstract JavaMethodDomain resolveMethod(Method method)

    List<JavaFieldDomain> resolveParameters(Method method) {
        List<JavaFieldDomain> fieldDomains = []

        def parameterName = ""

        method.parameters.each { parameter ->
            try {
                parameterName = parameter.name

                fieldDomains << buildFieldDomainFrom(parameter)
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

        return fieldDomains
    }

    List<JavaFieldDomain> resolveResponse(Method method) {
        def fieldDomains = []

        try {
            def response = method.genericReturnType

            if (response.typeName == "void") {
                return fieldDomains
            }

            fieldDomains << new JavaFieldDomain(
                    fieldName: response.typeName,
                    fieldTypeName: response.typeName,
                    fieldTypes: [resolveClassType(response)]
            )
        } catch (Exception e) {
            e.printStackTrace()
        }

        return fieldDomains
    }

    JavaServiceDomain buildServiceDomainFrom(Class clazz) {
        def serviceDomain = new JavaServiceDomain()

        serviceDomain.serviceName = clazz.simpleName

        serviceDomain.serviceQualifiedName = clazz.canonicalName

        serviceDomain.packageName = clazz['package']['name']

        return serviceDomain
    }

    JavaMethodDomain buildMethodDomainFrom(Method method) {
        def methodDomain = new JavaMethodDomain()

        methodDomain.methodName = method.name

        methodDomain.isDeprecated = isDeprecated(method)

        return methodDomain
    }

    JavaFieldDomain buildFieldDomainFrom(Parameter parameter) {
        def fieldDomain = new JavaFieldDomain()

        fieldDomain.fieldName = parameter.name

        fieldDomain.fieldTypeName = parameter.parameterizedType.typeName

        fieldDomain.fieldTypes = [resolveClassType(parameter.parameterizedType)]

        fieldDomain.isRequired = isRequired(parameter)

        return fieldDomain
    }

    /**
     * If using Type directly, groovy will throw MethodNotFoundException, it means generic mechanism doesn't work here.
     */
    private JavaAbstractType resolveClassType(type) {
        return resolveClassType(type as Type)
    }

    private JavaAbstractType resolveClassType(Type type) {
        if (isTypeVariable(type)) {
            return new JavaGenericType(simpleName: type.typeName)
        }

        if (isPrimitive(type)) {
            return new JavaPrimitiveType(simpleName: (type as Class).simpleName)
        }

        if (isEnum(type)) {
            def cls = type as Class

            def pojo = pojos.find {
                it.qualifiedName == cls.canonicalName
            }

            if (!pojo) {
                pojo = new JavaEnumType(
                        simpleName: cls.simpleName,
                        qualifiedName: cls.canonicalName,
                        packageName: cls['package']['name'])

                cls.declaredFields.each { field ->
                    if (field.type == cls) {
                        pojo.values << new JavaEnumValue(name: field.name, comment: "")
                    }
                }

                pojos << pojo
            }

            return pojo
        }

        if (isList(type)) {
            def listType = new JavaListType()

            if (type instanceof ParameterizedType && type.actualTypeArguments) {
                listType.typeArgument = [resolveClassType((type as ParameterizedType).actualTypeArguments.first())]
            }

            return listType
        }

        if (isArrayType(type)) {
            def listType = new JavaListType(isArray: true)

            listType.typeArgument = [new JavaPrimitiveType(simpleName: type.typeName)]

            return listType
        }

        if (isSet(type)) {
            def setType = new JavaSetType()

            if (type instanceof ParameterizedType && type.actualTypeArguments) {
                setType.typeArgument = [resolveClassType((type as ParameterizedType).actualTypeArguments.first())]
            }

            return setType
        }

        if (isMap(type)) {
            def pojo = new JavaMapType()

            if (type instanceof ParameterizedType && type.actualTypeArguments) {
                def keyType = (type as ParameterizedType).actualTypeArguments[0]

                def valueType = (type as ParameterizedType).actualTypeArguments[1]

                pojo.keyTypeArgument = [resolveClassType(keyType)]

                pojo.valueTypeArgument = [resolveClassType(valueType)]
            }

            return pojo
        }

        if (isObject(type)) {
            def cls

            if (type instanceof ParameterizedType) {
                return buildGenericPojo(type) // 处理泛型
            } else {
                cls = type as Class
            }

            def pojo = pojos.find {
                it.qualifiedName == cls.canonicalName
            }

            if (pojo) {
                return pojo
            }

            if (!cls['package']) {
                println cls
            }

            pojo = new JavaPojoType(
                    simpleName: cls.simpleName,
                    qualifiedName: cls.canonicalName,
                    packageName: cls['package']['name'],
                    imports: [])

            if (cls.typeParameters) {
                pojo.typeArguments = cls.typeParameters.collect {
                    it.typeName
                }
            }

            pojos << pojo

            cls.declaredFields.each { f ->
                if (Modifier.isStatic(f.getModifiers())) { //跳过静态字段
                    return
                }

                if (f.genericType.typeName == "Class") {
                    return
                }

                def ef = new JavaFieldDomain(
                        fieldName: f.name,
                        fieldTypeName: f.type.simpleName,
                        required: isRequired(f))

                ef.fieldTypes = [resolveClassType(f.genericType)]

                pojo.fields << ef
            }

            while (cls.superclass != Object) {
                cls = cls.superclass

                pojo.extensions << (resolveClassType(cls) as JavaPojoType)
            }

            return pojo
        }

        return null
    }

    private boolean isDeprecated(AnnotatedElement element) {
        return element.annotations.any {
            it.annotationType() == Deprecated
        }
    }

    private boolean isRequired(AnnotatedElement element) {
        return element.annotations.any {
            it.annotationType() == NotNull
        }
    }

    private JavaGenericType buildGenericPojo(Type type) {
        JavaGenericType javaGenericType = new JavaGenericType(simpleName: type.typeName)

        type = type as ParameterizedType

        def rawType = type.rawType

        def typeArguments = type.actualTypeArguments

        javaGenericType.genericType = resolveClassType(rawType)

        typeArguments.each {
            javaGenericType.argumentTypes << new JavaArgumentType(types: [resolveClassType(it)])
        }

        return javaGenericType
    }

    private boolean isPrimitive(Type type) {
        if (type instanceof ParameterizedType) {
            return false
        }

        try {
            return (type as Class).canonicalName.startsWith("java") || (type as Class).isPrimitive()
        } catch (Error e) {
            return false
        }
    }

    private boolean isTypeVariable(Type type) {
        return type instanceof TypeVariable
    }

    private boolean isEnum(Type type) {
        if (type instanceof ParameterizedType) {
            return false
        }

        return (type as Class).isEnum()
    }

    private boolean isList(Type type) {
        if (type instanceof ParameterizedType) {
            return List.isAssignableFrom((type.rawType as Class))
        }

        return List.isAssignableFrom((type as Class))
    }

    private boolean isArrayType(Type type) {
        if (type instanceof ParameterizedType) {
            return ((type.rawType as Class).isArray())
        }

        return (type as Class).isArray()
    }

    private boolean isSet(Type type) {
        if (type instanceof ParameterizedType) {
            return Set.isAssignableFrom((type.rawType as Class))
        }

        return Set.isAssignableFrom((type as Class))
    }

    private boolean isMap(Type type) {
        if (type instanceof ParameterizedType) {
            return Map.isAssignableFrom((type.rawType as Class))
        }

        return Map.isAssignableFrom((type as Class))
    }

    private boolean isObject(Type type) {
        return !isPrimitive(type) &&
                !isEnum(type) &&
                !isList(type) &&
                !isSet(type) &&
                !isMap(type)
    }
}

class ResolverConfiguration {
    String serviceType
}
