package com.adaptrule;

import com.visitors.ASTBaseVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import java.util.ArrayList;
import java.util.List;

public class HoleSearcher extends ASTBaseVisitor {
    boolean holeContained=false;
    private int largestHoleID=1;
    private List<Expression> holes= new ArrayList<>();


    @Override
    public void preVisit(ASTNode node) {

    }

    @Override
    public void postVisit(ASTNode node) {

    }

    //TODO: holes will be included later: implement these then
//    @Override
//    public Object visitHole(Hole node) throws Exception {
//        holeContained=true;
//        if (Integer.parseInt(node.getN().toString())>largestHoleID)
//            largestHoleID= Integer.parseInt(node.getN().toString());
//        holes.add(node);
//        return null;
//
//    }
//    @Override
//    public Object visitAlphHole(AlphHole node) throws Exception {
//        holeContained=true;
//        holes.add(node);
//        if (Integer.parseInt(node.getN().toString())>largestHoleID)
//            largestHoleID= Integer.parseInt(node.getN().toString());
//        return null;
//    }

    public int getLargestHoleID() {
        return largestHoleID;
    }

    public List<Expression> getHoles() {
        return holes;
    }
}
