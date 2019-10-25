package me.ele.napos.evalon.visitor

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.GenericVisitorAdapter
import com.github.javaparser.javadoc.Javadoc
import me.ele.napos.evalon.domain.JavaMethodDomain

class JavaMethodCommentVisitor extends GenericVisitorAdapter<Void, JavaMethodDomain> {
    @Override
    Void visit(MethodDeclaration n, JavaMethodDomain method) {
        method.responses.each {
            it.fieldName = n.type.toString()
            it.fieldTypeName = n.type.toString()
        }

        method.parameters.each { p ->
            def parameter = n.parameters.find {
                it.nameAsString == p.fieldName
            }

            if (!parameter) {
                return
            }

            p.fieldTypeName = parameter.type.toString()
        }

        readCommentFromJavaDoc(n, method)

        return null
    }

    void readCommentFromJavaDoc(MethodDeclaration n, JavaMethodDomain methodDomain) {
        if (!n.javadoc.present) {
            return
        }

        Javadoc javaDoc = n.javadoc.get()

        methodDomain.commentTitle = JavadocHelper.readJavaDocTitle(n)

        methodDomain.commentBody = JavadocHelper.readJavaDocBody(n)

        methodDomain.parameters.each { parameter ->
            parameter.fieldCommentTitle = JavadocHelper.readParameterComment(javaDoc, parameter.fieldName)
        }

        methodDomain.responses.each { response ->
            response.fieldCommentTitle = JavadocHelper.readResponseComment(javaDoc)
        }
    }
}
