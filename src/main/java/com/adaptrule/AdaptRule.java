package com.adaptrule;

import com.matching.fgpdg.MatchedNode;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.PDGNode;
import org.eclipse.jdt.core.dom.*;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdaptRule {
    MatchedNode graph;
    MethodDeclaration targetCodeAST;
    Module rhsAST;
    Map<PythonTree, Hole> nameToHole;
    public AdaptRule(MatchedNode graph, MethodDeclaration targetCodeAST, Module rpatternModule) {
        this.graph = graph;
        this.targetCodeAST = targetCodeAST;
        this.rhsAST = rpatternModule;
    }

    public Rule getAdaptedRule() {
        Rule rule=new Rule();
        MethodDeclaration lhsSubstitutedCode = substituteLHStoTargetCode();
        System.out.println(lhsSubstitutedCode);
        MethodDeclaration renamedNames = renameRestOfTheRenamedVarsWithHoles(lhsSubstitutedCode);
        MethodDeclaration lhs = normalizeLHSContext(renamedNames);
        System.out.println(lhs);
        List<ASTNode> collect = this.graph.getAllMatchedNodes().stream().map(MatchedNode::getPatternNode).
                map(PDGNode::getAstNode).collect(Collectors.toList());
        collect.addAll(this.graph.getAllMatchedNodes().stream().map(MatchedNode::getCodeNode).
                map(PDGNode::getAstNode).collect(Collectors.toList()));
        rule.setLHS(lhs.getInternalBody().stream().map(Object::toString).collect(Collectors.joining("\n")));
        MethodDeclaration rhs = createRHS(renamedNames,rhsAST,collect);
        rule.setRHS(rhs.getInternalBody().stream().map(Object::toString).collect(Collectors.joining("\n")));
//        System.out.println(getFunctionDef(rhs).toString());
        return rule;
    }

    public MethodDeclaration getFunctionDef(Module md){
        for (BodyDeclaration stmt : md.getInternalBody()) { //TODO: fix this, how to get methods?
            if (stmt instanceof MethodDeclaration)
                return (MethodDeclaration)stmt;
        }
        return null;
    }

    private MethodDeclaration createRHS(MethodDeclaration lhs, Module rhs, List<ASTNode> matchedNode) {
        FindDeletesFromLHS deletes = new FindDeletesFromLHS(matchedNode);
        try {
            deletes.visit(lhs);
            while (true){
                ASTNode updateTree = checkFinalDeleteNodeIsAChildOfOtherDeletes(deletes.deletes, deletes.finalDeletedNode);
                if (deletes.finalDeletedNode.equals(updateTree))
                    break;
                else{
                    if (updateTree instanceof Call && updateTree.getParent()!=null
                            && updateTree.getParent() instanceof Expression){
                        deletes.finalDeletedNode=updateTree.getParent();
                    }
                    else{
                        deletes.finalDeletedNode=updateTree;
                    }
                }
            }

//            List<PythonTree> deletesCopy = deletes.deletes;
//            for (PythonTree de1 : deletes.deletes) {
//                for (PythonTree de2 : deletes.deletes) {
//                    if (de1!=de2 && Util.isChildNode(de1,de2)){
//                        deletesCopy.remove(de1);
//                    }
//                }
//            }
            DeleteAndUpdateVisitor updator = new DeleteAndUpdateVisitor(deletes.deletes,deletes.finalDeletedNode,rhs);
            updator.visit(lhs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lhs;
    }

    private ASTNode checkFinalDeleteNodeIsAChildOfOtherDeletes(List<ASTNode> childs, ASTNode finalNode){
        for (ASTNode tree : childs) {
            for (ASTNode child : tree.getChildren()) { //TODO: get ast node's children
                if (finalNode.equals(child)){
                    return tree;
                 }
            }
        }
        return finalNode;
    }

    private MethodDeclaration normalizeLHSContext(MethodDeclaration code){
        HoleSearcher searcher = new HoleSearcher();
        try {
            searcher.visit(code);
            LHSNormalizer normalizer = new LHSNormalizer(searcher.getLargestHoleID()+1);
            normalizer.visit(code);
            RenameLHSVisitor renameLHSVisitor = new RenameLHSVisitor();
            renameLHSVisitor.setRenameChild(normalizer.codeAndHole);
            renameLHSVisitor.visit(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    private MethodDeclaration substituteLHStoTargetCode(){
        Map<PythonTree, List<PythonTree>> codeAndParaNode = new HashMap<>();
        for (MatchedNode matchedNode : graph.getAllMatchedNodes()) {
            PythonTree pASTNode = (PythonTree)matchedNode.getPatternNode().getAstNode();
            if (pASTNode!=null)
                pASTNode.isPatternNode=true;
            if (matchedNode.getCodeNode().getAstNode() instanceof stmt){
                continue;
            }
            if (codeAndParaNode.containsKey(matchedNode.getCodeNode().getAstNode())){
                codeAndParaNode.get(matchedNode.getCodeNode().getAstNode()).add(pASTNode);
            }
            else{
                List<PythonTree> matchedParaN = new ArrayList<>();
                matchedParaN.add(pASTNode);
                codeAndParaNode.put((PythonTree) matchedNode.getCodeNode().getAstNode(),matchedParaN);
            }
        }
        RenameLHSVisitor renameLHSVisitor = new RenameLHSVisitor();
        renameLHSVisitor.setRenameChild(codeAndParaNode);

        try {
            renameLHSVisitor.visit(targetCodeAST);
            nameToHole=renameLHSVisitor.getNameToHole();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetCodeAST;
    }

    private MethodDeclaration renameRestOfTheRenamedVarsWithHoles(MethodDeclaration targetCode){
        Map<PythonTree, List<PythonTree>> codeAndParaNode = new HashMap<>();
        CollectChangedNames changedNames = new CollectChangedNames(nameToHole.keySet());
        try {
            Map<PythonTree, List<PythonTree>> nodeAndHoleToRename = new HashMap<>();
            changedNames.visit(targetCode);
            for (Map.Entry<PythonTree, List<PythonTree>> entry : changedNames.getMatchedOtherNodes().entrySet()) {
                for (PythonTree tree : entry.getValue()) {
                    nodeAndHoleToRename.put(tree, List.of(nameToHole.get(entry.getKey())));
                }
            }
            RenameLHSVisitor renameLHSVisitor = new RenameLHSVisitor();
            renameLHSVisitor.setRenameChild(nodeAndHoleToRename);
            try {
                renameLHSVisitor.visit(targetCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetCode;

    }
}
