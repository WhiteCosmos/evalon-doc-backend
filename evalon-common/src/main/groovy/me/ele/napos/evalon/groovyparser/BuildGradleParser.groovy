package me.ele.napos.evalon.groovyparser

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase

class BuildGradleParser {
    BlockStatement root = null

    BuildGradleParser() {

    }

    String imports = """
import org.gradle.api.file.*
"""

    BuildGradleParser(String buildGradle) {
        buildGradle = imports + buildGradle
        root = new AstBuilder().buildFromString(buildGradle).first() as BlockStatement
    }

    boolean updatable(String plugin, String classpath) {
        if (!pluginExists(plugin)) {
            return true
        }

        def visitor = new ClasspathCheckVisitor(plugin, classpath)

        visitor.visitBlockStatement(root)

        return visitor.updatable
    }

    boolean pluginExists(String plugin) {
        def adapter = new PluginExistsVisitor(plugin)
        adapter.visitBlockStatement(root)
        return adapter.exists
    }

    boolean classpathExists(String plugin, String classpath) {
        def adapter = new ClasspathExistsVisitor(plugin, classpath)
        adapter.visitBlockStatement(root)
        return adapter.exists
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void updatePlugin(String plugin, String classPath) {
        if (!pluginExists(plugin)) {
            def statement = new AstBuilder().buildFromString("apply plugin: '${plugin}'").first()
            root.statements.add(0, statement)
        }

        if (classpathExists(plugin, classPath)) {
            new ClasspathUpdateVisitor(plugin, classPath).visitBlockStatement(root)
        } else {
            new ClasspathCreateVisitor(plugin, classPath).visitBlockStatement(root)
        }
    }

    void deletePlugin(String plugin) {

    }

    class MethodChain {
        List<String> methods = []
        int currentIndex = 0
        boolean complete = false
        boolean found = false

        def match(String methodName) {
            if (complete) {
                return
            }

            if (methods.get(currentIndex) == methodName) {
                currentIndex++
            }

            if (currentIndex == methods.size()) {
                found = true
            }
        }

        boolean complete() {
            if (found ^ complete) {
                complete = true
                return true
            }

            return false
        }
    }

    class ClasspathCreateVisitor extends GroovyCodeVisitorAdapter {
        String plugin
        String classpath
        MethodChain chain = new MethodChain(methods: ["buildscript", "dependencies"])

        ClasspathCreateVisitor(String plugin, String classpath) {
            this.plugin = plugin
            this.classpath = classpath
        }

        @Override
        void visitMethodCallExpression(MethodCallExpression call) {
            chain.match(call.methodAsString)

            super.visitMethodCallExpression(call)
        }

        @Override
        void visitBlockStatement(BlockStatement statement) {
            if (chain.complete()) {
                def classPath = new AstBuilder().buildFromString("classpath '${classpath}'")
                statement.addStatement(classPath)
            }

            super.visitBlockStatement(statement)
        }
    }

    class ClasspathUpdateVisitor extends GroovyCodeVisitorAdapter {
        String plugin
        String classpath

        ClasspathUpdateVisitor(String plugin, String classpath) {
            this.plugin = plugin
            this.classpath = classpath
        }

        @Override
        void visitConstantExpression(ConstantExpression expression) {
            String prefix = "me.ele.napos:saber"

            if ((expression.value as String).startsWith(prefix)) {
                expression.value = classpath
            }

            super.visitConstantExpression(expression)
        }
    }

    class ClasspathCheckVisitor extends GroovyCodeVisitorAdapter {
        String plugin
        String classpath
        boolean updatable = true

        ClasspathCheckVisitor(String plugin, String classpath) {
            this.plugin = plugin
            this.classpath = classpath
        }

        @Override
        void visitConstantExpression(ConstantExpression expression) {
            String prefix = "me.ele.napos:saber"

            if ((expression.value as String).startsWith(prefix)) {
                updatable = (classpath != (expression.value as String))
            }

            super.visitConstantExpression(expression)
        }
    }

    class ClasspathExistsVisitor extends GroovyCodeVisitorAdapter {
        String plugin
        String classpath
        boolean exists = false

        ClasspathExistsVisitor(String plugin, String classpath) {
            this.plugin = plugin
            this.classpath = classpath
        }

        @Override
        void visitConstantExpression(ConstantExpression expression) {
            Object value = expression.value

            if (value) {
                String prefix = "me.ele.napos:saber"
                if ((value as String).startsWith(prefix)) {
                    exists = true
                }
            }

            super.visitConstantExpression(expression)
        }
    }

    class PluginExistsVisitor extends GroovyCodeVisitorAdapter {
        String plugin

        boolean exists = false

        PluginExistsVisitor(String plugin) {
            this.plugin = plugin
        }

        @Override
        void visitMethodCallExpression(MethodCallExpression call) {
            if (call.methodAsString == "apply") {
                super.visitMethodCallExpression(call)
            }
        }

        @Override
        void visitMapEntryExpression(MapEntryExpression expression) {
            def keyExp = expression.keyExpression
            def valueExp = expression.valueExpression

            if ((keyExp instanceof ConstantExpression)
                    && (keyExp as ConstantExpression).value == "plugin"
                    && (valueExp instanceof ConstantExpression)
                    && (valueExp as ConstantExpression).value == plugin) {
                exists = true
            }
        }
    }

    String toBuildGradle() {
        StringWriter writer = new StringWriter()
        AstNodeToScriptVisitor visitor = new AstNodeToScriptVisitor(writer)
        visitor.visitBlockStatement(root)
        return writer.toString()
    }
}
