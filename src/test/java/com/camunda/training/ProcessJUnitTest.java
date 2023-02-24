package com.camunda.training;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.runtime.Job;
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
    @Deployment(resources = {"tweetApproval.dmn", "process.bpmn"})
    public  void testHappyPath(){
        // Create a HashMap to put in variables for the process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("content", "Exercise 12 test - Rajesh "+ System.currentTimeMillis());
        variables.put("email", "rajeshtshenoy@gmail.com");
        // Start process with Java API and variables
        ProcessInstance processInstance =
            runtimeService().startProcessInstanceByKey("TwitterQAProcess",variables);
        // Make assertions on the process instance
//        String taskId = findId("Review Tweet");
//        assertThat(processInstance).isWaitingAt(taskId);
//        assertThat(task()).hasCandidateGroup("management").isNotAssigned();
//        List<Task> taskList = taskService()
//            .createTaskQuery()
//            .taskCandidateGroup("management")
//            .processInstanceId(processInstance.getId())
//            .list();
//        assertThat(taskList).isNotNull();
//        assertThat(taskList).hasSize(1);
//
//        Task task = taskList.get(0);


//        Map<String, Object> approvedMap = new HashMap<String, Object>();
//        approvedMap.put("approved", true);
//        taskService().complete(task.getId(), approvedMap);
        List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();
        assertThat(jobList).hasSize(1);
        Job job = jobList.get(0);
        execute(job);
        assertThat(processInstance).isEnded();
    }


      @Test
      @Deployment(resources = "process.bpmn")
    public void testTweetRejected(){

          // start the process
          Map<String, Object> varMap = new HashMap<>();
          varMap.put("approved", false);
          varMap.put("content", "This is my exercise 8 JUnit tweet!! "
              + System.currentTimeMillis());
          ProcessInstance processInstance = runtimeService()
              .createProcessInstanceByKey("TwitterQAProcess")
              .setVariables(varMap)
              .startAfterActivity(findId("Review Tweet"))
              .execute();
          assertThat(processInstance)
              .isWaitingAt(findId("Notify user of rejection"))
              .externalTask()
              .hasTopicName("notification");
          complete(externalTask());

    }


    @Test
    @Deployment(resources = "process.bpmn")
    public void testTweetEvents(){
        // start the process
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("approved", false);
        varMap.put("content", "This is my exercise 8 JUnit tweet!! "
            + System.currentTimeMillis());
        ProcessInstance processInstance = runtimeService()
            .createMessageCorrelation("superuserTweet")
            .setVariable("content", "My Exercise 11 Tweet (ADD YOUR NAME HERE)- " + System.currentTimeMillis())
            .correlateWithResult()
            .getProcessInstance();

        assertThat(processInstance).isStarted();

        List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();

        // execute the job
        assertThat(jobList).hasSize(1);
        Job job = jobList.get(0);
        execute(job);

        assertThat(processInstance).isEnded();


    }


//    @Test
//    @Deployment(resources="process.bpmn")
//    public void testTweetWithdrawn() {
//        Map<String, Object> varMap = new HashMap<>();
//        varMap.put("content", "Test tweetWithdrawn message");
//        ProcessInstance processInstance = runtimeService()
//            .startProcessInstanceByKey("TwitterQAProcess", varMap);
//        assertThat(processInstance).isStarted().isWaitingAt(findId("Review Tweet"));
//        runtimeService()
//            .createMessageCorrelation("tweetWithdrawn")
//            .processInstanceVariableEquals("content", "Test tweetWithdrawn message")
//            .correlateWithResult();
//        assertThat(processInstance).isEnded();
//    }


    @Test
    @Deployment(resources = {"tweetApproval.dmn", "process.bpmn"})
    public void testTweetFromRajesh() {
        Map<String, Object> variables = withVariables("email", "rajeshtshenoy@gmail.com", "content", " test tweet exercise 12 ..this should be published");
        DmnDecisionTableResult decisionResult = decisionService().evaluateDecisionTableByKey("tweetApproval", variables);
        assertThat(decisionResult.getFirstResult()).contains(entry("approved", true));
    }



}