package me.ele.napos.evalon.gitlab

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.visitor.GenericVisitorAdapter

class EvalonPojoActionVisitor extends GenericVisitorAdapter<String, Closure> {
    @Override
    String visit(ClassOrInterfaceDeclaration n, Closure action) {
        action(n)

        return n.toString()
    }
}
