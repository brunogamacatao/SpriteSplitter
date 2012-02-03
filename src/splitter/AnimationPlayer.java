package splitter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JComponent;

public class AnimationPlayer extends JComponent {
	private static final long serialVersionUID = -287215430222690483L;
	private long delay = 100;
	private Image image;
	private List<Rectangle> animation;
	private int currentFrame;
	private long lastUpdate = 0;
	
	public AnimationPlayer() {
		new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// Do nothing
					}
					repaint();
				}
			}
		}.start();
	}
	
	@Override
	public void paint(Graphics g) {
		if (image != null && animation != null) {
			Rectangle rect = animation.get(currentFrame);
			
			g.drawImage(image, 0, 0, rect.width, rect.height, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, this);
			
			if (System.currentTimeMillis() - lastUpdate > delay) {
				currentFrame = (currentFrame + 1) % animation.size();
				lastUpdate = System.currentTimeMillis();
			}
		}
	}
	
	public void setImage(Image image) {
		this.image = image;
		currentFrame = 0;
	}
	
	public void setAnimation(List<Rectangle> animation) {
		this.animation = animation;
		currentFrame = 0;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}
	
	public long getDelay() {
		return delay;
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (animation == null || animation.isEmpty()) {
			return new Dimension(200, 200);
		}
		
		int maxWidth  = 0;
		int maxHeight = 0;
		
		for (Rectangle rect : animation) {
			maxWidth  = Math.max(rect.width, maxWidth);
			maxHeight = Math.max(rect.height, maxHeight);
		}
		
		return new Dimension(maxWidth, maxHeight);
	}
}
