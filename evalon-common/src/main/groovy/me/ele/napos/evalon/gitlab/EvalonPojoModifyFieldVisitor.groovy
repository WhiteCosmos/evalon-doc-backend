package me.ele.napos.evalon.gitlab

import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.visitor.GenericVisitorAdapter

class EvalonPojoModifyFieldVisitor extends GenericVisitorAdapter<String, Void> {
    String fieldName

    String fieldType

    @Override
    String visit(ClassOrInterfaceDeclaration n, Void arg) {
        n.fields.find {

        }

        n.addField(fieldType, fieldName, Modifier.PUBLIC)

        return this.toString()
    }
}
