package me.ele.napos.evalon.groovyparser

import org.codehaus.groovy.ast.GroovyCodeVisitor
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.classgen.BytecodeExpression


abstract class GroovyCodeVisitorAdapter implements GroovyCodeVisitor {
    @Override
    void visitBlockStatement(BlockStatement statement) {
        statement.statements.each {
            it.visit(this)
        }
    }

    @Override
    void visitForLoop(ForStatement forLoop) {

    }

    @Override
    void visitWhileLoop(WhileStatement loop) {

    }

    @Override
    void visitDoWhileLoop(DoWhileStatement loop) {

    }

    @Override
    void visitIfElse(IfStatement ifElse) {

    }

    @Override
    void visitExpressionStatement(ExpressionStatement statement) {
        statement.expression.visit(this)
    }

    @Override
    void visitReturnStatement(ReturnStatement statement) {
        statement.expression.visit(this)
    }

    @Override
    void visitAssertStatement(AssertStatement statement) {

    }

    @Override
    void visitTryCatchFinally(TryCatchStatement finally1) {

    }

    @Override
    void visitSwitch(SwitchStatement statement) {

    }

    @Override
    void visitCaseStatement(CaseStatement statement) {

    }

    @Override
    void visitBreakStatement(BreakStatement statement) {

    }

    @Override
    void visitContinueStatement(ContinueStatement statement) {

    }

    @Override
    void visitThrowStatement(ThrowStatement statement) {

    }

    @Override
    void visitSynchronizedStatement(SynchronizedStatement statement) {

    }

    @Override
    void visitCatchStatement(CatchStatement statement) {

    }

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        call.method.visit(this)
        call.arguments.visit(this)
    }

    @Override
    void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {

    }

    @Override
    void visitConstructorCallExpression(ConstructorCallExpression expression) {

    }

    @Override
    void visitTernaryExpression(TernaryExpression expression) {

    }

    @Override
    void visitShortTernaryExpression(ElvisOperatorExpression expression) {

    }

    @Override
    void visitBinaryExpression(BinaryExpression expression) {

    }

    @Override
    void visitPrefixExpression(PrefixExpression expression) {

    }

    @Override
    void visitPostfixExpression(PostfixExpression expression) {

    }

    @Override
    void visitBooleanExpression(BooleanExpression expression) {

    }

    @Override
    void visitClosureExpression(ClosureExpression expression) {
        expression.code.visit(this)
    }

    @Override
    void visitTupleExpression(TupleExpression expression) {
        expression.expressions.each {
            it.visit(this)
        }
    }

    @Override
    void visitMapExpression(MapExpression expression) {
        expression.mapEntryExpressions.each {
            it.visit(this)
        }
    }

    @Override
    void visitMapEntryExpression(MapEntryExpression expression) {
        expression.keyExpression.visit(this)
        expression.valueExpression.visit(this)
    }

    @Override
    void visitListExpression(ListExpression expression) {

    }

    @Override
    void visitRangeExpression(RangeExpression expression) {

    }

    @Override
    void visitPropertyExpression(PropertyExpression expression) {

    }

    @Override
    void visitAttributeExpression(AttributeExpression attributeExpression) {

    }

    @Override
    void visitFieldExpression(FieldExpression expression) {

    }

    @Override
    void visitMethodPointerExpression(MethodPointerExpression expression) {

    }

    @Override
    void visitConstantExpression(ConstantExpression expression) {

    }

    @Override
    void visitClassExpression(ClassExpression expression) {

    }

    @Override
    void visitVariableExpression(VariableExpression expression) {

    }

    @Override
    void visitDeclarationExpression(DeclarationExpression expression) {

    }

    @Override
    void visitGStringExpression(GStringExpression expression) {

    }

    @Override
    void visitArrayExpression(ArrayExpression expression) {

    }

    @Override
    void visitSpreadExpression(SpreadExpression expression) {

    }

    @Override
    void visitSpreadMapExpression(SpreadMapExpression expression) {

    }

    @Override
    void visitNotExpression(NotExpression expression) {

    }

    @Override
    void visitUnaryMinusExpression(UnaryMinusExpression expression) {

    }

    @Override
    void visitUnaryPlusExpression(UnaryPlusExpression expression) {

    }

    @Override
    void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {

    }

    @Override
    void visitCastExpression(CastExpression expression) {

    }

    @Override
    void visitArgumentlistExpression(ArgumentListExpression expression) {
        expression.expressions.each {
            it.visit(this)
        }
    }

    @Override
    void visitClosureListExpression(ClosureListExpression closureListExpression) {

    }

    @Override
    void visitBytecodeExpression(BytecodeExpression expression) {

    }

}
