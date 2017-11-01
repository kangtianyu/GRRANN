import java.util.ArrayList;

class Recorder {
		public int count = 0;
		public boolean end = false;
		public boolean hideIterMsg = false;
		public String output = "";
		public double error;
		public ArrayList<Double> l2;
		public ArrayList<Integer> predict;
		public double sensitivity;
		public double specificity;
		public ArrayList<Double> cutoff;
		public ArrayList<Double> tpr;
		public ArrayList<Double> fpr;
		public double lambda;
		public double alpha;
		public ArrayList<Double> outputs;
		public ObjectWithValue[] ary;
		public double useCutoff;
		
		public Recorder() {
			this(0);
		}
		
		public Recorder(int count) {
			super();
			this.count = count;
			cutoff = new ArrayList<Double>();
			tpr = new ArrayList<Double>();
			fpr = new ArrayList<Double>();
			l2 = new ArrayList<Double>();
		}
		
		public void println(String s){
			output += s+"\n";
		}
		
		public double getAUC(){
			double ttpr = tpr.get(0);
			double tfpr = fpr.get(0);
			double auc = 0;
			for(int i=1;i<tpr.size();i++){
				if(fpr.get(i)<tfpr){
					auc += (tfpr - fpr.get(i)) * (ttpr + tpr.get(i)) / 2;
					ttpr = tpr.get(i);
					tfpr = fpr.get(i);
				}
			}
			return auc;
		}
		
		public double getOutputProduct(){
			double result = 1;
			for(double d:outputs) result *= d;
			return result;
		}
		
		public String AUCOutput(){
			String s="";
			double ttpr = tpr.get(0);
			double tfpr = fpr.get(0);
			for(int i=1;i<tpr.size();i++){
				if(fpr.get(i)<tfpr || tpr.get(i)<ttpr){
					s += String.format("{\"X\":\"%.3f\",\"ROC\":\"%.3f\"},\n", tfpr,ttpr);
					ttpr = tpr.get(i);
					tfpr = fpr.get(i);
				}
			}
			s += String.format("{\"X\":\"%.3f\",\"ROC\":\"%.3f\"}", tfpr,ttpr);
			s = "[" + s + "]";
			return s;
		}
		
		public String AUCOutput_short(){
			String s="";
			double ttpr = tpr.get(0);
			double tfpr = fpr.get(0);
			for(int i=1;i<tpr.size();i++){
				if(fpr.get(i)<tfpr || tpr.get(i)<ttpr){
					s += String.format("{\"X\":\"%.3f\",\"ROC\":\"%.3f\"},\n", tfpr,ttpr);
					ttpr = tpr.get(i);
					tfpr = fpr.get(i);
				}
			}
			s += String.format("{\"X\":\"%.3f\",\"ROC\":\"%.3f\"}", tfpr,ttpr);
			s = "[" + s + "]";
			return s;
		}

	}