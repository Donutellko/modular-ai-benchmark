package org.donutellko.modularbench.evaluator.codeexecutor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JavaExecutorTest {

    @Test
    void execute() {
        JavaExecutor executor = new JavaExecutor();

        String code = """
            public class Main {
                public static int add(int a, int b) {
                    return a + b;
                }
            }
        """;

        String testCode = """
            public class Test {
                public static void main(String[] args) {
                    int result = Main.add(2, 3);
                    if (result != 5) {
                        throw new AssertionError("Test failed: Expected 5 but got " + result);
                    }
                    System.out.println("Test passed");
                }
            }
        """;

        CodeExecutionResult result = executor.execute(code, testCode);

        assertThat(result.getError()).isEmpty();
        assertEquals(0, result.getExitCode(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Test passed"), "Output should indicate the test passed");
    }

    @Test
    void executeNoClassCode() {
        JavaExecutor executor = new JavaExecutor();

        String code = """
                public static int add(int a, int b) {
                    return a + b;
                }
        """;

        String testCode = """
                void test() {
                    int result = Main.add(2, 3);
                    if (result != 5) {
                        throw new AssertionError("Test failed: Expected 5 but got " + result);
                    }
                    System.out.println("Test passed");
                }
        """;

        CodeExecutionResult result = executor.execute(code, testCode);

        assertThat(result.getError()).isEmpty();
        assertEquals(0, result.getExitCode(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Test passed"), "Output should indicate the test passed");
    }
}