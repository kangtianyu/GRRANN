import java.io.PrintStream;

import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.nnet.learning.BackPropagation;

public class LearningListener implements LearningEventListener {

    long start = System.currentTimeMillis();
    int t_id;
	private Recorder re;
	private PrintStream out;
    
    public LearningListener(int t_id, Recorder re, PrintStream out) {
		super();
		this.t_id = t_id;
		this.re = re;
		this.out = out;
	}

	@Override
    public void handleLearningEvent(LearningEvent event) {
        BackPropagation bp = (BackPropagation) event.getSource();
        if(re!=null){
        	re.count = bp.getCurrentIteration();
        	re.error = bp.getTotalNetworkError();
        	if(re.hideIterMsg){
//        		re.output+= String.format("Iter:%6d|Error:%10.7f%n",bp.getCurrentIteration(),bp.getTotalNetworkError());
        	}else{
        		int itr = bp.getCurrentIteration();
        		if(itr<100 || itr%100 == 0) {
	                out.println("Current iteration: " + bp.getCurrentIteration());
	                out.println("Error: " + bp.getTotalNetworkError());      
        		}
        	}
        }else{
        	out.print("Itr: " + bp.getCurrentIteration());    
        	out.println("|Error: " + bp.getTotalNetworkError());
        }
        start = System.currentTimeMillis();
    }
}
