package com.csv;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


public class splitter extends JFrame {
	private JTextField  tfColumnId, tfInputFilePath;
	public String fileID;
	public JFileChooser chooser;

	/** Constructor to setup the GUI */
	public splitter() {
		// Retrieve the content-pane of the top-level container JFrame
		// All operations done on the content-pane
		// 3 Panels stacked from top  to bottom: panelHeader, panelConfig, panelFooter
		// panelHeader contains title and checkbox to skip header (first row) from the CSV
		// panelConfig contains the fields for data entry and button to browse the file
		// panelFooter displays error messages
		JPanel panelHeader = new JPanel(new FlowLayout());
		panelHeader.setLayout(new BorderLayout());
		panelHeader.add(new JLabel("Welcome to CSV Splitter tool.", SwingConstants.LEFT), BorderLayout.NORTH);
		JCheckBox checkbox = new JCheckBox("File has a header");
		panelHeader.add(checkbox, BorderLayout.CENTER);
		//Checkbox is selected by default
		checkbox.setSelected(true);
		JPanel panelConfig = new JPanel(new GridLayout(3, 2, 10, 10));
		panelConfig.add(new JLabel("Enter the column number (eg., 0):"));
		tfColumnId = new JTextField(10);
		panelConfig.add(tfColumnId);
		tfInputFilePath = new JTextField(10);
		panelConfig.add(tfInputFilePath);
		tfInputFilePath.setText("<Select File>");
		JButton btnBrowse = new JButton("Browse");
		panelConfig.add(btnBrowse);
		JButton btnSend = new JButton("START SPLIT");
		panelConfig.add(btnSend);
		JButton btnReset = new JButton("CLEAR");
		panelConfig.add(btnReset);

		JPanel panelFooter = new JPanel(new FlowLayout());
		panelFooter.setLayout(new BorderLayout());
		JLabel messagebox = new JLabel("JAVA CSV SPLITTER", SwingConstants.CENTER);
		panelFooter.add(messagebox);
		
		//Stack all three components 
		this.setLayout(new BorderLayout());
		panelHeader.add(panelConfig, BorderLayout.SOUTH);
		this.add(panelHeader, BorderLayout.NORTH);
		this.add(panelConfig, BorderLayout.CENTER);
		this.add(panelFooter,BorderLayout.SOUTH);
		System.out.println("UI Generated");

		// Action event Listener for Browse Button
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Step1");
				if (e.getSource() == btnBrowse) {
					System.out.println("Inside Action Listner-Source btnBrowse");
					chooser = new JFileChooser(new File(System.getProperty("user.home") + "\\Downloads")); // Downloads as default dir
					chooser.setDialogTitle("Select Location");
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					chooser.setMultiSelectionEnabled(false);
					// chooser.setAcceptAllFileFilterUsed(false);
					if (chooser.showOpenDialog(btnBrowse) == JFileChooser.APPROVE_OPTION) {
						fileID = chooser.getSelectedFile().getPath();
						tfInputFilePath.setText(fileID);
						System.out.println(fileID);
					}
				}
				return;
			}
		});
		// Action event Listener for START SPLIT Button
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Step2");
				String returnMessage=null;
				String fileNameWithPath = tfInputFilePath.getText();
				System.out.println("fileNameWithPath from tfInputFilePath: " + fileNameWithPath);
				//Validate Mandatory Fields: Validating the Column ID
				if (tfColumnId.getText().equalsIgnoreCase(null) || !isNumber(tfColumnId.getText())) {
					messagebox.setText("COLUMN ID SHOULD BE A NUMBER!");
					panelFooter.setBackground(Color.ORANGE);
					return;
				}
				//Validating File path
				if (tfInputFilePath.getText().equalsIgnoreCase(null) ||tfInputFilePath.getText().equalsIgnoreCase("<Select File>")) {
					messagebox.setText("CLICK BROWSE TO SELECT A FILE!");
					panelFooter.setBackground(Color.ORANGE);
					return;
				}
				if (!isValidPath(tfInputFilePath.getText())) {
					messagebox.setText("UNSUPPORTED FILE PATH!");
					panelFooter.setBackground(Color.ORANGE);
					return;
				}
				
				//Read the column ID and send it to split method
				returnMessage = splitFile(fileNameWithPath,Integer.parseInt(tfColumnId.getText()), checkbox.isSelected());
				if (returnMessage.equals("SUCCESS")) {
					messagebox.setText("COMPLETED SUCCESSFULLY!");
					panelFooter.setBackground(Color.GREEN);
					panelFooter.setForeground(Color.WHITE);

				}else {
					messagebox.setText(returnMessage);
					panelFooter.setBackground(Color.RED);
					panelFooter.setForeground(Color.WHITE);
				}
				return;
			}
		});
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tfInputFilePath.setText("<Select File>");
				checkbox.setSelected(true);
				messagebox.setText("CSV FILE SPLITTER");
				panelFooter.setBackground(getBackground());
				panelFooter.setForeground(getForeground());
				tfColumnId.setText("");
			}
		});

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit program if
														// close-window button
														// clicked
		setTitle("CSV File Splitter"); // "this" Frame sets title
		setSize(400, 200); // "this" Frame sets initial size
		setVisible(true); // "this" Frame shows
	}

	//Method to Validate whether entered value is a number:
	private boolean isNumber(String text) {
	      try {
	         Integer.parseInt(text);
	         return true;
	      } catch (NumberFormatException e) {
	         return false;
	      }
	   }

	
	//method to create an output directory in the path where file is present
	public String createOutputDir(String fileNameWithPath) {
		String outputDir="";
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
		String baseDir=fileNameWithPath.substring(0, fileNameWithPath.lastIndexOf("\\"));
		System.out.println(baseDir);
		//Decide a name for the output directory
		outputDir=baseDir + "\\" + "output_"+timeStamp;
		System.out.println(outputDir);
		//Create the output directory
		new File(outputDir).mkdirs();
		return outputDir;
	}
	//Method to Validate File Paths:
	public static boolean isValidPath(String path) {
	    try {
	        Paths.get(path);
	    } catch (InvalidPathException | NullPointerException ex) {
	        return false;
	    }
	    return true;
	}
	//Method to Split the file
	public String splitFile (String fileNameWithPath, int columnID, boolean headerpresent) {
		String returnValue="SUCCESS";
		int numrows=0, failrows=0;
		String OutPutFilename=null;
		String SplitColumnValue=null;
		Boolean writeSuccess=true;
		System.out.println("Values recieved: ");
		System.out.println("Path: " + fileNameWithPath);
		System.out.println("Column ID: " + columnID);
		System.out.println("Header: "+ headerpresent);
		try {
			String outputFolder = createOutputDir(fileNameWithPath);
			Reader in = new FileReader(fileNameWithPath);
			Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
			for (CSVRecord record : records) {
				numrows ++;
				if (headerpresent == true && numrows == 1) {
					continue;
				}
				//Take the CSV Record & the value present in the column ID
				SplitColumnValue = record.get(columnID);
				//Remove all non-alphanumeric characters and replace it with hyphen:
				SplitColumnValue = SplitColumnValue.replaceAll("[^a-zA-Z0-9 _-]", "-");

				//Create the Output File name, to which this record need to be written
				OutPutFilename = outputFolder + "\\" + SplitColumnValue + ".csv";
				System.out.println("Output File Name Created: " + OutPutFilename);
				writeSuccess = writeCSVRecord(OutPutFilename,record);
				if(!writeSuccess) {
					failrows++;
				}
			}
			//Close the file reader
			in.close();
			if(failrows > 0) {
				returnValue = "FAILED "+failrows+ " OUT OF "+ numrows;
			}
		}
		catch (Exception E) {
			return E.getMessage();
		}
		return returnValue;
	}
	
	////
	//method to write a row into a file name
		public boolean writeCSVRecord (String fileNameWithPath,CSVRecord rowtowrite ) {

			try {
				File outFile=new File(fileNameWithPath);
				
		        //Check whether file already exist
	            if(!(outFile.exists())) {
	            	outFile.createNewFile();
	            }
	            //Create File output Stream to write, in APPEND mode
	            FileWriter filewriter = new FileWriter(fileNameWithPath, true);

	            // Creates a BufferedWriter
	            BufferedWriter output = new BufferedWriter(filewriter);
	            //Create CSVPrinter to write the data
	            CSVPrinter csvPrinter = new CSVPrinter(output, CSVFormat.DEFAULT);
	            csvPrinter.printRecord (rowtowrite);
	            //Cleanup the CSV Printer
	            csvPrinter.flush();
	            csvPrinter.close();
	            // Closes the writer
	            output.close();				
		        //Do cleanup and file close.
	            filewriter.close();
		        
			}catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
			
		}
	/** The entry main() method */
	public static void main(String[] args) {
		// Run the GUI construction in the Event-Dispatching thread for
		// thread-safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new splitter();
				// Let the constructor do the job
			}
		});
	}
}