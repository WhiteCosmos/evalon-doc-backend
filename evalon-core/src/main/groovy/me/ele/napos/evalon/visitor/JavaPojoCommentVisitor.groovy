package me.ele.napos.evalon.visitor

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc
import com.github.javaparser.ast.visitor.GenericVisitorAdapter
import me.ele.napos.evalon.domain.types.JavaEnumType
import me.ele.napos.evalon.domain.types.JavaPojoType

class JavaPojoCommentVisitor extends GenericVisitorAdapter<Void, JavaPojoType> {
    @Override
    Void visit(EnumDeclaration n, JavaPojoType pojo) {
        def enumType = pojo as JavaEnumType

        enumType.commentTitle = JavadocHelper.readJavaDocTitle(n)

        enumType.commentBody = JavadocHelper.readJavaDocBody(n)

        n.entries.each { entry ->
            def v = enumType.values.find {
                it.name == entry.nameAsString
            }

            v && (v.comment = VisitorHelper.getJavaDocText(entry))
        }

        return null
    }

    @Override
    Void visit(ClassOrInterfaceDeclaration n, JavaPojoType pojo) {
        pojo.commentTitle = JavadocHelper.readJavaDocTitle(n)

        pojo.commentBody= JavadocHelper.readJavaDocBody(n)

        n.fields.each { f ->
            String fieldName = f.variables[0].nameAsString

            def field = pojo.fields.find {
                it.fieldName == fieldName
            }

            if (!field) {
                return
            }

            field.fieldTypeName = f.elementType.toString()

            field.fieldCommentTitle = JavadocHelper.readJavaDocTitle(f)

            field.fieldCommentBody = JavadocHelper.readJavaDocBody(f)
        }

        return null
    }
}
