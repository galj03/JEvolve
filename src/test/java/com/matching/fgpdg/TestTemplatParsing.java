package com.matching.fgpdg;

import com.matching.ConcreteJavaParser;
import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import com.visitors.ASTBaseVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestTemplatParsing {

    @Test
    void testTemplate1() throws Exception {
        String code = """
                for :[[l2]] in :[[l6]]:
                    boo()
                    :[l11]
                    if (:[[l7]]):
                        xx.ccc.fff()
                        :[[l8]].:[[l7]].:[[l6]](2*:[[l9]])""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(8,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate2() throws Exception {
        String code = """
                [:[[l5]] for  :[[l11]] in :[[l12]] if :[[l13]] ]
                
                while (:[[l17]]):
                    :[[l19]]""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(6,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate3() throws Exception {
        String code = """
                with :[[l19]] as :[[l20]]:
                    :[[l21]]
                
                try:
                    :[[l23]]
                except :[[l23]] as xx:
                    print("division by zero!")
                except ZeroDivisionError as yy:
                    print("division by zero!")
                else:
                    print("result is")
                finally:
                    print("executing finally clause")""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(5,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate4() throws Exception {
        String code = """
                [:[[l51]],:[[l52]],:[[l53]],4,5,:[[l14]]]
                {'one':3,:[[l56]]::[[l57]],:[[l58]]::[[l59]]}
                
                { 3,:[[l56]],:[[l57]],:[[l58]],:[[l59]]}""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(12,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate5() throws Exception {
        String code = "{:[[l1]]::[[l2]] for :[[l3]] in iterable if :[[l5]]}";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(4,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate6() throws Exception {
        String code = "{:[[l1]] for :[[l3]] in iterable if :[[l5]]}";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(3,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate7() throws Exception {
        String code = "(:[[l1]] for :[[l3]] in iterable if :[[l5]])";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(3,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate8() throws Exception {
        String code = """
                :[[l1]] = 3
                choice = input(:[[l2]])
                word = input("Please enter text")
                letters = :[[l3]] + string.punctuation + string.digits
                encoded = ''
                if :[[l4]] == "encode":
                    for :[[l5]] in word:
                        if letter == ' ':
                            encoded = encoded + ' '
                        else:
                            x = letters.:[[l6]](letter) + shift
                            encoded = encoded + :[[l7]][x]""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(7,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate9() throws Exception {
        String code = """
                if :[[l1]] == "decode":
                    for letter in :[[l2]]:
                        if letter == ' ':
                            encoded = :[[l3]] + ' '
                        else:
                            x = letters.index(:[[l4]]) - shift
                            encoded = :[[l5]] + letters[x]""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(5,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate10() throws Exception {
        String code = """
                num = int(input("Enter a number: "))
                if (:[[l1]] % :[[l2]]) == 0:
                   print("{0} is Even".format(num))
                else:
                   print("{0} is Odd".format(num))""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(2,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate11() throws Exception {
        String code = "for i in range(1, :[[l3]]):\n" +
                "   print(num, 'x', i, '=', :[[l1]]*:[[l2]])";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(3,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate12() throws Exception {
        String code = """
                def generate_benchmark_params_cpu_gpu(*params_list):
                  ""\"Extend the benchmark names with CPU and GPU suffix.
                  Args:
                    *params_list: A list of tuples represents the benchmark parameters.
                  Returns:
                    A list of strings with the benchmark name extended with CPU and GPU suffix.
                  ""\"
                  benchmark_params = []
                  for params in params_list:
                    benchmark_params.extend([
                        ((:[[l1]][0] + '_CPU',) + param[1:]) for param in params
                    ])
                    benchmark_params.extend([
                        ((param[0] + :[[l2]],) + param[1:]) for param in params
                    ])
                  return benchmark_params""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(2,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate13() throws Exception {
        String code = """
                if :[[l1]] is :[[l2]]:
                    raise ValueError('Input data is required.')
                if 'optimizer' is None:
                    raise ValueError('Optimizer is required.')
                if 'loss' is None:
                    raise ValueError('Loss function is required.')
                if :[[l3]] < :[[l4]]:
                    raise ValueError('`num_gpus` cannot be negative')
                """;
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(4,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate14() throws Exception {
        String code = """
                app = tf.keras.applications.Xception
                :[[l1]], :[[l2]] = (
                        saved_model_benchmark_util.save_and_load_benchmark(app))
                
                self.report_benchmark(
                        iters=save_result['iters'],
                        wall_time=save_result['wall_time'],
                        name=save_result['name'])
                
                self.report_benchmark(
                        iters=load_result['iters'],
                        wall_time=:[[l3]],
                        name=load_result['name'])""";
        ConcreteJavaParser parser = new ConcreteJavaParser();
        CompilationUnit module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(3,count.alpHole+count.lazyHole);
    }
    private int getCharacterCount(String code,char charc){
        int count=0;
        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) == charc) {
                count++;
            }
        }
        return count;
    }

    static class HoleCounter extends ASTBaseVisitor {
        int alpHole=0;
        int lazyHole=0;
//        public Object visitHole(Hole node) throws Exception {
//            if (node instanceof AlphanumericHole){
//                alpHole=alpHole+1;
//            }
//            else if (node instanceof LazyHole)
//            {
//                lazyHole=lazyHole+1;
//            }
//            return super.visitHole(node);
//        }
//
//        public Object visitAlphHole(AlphHole node) throws Exception {
//
//            return super.visitAlphHole(node);
//        }
//
//        @Override
//        public Object visitAttribute(Attribute node) throws Exception {
//            return super.visitAttribute(node);
//        }

    }
}
