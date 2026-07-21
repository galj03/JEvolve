package com;

import com.matching.fgpdg.Configurations;
import com.utils.FileIO;
import com.utils.Utils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.inferrules.Utils.getPathToResources;

//TODO: adapt to Java
class MainAdaptorTest {
    @BeforeEach
    public void setUp(){
        Configurations.PROJECT_REPOSITORY = "/Users/malinda/Documents/ArtifactEvaluation_FSE2024/donotshare/PyEvolve/src/test/resources/";
        Configurations.TYPE_REPOSITORY = "/Users/malinda/Documents/ArtifactEvaluation_FSE2024/donotshare/TYPE_REPO/";
    }
    @Test
    void transplantPatternToFile1() {
        String projectFile = "author/project/test26.py";
        String LHS = getPathToResources("author/project/pattern12.py") ;
        String RHS =  getPathToResources("author/project/r_pattern12.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        Assertions.assertEquals("import numpy as np\n" +
                "\n" +
                "def function1(sentence,callbacks):\n" +
                "    ff = {\"one\":1,\"two\":2}\n" +
                "    print(ff)\n" +
                "    z = np.sum(ff.values())\n" +
                "    return z",s);
    }

    @Test
    void transplantPatternTestMicrosoft() {
        String projectFile = "author/project/detrex_modeling_backbone_torchvision_resnet.py";
        String LHS = getPathToResources("author/project/lp24.py") ;
        String RHS =  getPathToResources("author/project/rp24.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        Assertions.assertEquals("import numpy as np\n" +
                "\n" +
                "def scfunction1(sentence,callbacks):\n" +
                "    ff = {\"one\":1,\"two\":2}\n" +
                "    print(ff)\n" +
                "    z = np.sum(ff.values())\n" +
                "    return z",s);
    }

    @Test
    void transplantPatternToFile2() {
        String projectFile = "dipy/dipy/dipy/reconst/forecast.py";
        String LHS = getPathToResources("author/project/pattern17.py");
        String RHS = getPathToResources("author/project/r_pattern17.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        Assertions.assertEquals(" ",s);
    }

    @Test
    void transplantPatternToFunction() {
        String projectFile = "author/project/test26.py";
        String LHS = "/Users/malinda/Documents/Research3/InferRules/src/test/resources/author/project/pattern12.py";
        String RHS = "/Users/malinda/Documents/Research3/InferRules/src/test/resources/author/project/r_pattern12.py";

        CompilationUnit codeModule = com.utils.Utils.getCompilationUnit(projectFile);
        MethodDeclaration stmt = Utils.getAllMethods(codeModule).get(1);
        List<ImportDeclaration> imports = codeModule.imports();//Utils.getAllFunctions(codeModule);
        String s = MainAdaptor.transplantPatternToFunction(projectFile, stmt,imports,LHS,
                RHS,FileIO.readFile(Configurations.PROJECT_REPOSITORY + projectFile));
        System.out.println(s);
        Assertions.assertEquals("""
                def function1(sentence, callbacks):
                    ff = {one:1,two:2}
                    print(ff)
                    z = np.sum(ff.values())
                return z""",s);
    }

    @Test
    void testTransplantCPAT1(){
        String projectFile = "LxMLS/lxmls-toolkit/lxmls/classifiers/mira.py";
        String LHS = getPathToResources("author/project/p_l_npsum.py") ;
        String RHS = getPathToResources("author/project/p_r_npsum.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        System.out.println();


    }

    @Test
    void testTransplantCPAT2(){
        String projectFile = "LxMLS/lxmls-toolkit/lxmls/classifiers/perceptron.py";
        String LHS = getPathToResources("author/project/p_l_npsum.py") ;
        String RHS = getPathToResources("author/project/p_r_npsum.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        System.out.println();
    }


    @Test
    void testTransplantCPAT4(){
        String projectFile = "microsoft/nni/examples/trials/benchmarking/automlbenchmark/nni/extensions/NNI/architectures/run_mlp.py";
        String LHS = getPathToResources("author/project/p_l_mean.py") ;
        String RHS = getPathToResources("author/project/p_r_mean.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        System.out.println();
    }

    @Test
    void testTransplantCPAT5(){
        String projectFile = "microsoft/nni/examples/trials/benchmarking/automlbenchmark/nni/extensions/NNI/architectures/run_random_forest.py";
        String LHS = getPathToResources("author/project/p_l_mean.py") ;
        String RHS = getPathToResources("author/project/p_r_mean.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        System.out.println();
    }
    @Test
    void testTransplantCPAT6(){
        String projectFile = "idaholab/raven/ravenframework/SupervisedLearning/MSR.py";
        String LHS = getPathToResources("author/project/p_l_npsum2.py") ;
        String RHS = getPathToResources("author/project/p_r_npsum.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        System.out.println();
    }

    @Test
    void testTransplantCPAT7(){
        String projectFile = "keras-team/keras/keras/utils/layer_utils_test.py";
        String LHS = getPathToResources("author/project/l_join.py") ;
        String RHS = getPathToResources("author/project/r_join.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        System.out.println();
    }

    @Test
    void testTransplantCPAT8(){
        String projectFile = "keras-team/keras/keras/utils/layer_utils_test.py";
        String LHS = getPathToResources("author/project/l_with.py") ;
        String RHS = getPathToResources("author/project/r_with.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS, false);
        System.out.println();
    }




}