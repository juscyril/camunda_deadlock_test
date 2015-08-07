package org.camunda.bpm.unittest;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.claim;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.complete;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.task;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.taskService;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

public class SimpleConcurrentTestCase
{
	@Rule
	public ProcessEngineRule rule = new ProcessEngineRule();
	
	private int noOfThreads;
	private int noOfIterations;
	
	@Test
	@Deployment(resources = {"SampleOrder.bpmn"})
	public void shouldExecuteProcess()
	{
		// TODO Adjust these numbers if no deadlock is happening with these default numbers. 
		noOfThreads = 4;
		noOfIterations = 1;

		Thread[] threads = new Thread[noOfThreads];
		for (int i = 0; i < noOfThreads; i++)
		{
			Thread t = new Thread(new WorkflowLifecycleSimulator());
			threads[i] = t;
			t.start();
		}

		for (int i = 0; i < noOfThreads; i++)
		{
			try
			{
				threads[i].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class WorkflowLifecycleSimulator implements Runnable
	{
		int currentRun = 0;
		

		public void run()
		{
			String threadName = Thread.currentThread().getName();
			
			while (currentRun < noOfIterations)
			{
				System.out.println("Thread : "+threadName+" starting iteration : "+currentRun);
				
				// Create a new process instance, it should be active
				ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("SampleOrder");
				assertThat(processInstance).isActive();
				
				// Currently there will be only one active task within that process instance
				// 1) Claim it for the user "abc" because we have set the Candidate User as "abc" in SampleOrder.bpmn
				// 2) Complete the Task
				assertThat(task(processInstance)).isNotNull();
				claim(task(processInstance), "abc");
				complete(task(processInstance));

				// Now the second user task will be active. 
				// 1) Claim it
				// 2) Complete it
				TaskQuery taskQuery = taskService().createTaskQuery();
				taskQuery.active();
				assertThat(task(taskQuery, processInstance)).isNotNull();
				
				claim(task(taskQuery), "abc");
				complete(task(taskQuery));
				
				// Then the process instance should be ended
				assertThat(processInstance).isEnded();
				
				System.out.println("Thread : "+threadName+" ending iteration : "+currentRun);

				currentRun++;
			}
		}
	}
}
