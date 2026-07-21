package com.matching.fgpdg;

import com.matching.ConcreteJavaParser;
import com.utils.DotGraph;
import com.utils.JavaASTUtil;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PDGGraphTest {
    @Test
    void testPattern() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/pattern.py");
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(new ArrayList<>(),"author/project/pattern.py");
            PDGGraph pdg = new PDGGraph(parse,context);

            MatchPDG mpdg = new    MatchPDG();
            PDGGraph _pattern= mpdg.pruneAndCleanPatternPDG(pdg);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"__pattern__file___"+".dot"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG1() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test1.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test1.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (48, pdg.getNodes().size());
            Assertions.assertEquals (19, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG2() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test2.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test2.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (31, pdg.getNodes().size());
            Assertions.assertEquals (12, pdg.statementNodes.size());
            Assertions.assertEquals (9, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG3() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test3.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test3.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (34, pdg.getNodes().size());
            Assertions.assertEquals (13, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG4() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test4.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test4.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (44, pdg.getNodes().size());
            Assertions.assertEquals (18, pdg.statementNodes.size());
            Assertions.assertEquals (14, pdg.dataSources.size());
            //TODO Tuples do not engage with other elements-FIX IT
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG5() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test5.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test5.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (18, pdg.getNodes().size());
            Assertions.assertEquals (7, pdg.statementNodes.size());
            Assertions.assertEquals (5, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG6() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test6.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test6.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (37, pdg.getNodes().size());
            Assertions.assertEquals (19, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG7() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test7.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test7.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (25, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (6, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG8() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test8.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test8.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (26, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (9, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG9() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test9.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test9.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (31, pdg.getNodes().size());
            Assertions.assertEquals (12, pdg.statementNodes.size());
            Assertions.assertEquals (7, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG10() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test10.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test10.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (37, pdg.getNodes().size());
            Assertions.assertEquals (12, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG11() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test11.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test11.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (47, pdg.getNodes().size());
            Assertions.assertEquals (17, pdg.statementNodes.size());
            Assertions.assertEquals (12, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG12() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test12.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test12.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (34, pdg.getNodes().size());
            Assertions.assertEquals (13, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG13() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test13.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test13.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (34, pdg.getNodes().size());
            Assertions.assertEquals (13, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG14() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test14.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test14.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (34, pdg.getNodes().size());
            Assertions.assertEquals (13, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG15() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test15.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test15.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (33, pdg.getNodes().size());
            Assertions.assertEquals (11, pdg.statementNodes.size());
            Assertions.assertEquals (13, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG16() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test16.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test16.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (27, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (6, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG17() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test17.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test17.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (29, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (11, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG18() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test18.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test18.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (0, pdg.parameters.length);
            Assertions.assertEquals (16, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (1, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG19() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test19.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test19.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (0, pdg.parameters.length);
            Assertions.assertEquals (17, pdg.getNodes().size());
            Assertions.assertEquals (8, pdg.statementNodes.size());
            Assertions.assertEquals (3, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG20() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test20.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test20.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (0, pdg.parameters.length);
            Assertions.assertEquals (10, pdg.getNodes().size());
            Assertions.assertEquals (2, pdg.statementNodes.size());
            Assertions.assertEquals (5, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG21() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test21.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test21.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (0, pdg.parameters.length);
            Assertions.assertEquals (9, pdg.getNodes().size());
            Assertions.assertEquals (3, pdg.statementNodes.size());
            Assertions.assertEquals (2, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDGm2() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/testm2.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/testm2.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (52, pdg.getNodes().size());
            Assertions.assertEquals (21, pdg.statementNodes.size());
            Assertions.assertEquals (55, pdg.dataSources.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG23() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test22.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test22.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (28, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (6, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG24() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test23.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test23.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (28, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (38, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG25() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test26.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test26.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (2, pdg.parameters.length);
            Assertions.assertEquals (35, pdg.getNodes().size());
            Assertions.assertEquals (11, pdg.statementNodes.size());
            Assertions.assertEquals (2, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG26() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test27.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test27.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (1, pdg.parameters.length);
            Assertions.assertEquals (40, pdg.getNodes().size());
            Assertions.assertEquals (11, pdg.statementNodes.size());
            Assertions.assertEquals (2, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG27() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test28.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test28.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (1, pdg.parameters.length);
            Assertions.assertEquals (35, pdg.getNodes().size());
            Assertions.assertEquals (11, pdg.statementNodes.size());
            Assertions.assertEquals (2, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG28() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test29.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test29.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (0, pdg.parameters.length);
            Assertions.assertEquals (24, pdg.getNodes().size());
            Assertions.assertEquals (8, pdg.statementNodes.size());
            Assertions.assertEquals (2, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG29() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test30.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test30.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (0, pdg.parameters.length);
            Assertions.assertEquals (33, pdg.getNodes().size());
            Assertions.assertEquals(15, pdg.statementNodes.size());
            Assertions.assertEquals (9, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG30() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test31.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test30.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (0, pdg.parameters.length);
            Assertions.assertEquals (27, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    void testPDG31() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test32.py");
        MethodDeclaration func = (MethodDeclaration) JavaASTUtil.getChildren(parse).get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), "author/project/test32.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (0, pdg.parameters.length);
            Assertions.assertEquals (27, pdg.getNodes().size());
            Assertions.assertEquals (10, pdg.statementNodes.size());
            Assertions.assertEquals (10, pdg.dataSources.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testParse() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test24.py");
        System.out.println(parse);
    }
}