package com.matching.fgpdg;

import com.matching.ConcreteJavaParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCombyToCodeConversion {
    @Test
    void testTemplate1() throws Exception {
        String code = "for i in range(1, :[[l1]]):\n" +
                "   print(num, 'x', i, '=', :[[l2]]*:[[l3]])";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        String s = parser.convertComByTemplateToParsableCode(code);
        Assertions.assertEquals("for i in range(1, [$1]):\n   print(num, 'x', i, '=', [$2]*[$3])",s);
        System.out.println(s);
    }

    //what is this code variable??
    @Test
    void testTemplate2() throws Exception {
        String code = """
                "for :[[l2]] in :[[l6]]:\\n" +
                                "    boo()\\n" +
                                "    :[l11]\\n" +
                                "    if (:[[l7]]):\\n" +
                                "        xx.ccc.fff()\\n" +
                                "        :[[l8]].:[[l7]].:[[l6]](2*:[[l9]])""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        String s = parser.convertComByTemplateToParsableCode(code);
        Assertions.assertEquals("""
                "for [$2] in [$6]:\\n" +
                                "    boo()\\n" +
                                "    [%11]\\n" +
                                "    if ([$7]):\\n" +
                                "        xx.ccc.fff()\\n" +
                                "        [$8].[$7].[$6](2*[$9])""",s);
        System.out.println(s);
    }

    @Test
    void testTemplate3() throws Exception {
        String code = """
                [:[[l51]],:[[l52]],:[[l53]],4,5,:[l14]]
                {'one':3,:[[l56]]::[[l57]],:[[l58]]::[[l59]]}
                
                { 3,:[[l56]],:[[l57]],:[[l58]],:[[l59]]}""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        String s = parser.convertComByTemplateToParsableCode(code);
        Assertions.assertEquals("""
                [[$51],[$52],[$53],4,5,[%14]]
                {'one':3,[$56]:[$57],[$58]:[$59]}
                
                { 3,[$56],[$57],[$58],[$59]}""",s);
        System.out.println(s);
    }

}
