package com.matching.fgpdg;

import com.matching.ConcreteJavaParser;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.Utils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class PDGGraphOfTemplatesTest {
    @Test
    void testPDG1() throws Exception {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/testtemplate.py");
        Guards guards = new Guards(Utils.getFileContent(getPathToResources("author/project/testtemplate.py")),parse);
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext context = new PDGBuildingContext((List<ImportDeclaration>)parse.imports(), wrapper);
        PDGGraph pdg = new PDGGraph(parse,context);

        System.out.println(context);

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
