package com.example.dependency_version_collector.utils;


public class Prompts {

//    public static final String GET_PROJECTS_BY_SQUAD = """
//            You are experienced software engineer.
//
//            You will receive a YAML file with information about projects and their owners squad.
//            Please, analise it and return list of the projects, owned by squad, named {{squad}}.
//
//            File content to analise:
//            ```yaml
//            {{context}}
//            ```
//            """;

    public static final String GET_PROJECTS_BY_SQUAD = """
            Be a helpful assistant and answer the user's question.
            
            You MUST answer the question directly without any other
            text or explanation.
            
            **TASK**
            You will receive a YAML file with information about projects and their owners teams.
            Please, analise this file and **return list with the of the project names**, owned by team, named `{{squad}}`.
            
            Example:
            - sandbox1
            - sandbox2
            
            File content to analise:
            ```yaml
            {{context}}
            ```
            """;

    public static final String DEPENDENCIES_VERSIONS_PROMPT = """
            You are analyzing build files (`build.gradle`, `gradle.properties`, `libs.versions.toml`, etc.) from Java Gradle project.
            
            Your task is to:
            
            1. Identify all **Spring-related dependencies** (e.g. `spring-boot`, `spring-cloud`, `spring-cloud-dependencies`).
            2. Identify following project-specific dependencies: {internal-deps}
            3. Extract the **exact version number** used, resolving any property references if needed. **Do not allow** to left placeholders like `${version}` or `${project.version}` or `${dataDomainKafkaProducerVersion}` in the output.
            4. identify java version using `sourceCompatibility` and `targetCompatibility` properties (example `17` or `21` or `11`)
            5. Produce a single **HTML summary table** that consolidates all collected dependencies **and java version**
            6. No thinking or explanation should be included into response
            7. If certain dependency or java version was not found in provided context, just skip it
            
            
            The HTML table should include the following columns:
            
            - `Dependency`
            - `Version`
            
            ## ✅ Example Output
            ```html
            <table>
              <thead>
                <tr>
                  <th>Dependency</th><th>Version</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>org.springframework.boot</td>
                  <td>2.7.3</td>
                </tr>
                <tr>
                  <td>java</td>
                  <td>21</td>
                </tr>
              </tbody>
            </table>
            ```
            
            **IMPORTANT**: no other data or sentences or explanations should be included in the response, **only HTML table**.
            
            Files to analyze:
            ```text
            {{context}}
            ```
            """;
    public static final String DEPENDENCIES_VERSIONS_PROMPT_NO_JAVA = """
            You are analyzing build files (`build.gradle`, `gradle.properties`, `libs.versions.toml`, etc.) from Java Gradle project.
            
            Your task is to:
            
            1. Identify all **Spring-related dependencies** (e.g. `spring-boot`, `spring-cloud`, `spring-cloud-dependencies`).
            2. Identify following project-specific dependencies: {{inner-deps}}
            3. Extract the **exact version number** used, resolving any property references if needed. **Do not allow** to left placeholders like `${version}` or `${project.version}` or `${dataDomainKafkaProducerVersion}` in the output.
            5. Produce a single **HTML summary table** that consolidates all collected dependencies
            6. No thinking or explanation should be included into response
            7. If certain dependency was not found in provided context, just skip it
            
            The HTML table should include the following columns:
            
            - `Dependency`
            - `Version`
            
            ## ✅ Example Output
            ```html
            <table>
              <thead>
                <tr>
                  <th>Dependency</th><th>Version</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>org.springframework.boot</td>
                  <td>2.7.3</td>
                </tr>
                <tr>
                  <td>first-sdk</td>
                  <td>5.99.0</td>
                </tr>
              </tbody>
            </table>
            ```
            
            Files to analyze:
            ```text
            {{context}}
            ```
            """;

    public static final String DEPENDENCIES_SUMMARY = """
You are experienced Assistant.

You will be getting an dependency versions report for a set of SpringBoot projects in a form of HTML
with following structure:

first - project name in form of **PROJECT: `project name`**, example `**PROJECT: sandbox**`
then - a HTML table with information about project dependencies with version


Here is what you need to do:
- **create a summary HTML table* with information  across all project, where columns will be dependencies,
rows - projects, cells - dependency version of certain dependency in a certain project.

**IMPORTANT**: No thinking or explanation should be included into response, just a single **HTML table**

If Any dependency version is not in a project data, leave cell empty,

## ✅ Example Output

```html
<table>
  <thead>
    <tr>
      <th>Project</th>
      <th>org.springframework.boot</th>
      <th>io.spring.dependency-management</th>
      <th>java</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>sandbox</td>
      <td>2.7.3</td>
      <td>1.1.6</td>
      <td>21</td>
    </tr>
    <tr>
      <td>sandbox2</td>
      <td>3.4.5</td>
      <td>1.1.7</td>
      <td>17</td>
    </tr>
  </tbody>
</table>
```

Here is data to analyse:
{data}
""";
    public static final String INSTRUCTIONS_FOR_DEPS = """
Please, do multi-turn conversation to perform following task:

You are experienced Java developer with a strong knowledge in Spring and dependency management.
You will be getting an dependency versions report for a set of SpringBoot projects in a form of HTML
for all the projects, that belongs to squad 'r2t2'. In order to do so, you need to do following steps:

1. collect list of projects,that belongs to squad '{{squads}}' 
2. for each project, you need to get build.gradle file
3. and then collect dependencies version from this file.
4. repeat for all projects, found on step 1
5. concatenate all input data into one string, and return as a result
""";

}

