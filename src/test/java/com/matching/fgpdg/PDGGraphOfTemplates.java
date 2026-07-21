package com.matching.fgpdg;

import com.matching.ConcreteJavaParser;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.Utils;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;

import java.io.File;
import java.util.stream.Collectors;

public class PDGGraphOfTemplates {
    @Test
    void testPDG1() throws Exception {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        Module parse = parser.parse("author/project/testtemplate.py");
        Guards guards = new Guards(Utils.getFileContent(getPathToResources("author/project/testtemplate.py")),parse);
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph pdg = new PDGGraph(parse,context);




        System.out.println(context);

//
//            PDGGraph pdg = new PDGGraph(parse,context);
//            DotGraph dg = new DotGraph(pdg);
//            String dirPath = "./OUTPUT/";
//            dg.toDotFile(new File(dirPath  +"file___"+".dot"));




    }

    private String getPathToResources(String name){
        File f = new File(name);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        return getClass().getClassLoader().getResource(name).getPath();

    }

}
