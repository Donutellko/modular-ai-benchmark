import sys
import yaml
try:
    from ruamel.yaml import YAML
    from ruamel.yaml.scalarstring import LiteralScalarString
except ImportError:
    print("Please install ruamel.yaml: pip install ruamel.yaml")
    sys.exit(1)

def convert_humaneval(input_data):
    # Converts HumanEval YAML format to the target format
    output = {
        "version": 1,
        "name": "task-source-example-1",
        "tasks": []
    }
    for task in input_data:
        # Use LiteralScalarString for multiline fields
        prompt = task.get("prompt")
        test = task.get("test")
        canonical_solution = task.get("canonical_solution")
        task_entry = {
            "name": task.get("task_id"),
            "type": "implementation from zero",
            "difficulty": "easy" if "115" in task.get("task_id", "") else "medium",
            "area": "math",
            "source": task.get("task_id"),
            "languages": ["python"],
            "available_parameters": [
                "use-llm-judge",
                "all-tests-public",
                "all-tests-hidden"
            ],
            "available_criteria": [
                "unit-test",
                "ram-usage",
                "cpu-usage",
                "sonarqube",
                "llm-judge-code-quality",
                "llm-judge-comment-quality",
                "python-pmd",
                "python-pyright"
            ],
            "task": {
                "common_prompt": LiteralScalarString(prompt) if prompt and '\n' in prompt else prompt,
                "languages_specific": {
                    "python": {
                        "description": "${common_prompt}",
                        "hidden_tests": [
                            {"code": LiteralScalarString(test) if test and '\n' in test else test}
                        ]
                    }
                }
            },
            "golden_solution": {
                "python": LiteralScalarString(canonical_solution) if canonical_solution and '\n' in canonical_solution else canonical_solution
            },
            "llm_judge_prompt": LiteralScalarString(
                "You are an experienced interviewer assessing the candidate's solution. \n"
                "Here is the task that was given to the candidate:\n"
                "```\n"
                "${prompt}\n"
                "```\n"
                "Based on the given task, the candidate wrote the following solution:\n"
                "```\n"
                "${solution.code}\n"
                "```\n\n"
                "Based on the provided task and candidate's solution, \n"
                "respond with a YAML that contains numeric evaluations of the \n"
                "following concepts on a scale from 0 to 10:\n"
                "```\n"
                "solution_correctness: int\n"
                "code_quality: int\n"
                "style_quality: int\n"
                "<#if parameters['should-generate-tests'] >\n"
                "test_quality: int\n"
                "</#if>\n"
                "```"
            )
        }
        output["tasks"].append(task_entry)
    return output

def main():
    if len(sys.argv) != 4:
        print("Usage: python tasks_importer.py <input_yaml> <output_yaml> <type>")
        sys.exit(1)

    input_yaml = sys.argv[1]
    output_yaml = sys.argv[2]
    task_type = sys.argv[3]

    with open(input_yaml, "r") as f:
        input_data = list(yaml.safe_load_all(f))

    if task_type == "HumanEval":
        output_data = convert_humaneval(input_data)
    else:
        raise ValueError(f"Unknown type: {task_type}")

    yaml_ruamel = YAML()
    yaml_ruamel.default_flow_style = False
    yaml_ruamel.allow_unicode = True
    with open(output_yaml, "w") as f:
        yaml_ruamel.dump(output_data, f)

if __name__ == "__main__":
    main()
