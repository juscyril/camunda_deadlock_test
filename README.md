# camunda engine unit test template

This git repository contains a test to reproduce the deadlock associated with ACT_RU_AUTHORIZATION.

The project contains the following files:

```
src/
├── main
│   ├── java
│   └── resources
└── test
    ├── java
    │   └── org
    │       └── camunda
    │           └── bpm
    │               └── unittest
    │                   └── SimpleTestCase.java
    │                   └── SimpleConcurrentTestCase.java   	(1)
    └── resources
        ├── camunda.cfg.xml                       		(2)
        ├── camunda.cfg.xml.original              
        └── testProcess.bpmn                      
        └── SampleOrder.bpmn                      		(3)
pom.xml						  		(4)
```

Explanation:

* (1) A java class containing a JUnit Test which can reproduce the deadlock. Adjust the value of variables `noOfThreads` and `noOfIterations` if the default values (4 & 1 respectively) cannot reproduce a deadlock.
* (2) Configuration file for the process engine. A `ManagedProcessEngineFactoryBean` is used. Also `MySQL` datasource is configured.
* (3) An example BPMN process. `Candidate Users` is set for the User Tasks.
* (4) pom.xml contains `camunda-engine-spring` and `mysql-connector-java` extra.

## Running the test with maven

In order to run the testsuite with maven you can say:

```
mvn clean test
```

## Importing the project into eclipse.

If you use eclipse you can simply import the project by selecting `File / Import |-> Existing Maven Projects.
