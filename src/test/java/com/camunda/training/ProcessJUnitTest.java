package com.camunda.training;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {

    @Test
    @Deployment(resources = "process.bpmn")
  public  void testHappyPath(){
        // Create a HashMap to put in variables for the process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("content", "Exercise 4 test - RTS  "+ System.currentTimeMillis());

//        variables.put("approved", true);
        // Start process with Java API and variables
        ProcessInstance processInstance =
            runtimeService().startProcessInstanceByKey("TwitterQAProcess",variables);
        // Make assertions on the process instance
        String taskId = findId("Review Tweet");
        assertThat(processInstance).isWaitingAt(taskId);
        assertThat(task()).hasCandidateGroup("management").isNotAssigned();
        List<Task> taskList = taskService()
            .createTaskQuery()
            .taskCandidateGroup("management")
            .processInstanceId(processInstance.getId())
            .list();
        assertThat(taskList).isNotNull();
        assertThat(taskList).hasSize(1);

        Task task = taskList.get(0);


        Map<String, Object> approvedMap = new HashMap<String, Object>();
        approvedMap.put("approved", true);
        taskService().complete(task.getId(), approvedMap);
        assertThat(processInstance).isEnded();
    }

}