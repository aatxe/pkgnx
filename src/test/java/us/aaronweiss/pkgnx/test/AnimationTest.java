/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2013 Aaron Weiss
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package us.aaronweiss.pkgnx.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.format.NXNode;
import us.aaronweiss.pkgnx.format.nodes.NXBitmapNode;
import us.aaronweiss.pkgnx.format.nodes.NXNullNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * An animation test for testing image loading. Based on Cedric's NXAnimation.
 *
 * @author Aaron Weiss
 * @version 1.2.0
 * @since 5/27/13
 */
public class AnimationTest extends JPanel implements ActionListener {
	private static final Logger logger = LoggerFactory.getLogger(AnimationTest.class);
	private static final String FILE_PATH = "src/test/resources/Data.nx";

	/**
	 * Path to desired animation.
	 * <p/>
	 * Suggestions: Zakum: "Mob/8800000.img/attack1" Snail: "Mob/0100100.img/move"
	 */
	public static final String ANIMATION_PATH = "Mob/8800000.img/attack1";

	private NXFile file;
	private BufferedImage[] sprites;
	private Timer timer;
	private int index;

	/**
	 * Create a new {@code AnimationTest} panel.
	 *
	 * @param file          the file to load from
	 * @param animationPath the path to the node to load
	 */
	public AnimationTest(NXFile file, String animationPath) {
		this.file = file;
		loadSprites(animationPath);
		timer = new Timer(100, this);
		timer.start();
	}

	/**
	 * Loads sprites from the specified {@code animationPath}.
	 *
	 * @param animationPath the path to the node to load
	 */
	private void loadSprites(String animationPath) {
		NXNode node = file.resolve(animationPath);
		if (node instanceof NXNullNode) {
			List<BufferedImage> images = new ArrayList<BufferedImage>();
			for (NXNode child : node) {
				if (child instanceof NXBitmapNode) {
					NXBitmapNode bmp = (NXBitmapNode) child;
					images.add((BufferedImage) bmp.get());
				}
			}
			sprites = new BufferedImage[images.size()];
			for (int i = 0; i < sprites.length; i++) {
				sprites[i] = images.get(i);
			}
			logger.info("Loaded " + sprites.length + " sprites from " + animationPath);
			setPreferredSize(new Dimension(sprites[0].getWidth(), sprites[0].getHeight()));
		} else {
			throw new RuntimeException("Animations should be located in parent folders.");
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(sprites[index], 0, 0, this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		index = (index + 1) % sprites.length;
		repaint();
	}

	/**
	 * Runs the animation test.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) {
		try {
			NXFile file = new NXFile(FILE_PATH);
			JFrame frame = new JFrame("NX Animation Test: " + ANIMATION_PATH);
			JPanel panel = new JPanel();
			panel.add(new AnimationTest(file, ANIMATION_PATH));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			logger.error("Failed to load file.", e);
		}
	}
}