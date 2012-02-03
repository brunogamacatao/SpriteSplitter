package splitter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

public class ImageCanvas extends JComponent implements MouseListener, MouseMotionListener, KeyListener {
	private static final long serialVersionUID = 7201917714849924661L;
	private static final Dimension DEFAULT_DIMENSION = new Dimension(800, 600);
	private Image image;
	private List<Integer> rows    = new ArrayList<Integer>();
	private List<Point[]> columns = new ArrayList<Point[]>();
	private List<List<Rectangle>> unassignedRectangles;
	private Map<String, List<Rectangle>> assignedRectangles = new HashMap<String, List<Rectangle>>();
	private List<Rectangle> selectedRectangles = new ArrayList<Rectangle>();
	private boolean shiftPressed = false;
	
	public ImageCanvas() {
		super();
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.addMouseMotionListener(this);
	}
	
	public void selectNone() {
		selectedRectangles.clear();
		repaint();
	}
	
	public void selectAll() {
		selectNone();
		for (List<Rectangle> rects : unassignedRectangles) {
			selectedRectangles.addAll(rects);
		}
		repaint();
	}
	
	public void createAnimation(String name) {
		if (assignedRectangles.containsKey(name))
			throw new RuntimeException("There's already an animation with the specified name [" + name + "]");
		
		List<Rectangle> animation = new ArrayList<Rectangle>();
		animation.addAll(selectedRectangles);
		
		Collections.sort(animation, new Comparator<Rectangle>() {
			@Override
			public int compare(Rectangle r1, Rectangle r2) {
				if (r1.y != r2.y)
					return r1.y - r2.y;
				return r1.x - r2.x;
			}
		});
		
		assignedRectangles.put(name, animation);
		selectNone();
	}
	
	public Map<String, List<Rectangle>> getAnimations() {
		return assignedRectangles;
	}
	
	public String export() {
		StringBuilder output = new StringBuilder();

		output.append("{\n");
		output.append("  \"animations\": [\n");
		
		Object[] keys = assignedRectangles.keySet().toArray();
		
		for (int i = 0; i < keys.length; i++) {
			String animationName = (String)keys[i];
			
			output.append("    {\n");
			output.append("      \"name\": " + qq(animationName) + ", \"sprites\": [\n");
			
			List<Rectangle> rects = assignedRectangles.get(animationName);
			
			for (int j = 0; j < rects.size(); j++) {
				Rectangle rect = rects.get(j);
				output.append("        {");
				output.append("\"id\": " + qq("sprite_" + j) + ", ");
				output.append("\"x\": " + rect.x + ", ");
				output.append("\"y\": " + rect.y + ", ");
				output.append("\"w\": " + rect.width + ", ");
				output.append("\"h\": " + rect.height + "}");
				output.append((j < (rects.size() - 1) ? "," : "") + "\n");
			}

			output.append("      ]\n");
			output.append("    }" + (i < (keys.length - 1) ? "," : "") + "\n");
		}
		
		output.append("  ]\n");
		output.append("}");
		
		return output.toString();
	}
	
	private static String qq(String str) {
		return "\""+ str + "\"";
	}
	
	@Override
	public void paint(Graphics g) {
		if (image != null) {
			g.drawImage(image, 0, 0, this);
			
			for (List<Rectangle> rects : unassignedRectangles) {
				drawRectangles(rects, Color.MAGENTA, g);
			}
			
			if (assignedRectangles != null) {
				for (String animationName : assignedRectangles.keySet()) {
					drawRectangles(assignedRectangles.get(animationName), Color.GREEN, g);
				}
			}
			
			Graphics2D g2 = (Graphics2D)g;
			g2.setStroke(new BasicStroke(2.5f));
			drawRectangles(selectedRectangles, Color.YELLOW, g);
		} else {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);
		}
	}

	private void drawRectangles(List<Rectangle> rects, Color rectColor, Graphics g) {
		for (Rectangle rect : rects) {
			g.setColor(Color.BLACK);
			g.drawRect(rect.x + 1, rect.y + 1, rect.width, rect.height);
			g.setColor(rectColor);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
	}
	
	private List<List<Rectangle>> generateRectangles() {
		int row = -1;
		List<List<Rectangle>> rectangles = new ArrayList<List<Rectangle>>();
		List<Rectangle> currentRow = null;
		int x = 0;
		for (Point[] column : columns) {
			if (column[1].y != row) {
				//So we've a new row
				x = 0;
				currentRow = new ArrayList<Rectangle>();
				rectangles.add(currentRow);
			}
			currentRow.add(new Rectangle(x, column[1].y, column[0].x - x, column[0].y - column[1].y));
			x = column[0].x;
			row = column[1].y;
		}
		
		return rectangles;
	}
	
	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
		traceImage(image);
	}

	private void traceImage(Image image) {
		rows.clear();
		columns.clear();
		
		Dimension dim = new Dimension(image.getWidth(this), image.getHeight(this));
		BufferedImage buffer = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		buffer.getGraphics().drawImage(image, 0, 0, this);
		
		int firstPixel = buffer.getRGB(0, 0);
		int lastY      = 0;
		
		for (int y = 0; y < dim.height; y++) {
			boolean lineFound = true;
			for (int x = 0; x < dim.width / 2; x++) {
				if (buffer.getRGB(x, y) != firstPixel) {
					lineFound = false;
					break;
				}
			}
			
			if (lineFound) {
				if ((y - lastY) > 1) {
					rows.add(y);
				}
				lastY = y;
			}
		}
		
		int prevRow = 0;
		for (int row : rows) {
			int lastX = 0;
			for (int x = 0; x < dim.width; x++) {
				boolean columnFound = true;
				for (int y = row; y >= prevRow; y--) {
					if (buffer.getRGB(x, y) != firstPixel) {
						columnFound = false;
						break;
					}
				}
				if (columnFound) {
					if ((x - lastX) > 1) {
						columns.add(new Point[]{new Point(x, row), new Point(x, prevRow)});	
					}
					lastX = x;
				}
			}
			prevRow = row;
		}
		
		unassignedRectangles = generateRectangles();
	}

	@Override
	public Dimension getPreferredSize() {
		if (image != null) {
			return new Dimension(image.getWidth(this), image.getHeight(this));
		}
		
		return DEFAULT_DIMENSION;
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		selectRectangles(evt.getPoint());
	}

	private void selectRectangles(Point point) {
		if (!shiftPressed) {
			selectedRectangles.clear();
		}
		
		if (unassignedRectangles != null) {
			for (List<Rectangle> rects : unassignedRectangles) {
				for (Rectangle rect : rects) {
					if (rect.contains(point)) {
						if (!selectedRectangles.contains(rect))
							selectedRectangles.add(rect);
						break;
					}
				}
			}
		}
		
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent evt) {
	}

	@Override
	public void mouseExited(MouseEvent evt) {
	}

	@Override
	public void mousePressed(MouseEvent evt) {
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
			shiftPressed = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
		selectRectangles(evt.getPoint());
	}

	@Override
	public void mouseMoved(MouseEvent evt) {
	}
}
