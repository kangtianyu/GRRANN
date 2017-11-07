import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
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

import javax.swing.JTextArea;

public class BootStrapTestLabels implements Runnable{

	private static final String[] REGTYPENAME = {"(standard)l2-l1", "l2-Null", "Null-l1", "l2-l2", "l1-l1"};
	private static final String[] EDGTYPENAME = {"(standard)All", "positive", "negative", "other"};
	
	//configuration
	private static final int BOOTSTRAPNUM = 100 ;
	private static final int BOOTSTRAPTOPTHREASHOLD1 = 10;
	private static final int POOLSIZE = 4;
	private String networkEnds;
	private String networkRels;
	private String trainFile1;
	private String trainFile2;
	private boolean rndEdg;
	private int edgType;
	private PrintStream out;
	private boolean useLogFile;

	public BootStrapTestLabels(String networkEnds, String networkRels, String trainFile1, String trainFile2,
			boolean rndEdg, int edgType, PrintStream out) {
		this.networkEnds = networkEnds;
		this.networkRels = networkRels;
		this.trainFile1 = trainFile1;
		this.trainFile2 = trainFile2;
		this.rndEdg = rndEdg;
		this.edgType = edgType;
		this.out = out;
		this.useLogFile = false;
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
		String networkEnds = "data/kb/string.ents";
		String networkRels = "data/kb/string.rels";
		
		String dataset1 = "data/UC/GSE12251/GSE12251_data.txt";
		String dataset2 = "data/UC/GSE14580/GSE14580_data.txt";
		String dataset3 = "data/Rejection/GSE21374/GSE21374_data.txt";
		String dataset4 = "data/Rejection/GSE50058/GSE50058_data.txt";
		
		class FileOutputStream extends OutputStream {
		    private String record;
		    
		    public FileOutputStream() {
		        this.record = "";
		    }
		    public void write( int b ) throws IOException {
		    	record += String.valueOf((char)b);
		    }
		    public String getRecord() {
		    	return record;
		    }
		}
		
		FileOutputStream buffer = new FileOutputStream();
			
		BootStrapTestLabels mainIns = new BootStrapTestLabels(
				networkEnds,
				networkRels,
				dataset1,
				dataset2,
				false,
				0,
				new PrintStream(buffer));
		mainIns.setUseLogFile(true);
		mainIns.run();
		
		mainIns = new BootStrapTestLabels(
				networkEnds,
				networkRels,
				dataset3,
				dataset4,
				false,
				0,
				new PrintStream(buffer));
		mainIns.setUseLogFile(true);
		mainIns.run();
		
		bw = new BufferedWriter(new FileWriter(logPath,true));
		bw.write(buffer.getRecord());
		bw.flush();
		bw.close();

	}
	
	public void setUseLogFile(boolean b) {
		this.useLogFile = b;
	}

	@Override
	public void run() {

		// Confirm input information
		out.println("******Input Information******\nTop-10 Bootstrap");
		out.println("Train File1:\t" + trainFile1);
		out.println("Train File2:\t" + trainFile2);
		out.println("Network File:\t" + networkEnds +" | " + networkRels);
		out.println("Random Edge:\t" + rndEdg);
		out.println("Edge Selected:\t" + EDGTYPENAME[edgType]);
		out.println("*****************************");
		
		// Read input data
		StdDataset datasets = new StdDataset(networkEnds,networkRels,rndEdg,edgType,out);
		datasets.readData(trainFile1,out);
		datasets.readData(trainFile2,out);
		datasets.standardizeData(out);
		
		int n=datasets.getHiddenLayerSize();
		for(int i=0;i<n;i++) {
			out.println(datasets.getHiddenNodeContent(i).name);
		}
		out.println();
	}
}
