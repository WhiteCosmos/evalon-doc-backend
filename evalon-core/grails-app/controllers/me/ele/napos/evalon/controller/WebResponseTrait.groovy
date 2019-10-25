package me.ele.napos.evalon.controller

import grails.converters.JSON
import me.ele.napos.evalon.exceptions.EvalonException
import me.ele.napos.evalon.web.EvalonWebResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@SuppressWarnings("GrUnresolvedAccess")
trait WebResponseTrait {
    def success(result = null) {
        render EvalonWebResponse.success(result) as JSON
    }

    def failure(EvalonException e) {
        render EvalonWebResponse.fail(e) as JSON
    }

    def failure(Object message) {
        render EvalonWebResponse.fail(new EvalonException(message)) as JSON
    }

    def seriousFailure(Exception e) {
        try {
            exceptionService.logException(request, params, e)
        } catch (Exception ignored) {
            //忽略
        }

        try {
//            exceptionService.sendExceptionMessage(request, params, e)
        } catch (Exception ignored) {
            //忽略
        }

        e.printStackTrace()

        render EvalonWebResponse.fail(new EvalonException("服务器异常: ${e.message}")) as JSON
    }
}