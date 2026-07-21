package com.adaptrule;

import com.visitors.ASTBaseVisitor;
import org.eclipse.jdt.core.dom.*;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.*;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class DeleteAndUpdateVisitor extends ASTBaseVisitor {
    java.util.List<ASTNode> del;
    ASTNode finalDel;
    CompilationUnit rhs;
    public DeleteAndUpdateVisitor(java.util.List<ASTNode> deletes, ASTNode finalDeletedNode, CompilationUnit rhs) {
        this.del=deletes;
        this.finalDel=finalDeletedNode;
        this.rhs=rhs;
    }


    @Override
    public void preVisit(ASTNode node) {

    }

    @Override
    public void postVisit(ASTNode node) {

    }

    private void removeChild(ASTNode node){

    }

    @Override
    public boolean visit(Assignment node) {
        for (ASTNode tree : del) {
            if (node.getInternalValue()==tree && finalDel==node.getInternalValue()){
                if(rhs.getInternalBody().get(0) instanceof Expr){
                    node.setValue(((Expr)rhs.getInternalBody().get(0)).getInternalValue());
                }

            }
            if (node.getChildren().remove(tree)) {
                int location = node.getChildren().indexOf(tree);
                if (location != -1) {
                    node.getChildren().add(location,rhs);
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public Object visitModule(Module node) throws Exception {
        for (ASTNode tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visitModule(node);
    }

    @Override
    public Object visitExpression(Expression node) throws Exception {
        for (ASTNode tree : del) {
            int location = -1;
            if (node.getInternalBody().equals(tree)){
                node.setBody(tree);
            }
            location =  node.getChildren().indexOf(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                node.getChildren().add(location,tree);
            }
        }
        return super.visit(node);
    }

    //TODO: one of these is ExpressionStatement??
    @Override
    public Object visitExpression(Expression node) throws Exception{
        for (ASTNode tree : del) {
            if(node.getInternalValue()==tree){
                node.setValue(tree);
            }
            if (node.getChildren()!=null&&node.getChildren().contains(tree)){
                int location =  node.getChildren().indexOf(tree);
                if (location!=-1){
                    node.getChildren().add(location,tree);
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        for (ASTNode tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
            java.util.List<stmt> deletables = new ArrayList<>();
            for (Statement stmt : node.getInternalBody()) { //formerly: stmt
                if (Util.isChildNode(tree, stmt)&&!Util.isChildNode(finalDel, stmt)){
                    deletables.add(stmt);
                }
            }


            for (stmt stmt : deletables) {
                if (stmt instanceof Assign)
                    node.getInternalBody().remove(stmt);
            }
        }
        return super.visit(node);
    }

    @Override
    public Object visitAsyncFunctionDef(AsyncFunctionDef node) throws Exception {
        for (PythonTree tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visitAsyncFunctionDef (node);
    }

    @Override
    public boolean visit(ForStatement node) {
        for (ASTNode tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public Object visitAsyncFor(AsyncFor node) throws Exception {
        for (PythonTree tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visitAsyncFor(node);

    }

    @Override
    public boolean visit(WhileStatement node) {
        for (ASTNode tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(IfStatement node) {
        for (ASTNode tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public Object visitWith(With node) throws Exception {
        for (PythonTree tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visitWith (node);
    }

    @Override
    public Object visitAsyncWith(AsyncWith node) throws Exception {
        for (PythonTree tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visitAsyncWith (node);
    }

    @Override
    public Object visitTryExcept(TryExcept node) throws Exception {
        for (PythonTree tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visitTryExcept (node);
    }

    @Override
    public Object visitTryFinally(TryFinally node) throws Exception {
        for (PythonTree tree : del) {
            int location = -1;
            if (tree==finalDel && node.getInternalBody().contains(tree)){
                location =  node.getInternalBody().indexOf(tree);
            }
            node.getInternalBody().remove(tree);
            node.getChildren().remove(tree);
            if (location!=-1){
                for (stmt stmt : rhs.getInternalBody()) {
                    node.getInternalBody().add(location,stmt);
                    location+=1;
                }
            }
        }
        return super.visitTryFinally (node);
    }
}
