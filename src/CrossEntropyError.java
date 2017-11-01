import java.io.Serializable;

import org.neuroph.core.learning.error.ErrorFunction;

public class CrossEntropyError implements ErrorFunction, Serializable {

    private transient double totalError;
    private transient double patternCount;
    
	public CrossEntropyError() {
		reset();
	}

	@Override
	public double getTotalError() {
		return totalError/patternCount;
	}

	@Override
	public void reset() {
		totalError = 0d;
		patternCount = 0;
	}

	@Override
	public double[] calculatePatternError(double[] predictedOutput, double[] targetOutput) {
        double[] patternError = new double[targetOutput.length];

        for (int i = 0; i < predictedOutput.length; i++) {
        	if(predictedOutput[i] == targetOutput[i]){
        		patternError[i] = 0;
        	}else if(predictedOutput[i] == 0){
        		patternError[i] =  -1000000;
        	}else if(predictedOutput[i] == 1){
        		patternError[i] =  1000000;
        	}else{
        		patternError[i] =  -(targetOutput[i] - predictedOutput[i])/(predictedOutput[i] * (1-predictedOutput[i]));
        	}
            totalError += -(targetOutput[i] * Math.log(predictedOutput[i]) + (1-targetOutput[i]) * Math.log(1-predictedOutput[i]));
        }
        patternCount++;
        return patternError;
	}

}
