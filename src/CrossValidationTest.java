import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CrossValidationTest implements Runnable{

	//configuration
	private static final int TESTNUM = 5;
	private static final String[] REGTYPENAME = {"(standard)l2-l1", "l2-Null", "Null-l1", "l2-l2", "l1-l1"};
	private static final String[] EDGTYPENAME = {"(standard)All", "positive", "negative", "other"};
	
	private String networkEnds;
	private String networkRels;
	private String trainFile;
	private double lambda;
	private double eta;
	private int maxIter;
	private boolean rndEdg;
	private int regType;
	private int edgType;
	private int numOfVote;
	private PrintStream out;
	
	public CrossValidationTest(String networkEnds, String networkRels, String trainFile, double lambda,
			double eta, int maxIter, boolean rndEdg, int regType, int edgType, int numOfVote, PrintStream out) {
		this.networkEnds = networkEnds;
		this.networkRels = networkRels;
		this.trainFile = trainFile;
		this.lambda = lambda;
		this.eta = eta;
		this.maxIter = maxIter;
		this.rndEdg = rndEdg;
		this.regType = regType;
		this.edgType = edgType;
		this.numOfVote = numOfVote;
		this.out = out;
	}

	public static void main(String[] args) {
		// Default value
//		String networkEnds = "kb/BEL_LargeCorpus.ents";
//		String networkRels = "kb/BEL_LargeCorpus.rels";
		String networkEnds = "kb/string.ents";
		String networkRels = "kb/string.rels";
		
		String dataset1 = "UC/GSE12251/GSE12251_data.txt";
		String dataset2 = "UC/GSE14580/GSE14580_data.txt";
		String dataset3 = "Rejection/GSE21374/GSE21374_data.txt";
		String dataset4 = "Rejection/GSE50058/GSE50058_data.txt";
		
		//select test dataset		
		String trainFile = dataset1;
		
		// Confirm input information
		System.out.println("******Input Information******\nTrain File:\t" + trainFile);
		System.out.println("Network File:\t" + networkEnds +" | " + networkRels);
		System.out.println("*****************************");

		// Read input data
		StdDataset datasets = new StdDataset(networkEnds,networkRels);
		datasets.readData(trainFile);

		datasets.standardizeData();

		// Prepare Network
		RegNetsInstance[] ins = new RegNetsInstance[TESTNUM];
		Thread[] trd = new Thread[TESTNUM];

		//used data
		ArrayList<ArrayList<Double>> d1 = new ArrayList<ArrayList<Double>>(datasets.getDatasets().get(0));
		ArrayList<Integer> gt1 = new ArrayList<Integer>(datasets.getGroundTruths().get(0));
		ArrayList<ArrayList<ArrayList<Double>>> dt = new ArrayList<ArrayList<ArrayList<Double>>>();
		ArrayList<ArrayList<Integer>> gtt = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> poolp = new ArrayList<Integer>();
		ArrayList<Integer> pooln = new ArrayList<Integer>();
		for(int i=0;i<TESTNUM;i++){
			dt.add(new ArrayList<ArrayList<Double>>());
			gtt.add(new ArrayList<Integer>());
			poolp.add(i);
			pooln.add(i);
		}
		int n = d1.size();
		int num0 = Collections.frequency(gt1, 0);
		int num1 = n-num0;
		Random rnd = new Random();
		for(int i=0;i<n;i++){
			if(gt1.get(i)==0) {
				int rNum = rnd.nextInt(pooln.size());
				int idx = pooln.get(rNum);
				dt.get(idx).add(d1.get(i));
				gtt.get(idx).add(gt1.get(i));
				if(Collections.frequency(gtt.get(idx), 0)>=((double)num0/TESTNUM)){
					pooln.remove(rNum);
				}
				if(gtt.get(idx).size()>=((double)n/TESTNUM)){
					if(poolp.contains(idx)) poolp.remove(poolp.indexOf(idx));
					if(pooln.contains(idx)) pooln.remove(pooln.indexOf(idx));
				}
			}else {
				int rNum = rnd.nextInt(poolp.size());
				int idx = poolp.get(rNum);
				dt.get(idx).add(d1.get(i));
				gtt.get(idx).add(gt1.get(i));
				if(Collections.frequency(gtt.get(idx), 1)>=((double)num1/TESTNUM)){
					poolp.remove(rNum);
				}
				if(gtt.get(idx).size()>=((double)n/TESTNUM)){
					if(poolp.contains(idx)) poolp.remove(poolp.indexOf(idx));
					if(pooln.contains(idx)) pooln.remove(pooln.indexOf(idx));
				}
			}
		}
		for(int i=0;i<TESTNUM;i++){
			int gt_0 = Collections.frequency(gtt.get(i), 0);
			int gt_1 = Collections.frequency(gtt.get(i), 1);
			System.out.println(gt_0 + "," + gt_1);
		}
		for(int i=0;i<TESTNUM;i++){
			ArrayList<ArrayList<Double>> trainD = new ArrayList<ArrayList<Double>>();
			ArrayList<Integer> trainGt = new ArrayList<Integer>();
			ArrayList<ArrayList<Double>> testD = new ArrayList<ArrayList<Double>>();
			ArrayList<Integer> testGt = new ArrayList<Integer>();
			for(int j=0;j<TESTNUM;j++){
				if(i==j){
					testD.addAll(dt.get(j));
					testGt.addAll(gtt.get(j));
				}else{
					trainD.addAll(dt.get(j));
					trainGt.addAll(gtt.get(j));					
				}
			}
			ins[i] = new RegNetsInstance(
					trainD,
					trainGt,
					datasets);
			ins[i].setTid(i);
//			ins[i].setParameter(0.5, 7e-5, 0.5);
			ins[i].setParameter(1e0,1e-4, 0);
			ins[i].setMaxIter(100);
			ins[i].setInfo(trainD.size()+"/"+testD.size());
			if(i==0) ins[i].setHideIterMsg(false);

//			System.out.println("Instance " + i + " training...");
			trd[i] = new Thread(ins[i]);
			trd[i].start();
		}
		
		try {
			for(int i=0;i<TESTNUM;i++){
				trd[i].join();
				System.out.println(i+" joined");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		double errorMean = 0;
		double errorSD = 0;
		double tprMean = 0;
		double tprSD = 0;
		double fprMean = 0;
		double fprSD = 0;
		double bAccMean = 0;
		double bAccSD = 0;
		
		
		for(int i=0;i<TESTNUM;i++){
			ins[i].test(
					dt.get(i),
					gtt.get(i));
			Recorder re = ins[i].getRecorder();
			double useCutoff = re.useCutoff;
			int idx = re.cutoff.indexOf(useCutoff);
			double tpr = re.tpr.get(idx);
			double fpr = re.fpr.get(idx);
			double error = re.error;
			double bAcc = (re.sensitivity + re.specificity)/2;
			
			errorMean += error / TESTNUM;
			errorSD += error*error / TESTNUM;
			tprMean += tpr / TESTNUM;
			tprSD += tpr*tpr / TESTNUM;
			fprMean += fpr / TESTNUM;
			fprSD += fpr*fpr / TESTNUM;
			bAccMean += bAcc / TESTNUM;
			bAccSD += bAcc*bAcc / TESTNUM;
			
			System.out.println(re.output);
		}
		errorSD = Math.sqrt(errorSD - errorMean*errorMean);
		tprSD = Math.sqrt(tprSD - tprMean*tprMean);
		fprSD = Math.sqrt(fprSD - fprMean*fprMean);
		bAccSD = Math.sqrt(bAccSD - bAccMean*bAccMean);
		
		System.out.printf("Error: %5.4f +- %5.4f\n",errorMean,errorSD);
		System.out.printf("Tpr: %5.4f +- %5.4f\n",tprMean,tprSD);
		System.out.printf("Fpr: %5.4f +- %5.4f\n",fprMean,fprSD);
		System.out.printf("Balanced Accuracy: %5.4f +- %5.4f\n",bAccMean,bAccSD);
	}

	@Override
	public void run() {

		// Confirm input information
		out.println("******Input Information******\nCross Validation Test");
		out.println("Train File:\t" + trainFile);
		out.println("Network File:\t" + networkEnds +" | " + networkRels);
		out.println("Lambda:\t" + lambda +"\t\tLearning Rate:\t" + eta);
		out.println("Max Iteration:\t" + maxIter +"\t\tNumber of Votes:\t" + numOfVote);
		out.println("Random Edge:\t" + rndEdg);
		out.println("Regularization Type:\t" + REGTYPENAME[regType]);
		out.println("Edge Selected:\t" + EDGTYPENAME[edgType]);
		out.println("*****************************");
		
		// Read input data
		StdDataset datasets = new StdDataset(networkEnds,networkRels,rndEdg,edgType,out);
		datasets.readData(trainFile,out);
		datasets.standardizeData(out);
		
		// Prepare Network
		RegNetsInstance[] ins = new RegNetsInstance[numOfVote];
		Thread[] trd = new Thread[numOfVote];
		
		//used data
		ArrayList<ArrayList<Double>> d1 = new ArrayList<ArrayList<Double>>(datasets.getDatasets().get(0));
		ArrayList<Integer> gt1 = new ArrayList<Integer>(datasets.getGroundTruths().get(0));
		ArrayList<ArrayList<ArrayList<Double>>> dt = new ArrayList<ArrayList<ArrayList<Double>>>();
		ArrayList<ArrayList<Integer>> gtt = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> poolp = new ArrayList<Integer>();
		ArrayList<Integer> pooln = new ArrayList<Integer>();
		for(int i=0;i<numOfVote;i++){
			dt.add(new ArrayList<ArrayList<Double>>());
			gtt.add(new ArrayList<Integer>());
			poolp.add(i);
			pooln.add(i);
		}
		int n = d1.size();
		int num0 = Collections.frequency(gt1, 0);
		int num1 = n-num0;
		Random rnd = new Random();
		for(int i=0;i<n;i++){
			if(gt1.get(i)==0) {
				int rNum = rnd.nextInt(pooln.size());
				int idx = pooln.get(rNum);
				dt.get(idx).add(d1.get(i));
				gtt.get(idx).add(gt1.get(i));
				if(Collections.frequency(gtt.get(idx), 0)>=((double)num0/numOfVote)){
					pooln.remove(rNum);
				}
				if(gtt.get(idx).size()>=((double)n/numOfVote)){
					if(poolp.contains(idx)) poolp.remove(poolp.indexOf(idx));
					if(pooln.contains(idx)) pooln.remove(pooln.indexOf(idx));
				}
			}else {
				int rNum = rnd.nextInt(poolp.size());
				int idx = poolp.get(rNum);
				dt.get(idx).add(d1.get(i));
				gtt.get(idx).add(gt1.get(i));
				if(Collections.frequency(gtt.get(idx), 1)>=((double)num1/numOfVote)){
					poolp.remove(rNum);
				}
				if(gtt.get(idx).size()>=((double)n/numOfVote)){
					if(poolp.contains(idx)) poolp.remove(poolp.indexOf(idx));
					if(pooln.contains(idx)) pooln.remove(pooln.indexOf(idx));
				}
			}
		}
//		for(int i=0;i<numOfVote;i++){
//			int gt_0 = Collections.frequency(gtt.get(i), 0);
//			int gt_1 = Collections.frequency(gtt.get(i), 1);
//			out.println(gt_0 + "," + gt_1);
//		}
		for(int i=0;i<numOfVote;i++){
			ArrayList<ArrayList<Double>> trainD = new ArrayList<ArrayList<Double>>();
			ArrayList<Integer> trainGt = new ArrayList<Integer>();
			for(int j=0;j<numOfVote;j++){
				if(i!=j){
					trainD.addAll(dt.get(j));
					trainGt.addAll(gtt.get(j));					
				}
			}
			ins[i] = new RegNetsInstance(
					trainD,
					trainGt,
					datasets,
					out);
			ins[i].setTid(i);
			
			ins[i].setParameter(eta,lambda,regType);
			ins[i].setMaxIter(maxIter);
			ins[i].setInfo(trainD.size()+"/"+dt.get(i).size());
			if(i==0) ins[i].setHideIterMsg(false);

			trd[i] = new Thread(ins[i]);
			trd[i].start();
		}
		
		try {
			for(int i=0;i<numOfVote;i++){
				trd[i].join();
//				out.println(i+" joined");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		double errorMean = 0;
		double errorSD = 0;
		double tprMean = 0;
		double tprSD = 0;
		double fprMean = 0;
		double fprSD = 0;
		double bAccMean = 0;
		double bAccSD = 0;
		
		for(int i=0;i<numOfVote;i++){
			ins[i].test(
					dt.get(i),
					gtt.get(i));
			Recorder re = ins[i].getRecorder();
			double useCutoff = re.useCutoff;
			int idx = re.cutoff.indexOf(useCutoff);
			double tpr = re.tpr.get(idx);
			double fpr = re.fpr.get(idx);
			double error = re.error;
			double bAcc = (re.sensitivity + re.specificity)/2;
			
			errorMean += error / numOfVote;
			errorSD += error*error / numOfVote;
			tprMean += tpr / numOfVote;
			tprSD += tpr*tpr / numOfVote;
			fprMean += fpr / numOfVote;
			fprSD += fpr*fpr / numOfVote;
			bAccMean += bAcc / numOfVote;
			bAccSD += bAcc*bAcc / numOfVote;
			
			out.println(re.output);
		}
		errorSD = Math.sqrt(errorSD - errorMean*errorMean);
		tprSD = Math.sqrt(tprSD - tprMean*tprMean);
		fprSD = Math.sqrt(fprSD - fprMean*fprMean);
		bAccSD = Math.sqrt(bAccSD - bAccMean*bAccMean);
		
		out.printf("Error: %5.4f +- %5.4f\n",errorMean,errorSD);
		out.printf("Tpr: %5.4f +- %5.4f\n",tprMean,tprSD);
		out.printf("Fpr: %5.4f +- %5.4f\n",fprMean,fprSD);
		out.printf("Balanced Accuracy: %5.4f +- %5.4f\n",bAccMean,bAccSD);		
	}
}
