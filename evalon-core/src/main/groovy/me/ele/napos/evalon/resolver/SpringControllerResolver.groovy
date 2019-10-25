package me.ele.napos.evalon.resolver


import me.ele.napos.evalon.domain.JavaMethodDomain
import me.ele.napos.evalon.domain.JavaServiceDomain
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.lang.reflect.Method

class SpringControllerResolver extends AbstractResolver {
    SpringControllerResolver() {
        super()

        this.configuration.serviceType = "REST"
    }

    @Override
    boolean match(Class clazz) {
        try {
            return clazz.annotations.any {
                it.annotationType() == RestController || it.annotationType() == Controller
            }
        } catch (Exception e) {
            e.printStackTrace()

            return false
        } catch (Error e) {
            e.printStackTrace()

            return false
        }
    }

    @Override
    JavaServiceDomain resolveService(Class clazz) {
        def domain = buildServiceDomainFrom(clazz)

        def mapping = clazz.getAnnotation(RequestMapping) // read request url

        mapping && mapping.value() && (domain.serviceUrl = mapping.value().first())

        domain.serviceType = "SPRING"

        return domain
    }

    @Override
    JavaMethodDomain resolveMethod(Method method) {
        def domain = buildMethodDomainFrom(method)

        return domain
    }
}
