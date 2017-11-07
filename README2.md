In this work, we develop a Regularized Artificial Neural Network (ANN) that encodes the co-dependencies between genes and their regulators into the architecture of the classifier. Our model, GRRANN (Gene Regulatory network-based Regularized Artificial Neural Network), is specifically designed for prediction of phenotypes from gene-expression data.The induced sparsity on the ANN based on the gene-regulatory interactions, signifigantly reduces the number of model parameter and the need for large sample sizes that are typically required to train ANNs. The structure of our ANNs naturally lends itself to regularization models for group-wise and graph-based variable selection. It includes three type of tests, which are Cross Validation Test, Train-Test Validation, and Top-10 Bootstrap. They are based on Java Neural Network Framework "Neuroph". </p>

You can clone or download our the directly from GitHub. To run the code, you need to install java 8+ runtime environment on your platform. You can go to <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html">https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html</a> and follow the instructions to finish this step. We provide pre-build jar as well. We also provide a GUI for ease of use.  In the following sections, we will introduce the interfaces and commands for running the code.


To open this GUI, you can simply double click on GUI.jar. You can also open it in console with the command "java -jar GUI.jar" under the project directory. With this GUI, you can choose one of the three tests, change the hyper-parameters or change the network using and training/testing files. Press "Start" to start a train. The logs will show in the white text area. You can also save those logs for further use.


<p>You can also run the command line version of the code in a terminal. Go to the project directory, use "java -jar GRRANN.jar" to run the tests. The following parameters need to be set.</p>
<pre><code>-c            Performe cross-validation test.
-t            performe train-test validation.
-b            performe top-10 bootstrap analysis.
---            Please note that one flag is needed to run the code (-c/t/b)
-ent path        Path to the network node file.
-rel path        Path to the network relation file.
-train path        Path to the train file.
---            Please notice, these three flags are always necessary to run the code.
-test path        Path to the test file. Only needed if using the flag -t.
-nov integer        Change the number of votes in cross validations tests. This has no influence if using another test type.
-lamb double        Change the value of lambda, which will change the weight of regularization during training. Default value is  1e-5 .
-eta double        Change the learning rate. Default value is 5e-3.
-mitr integer        Change the max number of training epochs. Default value is 2000.
-rnde true|false    Use random shuffled network relations or not. Default value is false.
-regt 0-4        Type of regularizations. 0(default)=l2-l1|1=l2-null|2=null-l1|3=l2-l2|4=l1-l1
-edgt 0-3        Type of relation selected in the network. 0(default)=All|1=+(increase)|2=-(decrease)|3=other 
</code></pre>

<p>This is a test which does a cross-validation on a single dataset. It will create several threads with the different training set and testing set and compute them simultaneously. The output is the statistics and average results of the cross-validation tests. With GUI, you could click "Cross Validation" -&gt; "Default Setting" -&gt; "Start" and watch the results. With console, you should type</p>
<pre><code>java -jar GRRANN.jar -c -ent data/kb/string.ents -rel data/kb/string.rels -train results.data/UC/GSE12251/GSE12251_data.txt
</code></pre>
<h3><a href="#2-independent-train-test-validation" aria-hidden="true" class="anchor" id="user-content-2-independent-train-test-validation"><svg aria-hidden="true" class="octicon octicon-link" height="16" version="1.1" viewBox="0 0 16 16" width="16"><path fill-rule="evenodd" d="M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"></path></svg></a>2. Independent Train-Test Validation</h3>
<p>This is a test which using one dataset as the training set and the other as the testing set. It will create several threads with the same setting, compute simultaneously and output the statistics and average balanced accuracy. With GUI, you could click "Train-Test" -&gt; "Default Setting" -&gt; "Start" and watch the results. With console, you should type</p>
<pre><code>java -jar GRRANN.jar -t -ent data/kb/string.ents -rel data/kb/string.rels -train results.data/UC/GSE12251/GSE12251_data.txt -test data/UC/GSE14580/GSE14580_data.txt
</code></pre>
<h3><a href="#3-top-10-bootstrap-test" aria-hidden="true" class="anchor" id="user-content-3-top-10-bootstrap-test"><svg aria-hidden="true" class="octicon octicon-link" height="16" version="1.1" viewBox="0 0 16 16" width="16"><path fill-rule="evenodd" d="M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"></path></svg></a>3. Top-10 Bootstrap Test</h3>
<p>This is a Top-10 Bootstrap test progress, which will output the most common high-frequency genes. With GUI, you could click "Top-10 Bootstrap" -&gt; "Default Setting" -&gt; "Start" and watch the results. With console, you should type</p>
<pre><code>java -jar GRRANN.jar -b -ent data/kb/string.ents -rel data/kb/string.rels -train results.data/UC/GSE12251/GSE12251_data.txt
</code></pre>

5. Build from source </h2>
<p>You can build the project from source as follows: Under the project directory, you can use "ant GUI.xml" to rebuild GUI.jar, or use "ant GRRANN.xml" to rebuild GRRANN.jar.</p>