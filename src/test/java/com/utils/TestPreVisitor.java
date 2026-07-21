package com.utils;

import com.matching.ConcreteJavaParser;
import org.junit.jupiter.api.Test;
import org.python.antlr.Visitor;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;
import org.python.core.PyObject;

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
        Module parse = parser.parse(antlrSting);


    }


}
