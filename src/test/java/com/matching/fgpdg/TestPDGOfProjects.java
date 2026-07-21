package com.matching.fgpdg;

import com.matching.ConcreteJavaParser;
import com.utils.DotGraph;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.inferrules.Utils;
import org.junit.jupiter.api.Test;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.inferrules.Utils.getAllMethods;

public class TestPDGOfProjects {
    @Test
    void testKerasPDG() {
        String projectPath = Configurations.PROJECT_REPOSITORY+"keras-team/keras/";
        File dir = new File(projectPath);
        ArrayList<File> files = Utils.getJavaFiles(Objects.requireNonNull(dir.listFiles()));
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            ConcreteJavaParser parser = new ConcreteJavaParser();
            CompilationUnit parse = parser.parse(file.getAbsolutePath());
            List<ImportDeclaration> collect = (List<ImportDeclaration>)parse.imports();
            ArrayList<MethodDeclaration> functions = getAllMethods(parse);
            for (MethodDeclaration function : functions) {
                try {
                    String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                    PDGBuildingContext context =new PDGBuildingContext(collect, relative);
                    System.out.println(function.getName());
                    PDGGraph pdg = new PDGGraph(function,context);
                    DotGraph dg = new DotGraph(pdg);
                } catch (IOException e) {
                    System.out.println("Type File is Not available");
                }
            }
        }
    }

    @Test
    void testTensorFlowPDG() {
        String projectPath = Configurations.PROJECT_REPOSITORY+"nltk/nltk/";
        File dir = new File(projectPath);
        ArrayList<File> files = Utils.getJavaFiles(Objects.requireNonNull(dir.listFiles()));
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            ConcreteJavaParser parser = new ConcreteJavaParser();
            CompilationUnit parse = parser.parse(file.getAbsolutePath());
            List<ImportDeclaration> collect = (List<ImportDeclaration>)parse.imports();
            ArrayList<MethodDeclaration> functions = getAllMethods(parse);
            for (MethodDeclaration function : functions) {
                try {
                    String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                    PDGBuildingContext context =new PDGBuildingContext(collect, relative);
                    System.out.println(function.getName());
                    PDGGraph pdg = new PDGGraph(function,context);
                    DotGraph dg = new DotGraph(pdg);
                } catch (IOException e) {
                    System.out.println("Type File is Not available");
                }
            }
        }
    }

    @Test
    void testPytorchPDG() {
        String projectPath = Configurations.PROJECT_REPOSITORY+"pytorch/pytorch/";
        File dir = new File(projectPath);
        ArrayList<File> files = Utils.getJavaFiles(Objects.requireNonNull(dir.listFiles()));
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            ConcreteJavaParser parser = new ConcreteJavaParser();
            CompilationUnit parse = parser.parse(file.getAbsolutePath());
            List<ImportDeclaration> collect = (List<ImportDeclaration>)parse.imports();
            ArrayList<MethodDeclaration> functions = getAllMethods(parse);
            for (MethodDeclaration function : functions) {
                try {
                    String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                    PDGBuildingContext context =new PDGBuildingContext(collect, relative);
                    System.out.println(function.getName());
                    PDGGraph pdg = new PDGGraph(function,context);
                    DotGraph dg = new DotGraph(pdg);
                } catch (IOException e) {
                    System.out.println("Type File is Not available");
                }
            }
        }
    }
}
