package com.adaptrule;

import com.visitors.ASTBaseVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Name;

import java.util.*;

public class CollectChangedNames extends ASTBaseVisitor {
    private Set<ASTNode> changedNames;

    public CollectChangedNames(Set<ASTNode> changedNames) {
        this.changedNames =  changedNames;
    }

    Map<ASTNode, List<ASTNode>> matchedOtherNodes=new HashMap<>();

    @Override
    public boolean visit(Name node) { //TODO: what to catch here? Identifier??
        for (ASTNode name : changedNames) {
            if (name.toString().equals(node.toString())){
                if (matchedOtherNodes.get(name)==null)
                    matchedOtherNodes.put(name, new ArrayList<>(Arrays.asList(node)));
                else
                    matchedOtherNodes.get(name).add(node);
            }
        }
        return super.visit(node);
    }

    public Map<ASTNode, List<ASTNode>> getMatchedOtherNodes() {
        return matchedOtherNodes;
    }

    @Override
    public void preVisit(ASTNode node) {

    }

    @Override
    public void postVisit(ASTNode node) {

    }
}
