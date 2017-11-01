import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.nnet.learning.BackPropagation;

public class RegNetsInstance implements Runnable {

	private ArrayList<ArrayList<Double>> trainData;
	private ArrayList<Integer> groundTruth;
	private int inputSize;
	private StdDataset datasets;
	private RegNets neuralNetwork;
	private Recorder re;
	private int tid = -1;
	private HashMap<Integer, Integer> freq;
	private String info;
	private ReentrantLock lock;
	private double eta;
	private double lambda;
	private double alpha;
	private int maxIter;
	private PrintStream out;
	private String reg;

	public RegNetsInstance(
			ArrayList<ArrayList<Double>> trainData,
			ArrayList<Integer> groundTruth,
			StdDataset datasets) {
		this(	trainData,
				groundTruth,
				datasets,
				System.out);
	}
	
	public RegNetsInstance(
			ArrayList<ArrayList<Double>> trainData,
			ArrayList<Integer> groundTruth,
			StdDataset datasets,
			PrintStream out){
		this.trainData = trainData;
		this.groundTruth =groundTruth;
		this.out = out;
		this.inputSize= trainData.get(0).size();
		this.datasets = datasets;
		this.re = new Recorder();
		this.re.hideIterMsg = true;
		
		this.eta = 0.2;
		this.lambda = 1e-4;
		this.alpha = 0.5;
		this.reg = "normal";
		this.maxIter = 200;
	}

	@Override
	public void run() {
		out.println("Instance " + tid + " training("+this.info+")...");		
		
		runtest();
		calcResult();
		
		if(lock != null) {
			lock.lock();
			try {
				ObjectWithValue[] ary = re.ary;
				for(int j=ary.length-1;j>ary.length-2-10;j--){
					ObjectWithValue o = ary[j];
					if(freq.containsKey(o.o)){
						freq.put((Integer) o.o, freq.get(o.o)+1);
					}else{
						freq.put((Integer) o.o, 1);
					}
				}
				out.println(tid + " Done.");
			}finally {
				lock.unlock();
			}
		}
	}
	
	public void runtest(){
		if(tid<0) tid = 0;
		init();
		train();
	}
	
	public void setTid(int tid){
		this.tid  = tid;
	}
	
	public Recorder getRecorder() {
		return re;
	}

	public void init(){
		neuralNetwork = new RegNets(datasets);
		
        // set learnng rule
		BackPropagation backPropagation;
		if(reg.equals("normal")) {
			backPropagation = new L2MomentumBackpropagation(lambda,alpha);
		}else {
			backPropagation = new L2MomentumBackpropagation(lambda,reg);
		}
        backPropagation.setLearningRate(eta);
        backPropagation.setMaxError(0.0000005);
        backPropagation.setMaxIterations(maxIter);
        backPropagation.addListener(new LearningListener(tid,re,out));
        backPropagation.setErrorFunction(new MeanSquaredError());
        neuralNetwork.setLearningRule(backPropagation);

	}
	
	public void train(){        
        //init train data
		DataSet trainSet = new DataSet(inputSize,1);
		for(int i=0;i<trainData.size();i++){
			double[] input = Stream.of(trainData.get(i).toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
			double[] output = {(double)groundTruth.get(i)};
//			out.println(i);
			trainSet.addRow(input, output);
		}
		
		neuralNetwork.learn(trainSet);		
	}
	
	public void calcResult(){
		ObjectWithValue[] ary = new ObjectWithValue[datasets.getHiddenLayerSize()];
        for(int i=0;i<datasets.getHiddenLayerSize();i++){
        	ary[i] = new ObjectWithValue(i,neuralNetwork.getLayers()[1].getNeuronAt(i).getOutConnections()[0].getWeight().value);
        }
        Arrays.sort(ary);
        re.ary = ary;
	}

	public void test(ArrayList<ArrayList<Double>> testSet, ArrayList<Integer> testGt) {
		// get predict outputs
        ArrayList<Double> outputs = new ArrayList<Double>();
        for(int i=0;i<testSet.size();i++){
			double[] input = Stream.of(testSet.get(i).toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();

			neuralNetwork.setInput(input);
			neuralNetwork.calculate();
			double result = neuralNetwork.getOutput()[0];
			outputs.add(result);
        }
        re.outputs = outputs;
        
        //find threshold
        double minDistance = 2;
        double useCutoff = -1;
        int TP = 0;
        int TN = 0;
        int FP = 0;
        int FN = 0;
        for(double cutoff=0;cutoff<1.001;cutoff+=0.001){
	        int mTP = 0;
	        int mTN = 0;
	        int mFP = 0;
	        int mFN = 0;
			
	        for(int i=0;i<testSet.size();i++){
	        	double result = outputs.get(i);
	        	int output = testGt.get(i);

				if(output == 1 && result >= cutoff)	mTP++;
				if(output == 1 && result < cutoff)	mFN++;
				if(output == 0 && result >= cutoff)	mFP++;
				if(output == 0 && result < cutoff)	mTN++;
			}
			double tpr = ((double)mTP)/(mTP+mFN);
			double fpr = ((double)mFP)/(mFP+mTN);
			double distance = Math.sqrt((1-tpr)*(1-tpr)+fpr*fpr);
			if(distance < minDistance){
				TP = mTP;
				TN = mTN;
				FP = mFP;
				FN = mFN;
				minDistance = distance;
				useCutoff = cutoff;
			}
			
			if(useCutoff<0) {
				out.println(mTP + "," + mFN + "," +distance);
				out.println(mFP + "," + mTN );
			}
			re.cutoff.add(cutoff);
			re.tpr.add(tpr);
			re.fpr.add(fpr);
        }
        re.useCutoff = useCutoff;
        
        //output test result 
        ArrayList<Integer> predict = new ArrayList<Integer>();
        double error = 0.0;
        for(int i=0;i<testSet.size();i++){
        	double result = outputs.get(i);
        	int output = testGt.get(i);
        	error += (result-output)*(result-output);
	        if(result>=useCutoff){
				predict.add(1);
			}else{
				predict.add(0);
			}        	
        }
        int tot = TP+TN+FP+FN;
		DecimalFormat numberFormat = new DecimalFormat("0.0000");
		re.println("************ Use cutoff " + numberFormat.format(useCutoff) + " ************");
		re.println("\tpredict+\tpredict-\tPrevalence= " + numberFormat.format(((double)TP+FN)/tot));
		re.println("+\t" + TP +"("+numberFormat.format(((double)TP)/tot)+")\t" + 
				FN +"("+numberFormat.format(((double)FN)/tot)+
				")\tSensitivity="+numberFormat.format(((double)TP)/(TP+FN))+
				"\tMiss rate=  "+numberFormat.format(((double)FN)/(TP+FN)));
		re.println("-\t" + FP +"("+numberFormat.format(((double)FP)/tot)+")\t" +
				TN +"("+numberFormat.format(((double)TN)/tot)+
				")\tFall-out=   "+numberFormat.format(((double)FP)/(FP+TN))+
				"\tSpecificity="+numberFormat.format(((double)TN)/(FP+TN)));
		re.println("\t\t"+
				"\tAccuracy="+numberFormat.format(((double)TP+TN)/tot)+
				"\tPrecision=  "+numberFormat.format(((double)TP)/(TP+FP)));
		re.println("***************************************");
		re.predict = predict;
//		re.error = error;
		re.sensitivity = ((double)TP)/(TP+FN);
		re.specificity = ((double)TN)/(FP+TN);
		
	}
	
	public void setParameter(double eta,double lambda,int regType) {
		this.eta = eta;
		this.lambda=lambda;
		if(regType == 0) this.alpha=0.5;
		if(regType == 1) this.alpha=0.0;
		if(regType == 2) this.alpha=1.0;
		if(regType == 3) this.reg="l2";
		if(regType == 3) this.reg="l1";
	}
	
	public void setMaxIter(int iter) {
		this.maxIter = iter;
	}
	
	public void setHideIterMsg(boolean b){
		re.hideIterMsg = b;
	}

	public void setFreq(HashMap<Integer, Integer> freq) {
		this.freq = freq;
		
	}

	public void setInfo(String info) {
		this.info = info;
		
	}

	public void setLock(ReentrantLock lock) {
		this.lock=lock;		
	}

}
