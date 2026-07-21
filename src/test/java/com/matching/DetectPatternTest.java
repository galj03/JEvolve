package com.matching;
import com.utils.Utils;
import org.junit.jupiter.api.Test;

import static com.matching.fgpdg.Configurations.PROJECT_REPOSITORY;


public class DetectPatternTest {
    @Test
    void testPattern() throws Exception {
        String pattern = """
                # import numpy as np
                # type :[[l2]] : Any
                # type :[[l1]] : Any
                # type :[l3] : Any
                # type :[l4] : Any
                for :[[l2]] in :[l3]:
                    for :[[l1]] in :[l4]:
                        break
                """;
        String outPath = "./OUTPUT/"; //https://github.com/maldil/MLEditsTest.git
        String projectPath =  PROJECT_REPOSITORY +"pythonInfer/PatternTest";
        System.out.println(pattern);
        Utils.searchProjectForPatterns(projectPath,pattern,outPath);
    }
}
