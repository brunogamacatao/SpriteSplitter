package splitter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ExportDialog extends JDialog {
	private static final long serialVersionUID = 5338348984258825713L;
	
	private String content;
	
	public ExportDialog(Frame parent, String content) {
		super(parent, "Export Content");
		
		this.content = content;
		
		setupLayout();
	}
	
	private void setupLayout() {
		JPanel layout = new JPanel(new BorderLayout());
		
		final JTextArea contentArea = new JTextArea(content);
		contentArea.setPreferredSize(new Dimension(640, 480));
		
		layout.add(BorderLayout.CENTER, contentArea);
		
		JPanel bottomPanel = new JPanel(new FlowLayout());
		
		JButton copyToClipboard = new JButton("Copy to Clipboard");
		bottomPanel.add(copyToClipboard);
		copyToClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection selection = new StringSelection(contentArea.getText());
				clipboard.setContents(selection, null);
				
				JOptionPane.showMessageDialog(ExportDialog.this, 
						"The content were successfully copied to the clipboard", 
						"Success", 
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		JButton save = new JButton("Save");
		bottomPanel.add(save);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = new EnhancedFileChooser();
				if (chooser.showSaveDialog(ExportDialog.this) == JFileChooser.APPROVE_OPTION) {
					try {
						PrintWriter output = new PrintWriter(new FileWriter(chooser.getSelectedFile()));
						output.print(contentArea.getText());
						output.flush();
						output.close();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(ExportDialog.this, 
								"Occurred on error when tried to save the file: " + e.getMessage(), 
								"Error", 
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			}
		});

		layout.add(BorderLayout.SOUTH, bottomPanel);
		
		this.setContentPane(layout);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
}
