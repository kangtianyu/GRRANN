# Guide to Using GRRANN
## 1. Introduction

In this work, we develop a Regularized Artificial Neural Network (ANN) that encodes the co-dependencies between genes and their regulators into the architecture of the classifier. Our model, GRRANN (Gene Regulatory network-based Regularized Artificial Neural Network), is specifically designed for prediction of phenotypes from gene-expression data.The induced sparsity on the ANN based on the gene-regulatory interactions, signifigantly reduces the number of model parameter and the need for large sample sizes that are typically required to train ANNs. The structure of our ANNs naturally lends itself to regularization models for group-wise and graph-based variable selection. It includes three type of tests, which are Cross VAlidation Test, Train-Test Validation, and Top-10 Bootstrap. They are based on Java Neural Network Framework "Neuroph". We do some foundational level extensions and modifications, so the "Neuroph" include in this project may not work with other projects.

## 2. Project Setup

You can clone or download our code from GitHub. To run our code, you need have java runtime environment on your platform. You can go to https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html and follow the instructions to finish this step. We provide pre-build jar file which can make you run our code directly. You can run our code under a GUI or in console mode. In the following sections, we will introduce the interfaces and commands to run our code.Please notice, due to the space limit of GitHub, we only put part of data on GitHub.

## 3.1 Run Code under GUI

![Alt text](GUI.png?raw=true "GUI Screencut")
To open this GUI, you can simply double click on GUI.jar. You can also open it in console with the command "java -jar GUI.jar" under the project directory.
With this GUI, you can choose one of the three tests, change the hyper-parameters, change the network using and training/testing files. Press "Start" to start a train. The logs will show in the white text area. You can also save those logs for further use.

## 3.2 Run Code under console

You can also run our code under a pure console environment. Go to the project directory, use "java -jar GRRANN.jar" to run the tests. Please notice you need to add some parameters to run the code correctly.

    -c            Do cross-validation test.
    -t            Do train-test validation.
    -b            Do top-10 bootstrap.
    ---            Please notice, the last -c/t/b flag will active, and one -c/t/b is needed.
    -ent path        Path to network node file.
    -rel path        Path to network relation file.
    -train path        Path to train file.
    ---            Please notice, these three flags are always necessary to run the code.
    -test path        Path to test file. Only needed if using the flag -t.
    -nov integer        Change the number of votes in cross validations tests. Have no influence if using another test type.
    -lamb double        Change the value of lambda, which will change the weight of regularization during training. Use 1e-5 if not specified.
    -eta double        Change the learning rate. Use 5e-3 if not specified.
    -mitr integer        Change the max number of training epochs. Use 2000 if not specified.
    -rnde true|false    Use random shuffled network relations or not. The default value is false.
    -regt 0-4        Type of regularizations. 0(default)=l2-l1|1=l2-null|2=null-l1|3=l2-l2|4=l1-l1
    -edgt 0-3        Type of relation selected in the network. 0(default)=All|1=+(increase)|2=-(decrease)|3=other 

## 4. Sample Tests
### 1. Cross Validation Test

This is a test which does a cross-validation on a single dataset. It will create several threads with the different training set and testing set and compute them simultaneously. The output is the statistics and average results of the cross-validation tests. With GUI, you could click "Cross Validation" -> "Default Setting" -> "Start" and watch the results. With console, you should type

    java -jar GRRANN.jar -c -ent data/kb/string.ents -rel data/kb/string.rels -train results.data/UC/GSE12251/GSE12251_data.txt

### 2. Independent Train-Test Validation

This is a test which using one dataset as the training set and the other as the testing set. It will create several threads with the same setting, compute simultaneously and output the statistics and average balanced accuracy. With GUI, you could click "Train-Test" -> "Default Setting" -> "Start" and watch the results. With console, you should type

    java -jar GRRANN.jar -t -ent data/kb/string.ents -rel data/kb/string.rels -train results.data/UC/GSE12251/GSE12251_data.txt -test data/UC/GSE14580/GSE14580_data.txt

### 3. Top-10 Bootstrap Test

This is a Top-10 Bootstrap test progress, which will output the most common high-frequency genes. With GUI, you could click "Top-10 Bootstrap" -> "Default Setting" -> "Start" and watch the results. With console, you should type

    java -jar GRRANN.jar -b -ent data/kb/string.ents -rel data/kb/string.rels -train results.data/UC/GSE12251/GSE12251_data.txt
