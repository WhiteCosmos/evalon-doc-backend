package me.ele.napos.evalon.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

class JSONUtil {
    private static final mapper = new ObjectMapper()

    static {
        mapper.registerModule(new JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"))
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
    }

    static ObjectMapper objectMapper() {
        return mapper
    }

    static prettyPrint(obj) {
        println mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    }
}
