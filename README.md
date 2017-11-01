# Guide of Using RegNets
##1. Introduction
RegNets is the neural network structure We showed in the paper. This github project is the code We used for testing algorithm and hyper-parameters in our paper. It include three type of tests, which are Cross VAlidation Test, Train-Test Validation and Top-10 Bootstrap. They is based on Java Neural Network Framework "Neuroph". We do some foundational level extentions and modifications, so the "Neuroph" include in this project may not work with other projects.
##2. Project Setup
You can clone or download our code from github. To run our code, you need have java runtime environment on your platform. You can go to https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html and follow the instructions to finish this step. We provide pre-build jar file which can make you run our code directly. You can run our code under a GUI or in console mode. In the following sections, we will introduce the interfaces and commands to run our code.Please notice, due to the space limit of github, we only put part of data on github.
##3.1 Run Code under GUI
![Alt text](GUI.png?raw=true "GUI Screencut")
##3. Sample Test
    1. Cross Validation Test
    This is a test which do a cross validation on a single datasets. It will creat severl threads with different training set and testing set and compute them simultaneously. The output is the statistics and average results of the cross validation tests.
	-c -ent data/kb/string.ents -rel data/kb/string.rels -train results.data/UC/GSE12251/GSE12251_data.txt
    2. Independent Train-Test Validation
    This is a test which using one dataset as the training set and the other as the testing set. It will creat severl threads with the same setting, compute simultaneously and output the statistics and average 
    3. Top-10 Bootstrap Test
    This is a Boot Strap test progress, which will output the most common high frequency genes. 