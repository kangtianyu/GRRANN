import java.util.ArrayList;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.core.input.WeightedSum;
import org.neuroph.core.transfer.Linear;
import org.neuroph.nnet.comp.neuron.BiasNeuron;
import org.neuroph.nnet.comp.neuron.InputNeuron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.LayerFactory;
import org.neuroph.util.NeuralNetworkFactory;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;

public class RegNets extends NeuralNetwork<BackPropagation> {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -6502617352175930702L;
	public static final NeuronProperties DEFAULT_FULL_CONNECTED_NEURON_PROPERTIES = new NeuronProperties();
    static {
        DEFAULT_FULL_CONNECTED_NEURON_PROPERTIES.setProperty("useBias", true);
        DEFAULT_FULL_CONNECTED_NEURON_PROPERTIES.setProperty("transferFunction", TransferFunctionType.SIGMOID);
        DEFAULT_FULL_CONNECTED_NEURON_PROPERTIES.setProperty("inputFunction", WeightedSum.class);
    }
    
	private StdDataset stdDataset;
	private boolean useBias;

	public RegNets(StdDataset stdDataset) {
		this.stdDataset = stdDataset;
		
//        // init neuron settings
//        NeuronProperties neuronProperties = new NeuronProperties();
//        neuronProperties.setProperty("useBias", true);
//        neuronProperties.setProperty("transferFunction", TransferFunctionType.SIGMOID);

        this.createNetwork(stdDataset, DEFAULT_FULL_CONNECTED_NEURON_PROPERTIES);
	}

	private void createNetwork(StdDataset stdDataset,NeuronProperties neuronProperties) {
        // set network type
        this.setNetworkType(NeuralNetworkType.MULTI_LAYER_PERCEPTRON);
        this.stdDataset = stdDataset;

        // create input layer
        NeuronProperties inputNeuronProperties = new NeuronProperties(InputNeuron.class, Linear.class);
        Layer inputLayer = LayerFactory.createLayer(stdDataset.getInputLayerSize(), inputNeuronProperties);

        useBias = true; // use bias neurons by default
        if (neuronProperties.hasProperty("useBias")) {
            useBias = (Boolean) neuronProperties.getProperty("useBias");
        }

        if (useBias) {
        	inputLayer.addNeuron(new BiasNeuron());
        }

        this.addLayer(inputLayer);

        // create hidden layers
        Layer hiddenLayer = LayerFactory.createLayer(stdDataset.getHiddenLayerSize(), neuronProperties);
        hiddenLayer.addNeuron(new BiasNeuron());
        this.addLayer(hiddenLayer);
        
        
        //create output layer
        Layer outputLayer = LayerFactory.createLayer(1, neuronProperties);
        this.addLayer(outputLayer);

        //create connections
        geneNetConnection(inputLayer, hiddenLayer);
        ConnectionFactory.fullConnect(hiddenLayer, outputLayer);
        


        // set input and output cells for network
        NeuralNetworkFactory.setDefaultIO(this);

//        this.randomizeWeights(new RangeRandomizer(0.2, 0.5));

		
	}
	
	private void geneNetConnection(Layer fromLayer, Layer toLayer){
		Weight weight;
		ArrayList<ArrayList<Integer>> geneNetwork = stdDataset.getNetwork();
        for(int x = 0;x < geneNetwork.size();x++) {
        	Neuron fromNeuron = fromLayer.getNeuronAt(x);
        	for(int kx =0; kx < geneNetwork.get(x).size(); kx++){
        		Neuron toNeuron = toLayer.getNeuronAt(geneNetwork.get(x).get(kx));
        		weight = new Weight();
        		weight.randomize(0.2d,0.5d);
        		ConnectionFactory.createConnection(fromNeuron, toNeuron, weight);
        	}
        }
        
        //bias node
        if (useBias) {
        	Neuron fromNeuron = fromLayer.getNeuronAt(geneNetwork.size());
        	for(int x = 0;x < stdDataset.getHiddenLayerSize();x++) {
        		Neuron toNeuron = toLayer.getNeuronAt(x);
        		weight = new Weight();
        		weight.randomize(0.2d,0.5d);
        		ConnectionFactory.createConnection(fromNeuron, toNeuron, weight);
        	}
        }
	}

}
