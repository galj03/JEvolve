package com.adaptrule;

import com.visitors.ASTBaseVisitor;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class GetFlatAST extends ASTBaseVisitor {
    List<ASTNode> flatTree = new ArrayList<>();

    @Override
    public void preVisit(ASTNode node) {
        flatTree.add(node);
        super.preVisit(node);
    }
}
