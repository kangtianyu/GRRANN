import java.io.PrintStream;

public class IndependentTest implements Runnable{

	//configuration
	private static final int TESTNUM = 5;
	private static final String[] REGTYPENAME = {"(standard)l2-l1", "l2-Null", "Null-l1", "l2-l2", "l1-l1"};
	private static final String[] EDGTYPENAME = {"(standard)All", "positive", "negative", "other"};
	
	private String networkEnds;
	private String networkRels;
	private String trainFile;
	private String testFile;
	private double lambda;
	private double eta;
	private int maxIter;
	private boolean rndEdg;
	private int regType;
	private int edgType;
	private PrintStream out;

	public IndependentTest(String networkEnds, String networkRels, String trainFile, String testFile, double lambda,
			double eta, int maxIter, boolean rndEdg, int regType, int edgType, PrintStream out) {
		this.networkEnds = networkEnds;
		this.networkRels = networkRels;
		this.trainFile = trainFile;
		this.testFile = testFile;
		this.lambda = lambda;
		this.eta = eta;
		this.maxIter = maxIter;
		this.rndEdg = rndEdg;
		this.regType = regType;
		this.edgType = edgType;
		this.out = out;
	}

	public static void main(String[] args) {
		// Default value
		String networkEnds = "kb/BEL_LargeCorpus.ents";
		String networkRels = "kb/BEL_LargeCorpus.rels";
		
		String dataset1 = "UC/GSE12251/GSE12251_data.txt";
		String dataset2 = "UC/GSE14580/GSE14580_data.txt";
		String dataset3 = "Rejection/GSE21374/GSE21374_data.txt";
		String dataset4 = "Rejection/GSE50058/GSE50058_data.txt";
		
		//select test dataset		
		String trainFile = dataset3;
		String testFile = dataset4;
		
		// Confirm input information
		System.out.println("******Input Information******\nTrain File:\t" + trainFile);
		System.out.println("Test File:\t" + (testFile == null?"---Not UseObject---":testFile));
		System.out.println("Network File:\t" + networkEnds +" | " + networkRels);
		System.out.println("*****************************");
		
		// Read input data
		StdDataset datasets = new StdDataset(networkEnds,networkRels);
		datasets.readData(trainFile);
		datasets.readData(testFile);

		datasets.standardizeData();

		// Prepare Network
		RegNetsInstance[] ins = new RegNetsInstance[TESTNUM];
		Thread[] trd = new Thread[TESTNUM];
		
		//statistics
//		HashMap<Integer,Integer> frequency = new HashMap<Integer,Integer>();
		
		for(int i=0;i<TESTNUM;i++){
			ins[i] = new RegNetsInstance(
					datasets.getDatasets().get(0),
					datasets.getGroundTruths().get(0),
					datasets);
			ins[i].setHideIterMsg(false);

			System.out.println("Instance " + i + " training...");
			trd[i] = new Thread(ins[i]);
			trd[i].start();
//			System.out.println(re.output);
//			
//			ObjectWithValue[] ary = ins.getRecorder().ary;
//			for(int j=ary.length-1;j>ary.length - 2 - TOPTHREASHOLD;j--){
//				ObjectWithValue o = ary[j];
//				System.out.printf("%5d:%5.4f:%20s\n",(int)o.o,o.value,datasets.getHiddenNodeContent((int)o.o));
//			}
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
		
		
		for(int i=0;i<TESTNUM;i++){
			ins[i].test(
					datasets.getDatasets().get(1),
					datasets.getGroundTruths().get(1));
			Recorder re = ins[i].getRecorder();
			double useCutoff = re.useCutoff;
			int idx = re.cutoff.indexOf(useCutoff);
			double tpr = re.tpr.get(idx);
			double fpr = re.fpr.get(idx);
			double error = re.error;
			
			errorMean += error / TESTNUM;
			errorSD += error*error / TESTNUM;
			tprMean += tpr / TESTNUM;
			tprSD += tpr*tpr / TESTNUM;
			fprMean += fpr / TESTNUM;
			fprSD += fpr*fpr / TESTNUM;

			System.out.println(re.output);
		}
		errorSD = Math.sqrt(errorSD - errorMean*errorMean);
		tprSD = Math.sqrt(tprSD - tprMean*tprMean);
		fprSD = Math.sqrt(fprSD - fprMean*fprMean);
		
		System.out.printf("Error: %5.4f +- %5.4f\n",errorMean,errorSD);
		System.out.printf("Tpr: %5.4f +- %5.4f\n",tprMean,tprSD);
		System.out.printf("Fpr: %5.4f +- %5.4f\n",fprMean,fprSD);
		
	}

	@Override
	public void run() {

		// Confirm input information
		out.println("******Input Information******\nTrain-Test Validation");
		out.println("Train File:\t" + trainFile);
		out.println("Test File:\t" + testFile);
		out.println("Network File:\t" + networkEnds +" | " + networkRels);
		out.println("Lambda:\t" + lambda +"\t\tLearning Rate:\t" + eta);
		out.println("Max Iteration:\t" + maxIter);
		out.println("Random Edge:\t" + rndEdg);
		out.println("Regularization Type:\t" + REGTYPENAME[regType]);
		out.println("Edge Selected:\t" + EDGTYPENAME[edgType]);
		out.println("*****************************");
		
		// Read input data
		StdDataset datasets = new StdDataset(networkEnds,networkRels,rndEdg,edgType,out);
		datasets.readData(trainFile,out);
		datasets.readData(testFile,out);
		datasets.standardizeData(out);

		// Prepare Network
		RegNetsInstance[] ins = new RegNetsInstance[TESTNUM];
		Thread[] trd = new Thread[TESTNUM];
		
		// Train and Statistics
		
		for(int i=0;i<TESTNUM;i++){
			ins[i] = new RegNetsInstance(
					datasets.getDatasets().get(0),
					datasets.getGroundTruths().get(0),
					datasets,
					out);
			ins[i].setTid(i);
			
			ins[i].setParameter(eta,lambda,regType);
			ins[i].setMaxIter(maxIter);
			ins[i].setInfo(datasets.getDatasets().get(0).size()+"/"+datasets.getDatasets().get(1).size());
			if(i==0) ins[i].setHideIterMsg(false);

			trd[i] = new Thread(ins[i]);
			trd[i].start();
		}
		try {
			for(int i=0;i<TESTNUM;i++){
				trd[i].join();
//				System.out.println(i+" joined");
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
					datasets.getDatasets().get(1),
					datasets.getGroundTruths().get(1));
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
