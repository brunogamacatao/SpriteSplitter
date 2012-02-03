package splitter;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class SpriteSplitter extends JFrame {
	private static final long serialVersionUID = 7169409593956442822L;
	
	private ImageCanvas imageCanvas;
	
	public SpriteSplitter() {
		super("Sprite Splitter - @brunocatao");
		setupLayout();
	}
	
	private void setupLayout() {
		JPanel layout = new JPanel(new BorderLayout());
		this.setContentPane(layout);
		
		imageCanvas = new ImageCanvas();
		this.addKeyListener(imageCanvas);
		layout.add(BorderLayout.CENTER, new JScrollPane(imageCanvas));

		layout.add(BorderLayout.NORTH, setupMenu());
		
		this.pack();
		this.setLocationRelativeTo(null);
	}

	private JMenuBar setupMenu() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu file = createMenu("File", menuBar);
		file.add(createMenuItem("Open", "open"));
		file.add(createMenuItem("Export", "export"));
		file.addSeparator();
		file.add(createMenuItem("Exit", "exit"));

		JMenu edit = createMenu("Edit", menuBar);
		edit.add(createMenuItem("Select All", "selectAll"));
		edit.add(createMenuItem("Select None", "selectNone"));
		
		JMenu animation = createMenu("Animation", menuBar);
		animation.add(createMenuItem("Create", "createAnimation"));
		animation.add(createMenuItem("Play", "playAnimation"));
		
		return menuBar;
	}
	
	@SuppressWarnings("unused")
	private void open() {
		JFileChooser openDialog = new EnhancedFileChooser();
		
		openDialog.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Image Files (jpeg, png or gif)";
			}
			
			@Override
			public boolean accept(File file) {
				if (file.isDirectory())
					return true;
				
				String filename = file.getName();
				
				if (filename.indexOf('.') > 0) {
					String extension = filename.substring(filename.lastIndexOf('.') + 1, filename.length());
					extension = extension.toLowerCase().trim();
					
					return extension.equals("jpg") || extension.equals("jpeg") || 
						   extension.equals("png") || extension.equals("gif");
				}
				
				return false;
			}
		});
		
		openDialog.showOpenDialog(this);
		
		File selectedFile = openDialog.getSelectedFile();
		if (selectedFile != null) {
			Image img = Toolkit.getDefaultToolkit().createImage(openDialog.getSelectedFile().getAbsolutePath());
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(img, 1);
			
			try {
				tracker.waitForAll();
			} catch (InterruptedException e) {
				// Do nothing
			}
			
			imageCanvas.setImage(img);
			imageCanvas.repaint();
			SwingUtilities.updateComponentTreeUI(this);
		}
	}
	
	//Below we have the action methods
	@SuppressWarnings("unused")
	private void exit() {
		System.exit(0);
	}
	
	@SuppressWarnings("unused")
	private void selectAll() {
		imageCanvas.selectAll();
	}
	
	@SuppressWarnings("unused")
	private void selectNone() {
		imageCanvas.selectNone();
	}
	
	@SuppressWarnings("unused")
	private void createAnimation() {
		String name = JOptionPane.showInputDialog(this, "What's the name for this animation ?");
		if (name != null && name.trim().length() > 0) {
			try {
				imageCanvas.createAnimation(name);
			} catch (RuntimeException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void playAnimation() {
		AnimationDialog dialog = new AnimationDialog(this, imageCanvas.getImage(), imageCanvas.getAnimations());
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
	
	@SuppressWarnings("unused")
	private void export() {
		ExportDialog dialog = new ExportDialog(this, imageCanvas.export());
		dialog.setVisible(true);
	}
	
	//Below we have the factory methods
	private JMenu createMenu(String caption, JMenuBar menuBar) {
		JMenu menu = new JMenu(caption);
		menuBar.add(menu);
		return menu;
	}
	
	protected JMenuItem createMenuItem(String caption, final String action) {
		JMenuItem menuItem = new JMenuItem(caption);
		
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					Method actionMethod = SpriteSplitter.class.getDeclaredMethod(action, new Class[]{});
					actionMethod.setAccessible(true);
					actionMethod.invoke(SpriteSplitter.this, new Object[]{});
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(
							SpriteSplitter.this, 
							"There's no method " + action, 
							"Invalid Action", 
							JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
		
		return menuItem;
	}

	//And finally, we have the main method
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				SpriteSplitter splitter = new SpriteSplitter();
				splitter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				splitter.setVisible(true);
			}
		});
	}
}
