package com.visitors;

import org.eclipse.jdt.core.dom.*;

public class ASTBaseVisitor extends ASTVisitor {
    public boolean visit(ASTNode node) {
        if (node instanceof AnnotationTypeDeclaration) {
            return visit((AnnotationTypeDeclaration) node);
        }
        if (node instanceof AnnotationTypeMemberDeclaration) {
            return visit((AnnotationTypeMemberDeclaration) node);
        }
        if (node instanceof AnonymousClassDeclaration) {
            return visit((AnonymousClassDeclaration) node);
        }
        if (node instanceof ArrayAccess) {
            return visit((ArrayAccess) node);
        }
        if (node instanceof ArrayCreation) {
            return visit((ArrayCreation) node);
        }
        if (node instanceof ArrayInitializer) {
            return visit((ArrayInitializer) node);
        }
        if (node instanceof ArrayType) {
            return visit((ArrayType) node);
        }
        if (node instanceof AssertStatement) {
            return visit((AssertStatement) node);
        }
        if (node instanceof Assignment) {
            return visit((Assignment) node);
        }
        if (node instanceof Block) {
            return visit((Block) node);
        }
        if (node instanceof BlockComment) {
            return visit((BlockComment) node);
        }
        if (node instanceof BooleanLiteral) {
            return visit((BooleanLiteral) node);
        }
        if (node instanceof BreakStatement) {
            return visit((BreakStatement) node);
        }
        if (node instanceof CastExpression) {
            return visit((CastExpression) node);
        }
        if (node instanceof CatchClause) {
            return visit((CatchClause) node);
        }
        if (node instanceof CharacterLiteral) {
            return visit((CharacterLiteral) node);
        }
        if (node instanceof ClassInstanceCreation) {
            return visit((ClassInstanceCreation) node);
        }
        if (node instanceof CompilationUnit) {
            return visit((CompilationUnit) node);
        }
        if (node instanceof ConditionalExpression) {
            return visit((ConditionalExpression) node);
        }
        if (node instanceof ConstructorInvocation) {
            return visit((ConstructorInvocation) node);
        }
        if (node instanceof ContinueStatement) {
            return visit((ContinueStatement) node);
        }
        if (node instanceof CreationReference) {
            return visit((CreationReference) node);
        }
        if (node instanceof Dimension) {
            return visit((Dimension) node);
        }
        if (node instanceof DoStatement) {
            return visit((DoStatement) node);
        }
        if (node instanceof EmptyStatement) {
            return visit((EmptyStatement) node);
        }
        if (node instanceof EnhancedForStatement) {
            return visit((EnhancedForStatement) node);
        }
        if (node instanceof EnumConstantDeclaration) {
            return visit((EnumConstantDeclaration) node);
        }
        if (node instanceof EnumDeclaration) {
            return visit((EnumDeclaration) node);
        }
        if (node instanceof ExportsDirective) {
            return visit((ExportsDirective) node);
        }
        if (node instanceof ExpressionMethodReference) {
            return visit((ExpressionMethodReference) node);
        }
        if (node instanceof ExpressionStatement) {
            return visit((ExpressionStatement) node);
        }
        if (node instanceof FieldAccess) {
            return visit((FieldAccess) node);
        }
        if (node instanceof FieldDeclaration) {
            return visit((FieldDeclaration) node);
        }
        if (node instanceof ForStatement) {
            return visit((ForStatement) node);
        }
        if (node instanceof IfStatement) {
            return visit((IfStatement) node);
        }
        if (node instanceof ImportDeclaration) {
            return visit((ImportDeclaration) node);
        }
        if (node instanceof InfixExpression) {
            return visit((InfixExpression) node);
        }
        if (node instanceof Initializer) {
            return visit((Initializer) node);
        }
        if (node instanceof InstanceofExpression) {
            return visit((InstanceofExpression) node);
        }
        if (node instanceof IntersectionType) {
            return visit((IntersectionType) node);
        }
        if (node instanceof Javadoc) {
            return visit((Javadoc) node);
        }
        if (node instanceof LabeledStatement) {
            return visit((LabeledStatement) node);
        }
        if (node instanceof LambdaExpression) {
            return visit((LambdaExpression) node);
        }
        if (node instanceof LineComment) {
            return visit((LineComment) node);
        }
        if (node instanceof MarkerAnnotation) {
            return visit((MarkerAnnotation) node);
        }
        if (node instanceof MemberRef) {
            return visit((MemberRef) node);
        }
        if (node instanceof MemberValuePair) {
            return visit((MemberValuePair) node);
        }
        if (node instanceof MethodRef) {
            return visit((MethodRef) node);
        }
        if (node instanceof MethodRefParameter) {
            return visit((MethodRefParameter) node);
        }
        if (node instanceof MethodDeclaration) {
            return visit((MethodDeclaration) node);
        }
        if (node instanceof MethodInvocation) {
            return visit((MethodInvocation) node);
        }
        if (node instanceof Modifier) {
            return visit((Modifier) node);
        }
        if (node instanceof ModuleDeclaration) {
            return visit((ModuleDeclaration) node);
        }
        if (node instanceof ModuleModifier) {
            return visit((ModuleModifier) node);
        }
        if (node instanceof NameQualifiedType) {
            return visit((NameQualifiedType) node);
        }
        if (node instanceof NormalAnnotation) {
            return visit((NormalAnnotation) node);
        }
        if (node instanceof NullLiteral) {
            return visit((NullLiteral) node);
        }
        if (node instanceof NumberLiteral) {
            return visit((NumberLiteral) node);
        }
        if (node instanceof OpensDirective) {
            return visit((OpensDirective) node);
        }
        if (node instanceof PackageDeclaration) {
            return visit((PackageDeclaration) node);
        }
        if (node instanceof ParameterizedType) {
            return visit((ParameterizedType) node);
        }
        if (node instanceof ParenthesizedExpression) {
            return visit((ParenthesizedExpression) node);
        }
        if (node instanceof PostfixExpression) {
            return visit((PostfixExpression) node);
        }
        if (node instanceof PrefixExpression) {
            return visit((PrefixExpression) node);
        }
        if (node instanceof ProvidesDirective) {
            return visit((ProvidesDirective) node);
        }
        if (node instanceof PrimitiveType) {
            return visit((PrimitiveType) node);
        }
        if (node instanceof QualifiedName) {
            return visit((QualifiedName) node);
        }
        if (node instanceof QualifiedType) {
            return visit((QualifiedType) node);
        }
        if (node instanceof ModuleQualifiedName) {
            return visit((ModuleQualifiedName) node);
        }
        if (node instanceof RequiresDirective) {
            return visit((RequiresDirective) node);
        }
        if (node instanceof RecordDeclaration) {
            return visit((RecordDeclaration) node);
        }
        if (node instanceof ReturnStatement) {
            return visit((ReturnStatement) node);
        }
        if (node instanceof SimpleName) {
            return visit((SimpleName) node);
        }
        if (node instanceof SimpleType) {
            return visit((SimpleType) node);
        }
        if (node instanceof SingleMemberAnnotation) {
            return visit((SingleMemberAnnotation) node);
        }
        if (node instanceof SingleVariableDeclaration) {
            return visit((SingleVariableDeclaration) node);
        }
        if (node instanceof StringLiteral) {
            return visit((StringLiteral) node);
        }
        if (node instanceof SuperConstructorInvocation) {
            return visit((SuperConstructorInvocation) node);
        }
        if (node instanceof SuperFieldAccess) {
            return visit((SuperFieldAccess) node);
        }
        if (node instanceof SuperMethodInvocation) {
            return visit((SuperMethodInvocation) node);
        }
        if (node instanceof SuperMethodReference) {
            return visit((SuperMethodReference) node);
        }
        if (node instanceof SwitchCase) {
            return visit((SwitchCase) node);
        }
        if (node instanceof SwitchExpression) {
            return visit((SwitchExpression) node);
        }
        if (node instanceof SwitchStatement) {
            return visit((SwitchStatement) node);
        }
        if (node instanceof SynchronizedStatement) {
            return visit((SynchronizedStatement) node);
        }
        if (node instanceof TagElement) {
            return visit((TagElement) node);
        }
        if (node instanceof TextBlock) {
            return visit((TextBlock) node);
        }
        if (node instanceof TextElement) {
            return visit((TextElement) node);
        }
        if (node instanceof ThisExpression) {
            return visit((ThisExpression) node);
        }
        if (node instanceof ThrowStatement) {
            return visit((ThrowStatement) node);
        }
        if (node instanceof TryStatement) {
            return visit((TryStatement) node);
        }
        if (node instanceof TypeDeclaration) {
            return visit((TypeDeclaration) node);
        }
        if (node instanceof TypeDeclarationStatement) {
            return visit((TypeDeclarationStatement) node);
        }
        if (node instanceof TypeLiteral) {
            return visit((TypeLiteral) node);
        }
        if (node instanceof TypeMethodReference) {
            return visit((TypeMethodReference) node);
        }
        if (node instanceof TypeParameter) {
            return visit((TypeParameter) node);
        }
        if (node instanceof UnionType) {
            return visit((UnionType) node);
        }
        if (node instanceof UsesDirective) {
            return visit((UsesDirective) node);
        }
        if (node instanceof VariableDeclarationExpression) {
            return visit((VariableDeclarationExpression) node);
        }
        if (node instanceof VariableDeclarationStatement) {
            return visit((VariableDeclarationStatement) node);
        }
        if (node instanceof VariableDeclarationFragment) {
            return visit((VariableDeclarationFragment) node);
        }
        if (node instanceof WhileStatement) {
            return visit((WhileStatement) node);
        }
        if (node instanceof WildcardType) {
            return visit((WildcardType) node);
        }
        if (node instanceof YieldStatement) {
            return visit((YieldStatement) node);
        }

        return false;
    }
}
