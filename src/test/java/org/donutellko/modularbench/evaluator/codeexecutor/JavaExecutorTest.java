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

        assertThat(result.getError()).withFailMessage(result.getPreparedCode()).isEmpty();
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
                            int result = ${solution.function_name}(2, 3);
                            if (result != 5) {
                                throw new AssertionError("Test failed: Expected 5 but got " + result);
                            }
                            System.out.println("Test passed");
                        }
                """;

        CodeExecutionResult result = executor.execute(code, testCode);

        assertThat(result.getError()).withFailMessage(result.getPreparedCode()).isEmpty();
        assertEquals(0, result.getExitCode(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Test passed"), "Output should indicate the test passed");
    }

    @Test
    void handlesCodeWithCompilationWarnings() {
        JavaExecutor executor = new JavaExecutor();
        String code = "public class Main { public static void main(String[] args) { int unusedVar = 0; System.out.println(\"Hello, World!\"); } }";
        String testCode = "public class Test { public static void main(String[] args) { Main.main(args); } }";

        CodeExecutionResult result = executor.execute(code, testCode);

        assertEquals(0, result.getExitCode());
        assertEquals("Hello, World!\n", result.getOutput());
        assertEquals("", result.getError());
    }

    @Test
    void handlesCodeWithExternalLibraryUsage() {
        JavaExecutor executor = new JavaExecutor();
        String code = "import java.util.ArrayList; public class Main { public static void main(String[] args) { ArrayList<String> list = new ArrayList<>(); list.add(\"Hello\"); System.out.println(list.get(0)); } }";
        String testCode = "public class Test { public static void main(String[] args) { Main.main(args); } }";

        CodeExecutionResult result = executor.execute(code, testCode);

        assertEquals(0, result.getExitCode());
        assertEquals("Hello\n", result.getOutput());
        assertEquals("", result.getError());
    }

    @Test
    void handlesCodeWithRecursiveMethods() {
        JavaExecutor executor = new JavaExecutor();
        String code = "public class Main { public static int factorial(int n) { return n == 0 ? 1 : n * factorial(n - 1); } public static void main(String[] args) { System.out.println(factorial(5)); } }";
        String testCode = "public class Test { public static void main(String[] args) { Main.main(args); } }";

        CodeExecutionResult result = executor.execute(code, testCode);

        assertEquals(0, result.getExitCode());
        assertEquals("120\n", result.getOutput());
        assertEquals("", result.getError());
    }

    @Test
    void handlesCodeWithLargeMemoryUsage() {
        JavaExecutor executor = new JavaExecutor();
        String code = """
                public class Main { public static void main(String[] args) { throw new IllegalStateException("oops"); } }
        """;
        String testCode = "public class Test { public static void main(String[] args) { Main.main(args); } }";

        CodeExecutionResult result = executor.execute(code, testCode);

        assertNotEquals(0, result.getExitCode());
        assertTrue(result.getError().contains("IllegalStateException"));
    }

    /*
        We will need a way to execute code in a sandbox to preserve our environment.

        @Test
        void handlesCodeWithSystemExitCall() {
            JavaExecutor executor = new JavaExecutor();
            String code = "public class Main { public static void main(String[] args) { System.exit(0); } }";
            String testCode = "public class Test { public static void main(String[] args) { Main.main(args); } }";

            CodeExecutionResult result = executor.execute(code, testCode);

            assertEquals(0, result.getExitCode());
            assertEquals("", result.getOutput());
            assertEquals("", result.getError());
        }
    */
}