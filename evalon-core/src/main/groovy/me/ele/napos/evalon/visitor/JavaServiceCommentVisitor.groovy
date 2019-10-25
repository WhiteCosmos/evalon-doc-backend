package me.ele.napos.evalon.visitor

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.visitor.GenericVisitorAdapter
import me.ele.napos.evalon.domain.JavaServiceDomain

class JavaServiceCommentVisitor extends GenericVisitorAdapter<Void, JavaServiceDomain> {
    @Override
    Void visit(ClassOrInterfaceDeclaration n, JavaServiceDomain service) {
        service.commentTitle = VisitorHelper.getFirstValidComment(n)

        n.methods.each { m ->
            def method = service.methods.find {
                it.methodName == m.nameAsString
            }

            method && new JavaMethodCommentVisitor().visit(m, method)
        }

        return null
    }
}
