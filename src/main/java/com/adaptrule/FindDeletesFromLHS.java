package com.adaptrule;

import org.eclipse.jdt.core.dom.*;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.List;

public class FindDeletesFromLHS extends ASTVisitor {
    ASTNode finalDeletedNode=null; //this was PythonTree
    List<ASTNode> matchedNode;
    java.util.List<ASTNode> deletes = new ArrayList<>(); //this was PythonTree


    //TODO: finish this: understand how it works + adapt
    @Override
    public void preVisit(ASTNode node) {

        if (node instanceof MethodDeclaration || node instanceof CompilationUnit) { //Module
            super.preVisit(node);
            return;
        }
        else if (node instanceof Statement && this.matchedNode.contains(node)){
            deletes.add(node);
            int fline=0 ;
            int line=0;
            if (finalDeletedNode!=null) {
                fline = finalDeletedNode.getMyLineNumber() != -1 ? finalDeletedNode.getMyLineNumber() : finalDeletedNode.getLine();
                line = node.getMyLineNumber() != -1 ? node.getMyLineNumber() : node.getLine();
            }
            if (finalDeletedNode==null)
                finalDeletedNode=node;
            else if (fline<line)
                finalDeletedNode=node;
        }
        else if (node instanceof Call && this.matchedNode.contains(node)){
            if (node.getParent() !=null && node.getParent() instanceof Expression){ //TODO: or ExpressionStatement?
                deletes.add(node.getParent());
            }
            else{
                deletes.add(node);
            }


            int fline=0;
            int line=0;
            if (finalDeletedNode != null) {
                fline = finalDeletedNode.getMyLineNumber()!=-1 ? finalDeletedNode.getMyLineNumber():finalDeletedNode.getLine();
                line = node.getMyLineNumber()!=-1 ? node.getMyLineNumber():node.getLine();
            }

            if (finalDeletedNode==null) {
                if(node.getParent()!=null && node.getParent() instanceof Expression){
                    finalDeletedNode = node.getParent();
                }else{
                    finalDeletedNode = node;
                }
            }
            else if (fline<line){
                if(node.getParent()!=null && node.getParent() instanceof Expression){
                    finalDeletedNode = node.getParent();
                }else{
                    finalDeletedNode = node;
                }
            }
        }
        super.preVisit(node);
    }

    @Override
    public void postVisit(ASTNode node) {

    }

    public FindDeletesFromLHS(List<ASTNode> matchedNode) {
        this.matchedNode = matchedNode;
    }

//    @Override
//    public Object visitExpr(Expr node) throws Exception {
//        if (node instanceof stmt && node.isPatternNode)
//            deletes.add(node);
//        return super.visitExpr(node);
//    }

    public ASTNode getFinalDeletedNode() {
        return finalDeletedNode;
    }
}
