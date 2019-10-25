package me.ele.napos.evalon.gitlab

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration

class EvalonJavaCodeHelper {
    Integer projectId

    String branch

    EvalonJavaCodeHelper(Integer projectId, String branch) {
        this.projectId = projectId
        this.branch = branch
    }

//    addService() {
//
//    }
//
//    modifyService() {
//
//    }
//
//    deleteService() {
//
//    }
//
//    addMethod() {
//
//    }
//
//    modifyMethod() {
//
//    }

//    deleteMethod() {
//
//    }

    void addField(String path, String fieldName, Class fieldType) {

    }

    void addField(String path, String fieldName, String fieldType) {
        String content = GitlabApiV3.filesAsContent(projectId, branch, path)

        Closure action = { ClassOrInterfaceDeclaration n ->
            def fields = n.fields.findAll() { field ->
                fieldName == getFieldName(field)
            }

            fields.each {
                it.remove()
            }

            n.addField(fieldType, fieldName)
        }

        def unit = JavaParser.parse(content)

        new EvalonPojoActionVisitor().visit(unit, action)

        content = unit.toString()

        println content

        GitlabApiV3.commit(projectId, branch, [
                new GitlabApiV3.GitlabUpdateAction(file_path: path, content: content)
        ])
    }

    void modifyField(String path, String fieldName) {
        String content = GitlabApiV3.filesAsContent(projectId, branch, path)

    }

    void deleteField(String path, String fieldName) {
        String content = GitlabApiV3.filesAsContent(projectId, branch, path)

        Closure action = { ClassOrInterfaceDeclaration n ->
            n.fields.findAll() { field ->
                fieldName.startsWith(getFieldName(field))
            }.each {
                it.remove()
            }
        }

        def unit = JavaParser.parse(content)

        new EvalonPojoActionVisitor().visit(unit, action)

        content = unit.toString()

        println content

        GitlabApiV3.commit(projectId, branch, [
                new GitlabApiV3.GitlabUpdateAction(file_path: path, content: content)
        ])
    }

    private String getFieldName(FieldDeclaration n) {
        return n.variables[0].nameAsString
    }
}
