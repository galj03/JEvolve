package com.matching.fgpdg.nodes;

import org.eclipse.jdt.core.dom.ASTNode;

public class PDGLazyHole extends PDGHoleNode {
    public PDGLazyHole(ASTNode astNode, int nodeType, String value, String key, String dataType, String dataName, boolean isDataNode, boolean isActionNode, boolean isContralNode) {
        super(astNode, nodeType, value, key, dataType, dataName, isDataNode, isActionNode, isContralNode);
    }

    @Override
    public boolean isEqualNodes(PDGNode node) {
        return true;
    }
}
