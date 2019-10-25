package me.ele.napos.evalon.gitlab

import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.visitor.GenericVisitorAdapter

class EvalonPojoAddFieldVisitor extends GenericVisitorAdapter<String, Void> {
    String fieldName

    String fieldType

    EvalonPojoAddFieldVisitor(String fieldName, String fieldType) {
        this.fieldName = fieldName
        this.fieldType = fieldType
    }

    @Override
    String visit(ClassOrInterfaceDeclaration n, Void arg) {
        n.addField(fieldType, fieldName)

        return n.toString()
    }
}
