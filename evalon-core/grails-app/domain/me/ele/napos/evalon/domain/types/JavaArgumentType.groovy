package me.ele.napos.evalon.domain.types
/**
 * Grails Gorm不支持同时保存List中的不同类型，所以提供一个包装类
 */
class JavaArgumentType {
    List<JavaAbstractType> types = [] // 不使用List会发生奇怪的错误

    static mapWith = "mongo"

    static hasMany = [
            types: JavaAbstractType
    ]

    static constraints = {

    }
}
