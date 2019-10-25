package me.ele.napos.evalon.domain.types


class JavaEnumType extends JavaPojoType {
    List<JavaEnumValue> values = []

    static mapWith = "mongo"

    static embedded = ['values']

    static constraints = {

    }

    void updateByModifiedPojo(JavaEnumType modifiedType) {
        this.values = modifiedType.values
    }
}
