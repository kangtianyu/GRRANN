import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BootStrapTest implements Runnable{

	private static final String[] REGTYPENAME = {"(standard)l2-l1", "l2-Null", "Null-l1", "l2-l2", "l1-l1"};
	private static final String[] EDGTYPENAME = {"(standard)All", "positive", "negative", "other"};
	
	//configuration
	private static final int BOOTSTRAPNUM = 100 ;
	private static final int BOOTSTRAPTOPTHREASHOLD1 = 10;
	private static final int POOLSIZE = 4;
	private String networkEnds;
	private String networkRels;
	private String trainFile;
	private double lambda;
	private double eta;
	private int maxIter;
	private boolean rndEdg;
	private int regType;
	private int edgType;
	private PrintStream out;

	public BootStrapTest(String networkEnds, String networkRels, String trainFile, double lambda, double eta, int maxIter,
			boolean rndEdg, int regType, int edgType, PrintStream out) {
		this.networkEnds = networkEnds;
		this.networkRels = networkRels;
		this.trainFile = trainFile;
		this.lambda = lambda;
		this.eta = eta;
		this.maxIter = maxIter;
		this.rndEdg = rndEdg;
		this.regType = regType;
		this.edgType = edgType;
		this.out = out;
	}

	public static void main(String[] args) throws InterruptedException, IOException {		
		// Default value
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String ctime = sdf.format(cal.getTime());
		String logPath = System.getProperty("user.dir") + "/log/" +ctime + ".log";
		BufferedWriter bw = new BufferedWriter(new FileWriter(logPath));
		bw.write(ctime+"\n");
		bw.flush();
		bw.close();
//		String networkEnds = "kb/BEL_LargeCorpus.ents";
//		String networkRels = "kb/BEL_LargeCorpus.rels";
		String networkEnds = "kb/string.ents";
		String networkRels = "kb/string.rels";
		
		String dataset1 = "UC/GSE12251/GSE12251_data.txt";
		String dataset2 = "UC/GSE14580/GSE14580_data.txt";
		String dataset3 = "Rejection/GSE21374/GSE21374_data.txt";
		String dataset4 = "Rejection/GSE50058/GSE50058_data.txt";
		String[] alldatasets = {dataset1,dataset2,dataset3,dataset4};
		
		int dataset_idx = 0;
		for(String dataset:alldatasets) {
			
			//select test dataset
			String useDataset = dataset;
			
			// Confirm input information
			System.out.println("******Input Information******\nTrain File:\t" + useDataset);
			System.out.println("Network File:\t" + networkEnds +" | " + networkRels);
			System.out.println("*****************************");
			bw = new BufferedWriter(new FileWriter(logPath,true));
			bw.write("******Input Information******\nTrain File:\t" + useDataset+"\n");
			bw.write("Network File:\t" + networkEnds +" | " + networkRels+"\n");
			bw.write("*****************************\n");
			bw.flush();
			bw.close();
			
			// Read input data
			StdDataset datasets = new StdDataset(networkEnds,networkRels);
			datasets.readData(useDataset);
	
			datasets.standardizeData();
	
			// Prepare Network
			RegNetsInstance ins;
			
			//used data
			ArrayList<ArrayList<Double>> d0 = datasets.getDatasets().get(0);
			ArrayList<Integer> g0 = new ArrayList<Integer>(datasets.getGroundTruths().get(0));
			
			//statistics
			HashMap<Integer,Integer> frequency1 = new HashMap<Integer,Integer>();
			
			ExecutorService executor = Executors.newFixedThreadPool(POOLSIZE);
			ReentrantLock lock = new ReentrantLock();
			
			//bootstrap
			for(int i=0;i<BOOTSTRAPNUM;i++){
				int n = d0.size();
				ArrayList<ArrayList<Double>> trainSet = new ArrayList<ArrayList<Double>>();
				ArrayList<Integer> trainGt = new ArrayList<Integer>();
				ArrayList<ArrayList<Double>> validSet = new ArrayList<ArrayList<Double>>();
				ArrayList<Integer> validGt = new ArrayList<Integer>();
				
				HashSet<Integer> hs = new HashSet<Integer>();
				for(int j=0;j<n;j++){
					int idx = (int) (Math.random()*n);
					trainSet.add(d0.get(idx));
					trainGt.add(g0.get(idx));
					hs.add(idx);
				}
				for(int j=0;j<n;j++){
					if(!hs.contains(j)){
						validSet.add(d0.get(j));
						validGt.add(g0.get(j));
					}
				}
				
				ins = new RegNetsInstance(
						trainSet,
						trainGt,
						datasets);
	//			System.out.println("Instance " + i + " training("+hs.size()+"/"+d0.size()+")...");
				ins.setInfo(hs.size()+"/"+d0.size());
				if(dataset_idx>1) {
					ins.setParameter(0.5, 7e-5 , 0);
				}else {
					ins.setParameter(0.01,1e-6, 0);
					ins.setMaxIter(500);					
				}
				ins.setLock(lock);
				ins.setTid(i);
				ins.setFreq(frequency1);
				executor.execute(ins);
				
	//			ObjectWithValue[] ary = ins.getRecorder().ary;
	//			for(int j=ary.length-1;j>ary.length-2-BOOTSTRAPTOPTHREASHOLD1;j--){
	//				ObjectWithValue o = ary[j];
	//				if(frequency1.containsKey(o.o)){
	//					frequency1.put((Integer) o.o, frequency1.get(o.o)+1);
	//				}else{
	//					frequency1.put((Integer) o.o, 1);
	//				}
	//			}
	//			System.out.println(" Done.");
			}
			
			executor.shutdown();
			executor.awaitTermination(1L, TimeUnit.DAYS);
			
			ObjectWithValue[]  ary;
			int j;
			
			ary = new ObjectWithValue[frequency1.size()];
			j=0;
	        for(Integer i:frequency1.keySet()){
	        	ary[j++] = new ObjectWithValue(i,frequency1.get(i));
	        }
	        Arrays.sort(ary);
			bw = new BufferedWriter(new FileWriter(logPath,true));
	        for(int i= ary.length>300?ary.length-300:0;i<ary.length;i++){
	        	ObjectWithValue o = ary[i];
	        	bw.write(String.format("%10d:%5d:%25s\n",datasets.getHiddenNodeId((int)o.o),(int)o.value,datasets.getHiddenNodeContent((int)o.o)));
	        }
	        bw.write("------\n");
			bw.flush();
			bw.close();
			dataset_idx ++;
		}
	}

	@Override
	public void run() {

		// Confirm input information
		out.println("******Input Information******\nTop-10 Bootstrap");
		out.println("Train File:\t" + trainFile);
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
		datasets.standardizeData(out);
		
		// Prepare Network
		RegNetsInstance ins;
		
		//used data
		ArrayList<ArrayList<Double>> d0 = datasets.getDatasets().get(0);
		ArrayList<Integer> g0 = new ArrayList<Integer>(datasets.getGroundTruths().get(0));
		
		//statistics
		HashMap<Integer,Integer> frequency1 = new HashMap<Integer,Integer>();
		
		ExecutorService executor = Executors.newFixedThreadPool(POOLSIZE);
		ReentrantLock lock = new ReentrantLock();
		
		//bootstrap
		for(int i=0;i<BOOTSTRAPNUM;i++){
			int n = d0.size();
			ArrayList<ArrayList<Double>> trainSet = new ArrayList<ArrayList<Double>>();
			ArrayList<Integer> trainGt = new ArrayList<Integer>();
			ArrayList<ArrayList<Double>> validSet = new ArrayList<ArrayList<Double>>();
			ArrayList<Integer> validGt = new ArrayList<Integer>();
			
			HashSet<Integer> hs = new HashSet<Integer>();
			for(int j=0;j<n;j++){
				int idx = (int) (Math.random()*n);
				trainSet.add(d0.get(idx));
				trainGt.add(g0.get(idx));
				hs.add(idx);
			}
			for(int j=0;j<n;j++){
				if(!hs.contains(j)){
					validSet.add(d0.get(j));
					validGt.add(g0.get(j));
				}
			}
			
			ins = new RegNetsInstance(
					trainSet,
					trainGt,
					datasets,
					out);
//			System.out.println("Instance " + i + " training("+hs.size()+"/"+d0.size()+")...");
			ins.setInfo(hs.size()+"/"+d0.size());
			ins.setParameter(eta,lambda,regType);
			ins.setMaxIter(maxIter);
			ins.setLock(lock);
			ins.setTid(i);
			ins.setFreq(frequency1);
			executor.execute(ins);
			
//			ObjectWithValue[] ary = ins.getRecorder().ary;
//			for(int j=ary.length-1;j>ary.length-2-BOOTSTRAPTOPTHREASHOLD1;j--){
//				ObjectWithValue o = ary[j];
//				if(frequency1.containsKey(o.o)){
//					frequency1.put((Integer) o.o, frequency1.get(o.o)+1);
//				}else{
//					frequency1.put((Integer) o.o, 1);
//				}
//			}
//			System.out.println(" Done.");
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(1L, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ObjectWithValue[]  ary;
		int j;
		
		ary = new ObjectWithValue[frequency1.size()];
		j=0;
        for(Integer i:frequency1.keySet()){
        	ary[j++] = new ObjectWithValue(i,frequency1.get(i));
        }
        Arrays.sort(ary);
        for(int i= ary.length>300?ary.length-300:0;i<ary.length;i++){
        	ObjectWithValue o = ary[i];
        	out.printf(String.format("%10d:%5d:%25s\n",datasets.getHiddenNodeId((int)o.o),(int)o.value,datasets.getHiddenNodeContent((int)o.o)));
        }
	}
}