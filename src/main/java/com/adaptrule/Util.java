package com.adaptrule;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Statement;

public class Util {
    public static boolean isChildNode(ASTNode childNode, Statement parentNode){
        CheckChildNode childChecker = new CheckChildNode(childNode);
        try {
            childChecker.visit(parentNode);
            //TODO: how to call this??? - new approach for the same result? (maybe we don't need the visitor)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return childChecker.isChild();
    }

    static class CheckChildNode extends ASTVisitor{
        private boolean isChild = false;
        private ASTNode childTree;
        public CheckChildNode(ASTNode childTree) {
            this.childTree = childTree;
        }

        @Override
        public void preVisit(ASTNode node) {
            if (node.getChildren()!=null && node.getChildren().contains(childTree))
                isChild=true;
            super.preVisit(node);
        }

        @Override
        public void postVisit(ASTNode node) {

        }
        public boolean isChild() {
            return isChild;
        }
    }
}
