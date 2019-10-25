package me.ele.napos.evalon.groovyparser


import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.classgen.BytecodeExpression

/**
 * An implementation of the visitor pattern for working with ASTNodes
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */

interface EvalonGroovyCodeVisitor<R, A> {

    // statements

    //-------------------------------------------------------------------------

    R visitBlockStatement(BlockStatement statement, A arg)

    R visitForLoop(ForStatement forLoop, A arg)

    R visitWhileLoop(WhileStatement loop, A arg)

    R visitDoWhileLoop(DoWhileStatement loop, A arg)

    R visitIfElse(IfStatement ifElse, A arg)

    R visitExpressionStatement(ExpressionStatement statement, A arg)

    R visitReturnStatement(ReturnStatement statement, A arg)

    R visitAssertStatement(AssertStatement statement, A arg)

    R visitTryCatchFinally(TryCatchStatement finally1, A arg)

    R visitSwitch(SwitchStatement statement, A arg)

    R visitCaseStatement(CaseStatement statement, A arg)

    R visitBreakStatement(BreakStatement statement, A arg)

    R visitContinueStatement(ContinueStatement statement, A arg)

    R visitThrowStatement(ThrowStatement statement, A arg)

    R visitSynchronizedStatement(SynchronizedStatement statement, A arg)

    R visitCatchStatement(CatchStatement statement, A arg)

    // expressions

    //-------------------------------------------------------------------------

    R visitMethodCallExpression(MethodCallExpression call, A arg)

    R visitStaticMethodCallExpression(StaticMethodCallExpression expression, A arg)

    R visitConstructorCallExpression(ConstructorCallExpression expression, A arg)

    R visitTernaryExpression(TernaryExpression expression, A arg)

    R visitShortTernaryExpression(ElvisOperatorExpression expression, A arg)

    R visitBinaryExpression(BinaryExpression expression, A arg)

    R visitPrefixExpression(PrefixExpression expression, A arg)

    R visitPostfixExpression(PostfixExpression expression, A arg)

    R visitBooleanExpression(BooleanExpression expression, A arg)

    R visitClosureExpression(ClosureExpression expression, A arg)

    R visitTupleExpression(TupleExpression expression, A arg)

    R visitMapExpression(MapExpression expression, A arg)

    R visitMapEntryExpression(MapEntryExpression expression, A arg)

    R visitListExpression(ListExpression expression, A arg)

    R visitRangeExpression(RangeExpression expression, A arg)

    R visitPropertyExpression(PropertyExpression expression, A arg)

    R visitAttributeExpression(AttributeExpression attributeExpression, A arg)

    R visitFieldExpression(FieldExpression expression, A arg)

    R visitMethodPointerExpression(MethodPointerExpression expression, A arg)

    R visitConstantExpression(ConstantExpression expression, A arg)

    R visitClassExpression(ClassExpression expression, A arg)

    R visitVariableExpression(VariableExpression expression, A arg)

    R visitDeclarationExpression(DeclarationExpression expression, A arg)

    R visitGStringExpression(GStringExpression expression, A arg)

    R visitArrayExpression(ArrayExpression expression, A arg)

    R visitSpreadExpression(SpreadExpression expression, A arg)

    R visitSpreadMapExpression(SpreadMapExpression expression, A arg)

    R visitNotExpression(NotExpression expression, A arg)

    R visitUnaryMinusExpression(UnaryMinusExpression expression, A arg)

    R visitUnaryPlusExpression(UnaryPlusExpression expression, A arg)

    R visitBitwiseNegationExpression(BitwiseNegationExpression expression, A arg)

    R visitCastExpression(CastExpression expression, A arg)

    R visitArgumentlistExpression(ArgumentListExpression expression, A arg)

    R visitClosureListExpression(ClosureListExpression closureListExpression, A arg)

    R visitBytecodeExpression(BytecodeExpression expression, A arg)
}

