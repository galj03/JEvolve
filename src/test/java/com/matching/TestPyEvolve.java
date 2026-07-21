package com.matching;

import com.adaptrule.AdaptRule;
import com.adaptrule.Rule;
import com.inferrules.comby.jsonResponse.CombyRewrite;
import com.inferrules.comby.operations.BasicCombyOperations;
import com.matching.fgpdg.MatchedNode;
import io.vavr.control.Try;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.inferrules.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.inferrules.Utils.getMatchedNodes;

public class TestPyEvolve {
    @Test
    void testPipeline1() throws Exception {
        String filename = "test26";
        String lpatternname = "pattern12";
        String rpatternname = "r_pattern12";

        CompilationUnit codeModule = Utils.getCompilationUnit("author/project/" + filename + ".py");
        String code = Utils.getAllMethods(codeModule).getFirst().toString();
        CompilationUnit lpatternModule = Utils.getCompilationUnitForTemplate(Utils.getPathToResources("author/project/" + lpatternname + ".py"));
        CompilationUnit rpatternModule = Utils.getCompilationUnitForTemplate(Utils.getPathToResources("author/project/" + rpatternname + ".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname, rpatternname, codeModule, lpatternModule, rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).toList();
        AdaptRule aRule = new AdaptRule(allMatchedGraphs.getFirst(), Utils.getAllMethods(codeModule).getFirst(), rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        Try<CombyRewrite> changedCode = BasicCombyOperations.rewrite(rule.getLHS(), rule.getRHS(), code, ".python");
        Assertions.assertEquals("""
                def function1(sentence, callbacks):
                    ff = {one:1,two:2}
                    print(ff)
                    z = np.sum(ff.values())
                return z
                """, changedCode.get().getRewrittenSource());
    }

    @Test
    void testPipelineForProject() throws Exception {
        String filename = "test26";
        String lpatternname = "pattern12";
        String rpatternname = "r_pattern12";

        CompilationUnit codeModule = Utils.getCompilationUnit("author/project/" + filename + ".py");
        String code = Utils.getAllMethods(codeModule).getFirst().toString();
        CompilationUnit lpatternModule = Utils.getCompilationUnitForTemplate(Utils.getPathToResources("author/project/" + lpatternname + ".py"));
        CompilationUnit rpatternModule = Utils.getCompilationUnitForTemplate(Utils.getPathToResources("author/project/" + rpatternname + ".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname, rpatternname, codeModule, lpatternModule, rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).toList();
        AdaptRule aRule = new AdaptRule(allMatchedGraphs.getFirst(), Utils.getAllMethods(codeModule).getFirst(), rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        Try<CombyRewrite> changedCode = BasicCombyOperations.rewrite(rule.getLHS(), rule.getRHS(), code, ".python");
        Assertions.assertEquals("""
                def function1(sentence, callbacks):
                    ff = {one:1,two:2}
                    print(ff)
                    z = np.sum(ff.values())
                return z
                """, changedCode.get().getRewrittenSource());
    }
}
