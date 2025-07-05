package org.donutellko.modularbench.evaluator;

import lombok.RequiredArgsConstructor;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EvaluatorsRegistry {
    private final List<Evaluator> evaluators;

    public List<Evaluator> getEvaluators(ExecutionConfig config, TaskSource.TaskDefinition task) {
        return evaluators.stream()
                .filter(evaluator -> evaluator.matches(config, task))
                .toList();
    }
}
