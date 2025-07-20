package org.donutellko.modularbench.evaluator.codeexecutor;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JavaExecutor implements CodeExecutor {

    @Override
    public CodeExecutionResult execute(String code, String testCode) {
        String output = "";
        String error = "";
        int exitCode = -1;
        Double executionTime = null;
        Double solutionTime = null;
        Double memoryUsage = null;

        long startTime = System.nanoTime();

        String solutionClassName = "Main";
        String testClassName = "Test";
        String preparedCode = prepareCode(code, solutionClassName);
        String preparedTestCode = prepareTestCode(testCode, solutionClassName, testClassName);

        try {
            // In-memory Java file manager
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
            InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(standardFileManager);

            // Prepare source files
            fileManager.addSource(solutionClassName, preparedCode);
            fileManager.addSource(testClassName, preparedTestCode);

            // Compile source files
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, fileManager.getJavaFileObjects());
            boolean success = task.call();

            if (!success) {
                StringBuilder errorBuilder = new StringBuilder();
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    errorBuilder.append(diagnostic.toString()).append("\n");
                }
                error = errorBuilder.toString();
                return CodeExecutionResult.builder()
                        .output("Error")
                        .error(error)
                        .exitCode(1)
                        .executionTime(null)
                        .memoryUsage(null)
                        .preparedCode(preparedCode + "\n/**********/\n" + preparedTestCode)
                        .build();
            }

            // Load and execute the test class
            ClassLoader classLoader = fileManager.getClassLoader(null);
            Class<?> testClass = classLoader.loadClass(testClassName);

            Method mainMethod = testClass.getMethod("main", String[].class);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            System.setOut(new PrintStream(outStream));
            System.setErr(new PrintStream(outStream));

            long startSolutionTime = System.nanoTime();
            try {
                mainMethod.invoke(null, (Object) new String[]{});
                exitCode = 0;
            } catch (Exception e) {
                error = e.getCause().toString();
                exitCode = 1;
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            long stopSolutionTime = System.nanoTime();
            solutionTime = (stopSolutionTime - startSolutionTime) / 1_000_000.0;

            output = outStream.toString();

        } catch (Exception e) {
//            e.printStackTrace();
            error = e.toString();
        }
        double endTime = System.nanoTime();
        executionTime = (endTime - startTime) / 1_000_000.0; // ms

        return CodeExecutionResult.builder()
                .output(output)
                .error(error)
                .exitCode(exitCode)
                .executionTime(executionTime)
                .solutionTime(solutionTime)
                .memoryUsage(memoryUsage)
                .preparedCode(preparedCode + "\n/**********/\n" + preparedTestCode)
                .build();
    }

    private static String prepareCode(String code, String solutionClassName) {
        String mainFunctionName = solutionClassName.toLowerCase();

        if (code.contains(" " + mainFunctionName + "(")) {
            // already has a main function with the same name
        } else if (code.contains("${solution.function_name}")) {
            code = code.replaceAll("\\$\\{solution\\.function_name}", mainFunctionName);
        } else if (code.contains("public static ")) {
            code = code.replaceFirst("public static (\\S+) \\S+?\\(", "public static $1 " + mainFunctionName + "(");
        }

        if (code.contains("public class")) {
            if (code.contains("public class Main")) {
                return code;
            } else {
                return code.replaceFirst("public class \\w+", "public class Main ");
            }
        } else {
            return "public class Main {\n" + code + "\n}";
        }
    }

    private static String prepareTestCode(String code, String solutionClassName, String testClassName) {
        String solutionFunctionName = solutionClassName.toLowerCase();

        if (code.contains("${solution.function_name}")) {
            code = code.replaceAll("\\$\\{solution\\.function_name}", solutionClassName + "." + solutionFunctionName);
        } else if (code.contains("Main.")) {
            code = code.replaceAll("Main\\.\\S+\\(", solutionClassName + "." + solutionFunctionName + "(");
        }

        if (code.contains("public static void main(String[] args)")) {
            code = code;
        } else if (code.contains("void")) {
            code = code.replaceFirst(".*void[^{]+", "public static void main(String[] args)");
        } else {
            code = "public static void main(String[] args) {\n" + code + "\n}";
        }

        if (code.contains("public class")) {
            if (code.contains("public class " + testClassName)) {
                code = code;
            } else {
                code = code.replaceFirst("public class [\\w]+", "public class " + testClassName + " ");
            }
        } else {
            code = "public class " + testClassName + " {\n" + code + "\n}";
        }

        return code;
    }

    // In-memory Java file manager and file object
    private static class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, InMemoryJavaFileObject> fileObjects = new HashMap<>();

        protected InMemoryJavaFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        void addSource(String className, String sourceCode) {
            fileObjects.put(className, new InMemoryJavaFileObject(className, sourceCode));
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            return fileObjects.get(className);
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return new InMemoryClassLoader(fileObjects);
        }

        Collection<InMemoryJavaFileObject> getJavaFileObjects() {
            return fileObjects.values();
        }
    }

    private static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private final String sourceCode;
        private ByteArrayOutputStream byteCode;

        protected InMemoryJavaFileObject(String className, String sourceCode) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.sourceCode = sourceCode;
            this.byteCode = null;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return sourceCode;
        }

        @Override
        public OutputStream openOutputStream() {
            byteCode = new ByteArrayOutputStream();
            return byteCode;
        }

        public byte[] getBytes() {
            return byteCode != null ? byteCode.toByteArray() : null;
        }
    }

    private static class InMemoryClassLoader extends ClassLoader {
        private final Map<String, InMemoryJavaFileObject> fileObjects;

        public InMemoryClassLoader(Map<String, InMemoryJavaFileObject> fileObjects) {
            this.fileObjects = fileObjects;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            InMemoryJavaFileObject fileObject = fileObjects.get(name);
            if (fileObject != null) {
                byte[] bytes = fileObject.getBytes();
                if (bytes != null) {
                    return defineClass(name, bytes, 0, bytes.length);
                }
            }
            return super.findClass(name);
        }

    }
}
