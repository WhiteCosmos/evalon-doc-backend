package me.ele.napos.evalon.resolver

import me.ele.napos.evalon.domain.JavaMethodDomain
import me.ele.napos.evalon.domain.JavaServiceDomain

import java.lang.reflect.Method

/**
 * Default doc resolver for java interface
 */
class DefaultResolver extends AbstractResolver {
    @Override
    boolean match(Class clazz) {
        try {
            return clazz.isInterface()
        } catch (Exception ignore) {
            return false
        } catch (Error ignore) {
            return false
        }
    }

    @Override
    JavaServiceDomain resolveService(Class clazz) {
        return this.buildServiceDomainFrom(clazz)
    }

    @Override
    JavaMethodDomain resolveMethod(Method method) {
        return this.buildMethodDomainFrom(method)
    }
}
