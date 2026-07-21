package com.matching;


import com.utils.JavaASTUtil;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//TODO: adapt files!
class ConcreteJavaParserTest {
    @Test
    void parse() {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse("author/project/test1.py");
        System.out.println(parse.toString());

        assertEquals(3, JavaASTUtil.getChildren(parse).size());
    }

    @Test
    void parseCode() {
        String code = "import numpy as np \nx=True";
        InputStream codeStream = new ByteArrayInputStream(code.getBytes());
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit parse = parser.parse(codeStream);
        assertEquals(2, JavaASTUtil.getChildren(parse).size());
    }
}