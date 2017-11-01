import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.inference.TTest;

public class StdDataset {
	public static final int DIRECTION_BOTH = 0;
	public static final int DIRECTION_FORWARD = 1;
	public static final int DIRECTION_BACKWARD = 2;
	
	private static String mainDirectory = System.getProperty("user.dir");
	private static String dataDirectory = mainDirectory + "/../networkClassification/";
	
	private String networkEnds = "kb/string.ents";
	private String networkRels = "kb/string.rels";
	
	private ArrayList<String> datafiles = new ArrayList<String>();
	
	
	// Sub dataset number
	private static int subNmb;
	// A map of gene uid(mRNA) to gene id
	private static HashMap<Integer,Integer> uid2id;
	// A map of gene id to gene uid(mRNA) 
	private static HashMap<Integer,Integer> id2uid;
	// A map of gene uid(mRNA or other) to content
	private static HashMap<Integer, GeneContent> uid2content;
	// A network of genes. Use gene id to identify different genes. Strings contain the edge type. The links are in both directions.
	private static HashMap<Integer,HashMap<Integer,String>> geneNetwork;
	// A set of all gene ids will use in the network.
	private static ArrayList<Integer> labels;
	// A set of all hidden layer node will use in the network.
	private static HashMap<Integer, Integer> hidnodes;
	// Sub datasets
	private static ArrayList<ArrayList<ArrayList<Double>>> datasets;
	// Ground truths
	private static ArrayList<ArrayList<Integer>> groundTruths;
	// Labels of input data
	private static ArrayList<ArrayList<Integer>> rawLabels;
	// After standardize data, can't read data anymore.(For safety)
	private static boolean readEnd;
	// P value of T-Test
	private double[] tTestPVal;
	private HashMap<Integer, Integer> hide_node_content;
	// sorted gene network
	private static ArrayList<ArrayList<Integer>> network;
	
	public StdDataset(){
		this("kb/string.ents","kb/string.rels");
	}

	public StdDataset(String networkEnds, String networkRels){
		this(networkEnds,networkRels,false,0,System.out);
	}
	
	public StdDataset(String networkEnds, String networkRels, boolean rndEdg, int edgType, PrintStream out) {
		
		this.networkEnds = networkEnds;
		this.networkRels = networkRels;		
		subNmb = 0;
		uid2id = new HashMap<Integer,Integer>();
		id2uid = new HashMap<Integer,Integer>();
		hidnodes = new HashMap<Integer,Integer>();
		hide_node_content = new HashMap<Integer,Integer>();
		uid2content = new HashMap<Integer,GeneContent>();
		geneNetwork = new HashMap<Integer,HashMap<Integer,String>>();
		datasets = new ArrayList<ArrayList<ArrayList<Double>>>();
		groundTruths = new ArrayList<ArrayList<Integer>>();
		rawLabels = new ArrayList<ArrayList<Integer>>();
		readEnd = false;		
		
		File geneNode = new File(networkEnds);
		try {
			BufferedReader br = new BufferedReader(new FileReader(geneNode));
			String readLine  = br.readLine();
			while((readLine = br.readLine())!=null){
				if(!readLine.equals("")){
					String[] node = readLine.split("\t");
					String type = node[3].replace("\"", "");
					int uid = Integer.valueOf(node[0].replace("\"", ""));
					String id = node[2].replace("\"", "");
					if(!id.equals("-1") && !id.equals("") &&(!node[1].equals("NA"))){
						if(type.equals("mRNA")){
							// input layer node
							uid2id.put(uid, Integer.valueOf(id));
							id2uid.put(Integer.valueOf(id),uid);
							uid2content.put(uid,new GeneContent(node[1],node[2],node[3]));						
						}else{
							// hidden layer node
							uid2id.put(uid, Integer.valueOf(id));
							hidnodes.put(Integer.valueOf(id),uid);
							uid2content.put(uid,new GeneContent(node[1],node[2],node[3]));
						}
					}
				}
			}
			br.close();
		} catch (IOException e) {
			out.println("geneNode read error");
			e.printStackTrace();
		}
	
		File geneEdge = new File(networkRels);
		try {
			BufferedReader br = new BufferedReader(new FileReader(geneEdge));
			String readLine  = br.readLine();
			int eNum = 0;
			while((readLine = br.readLine())!=null){
				if(!readLine.equals("")){
					String[] data = readLine.split("\t");
					if(!fitCondition(data[3].replace("\"", ""),edgType)) continue;
					int srcuid = Integer.valueOf(data[1]);
					int trguid = Integer.valueOf(data[2]);
					if(uid2id.containsKey(srcuid) && uid2id.containsKey(trguid)){
						eNum++;
						int srcid = uid2id.get(Integer.valueOf(data[1]));
						int trgid = uid2id.get(Integer.valueOf(data[2]));
						if(!geneNetwork.containsKey(trgid)){
							geneNetwork.put(trgid, new HashMap<Integer,String>());
						}
						geneNetwork.get(trgid).put(srcid, data[3]);
					}
				}
			}
			out.println("eNum: " + eNum);
			br.close();
		} catch (IOException e) {
			System.out.println("geneEdge read error");
			e.printStackTrace();
		}
		
		if(rndEdg) randomNetwork();
	}

	public void readData(String fileName){		
		readData(fileName,System.out);
	}
	
	public void readData(String fileName, PrintStream out){		
		try {
			if(readEnd) throw new Exception("Can't read data after standardized data.");
			
			datafiles.add(fileName);
			
			File dataFile = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(dataFile));
			String readLine  = br.readLine();
		
			String[] lbs = readLine.split("\t");	
			ArrayList<Integer> tLabels = new ArrayList<Integer>();
			out.println(fileName + " raw data have "+lbs.length+" labels.");
			
			if(labels == null){
				labels = new ArrayList<Integer>();
				for(int i=1;i<lbs.length;i++){
					int id = Integer.valueOf(lbs[i]);
					tLabels.add(id);
					if(id2uid.containsKey(id) && !labels.contains(id)){
						labels.add(id);
					}
				}
			}else{
				ArrayList<Integer> tmplabels = new ArrayList<Integer>();
				for(int i=1;i<lbs.length;i++){
					int id = Integer.valueOf(lbs[i]);
					tLabels.add(id);
					if(labels.contains(id) && !tmplabels.contains(id)){
						tmplabels.add(id);
					}
				}
				labels = tmplabels;
			}

			ArrayList<ArrayList<Double>> trainData = new ArrayList<ArrayList<Double>>();
			ArrayList<Integer> groundTruth = new ArrayList<Integer>();
			while((readLine = br.readLine())!=null){
				if(!readLine.equals("")){
					String[] data = readLine.split("\t");
					int y = Integer.valueOf(data[0]);
					ArrayList<Double> x = new ArrayList<Double>();
					for(int i=0;i<lbs.length;i++){
						x.add(Double.valueOf(data[i]));
					}
					trainData.add(x);
					groundTruth.add(y);
				}
			}
			datasets.add(trainData);
			groundTruths.add(groundTruth);
			rawLabels.add(tLabels);
			
			br.close();
			subNmb++;
		} catch (Exception e) {
			out.println("data read error");
			e.printStackTrace();
		}
	}	
	public void standardizeData(){
		standardizeData(System.out);		
	}
	public void standardizeData(PrintStream out){
		
		readEnd = true;
		
		out.println("selected data have "+labels.size()+" labels.");
		
		tTestPVal = new double[labels.size()];
		ArrayList<ArrayList<Double>> t0 = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> t1 = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> zNorm = new ArrayList<ArrayList<Double>>();
		for(int i=0;i<labels.size();i++){
			t0.add(new ArrayList<Double>());
			t1.add(new ArrayList<Double>());
			zNorm.add(new ArrayList<Double>());
		}
		
		for(int datasetIdx = 0; datasetIdx < subNmb ; datasetIdx++){
			ArrayList<ArrayList<Double>> dataset = datasets.get(datasetIdx);
			ArrayList<Integer> groundTruth = groundTruths.get(datasetIdx);
			ArrayList<Integer> lbs = rawLabels.get(datasetIdx);
			ArrayList<ArrayList<Double>> newdataset = new ArrayList<ArrayList<Double>>();
			for(int rowNmb = 0; rowNmb < dataset.size(); rowNmb ++){
				ArrayList<Double> oldrow = dataset.get(rowNmb);
				ArrayList<Double> newRow = new ArrayList<Double>();
				for(int i=0;i<labels.size();i++){
					int idx = lbs.indexOf(labels.get(i));
					newRow.add(oldrow.get(idx));
					zNorm.get(i).add(oldrow.get(idx));
					if(groundTruth.get(rowNmb) == 0){
						t0.get(i).add(oldrow.get(idx));
					}else{
						t1.get(i).add(oldrow.get(idx));
					}
				}
				newdataset.add(newRow);
			}
			datasets.set(datasetIdx, newdataset);
		}
		
		double[] mean = new double[labels.size()];
		double[] sd = new double[labels.size()];
		for(int i=0;i<labels.size();i++){
			double[] column = Stream.of(zNorm.get(i).toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
			double sum = 0.0;
			double sqrsum = 0.0;
			int n = column.length;
			for(double d:column){
				sum += d;
				sqrsum += d*d;
			}
			mean[i] = sum/n;
			double variance = (sqrsum - 2 * sum * mean[i] + n * mean[i] * mean[i]) / (n - 1);
			sd[i] = Math.sqrt(variance);
			
		}		

		// normalize
		for(int datasetIdx = 0; datasetIdx < subNmb ; datasetIdx++){
			ArrayList<ArrayList<Double>> dataset = datasets.get(datasetIdx);
			for(int j=0;j<dataset.size();j++){
				ArrayList<Double> row = dataset.get(j);
				for(int i=0;i<row.size();i++){
					row.set(i, (row.get(i)-mean[i])/sd[i]);
				}
			}
		}
		
		// p value
		for(int i=0;i<tTestPVal.length;i++){
			TTest tTest = new TTest();
			double[] sample1 = Stream.of(t0.get(i).toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
			double[] sample2 = Stream.of(t1.get(i).toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
			tTestPVal[i] = tTest.tTest(sample1, sample2);
		}
		
		// sort network edges by p value
		int count = 0;
		network = new ArrayList<ArrayList<Integer>>();
		HashMap<Integer, Integer> hide_node_idx = new HashMap<Integer,Integer>();
		int newIdx = 0;
		for(int i=0;i<labels.size();i++){
			ArrayList<Integer> nodeConnection = new ArrayList<Integer>();
//			ArrayList<ObjectWithValue> tmp = new ArrayList<ObjectWithValue>();
//			nodeConnection.add(i);
			int idx = id2uid.get(Integer.valueOf(labels.get(i)));
			if(geneNetwork.containsKey(idx)){
				for(int j:geneNetwork.get(idx).keySet()){
					int jidx;
					//				if(labels.contains(j)){
	//					tmp.add(new ObjectWithValue(labels.indexOf(j),tTestPVal[labels.indexOf(j)]));
	//				}
					if(!hide_node_idx.containsKey(j)) {
						jidx = newIdx;
						hide_node_idx.put(j, jidx);
						hide_node_content.put(jidx,j);
						newIdx++;
					}else {
						jidx = hide_node_idx.get(j);
					}
					nodeConnection.add(jidx);
					count++;
				}
			}
//			Collections.sort(tmp);
//			for(ObjectWithValue obj:tmp){
//				nodeConnection.add((Integer) obj.o);
//			}
			network.add(nodeConnection);
		}		
		out.println("Edge number: " + count);
		
		out.println("standardize end");
	}

	public String getLabelsName(int k) {
		return uid2content.get(id2uid.get(labels.get(k))).name.replaceAll("\"", "");
	}

	public ArrayList<ArrayList<ArrayList<Double>>> getDatasets() {
		return datasets;
	}

	public ArrayList<ArrayList<Integer>> getGroundTruths() {
		return groundTruths;
	}

	public ArrayList<ArrayList<Integer>> getNetwork() {
		return network;
	}
	
	public int getHiddenLayerSize(){
		return hidnodes.size();
	}
	
	public int getInputLayerSize(){
		return labels.size();
	}
	
	public GeneContent getHiddenNodeContent(int idx){
		return uid2content.get(hidnodes.get(hide_node_content.get(idx)));
	}
	
	public int getHiddenNodeId(int idx) {
		return hide_node_content.get(idx);
	}
	
	public static void randomNetwork(){
		ArrayList<ArrayList<Integer>> newNetwork = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<labels.size();i++){
			newNetwork.add(new ArrayList<Integer>());
		}
		Random rnd = new Random();
		int from,to;
		for(int i=0;i<network.size();i++){
			from = rnd.nextInt(labels.size());
			to = rnd.nextInt(labels.size());
			newNetwork.get(from).add(to);
		}		
		network = newNetwork;
	}
	
	private boolean fitCondition(String str, int edgType) {
		if(edgType == 0) return true;
		if(edgType == 1 && str.equals("increase")) return true;
		if(edgType == 2 && str.equals("decrease")) return true;
		if(edgType == 3 && (str.equals("increase") || str.equals("decrease"))) return false;		
		return false;
	}

	public String showInfo(){
		String out = "******Input Information******\n" + "Network File:\t" + networkEnds +" | " + networkRels+"\nData file(s)\n";
		for(String s:datafiles){
			out += s + "\n"; 
		}
		out += "*****************************\n";
		
		return out;
				
	}
}
