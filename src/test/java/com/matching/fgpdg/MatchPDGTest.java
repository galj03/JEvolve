package com.matching.fgpdg;

import com.utils.JavaASTUtil;
import com.utils.Utils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MatchPDGTest {

    @Test
    void getSubGraphs() {
        CompilationUnit codeModule = Utils.getCompilationUnit("author/project/test1.py");
        CompilationUnit patternModule = Utils.getCompilationUnit("author/project/pattern.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(codeModule).get(1);
        PDGBuildingContext fcontext = null;
        try {
            fcontext = new PDGBuildingContext(new ArrayList<>(),"author/project/test1.py");
            PDGGraph fpdg = new PDGGraph(func,fcontext);

            PDGBuildingContext mcontext = new PDGBuildingContext(new ArrayList<>(),"author/project/pattern.py");
            PDGGraph mpdg = new PDGGraph(patternModule,mcontext);

            MatchPDG match = new MatchPDG();
            List<MatchedNode> graphs = match.getSubGraphs(mpdg,fpdg,mcontext,fcontext );

            match.drawMatchedGraphs(fpdg,graphs,"OUTPUT/matches/text1.dot");
            Utils.markNodesInCode("src/test/resources/author/project/test1.py",graphs,"OUTPUT/matches/text1.html","","");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}