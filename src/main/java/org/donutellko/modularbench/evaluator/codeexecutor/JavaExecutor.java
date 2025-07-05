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
        long executionTime = 0;
        long cpuTime = 0;
        long memoryUsage = 0;

        try {
            // In-memory Java file manager
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
            InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(standardFileManager);

            // Prepare source files
            String mainClassName = "Main";
            String testClassName = "Test";
            fileManager.addSource(mainClassName, code);
            fileManager.addSource(testClassName, testCode.replace("Main", mainClassName));

            // Compile source files
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, fileManager.getJavaFileObjects());
            boolean success = task.call();

            if (!success) {
                StringBuilder errorBuilder = new StringBuilder();
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    errorBuilder.append(diagnostic.toString()).append("\n");
                }
                return CodeExecutionResult.builder()
                        .output("")
                        .error(errorBuilder.toString())
                        .exitCode(1)
                        .executionTime(0)
                        .cpuTime(0)
                        .memoryUsage(0)
                        .build();
            }

            // Load and execute the test class
            ClassLoader classLoader = fileManager.getClassLoader(null);
            Class<?> testClass = classLoader.loadClass(testClassName);

            long startTime = System.nanoTime();
            Method mainMethod = testClass.getMethod("main", String[].class);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            System.setOut(new PrintStream(outStream));
            System.setErr(new PrintStream(outStream));

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

            long endTime = System.nanoTime();
            output = outStream.toString();
            executionTime = (endTime - startTime) / 1_000_000; // ms

        } catch (Exception e) {
            e.printStackTrace();
            error = e.getMessage();
        }

        return CodeExecutionResult.builder()
                .output(output)
                .error(error)
                .exitCode(exitCode)
                .executionTime(executionTime)
                .cpuTime(cpuTime)
                .memoryUsage(memoryUsage)
                .build();
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
