package org.inferrules;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.inferrules.comby.jsonResponse.CombyMatch;
import com.inferrules.utils.Utilities;
import com.matching.ConcreteJavaParser;
import com.matching.fgpdg.*;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.FileIO;
import com.visitors.ASTBaseVisitor;
import io.vavr.Tuple;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Utils {
    private static final String matchCommand = "echo \"{0}\" | comby \"{1}\" -stdin -json-lines -match-newline-at-toplevel -match-only  -matcher {2} \"foo\"";
    private static final String defaultLanguage = ".java";

    public static CombyMatch getMatch(String template, String value, String language, boolean isPerfect) {
        Object[] arguments = {value, template, language == null ? defaultLanguage : language};
        return Utilities.runBashCommand(MessageFormat.format(matchCommand, arguments))
                .map(x -> new Gson().fromJson(x, CombyMatch.class))
                .onFailure(x -> System.out.println(x.toString()))
                .filter(x -> !isPerfect || x.isPerfect(value)).getOrNull();

    }

    public static List<String> tokenizeTemplate(String tempalte){
        CombyMatch m = getMatch(":[a.]", tempalte,".java", false);
        return m.getMatches().stream().map(x->x.getMatched()).collect(Collectors.toList());

    }

    public static boolean areAlphaEquivalent(String template1, String template2){
        return areAlphaEquivalent(tokenizeTemplate(template1), tokenizeTemplate(template2));
    }

    public static <T> boolean areAlphaEquivalent(List<T> ls1, List<T> ls2) {
        if(ls1.size()!= ls2.size())
            return false;
        return Streams.zip(ls1.stream(), ls2.stream(), Tuple::of)
                .collect(groupingBy(x->x._1(), collectingAndThen(toList(), xs -> xs.stream().map(x -> x._2()).distinct().count())))
                .entrySet().stream().allMatch(x->x.getValue() == 1)
                && Streams.zip(ls2.stream(), ls1.stream(), Tuple::of)
                .collect(groupingBy(x->x._1(), collectingAndThen(toList(), xs -> xs.stream().map(x -> x._2()).distinct().count())))
                .entrySet().stream().allMatch(x->x.getValue() == 1)
                ;
    }

    public static ArrayList<File> getJavaFiles(File[] files) {
        ArrayList<File> javaFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.getName().startsWith(".")) {
                    javaFiles.addAll(getJavaFiles(Objects.requireNonNull(file.listFiles()))); // Calls same method again.
                }
            } else {
                if (file.getName().endsWith(".java")) { //prev: ".py"
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }

    public static ArrayList<MethodDeclaration>  getAllFunctions(CompilationUnit ast){
        JavaMethodDeclarationVisitor fu = new JavaMethodDeclarationVisitor();
        try {
            fu.visit(ast);
            return fu.funcDefs;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    static class JavaMethodDeclarationVisitor extends ASTBaseVisitor {
        ArrayList<MethodDeclaration> funcDefs = new ArrayList<>();
        @Override
        public boolean visit(MethodDeclaration node) {
            funcDefs.add(node);
            return super.visit(node);
        }
    }

    public static List<MatchedNode> getMatchedNodes(String filename, String patternname, List<MatchedNode> graphs) throws Exception {
        CompilationUnit codeModule = getCompilationUnit("author/project/"+filename+".py");
        CompilationUnit patternModule = getCompilationUnitForTemplate(getPathToResources("author/project/"+patternname+".py"));
        return getMatchedNodes(filename, patternname,null, codeModule, patternModule,null);
    }

    public static List<MatchedNode> getMatchedNodes(String filename, String lpatternname,String rpatternname,
                                    CompilationUnit codeModule, CompilationUnit lpatternModule, CompilationUnit rpatternModule) throws IOException {
        List<MatchedNode> graphs;
        FunctionDef func=null;
        for (org.python.antlr.base.stmt stmt : codeModule.getInternalBody()) {
            if (stmt instanceof FunctionDef){
                func= (FunctionDef) stmt;
                break;
            }
            else if (stmt instanceof ClassDef){
                for (org.python.antlr.base.stmt stmt1 : ((ClassDef) stmt).getInternalBody()) {
                    if (stmt1 instanceof FunctionDef){
                        func= (FunctionDef) stmt1;
                        break;
                    }
                }

            }
        }
        PDGBuildingContext fcontext = null;
        fcontext = new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/"+filename+".py");
        PDGGraph fpdg = new PDGGraph(func,fcontext);
//        fpdg.getNodes().forEach(x-> System.out.println(x.getId()));
        Guards guards = new Guards(com.utils.Utils.getFileContent(getPathToResources("author/project/"+lpatternname+".py")),lpatternModule);
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext mcontext = new PDGBuildingContext(lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph mpdg = new PDGGraph(lpatternModule,mcontext);
        MatchPDG match = new MatchPDG();
        graphs=match.getSubGraphs(mpdg,fpdg,mcontext,fcontext );
        graphs.forEach(x->x.updateAllMatchedNodes(x,mpdg));
        List<MatchedNode> finalPatterns = graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        match.drawMatchedGraphs(fpdg,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+filename+".dot");
        com.utils.Utils.markNodesInCode("src/test/resources/author/project/"+filename+".py",
                graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+filename+".html","","x@x");
        ;

        return graphs;
    }

    public static CompilationUnit getCompilationUnit(String fileName){
        ConcreteJavaParser parser = new ConcreteJavaParser();
        return parser.parse(fileName);
    }

    public static CompilationUnit getCompilationUnitForTemplate(String fileName) throws Exception {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        return parser.parseTemplates(FileIO.readStringFromFile(fileName));
    }

    public static String getPathToResources(String name){
        File f = new File(name);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        return Utils.class.getClassLoader().getResource(name).getPath();
    }
}
