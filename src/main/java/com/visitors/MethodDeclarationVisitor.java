package com.visitors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;

public class MethodDeclarationVisitor extends ASTBaseVisitor {
    ArrayList<MethodDeclaration> methodDeclarations = new ArrayList<>();
    @Override
    public boolean visit(MethodDeclaration node){
        methodDeclarations.add(node);
        return super.visit(node);
    }

    @Override
    public void preVisit(ASTNode node) {

    }

    @Override
    public void postVisit(ASTNode node) {

    }

    public ArrayList<MethodDeclaration> getMethodDeclarations(){
        return methodDeclarations;
    }
}
