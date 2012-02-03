package splitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;

public class EnhancedFileChooser extends JFileChooser {
	private static final long serialVersionUID = -2377172084657640346L;
	private static File lastVisitedDirectory;
	
	static {
		final File lastDirInfoFile = new File(".last_dir");
		
		if (lastDirInfoFile.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(lastDirInfoFile));
				File file = new File(reader.readLine());
				
				if (file != null && file.exists() && file.isDirectory()) {
					lastVisitedDirectory = file;
				}
				
				reader.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
		
		if (lastVisitedDirectory == null) {
			lastVisitedDirectory = new File(".");
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (lastVisitedDirectory != null) {
					try {
						PrintWriter out = new PrintWriter(new FileWriter(lastDirInfoFile));
						out.println(lastVisitedDirectory.getCanonicalPath());
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public EnhancedFileChooser() {
		super(lastVisitedDirectory);
	}

	@Override
	public File getSelectedFile() {
		File selectedFile =  super.getSelectedFile();
		
		if (selectedFile != null && selectedFile.exists()) {
			lastVisitedDirectory = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
		}
		
		return selectedFile;
	}
}
