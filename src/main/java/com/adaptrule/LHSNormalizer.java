package com.adaptrule;

import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import com.visitors.ASTBaseVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Name;
import org.python.antlr.ast.*;
import org.python.core.PyLong;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LHSNormalizer extends ASTBaseVisitor {
    int start ;
    Map<ASTNode, List<ASTNode>> codeAndHole= new HashMap<>();
    public LHSNormalizer(int holeStarter) {
        this.start=holeStarter;
    }

    @Override
    public void preVisit(ASTNode node) {

    }

    @Override
    public void postVisit(ASTNode node) {

    }

    private boolean normalize(ASTNode node) {
        HoleSearcher searcher = new HoleSearcher();
        try {
            searcher.visit(node);
            if (!searcher.holeContained){
                Hole hole;
                if (node instanceof Name)
                    hole = new AlphanumericHole();
                else
                    hole = new LazyHole();
                PyLong pyLong = new PyLong(start);
                hole.setParent(node.getParent());
                hole.setN(pyLong);
                start++;
                codeAndHole.put(node, List.of(hole));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean visit(ForStatement node) {
        if(normalize(node))
            return null;
        return super.visit(node);
    }

    @Override
    public Object visitExpr(Expr node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitExpr(node);
    }

    @Override
    public Object visitAttribute(Attribute node)  throws Exception {

        return super.visitAttribute(node);
    }

    @Override
    public boolean visit(Name node) {
        return super.visit(node);
    }

    @Override
    public Object visitAssign(Assign node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitAssign(node);
    }

    @Override
    public Object visitTryExcept(TryExcept node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitTryExcept(node);
    }

    @Override
    public Object visitCall(Call node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitCall(node);
    }

    @Override
    public Object visitDict(Dict node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitDict(node);
    }
}
