package com.adaptrule;

import com.utils.JavaASTUtil;
import com.visitors.ASTBaseVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

public class Util {
    public static boolean isChildNode(ASTNode childNode, Statement parentNode){
        CheckChildNode childChecker = new CheckChildNode(childNode);
        try {
            childChecker.visit(parentNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return childChecker.isChild();
    }

    static class CheckChildNode extends ASTBaseVisitor {
        private boolean isChild = false;
        private ASTNode childTree;
        public CheckChildNode(ASTNode childTree) {
            this.childTree = childTree;
        }

        @Override
        public void preVisit(ASTNode node) {
            if (JavaASTUtil.getChildren(node).contains(childTree))
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
