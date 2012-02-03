package splitter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class AnimationDialog extends JDialog {
	private static final long serialVersionUID = 5405419250885892274L;
	
	private Map<String, List<Rectangle>> animations = new HashMap<String, List<Rectangle>>();
	private AnimationPlayer player;
	private JList animationList;
	private JSpinner delay;
	
	public AnimationDialog(Frame parent, Image img, Map<String, List<Rectangle>> animations) {
		super(parent, "Animation Player");
		this.animations = animations;
		
		player = new AnimationPlayer();
		player.setImage(img);
		
		JPanel layout = new JPanel(new BorderLayout());
		this.setContentPane(layout);
		
		layout.add(BorderLayout.CENTER, player);
		
		JPanel topLayout = new JPanel(new FlowLayout());
		topLayout.add(new JLabel("Delay:"));
		
		delay = new JSpinner();
		delay.setModel(new SpinnerNumberModel((int)player.getDelay(), 10, 5000, 1));
		
		topLayout.add(delay);
		
		layout.add(BorderLayout.NORTH, topLayout);
		
		animationList = new JList(animations.keySet().toArray());
		JScrollPane scroll = new JScrollPane(animationList);
		scroll.setBorder(BorderFactory.createTitledBorder("Animations"));
		
		layout.add(BorderLayout.WEST, scroll);
		
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		animationList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				if (animationList.getSelectedValue() != null) {
					List<Rectangle> animation = AnimationDialog.this.animations.get((String)animationList.getSelectedValue());
					AnimationDialog.this.player.setAnimation(animation);
				}
			}
		});
		
		delay.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				AnimationDialog.this.player.setDelay((long)((Integer)AnimationDialog.this.delay.getValue()));
			}
		});
		
		this.pack();
	}
	
	
}
