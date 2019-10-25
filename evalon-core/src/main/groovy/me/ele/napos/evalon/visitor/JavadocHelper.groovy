package me.ele.napos.evalon.visitor

import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc
import com.github.javaparser.javadoc.Javadoc
import com.github.javaparser.javadoc.JavadocBlockTag

class JavadocHelper {
    static String readJavaDocTitle(NodeWithJavadoc n) {
        if (!n.getJavadoc().present) {
            return ""
        }

        def javaDoc = n.getJavadoc().get()

        return javaDoc.toText().readLines().first() // read first line of javadoc
    }

    static String readJavaDocBody(NodeWithJavadoc n) {
        if (!n.getJavadoc().present) {
            return ""
        }

        def javaDoc = n.getJavadoc().get()

        return javaDoc.description.toText()
    }

    static String readParameterComment(Javadoc javadoc, String fieldName) {
        def doc = javadoc.blockTags.find {
            it.type == JavadocBlockTag.Type.PARAM && it.name.present && it.name.get() == fieldName
        }

        return doc ? doc.getContent().toText() : ""
    }

    static String readResponseComment(Javadoc javadoc) {
        def doc = javadoc.blockTags.find {
            it.type == JavadocBlockTag.Type.RETURN
        }

        return doc ? doc.getContent().toText() : ""
    }
}
