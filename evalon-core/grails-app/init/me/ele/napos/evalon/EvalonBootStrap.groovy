package me.ele.napos.evalon

import grails.converters.JSON

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 启动或关闭系统时需要执行的操作
 */
class EvalonBootStrap {
    static initializeLocalDateTimeSupport() {
        def dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        def timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        def dateTimeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        JSON.registerObjectMarshaller(LocalDate) {
            return dateFormatter.format(it)
        }

        JSON.registerObjectMarshaller(LocalTime) {
            return timeFormatter.format(it)
        }

        JSON.registerObjectMarshaller(LocalDateTime) {
            return dateTimeformatter.format(it)
        }
    }
}
