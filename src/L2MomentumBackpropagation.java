

import org.neuroph.core.Connection;
import org.neuroph.core.Layer;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.learning.BackPropagation;

public class L2MomentumBackpropagation extends BackPropagation {
	

    /**
     * The class fingerprint that is set to indicate serialization compatibility
     * with a previous version of the class.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Momentum factor
     */
//    protected double momentum = 0.0d;
    protected double momentum = 0.25d;
	private double lambda;
	private double alpha;
	private String reg;
	private double outl1norm;
	private double outl2norm;
	private double intl1norm;
	private double intl2norm;

	public L2MomentumBackpropagation(double lambda, double alpha){
		super();	
		this.lambda = lambda;
		this.reg = "normal";
		this.alpha = alpha;
	}

    public L2MomentumBackpropagation(double lambda, String reg) {
		super();	
		this.lambda = lambda;
		this.reg = reg;
		this.alpha = 0.5;
	}

	/**
     * This method implements weights update procedure for the single neuron for
     * the back propagation with momentum factor
     *
     * @param neuron neuron to update weights
     */
	@Override
    public void updateNeuronWeights(Neuron neuron) {
        
        for (Connection connection : neuron.getInputConnections()) {
            double input = connection.getInput();
            if (input == 0) {
                continue;
            }

            // get the error for specified neuron,
            double neuronError = neuron.getError();

            // tanh can be used to minimise the impact of big error values, which can cause network instability
            // suggested at https://sourceforge.net/tracker/?func=detail&atid=1107579&aid=3130561&group_id=238532
            // double neuronError = Math.tanh(neuron.getError());

            Weight weight = connection.getWeight();
            MomentumWeightTrainingData weightTrainingData = (MomentumWeightTrainingData) weight.getTrainingData();

            //double currentWeightValue = weight.getValue();
            double previousWeightValue = weightTrainingData.previousValue;
//            double weightChange = this.learningRate * (neuronError + alpha * l2 )* input
//                    + momentum * (weight.value - previousWeightValue);
            double weightChange = this.learningRate * neuronError * input
                    + momentum * (weight.value - previousWeightValue);
    		if(reg.equals("normal")) {

                weightChange += lambda * (1 -alpha) * weight.value / intl2norm;
    		}else if(reg.equals("l1")){

                weightChange += lambda * (1 -alpha) * weight.value / intl1norm;			
    		}else if(reg.equals("l2")){
                weightChange += lambda * (1 -alpha) * weight.value / intl2norm;			
    		}else {
    			System.out.println("Error: Wrong Regularization Setting");
                weightChange += 0;
    		}
            // save previous weight value
            //weight.getTrainingData().set(TrainingData.PREVIOUS_WEIGHT, currentWeightValue);
            weightTrainingData.previousValue = weight.value;


            // if the learning is in batch mode apply the weight change immediately
            if (this.isInBatchMode() == false) {
                weight.weightChange = weightChange;
                weight.value += weightChange;
            } else { // otherwise, sum the weight changes and apply them after at the end of epoch
                weight.weightChange += weightChange;
            }
        }
    }

    /**
     * Returns the momentum factor
     *
     * @return momentum factor
     */
    public double getMomentum() {
        return momentum;
    }

    /**
     * Sets the momentum factor
     *
     * @param momentum momentum factor
     */
    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    public static class MomentumWeightTrainingData {

        public double previousValue;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // create MomentumWeightTrainingData objects that will be used during the training to store previous weight value
        for (Layer layer : this.neuralNetwork.getLayers()) {
            for (Neuron neuron : layer.getNeurons()) {
                for (Connection connection : neuron.getInputConnections()) {
                	connection.getWeight().setTrainingData(new MomentumWeightTrainingData());
                }
            } // for
        } // for        
    }
    
    @Override
    protected void beforeEpoch() {
    	super.beforeEpoch();
    	this.outl1norm = 0.0;
    	this.outl2norm = 0.0;
    	this.intl1norm = 0.0;
    	this.intl2norm = 0.0;
//    	// compute norms
//    	for (Layer layer : this.neuralNetwork.getLayers()) {
//            for (Neuron neuron : layer.getNeurons()) {
//            	double groupl2norm = 0.0;
//                for (Connection connection : neuron.getInputConnections()) {
//                	l1norm += Math.abs(connection.getWeight().value);
//                	l2norm += connection.getWeight().value * connection.getWeight().value;
//                	groupl2norm += connection.getWeight().value * connection.getWeight().value;
//                }
//                l12norm += Math.sqrt(groupl2norm);
//            } // for
//        } // for 
//    	l2norm = Math.sqrt(l2norm);
    	
    	for (Neuron neuron : neuralNetwork.getOutputNeurons()) {
    		for (Connection connection : neuron.getInputConnections()) {
    			outl1norm += Math.sqrt(connection.getFromNeuron().getInputConnections().length) * Math.abs(connection.getWeight().value);
    			outl2norm += Math.sqrt(connection.getFromNeuron().getInputConnections().length) * connection.getWeight().value* connection.getWeight().value;
            }
    		outl2norm = Math.sqrt(outl2norm);
    	}
        Layer[] layers = neuralNetwork.getLayers();
    	for (int layerIdx = layers.length - 2; layerIdx > 0; layerIdx--) {
            for (Neuron neuron : layers[layerIdx].getNeurons()) {
            	double groupl2norm = 0.0;
            	for (Connection connection : neuron.getInputConnections()) {
            		intl1norm += Math.abs(connection.getWeight().value);
            		groupl2norm += connection.getWeight().value * connection.getWeight().value;
//            		groupl2norm += connection.getWeight().value * connection.getWeight().value;
            	}
            	intl2norm += Math.sqrt(groupl2norm);
            } // for
        } // for
//    	intl2norm = Math.sqrt(intl2norm);
    	
    }
    

//    @Override
//    protected void afterEpoch(){
//    	super.afterEpoch();
//    	for (Neuron neuron : neuralNetwork.getOutputNeurons()) {
//    		for (Connection connection : neuron.getInputConnections()) {
//    			if(connection.getWeight().value<0){
//    				connection.getWeight().setValue(-connection.getWeight().value);
//    				for (Connection connection2 : connection.getFromNeuron().getInputConnections()) {
//    					connection2.getWeight().setValue(-connection2.getWeight().value);
//    				}
//    			}
//            }
//    	}
//    }
    
    @Override
    protected void calculateErrorAndUpdateOutputNeurons(double[] outputError) {
        int i = 0;
        // for all output neurons
        for (Neuron neuron : neuralNetwork.getOutputNeurons()) {
            // if error is zero, just set zero error and continue to next neuron
            if (outputError[i] == 0) {
                neuron.setError(0);
                i++;
                continue;
            }

            // otherwise calculate and set error/delta for the current neuron
            TransferFunction transferFunction = neuron.getTransferFunction();
            double neuronInput = neuron.getNetInput();
            double delta = outputError[i] * transferFunction.getDerivative(neuronInput); // delta = (d-y)*df(net)
            neuron.setError(delta);

            // and update weights of the current neuron
            this.updateOutputNeuronWeights(neuron);
            i++;
        } // for
    }

	private void updateOutputNeuronWeights(Neuron neuron) {
		for (Connection connection : neuron.getInputConnections()) {
            double input = connection.getInput();
            if (input == 0) {
                continue;
            }

            // get the error for specified neuron,
            double neuronError = neuron.getError();

            // tanh can be used to minimise the impact of big error values, which can cause network instability
            // suggested at https://sourceforge.net/tracker/?func=detail&atid=1107579&aid=3130561&group_id=238532
            // double neuronError = Math.tanh(neuron.getError());

            Weight weight = connection.getWeight();
            MomentumWeightTrainingData weightTrainingData = (MomentumWeightTrainingData) weight.getTrainingData();

            //double currentWeightValue = weight.getValue();
            double previousWeightValue = weightTrainingData.previousValue;
            double weightChange = this.learningRate * neuronError * input
                    + momentum * (weight.value - previousWeightValue);
            if(Math.abs(weight.value)>0){
              weightChange += lambda * alpha * Math.sqrt(connection.getFromNeuron().getInputConnections().length) * weight.value / Math.abs(weight.value);
            }
            // save previous weight value
            //weight.getTrainingData().set(TrainingData.PREVIOUS_WEIGHT, currentWeightValue);
            weightTrainingData.previousValue = weight.value;


            // if the learning is in batch mode apply the weight change immediately
            if (this.isInBatchMode() == false) {
                weight.weightChange = weightChange;
                weight.value += weightChange;
            } else { // otherwise, sum the weight changes and apply them after at the end of epoch
                weight.weightChange += weightChange;
            }
        }
		
	}
	
	@Override
	public double getTotalNetworkError() {
		if(reg.equals("normal")) {
			return this.getErrorFunction().getTotalError()+lambda * alpha * outl1norm + lambda * (1- alpha) * intl2norm;
		}else if(reg.equals("l1")){
			return this.getErrorFunction().getTotalError()+lambda * alpha * outl1norm + lambda * (1- alpha) * intl1norm;			
		}else if(reg.equals("l2")){
			return this.getErrorFunction().getTotalError()+lambda * alpha * outl2norm + lambda * (1- alpha) * intl2norm;			
		}else {
			System.out.println("Error: Wrong Regularization Setting");
			return 0;
		}
    }
}
