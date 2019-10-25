package me.ele.napos.evalon.resolver

import me.ele.napos.evalon.domain.JavaMethodDomain
import me.ele.napos.evalon.domain.JavaServiceDomain

import java.lang.reflect.Method

class WineResolver extends AbstractResolver {
    @Override
    boolean match(Class clazz) {
        def annos = ["SoaService", "NcpService", "NopService", "RestService"]
        try {
            return clazz.annotations.any {
                it.annotationType().simpleName in annos
            }
        } catch (Exception ignore) {
            return false
        } catch (Error ignore) {
            return false
        }
    }

    @Override
    JavaServiceDomain resolveService(Class clazz) {
        def serviceDomain = buildServiceDomainFrom(clazz)

        return serviceDomain
    }

    @Override
    JavaMethodDomain resolveMethod(Method method) {
        def methodDomain = buildMethodDomainFrom(method)

        return methodDomain
    }
}
