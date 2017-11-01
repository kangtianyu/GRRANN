import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;

public class GUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JFileChooser fc;
	private static String dataDirectory;
	private static String saveDirectory;
	private static String saveFile;
	private static JFrame gui;

	private static JRadioButton cvButton;
	private static JRadioButton ttButton;
	private static JRadioButton bsButton;
	private static JTextField novt;
	
	private static JTextField ftext;
	private static JTextField rtext;
	private static JTextField trtext;
	private static JTextField tetext;
	
	private static JTextField lambtext;
	private static JTextField lerntext;
	private static JTextField maxItrtext;
	private static JCheckBox reButton;
	private static JComboBox<String> regSell;
	private static JComboBox<String> edgSell;
	
	private static JTextArea logbox;
	private static JScrollPane logboxp;

	private GUI() throws HeadlessException {
		this.setTitle("GRRANN GUI");
		this.setSize(1024, 768);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dataDirectory = System.getProperty("user.dir") + "/";
		saveDirectory = System.getProperty("user.dir") + "/log";
		saveFile = null;
		
		fc = new JFileChooser();
		ActionListener selectLis = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String active = ((JButton)e.getSource()).getName();
				switch(active) {
				case "fButton":
					seletcFile(ftext);
					break;
				case "rButton":
					seletcFile(rtext);
					break;
				case "trButton":
					seletcFile(trtext);
					break;
				case "teButton":
					seletcFile(tetext);
					break;
				case "saveLog":
					saveLog();
					break;
				default:
					System.out.println("Not Defined " + e.getSource());
				}
			}			
		};
		
		this.setLayout(new BorderLayout());
		
		JPanel topp = new JPanel();
		JPanel leftp = new JPanel();
		JPanel centerp = new JPanel();
		JPanel rightp = new JPanel();
		JPanel bottomp = new JPanel();

		// left
		cvButton = new JRadioButton("Cross Validation");
		ttButton = new JRadioButton("Train-Test");
		bsButton = new JRadioButton("Top-10 Bootstrap");
		cvButton.setSelected(true);
		cvButton.setFocusable(false);
		ttButton.setFocusable(false);
		bsButton.setFocusable(false);
		JPanel novp = new JPanel();
		novp.setLayout(new BorderLayout());
		JLabel novl = new JLabel("        # of Votes: ");
		novt = new JTextField("5");
		novp.add(novl, BorderLayout.WEST);
		novp.add(novt, BorderLayout.CENTER);
		JButton dSetButton = new JButton("Default Setting");
		dSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUI.setDefault();				
			}
			
		});
		
		ButtonGroup selectTest = new ButtonGroup();
		selectTest.add(cvButton);
		selectTest.add(ttButton);
		selectTest.add(bsButton);
		
		leftp.setLayout(new SpringLayout());
		leftp.add(cvButton);
		leftp.add(novp);
		leftp.add(ttButton);
		leftp.add(bsButton);
		leftp.add(new JSeparator(SwingConstants.HORIZONTAL));
		leftp.add(dSetButton);

		SpringUtilities.makeCompactGrid(leftp, //parent
                6, 1,
                5, 5,  //initX, initY
                5, 5); //xPad, yPad
		
		// center
		JPanel mainp = new JPanel();
		mainp.setLayout(new BorderLayout());
		
		centerp.setLayout(new SpringLayout());
		centerp.add(new JLabel("Node File:"));
		ftext = new JTextField("data/kb/string.ents");
		JButton fButton = new JButton("Select");
		fButton.setName("fButton");
		fButton.addActionListener(selectLis);
		centerp.add(ftext);
		centerp.add(fButton);
		centerp.add(new JLabel("Relation File:"));
		rtext = new JTextField("data/kb/string.rels");
		JButton rButton = new JButton("Select");
		rButton.setName("rButton");
		rButton.addActionListener(selectLis);
		centerp.add(rtext);
		centerp.add(rButton);
		centerp.add(new JLabel("Train File:"));
		trtext = new JTextField("data/UC/GSE12251/GSE12251_data.txt");
		JButton trButton = new JButton("Select");
		trButton.setName("trButton");
		trButton.addActionListener(selectLis);
		centerp.add(trtext);
		centerp.add(trButton);
		centerp.add(new JLabel("Test File:"));
		tetext = new JTextField("data/UC/GSE14580/GSE14580_data.txt");
		JButton teButton = new JButton("Select");
		teButton.setName("teButton");
		teButton.addActionListener(selectLis);
		centerp.add(tetext);
		centerp.add(teButton);

		SpringUtilities.makeCompactGrid(centerp, //parent
                4, 3,
                5, 5,  //initX, initY
                5, 5); //xPad, yPad
		
		mainp.add(centerp,BorderLayout.CENTER);
		JButton runButton = new JButton("Start");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUI.startTrain();
			}			
		});
		mainp.add(runButton,BorderLayout.SOUTH);
		
		// right
		rightp.setLayout(new SpringLayout());
		rightp.add(new JLabel("Lambda λ:"));
		lambtext = new JTextField("0.00001");
		rightp.add(lambtext);
		rightp.add(new JLabel("Learning Rate:"));
		lerntext = new JTextField("0.005");
		rightp.add(lerntext);
		rightp.add(new JLabel("Max Iteration:"));
		maxItrtext = new JTextField("2000");
		rightp.add(maxItrtext);
		rightp.add(new JLabel("Rnd. Edge:"));
		reButton = new JCheckBox();
		rightp.add(reButton);
		rightp.add(new JLabel("Regularization:"));
		String regSel[] = {"(standard)l2-l1", "l2-Null", "Null-l1", "l2-l2", "l1-l1"};
		regSell = new JComboBox<String>(regSel);
		rightp.add(regSell);
		rightp.add(new JLabel("Use specific Edges:"));
		String edgSel[] = {"(standard)All", "positive", "negative", "other"};
		edgSell = new JComboBox<String>(edgSel);
		rightp.add(edgSell);
		
		SpringUtilities.makeCompactGrid(rightp, //parent
                6, 2,
                5, 5,  //initX, initY
                5, 5); //xPad, yPad
		
		// bottom
		TitledBorder bottompTitle;
		bottompTitle = BorderFactory.createTitledBorder("Log");
		bottomp.setBorder(bottompTitle);
		bottomp.setLayout(new BorderLayout());
		logbox = new JTextArea();
		logbox.setEditable(false);
		((DefaultCaret) logbox.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		logboxp = new JScrollPane(logbox);
		bottomp.add(logboxp,BorderLayout.CENTER);
		JButton saveLog = new JButton("Save Log");
		saveLog.setName("saveLog");
		saveLog.addActionListener(selectLis);
		bottomp.add(saveLog,BorderLayout.SOUTH);
		
		
		// add panels
		
		topp.setLayout(new BorderLayout());
		topp.add(leftp, BorderLayout.WEST);
		topp.add(mainp, BorderLayout.CENTER);
		topp.add(rightp, BorderLayout.EAST);
		this.add(topp, BorderLayout.NORTH);
		this.add(bottomp, BorderLayout.CENTER);
		
		this.setVisible(true);
	}

	public static void seletcFile(JTextField jtf) {
		if(new File(jtf.getText()).isAbsolute()) {
			fc.setSelectedFile(new File(jtf.getText()));
		}else{
			fc.setSelectedFile(new File(dataDirectory + jtf.getText()));
		}
		int returnVal = fc.showOpenDialog(GUI.getInstance());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File base = new File(dataDirectory);
			File file = new File(fc.getSelectedFile().getPath());
			String rel = base.toURI().relativize(file.toURI()).getPath();
			jtf.setText(rel);
		}
	}
	
	public static void saveLog() {
		fc.setCurrentDirectory(new File(saveDirectory));
		if(saveFile != null) fc.setSelectedFile(new File(saveFile));
		int returnVal = fc.showSaveDialog(GUI.getInstance());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			saveDirectory = fc.getCurrentDirectory().getPath();
			BufferedWriter bw;
			try {
				String path = fc.getSelectedFile().getPath();
				if(!path.substring(path.length()-4).equals(".log")) {
					path = path + ".log";
					fc.setSelectedFile(new File(path));
				}
				saveFile = path;
				bw = new BufferedWriter(new FileWriter(path));
				bw.write(logbox.getText());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static JFrame getInstance() {
		if(gui == null) {
			gui = new GUI();
			return gui;
		}else {
			return gui;
		}
	}

	private static void setDefault() {
		ftext.setText("data/kb/string.ents");
		rtext.setText("data/kb/string.rels");
		trtext.setText("data/UC/GSE12251/GSE12251_data.txt");
		tetext.setText("data/UC/GSE14580/GSE14580_data.txt");
		lambtext.setText("0.00001");
		lerntext.setText("0.005");
		maxItrtext.setText("2000");
		reButton.setSelected(false);
		regSell.setSelectedIndex(0);
		edgSell.setSelectedIndex(0);
	}
	
	protected static void startTrain() {
		String entFile = ftext.getText();
		String relFile = rtext.getText();
		String trainFile = trtext.getText();
		String testFile = tetext.getText();
		int numOfVote = new Integer(novt.getText());
		double lambda = new Double(lambtext.getText());
		double eta = new Double(lerntext.getText());
		int maxIter = new Integer(maxItrtext.getText());
		boolean rndEdg = reButton.isSelected();
		int regType = regSell.getSelectedIndex();
		int edgType = edgSell.getSelectedIndex();
		
		class TextAreaOutputStream extends OutputStream {
		    private JTextArea target;
		    
		    public TextAreaOutputStream(JTextArea target) {
		        this.target = target;
		    }
		    public void write( int b ) throws IOException {
		    	target.append( String.valueOf( ( char )b ) );
		    }  
		}
		
		if(cvButton.isSelected()) {
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
							new PrintStream(new TextAreaOutputStream(logbox)))
			).start();
		}else if(ttButton.isSelected()) {
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
							new PrintStream(new TextAreaOutputStream(logbox)))
			).start();			
		}else if(bsButton.isSelected()) {
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
							new PrintStream(new TextAreaOutputStream(logbox)))
			).start();
			
		}else {
			System.out.println("Unknown Test Type");
		}		
	}

	public static void main(String[] args) {
		GUI.getInstance();
	}

}
