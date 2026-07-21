package com.inferrules.comby.operations;

import com.inferrules.comby.jsonResponse.CombyRewrite;
import io.vavr.control.Try;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.inferrules.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BasicCombyOperationsTest {

    @Test
    void rewrite() {
        CompilationUnit code = Utils.getCompilationUnit("author/project/test22.py");
        String strCode = com.utils.Utils.getAllMethods(code).getFirst().toString(); //TODO: test if toString really works
        String matcher = """
                def function1(sentence, intArray):
                    :[l1]
                    :[[l6]] = :[[l8]]
                    :[l3]
                    for :[[l4]] in :[[l5]]:
                        :[[l6]] = :[[l6]] + :[[l4]]
                return :[[l7]]""";
        String rewrite = """
                def function1(sentence, intArray):
                    :[l1]
                    :[[l6]] = np.sum(:[[l5]])
                    :[l3]
                return :[[l7]]""";

        Try<CombyRewrite> changedCode = BasicCombyOperations.rewrite(matcher, rewrite, strCode, ".python");
        Assertions.assertEquals("""
                def function1(sentence, intArray):
                    hhh = 0
                    number = np.sum(intArray)
                    print(ff)
                return hhh
                """,changedCode.get().getRewrittenSource());
    }
}