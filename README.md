# Guide of Using RegNets
## 1. Introduction

RegNets is the neural network structure We showed in the paper. This github project is the code We used for testing algorithm and hyper-parameters in our paper. It include three type of tests, which are Cross VAlidation Test, Train-Test Validation and Top-10 Bootstrap. They is based on Java Neural Network Framework "Neuroph". We do some foundational level extentions and modifications, so the "Neuroph" include in this project may not work with other projects.

## 2. Project Setup

You can clone or download our code from github. To run our code, you need have java runtime environment on your platform. You can go to https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html and follow the instructions to finish this step. We provide pre-build jar file which can make you run our code directly. You can run our code under a GUI or in console mode. In the following sections, we will introduce the interfaces and commands to run our code.Please notice, due to the space limit of github, we only put part of data on github.

## 3.1 Run Code under GUI

![Alt text](GUI.png?raw=true "GUI Screencut")
To open this GUI, you can simply double click on GUI.jar. You can also open it in console with command "java -jar GUI.jar" under the project directory.
With this GUI, you can choose one of the three tests, change the hyper-parameters, change the network using and training/testing files. Press "Start" to start a train. The logs will show in the white text area. You can also save those logs for further use.

## 3.2 Run Code under console

You can also run our code under a pure console environment. Go to the project directory, use "java -jar GRRANN.jar" to run the tests. Please notice you need to add some parameters to run the code correctly.
    -c		Do cross validation test.
    -t		Do train-test validation.
    -b		Do top-10 bootstrap.
    ---		Please notice, the last -c/t/b flag will active, and one -c/t/b is needed.
    -ent path	Path to network node file.
    -rel path	Path to network relation file.
    -train path	Path to train file.
    ---		Please notice, these three flags are always necessary to run the code.
    -test path	Path to test file. Only needed if using flag -t.
    -nov integer	Change the number of votes in cross validations tests. Have no influence if using other test type.
    -lamb double	Change the value of lambda, which will change the weight of regularization during training. Use 1e-5 if not specified.
    -eta double	Change the learning rate. Use 5e-3 if not specified.
    -mitr integer	Change the max number of training epoches. Use 2000 if not specified.
    -rnde true|false	Use random shuffeled network relations or not. The default value is false.
    -regt 0-4	Type of regularizations. 0(default)=l2-l1|1=l2-null|2=null-l1|3=l2-l2|4=l1-l1
    -edgt 0-3	Type of relation selected in network. 0(default)=All|1=+(increase)|2=-(decrease)|3=other 

## 4. Sample Test
### 1. Cross Validation Test

This is a test which do a cross validation on a single datasets. It will creat severl threads with different training set and testing set and compute them simultaneously. The output is the statistics and average results of the cross validation tests.
	-c -ent data/kb/string.ents -rel data/kb/string.rels -train results.data/UC/GSE12251/GSE12251_data.txt

### 2. Independent Train-Test Validation

This is a test which using one dataset as the training set and the other as the testing set. It will creat severl threads with the same setting, compute simultaneously and output the statistics and average 

### 3. Top-10 Bootstrap Test

This is a Boot Strap test progress, which will output the most common high frequency genes. 
