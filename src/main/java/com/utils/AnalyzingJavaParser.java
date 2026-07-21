package com.utils;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Analyzer parser for Java, mirroring AnalyzingParser functionality.
 * Collects parsing errors and provides AST with precise token bounds for IDE use.
 */
public class AnalyzingJavaParser {

    private final List<IProblem> recognitionErrors = new ArrayList<>();
    private final String source;
    private final String unitName;

    public AnalyzingJavaParser(String source, String unitName) {
        this.source = source;
        this.unitName = unitName;
    }

    /**
     * Parse the source and return the AST, collecting errors along the way.
     */
    public CompilationUnit parse() {
        ASTParser parser = ASTParser.newParser(AST.JLS15);

        // Configure parser like AnalyzingParser does
        parser.setSource(source.toCharArray());
        parser.setUnitName(unitName);
        parser.setResolveBindings(false); // Don't resolve for indexing speed

        // Configure for error collection (key difference from standard parsing)
        Map<String, String> options = JavaCore.getDefaultOptions();
//        options.put(JavaCore.COMPILER_PROBLEMS_SEVERITY, JavaCore.WARNING);
        parser.setCompilerOptions(options);

        // Parse and extract AST
        CompilationUnit ast = (CompilationUnit) parser.createAST(null);

        // Record all problems (errors and warnings)
        recognitionErrors.addAll(Arrays.asList(ast.getProblems()));

        return ast;
    }

    /**
     * Get accumulated parsing errors, analogous to AnalyzingParser.getRecognitionErrors()
     */
    public List<IProblem> getRecognitionErrors() {
        return recognitionErrors;
    }

    /**
     * Utility to extract precise token bounds for a node (IDE support)
     */
    public int[] getTokenBounds(org.eclipse.jdt.core.dom.ASTNode node) {
        return new int[]{node.getStartPosition(), node.getLength()};
    }

    //TODO: test (with a pattern!!!), then remove main method
    public static void main(String[] args) {
        String source = "public class Test { }";
        AnalyzingJavaParser parser = new AnalyzingJavaParser(source, "Test.java");

        CompilationUnit ast = parser.parse();
        if (parser.getRecognitionErrors().isEmpty()) {
            System.out.println("Parse successful: " + ast);
        } else {
            System.out.println("Errors found:");
            for (IProblem problem : parser.getRecognitionErrors()) {
                System.out.println("  " + problem.getMessage()
                        + " at line " + problem.getSourceLineNumber());
            }
        }
    }
}