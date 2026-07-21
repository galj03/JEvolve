package com.adaptrule;

import com.matching.fgpdg.nodes.ast.LazyHole;
import com.visitors.ASTBaseVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenameLHSVisitor extends ASTBaseVisitor {
    Map<ASTNode, List<ASTNode>> codeAndPara;
    Map<ASTNode, ASTNode> holeAndCode = new HashMap<>();
    Map<ASTNode, Hole> nameToHole = new HashMap<>();
    int largestHoleID=0;

    @Override
    public void preVisit(ASTNode node) {

    }

    @Override
    public void postVisit(ASTNode node) {

    }
    @Override
    public Object visitAssign(Assign node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalValue().equals(child)){
                node.setValue(replacement.get(0));
                replacement.get(0).setMyLineNumber(child.getLine());
            }else if (node.getInternalTargets().contains(child)){
                int childIndex= node.getInternalTargets().indexOf(child);
                node.getInternalTargets().remove(child);
                node.getInternalTargets().add(childIndex, (expr) replacement.get(0));
                replacement.get(0).setMyLineNumber(child.getLine());
            }
            if(node.getChildren().contains(child)){

                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitAssign(node);
    }

    @Override
    public Object visitCall(Call node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalArgs().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                int childIndex= node.getInternalArgs().indexOf(child);
                node.getInternalArgs().remove(child);
                node.getInternalArgs().add(childIndex,(expr)replacement.get(0));

            }else if (node.getInternalFunc().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setFunc(replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitCall(node);
    }

    @Override
    public Object visitListComp(ListComp node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalElt().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setElt(replacement.get(0));
            }
            for (comprehension generator : node.getInternalGenerators()) {
                if (generator.getInternalIter().equals(child)){
                    replacement.get(0).setMyLineNumber(child.getLine());
                    generator.setIter(replacement.get(0));
                }
                else if (generator.getInternalTarget().equals(child)){
                    replacement.get(0).setMyLineNumber(child.getLine());
                    generator.setTarget(replacement.get(0));
                }
                if(generator.getChildren().contains(child)){
                    replacement.get(0).setMyLineNumber(child.getLine());
                    if (child instanceof Name && replacement.get(0) instanceof Hole){
                        nameToHole.put((Name) child,(Hole)replacement.get(0));
                    }
                    else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                        nameToHole.put((Name) child,(Hole)replacement.get(0));
                    }
                    int childIndex= generator.getChildren().indexOf(child);
                    generator.getChildren().remove(child);
                    generator.getChildren().add(childIndex,replacement.get(0));
                }

            }

            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }

        return super.visitListComp(node);
    }




    @Override
    public Object visitFor(For node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalIter().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setIter(replacement.get(0));
            }else if (node.getInternalTarget().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setTarget(replacement.get(0));
            }
            else if (node.getInternalBody().contains(child)){
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                Expr expr = new Expr();
                expr.setValue(replacement.get(0));
                node.getInternalBody().add(childIndex,expr);
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitFor(node);
    }

    @Override
    public Object visitReturn(Return node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalValue().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setValue(replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitReturn(node);

    }

    @Override
    public Object visitModule(Module node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if(node.getInternalBody().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                Expr e = new Expr();
                e.setValue(replacement.get(0));
                node.getInternalBody().add(childIndex, e);
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));

                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitModule(node);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if(node.getInternalBody().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                if (replacement.get(0) instanceof stmt){
                    node.getInternalBody().add(childIndex, (stmt) replacement.get(0));
                }
                else{
                    Expr e = new Expr();
                    e.setValue(replacement.get(0));
                    e.setParent(node);
                    node.getInternalBody().add(childIndex, e);
                }
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setCharStartIndex(child.getCharStartIndex());
                replacement.get(0).setCharStopIndex(child.getCharStopIndex());
                replacement.get(0).setChildIndex(child.getChildIndex());
                replacement.get(0).setTokenStartIndex(child.getTokenStartIndex());
                replacement.get(0).setTokenStopIndex(child.getTokenStopIndex());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                HoleSearcher searcher = new HoleSearcher();
                searcher.visit(replacement.get(0));
                for (expr hole : searcher.getHoles()) {
                    if (holeAndCode.containsKey(hole)){
                        nameToHole.put(holeAndCode.get(hole), (Hole) hole);
                    }
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitFunctionDef(node);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if(node.getInternalValue()==child){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                node.setValue(replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitAttribute(node);
    }

    public Map<ASTNode, Hole> getNameToHole() {
        return nameToHole;
    }

    public Map<ASTNode, List<ASTNode>> getRenameChild() {
        return codeAndPara;
    }

    public void setRenameChild(Map<ASTNode,List<ASTNode>> renameChild) {
        this.codeAndPara = renameChild;
        for (Map.Entry<ASTNode, List<ASTNode>> entry : renameChild.entrySet()) {
            if(!codeAndPara.containsKey(entry.getValue().getFirst())){
                holeAndCode.put(entry.getValue().getFirst(),entry.getKey());
            }
        }
    }

    public int getLargestHoleID() {
        return largestHoleID;
    }
}
