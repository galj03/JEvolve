package com.utils;

import com.matching.ConcreteJavaParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestPreVisitor {
    @Test
    void testVisitor1() throws Exception {
        ConcreteJavaParser parser = new ConcreteJavaParser();
        String code =   "class Person:\n" +
                "  def __init__(self, name, age):\n" +
                "    self.name = name\n" +
                "    self.age = age";
        InputStream antlrSting =  new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        CompilationUnit parse = parser.parse(antlrSting);

        //TODO: finish this myself??
    }
}
