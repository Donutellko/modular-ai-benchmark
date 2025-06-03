package org.donutellko.modularbench;

public class StubCodeExecutor implements CodeExecutor {
    @Override
    public void execute(TaskResult result) {
        // Stub: print or log the result
        System.out.println("Executing code for: " + result.getTaskDefinition().getName() +
                ", LLM: " + result.getLlmName() +
                ", Language: " + result.getLanguage());
    }
}
