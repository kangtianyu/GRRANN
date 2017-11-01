import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GRRANN {

	private static String dataDirectory;
	private static String saveDirectory;
	private static String logName;
	private static String entFile;
	private static String relFile;
	private static String trainFile;
	private static String testFile;
	private static int numOfVote;
	private static double lambda;
	private static double eta;
	private static int maxIter;
	private static boolean rndEdg;
	private static int regType;
	private static int edgType;
	private static int testType;
	
	public GRRANN() {

		dataDirectory = System.getProperty("user.dir") + "/";
		saveDirectory = System.getProperty("user.dir") + "/log/";
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String ctime = sdf.format(cal.getTime());
		logName = ctime + ".log";

		entFile = null;
		relFile = null;
		trainFile = null;
		testFile = null;
		numOfVote = 5;
		lambda = 0.00001;
		eta = 0.005;
		maxIter = 2000;
		rndEdg = false;
		regType = 0;
		edgType = 0;
		testType = -1;
	}

	public static void main(String[] args) {
		
		new  GRRANN();
		
		for(int i=0;i<args.length;i++) {
			switch(args[i]) {
			case "-ent":
				entFile = args[i+1];
				i++;
				break;
			case "-rel":
				relFile = args[i+1];
				i++;
				break;
			case "-train":
				trainFile = args[i+1];
				i++;
				break;
			case "-test":
				testFile = args[i+1];
				i++;
				break;
			case "-nov":
				numOfVote = new Integer(args[i+1]);
				i++;
				break;
			case "-lamb":
				lambda = new Double(args[i+1]);
				i++;
				break;
			case "-eta":
				eta =  new Double(args[i+1]);
				i++;
				break;
			case "-mitr":
				maxIter = new Integer(args[i+1]);
				i++;
				break;
			case "-rnde":
				rndEdg = new Boolean(args[i+1]);
				i++;
				break;
			case "-regt":
				regType = new Integer(args[i+1]);
				i++;
				break;
			case "-edgt":
				edgType = new Integer(args[i+1]);
				i++;
				break;
			case "-save":
				i++;
				logName = args[i+1];
				break;
			case "-c":
				testType = 0;
				break;
			case "-t":
				testType = 1;
				break;
			case "-b":
				testType = 2;
				break;
			default:
				System.out.println("Unknown Command " + args[i]);	
			}
		}
		
		boolean canRun = true;
		switch(testType) {
		case 0:
			if(entFile == null) {
				System.out.println("Need Network Nodes File");
				canRun = false;
			}
			if(relFile == null) {
				System.out.println("Need Network Relations File");
				canRun = false;
			}
			if(trainFile == null) {
				System.out.println("Need Network Train File");
				canRun = false;
			}
			if(canRun) {
				new Thread(
						new CrossValidationTest(
								entFile,
								relFile,
								trainFile,
								lambda,
								eta,
								maxIter,
								rndEdg,
								regType,
								edgType,
								numOfVote,
								System.out)
				).start();
			}
			break;
		case 1:
			if(entFile == null) {
				System.out.println("Need Network Nodes File");
				canRun = false;
			}
			if(relFile == null) {
				System.out.println("Need Network Relations File");
				canRun = false;
			}
			if(trainFile == null) {
				System.out.println("Need Network Train File");
				canRun = false;
			}
			if(trainFile == null) {
				System.out.println("Need Network Test File");
				canRun = false;
			}
			new Thread(
					new IndependentTest(
							entFile,
							relFile,
							trainFile,
							testFile,
							lambda,
							eta,
							maxIter,
							rndEdg,
							regType,
							edgType,
							System.out)
			).start();	
			break;
		case 2:
			if(entFile == null) {
				System.out.println("Need Network Nodes File");
				canRun = false;
			}
			if(relFile == null) {
				System.out.println("Need Network Relations File");
				canRun = false;
			}
			if(trainFile == null) {
				System.out.println("Need Network Train File");
				canRun = false;
			}
			new Thread(
					new BootStrapTest(
							entFile,
							relFile,
							trainFile,
							lambda,
							eta,
							maxIter,
							rndEdg,
							regType,
							edgType,
							System.out)
			).start();
			break;
		default:
			System.out.println("Unknown Test Type");
		}

	}

}
