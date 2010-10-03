/*
 *  Copyright 2010 Barend Scholtus
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package imagecoder;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author Barend Scholtus
 */
public class ImageLabel extends JLabel implements MouseMotionListener {

    public static final int MIN_PROGRESS_TO_REPAINT = 5;
    private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
    private int lastRepaintProgress;
    private int lastScrolledX;
    private int lastScrolledY;

    public ImageLabel() {
        super("No image loaded.", SwingConstants.CENTER);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {

        BufferedImage image = getLastImage();
        if (image == null || image.getWidth() < getWidth() || image.getHeight() < getHeight()) {
            super.paintComponent(g);
        }
        if (image != null) {
            g.drawImage(getLastImage(), 0, 0, this);
        }
    }

    public void setImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }

        if (getLayout() != null) {
            removeAll();
            setLayout(null);
        }

        images.clear();
        images.add(image);
        setText(null);
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        invalidate();
    }

    public void applyFilter(ImageFilterTask filterTask) {
        if (!canApplyFilter()) {
            return;
        }

        lastRepaintProgress = 0;
        filterTask.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())
                        && evt.getNewValue() instanceof Integer) {
                    int newProgress = (Integer) evt.getNewValue();
                    if (newProgress > lastRepaintProgress + MIN_PROGRESS_TO_REPAINT) {
                        repaint();
                        lastRepaintProgress = newProgress;
                    }
                }
            }
        });
        filterTask.filter(images.get(images.size() - 1));
    }

    public void undo() {
        if (!canUndo()) {
            return;
        }

        // undo
        images.remove(images.size() - 1);
        repaint();
    }

    public void revert() {
        if (!canRevert()) {
            return;
        }

        BufferedImage firstImage = images.get(0);
        images.clear();
        images.add(firstImage);
        repaint();
    }

    public void purgeUndoList() {
        if (!canUndo()) {
            return;
        }

        BufferedImage lastImage = images.get(images.size() - 1);
        images.clear();
        images.add(lastImage);
    }

    public boolean hasImage() {
        return !images.isEmpty();
    }

    public BufferedImage getLastImage() {
        return hasImage() ? images.get(images.size() - 1) : null;
    }

    public boolean canApplyFilter() {
        return !images.isEmpty();
    }

    public boolean canUndo() {
        return images.size() > 1;
    }

    public boolean canRevert() {
        return images.size() > 1;
    }

    public BufferedImage addCopyOfLast() {
        BufferedImage lastImage = images.get(images.size() - 1);
        BufferedImage newImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(lastImage.getWidth(), lastImage.getHeight());
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(lastImage, 0, 0, null);
        g2d.dispose();
        images.add(newImage);
        return newImage;
    }

    public void mouseMoved(MouseEvent e) {
        lastScrolledX = e.getXOnScreen();
        lastScrolledY = e.getYOnScreen();
        BufferedImage image = getLastImage();
        int color = 0;
        if (image != null && e.getX() >= 0 && e.getY() >= 0
                && e.getX() < image.getWidth() && e.getY() < image.getHeight()) {
            color = image.getRGB(e.getX(), e.getY());
            firePropertyChange("mouseMoved", e, color);
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (e.getX() < 0 || e.getX() >= getWidth()
                || e.getY() < 0 || e.getY() >= getHeight()) {
            return;
        }

        int dx = e.getXOnScreen() - lastScrolledX;
        int dy = e.getYOnScreen() - lastScrolledY;

        Rectangle vis = getVisibleRect();
        Rectangle r = new Rectangle(0, 0, 1, 1);
        if (dx >= 0) {
            r.x = vis.x - dx;
            if (r.x < 0) {
                r.x = 0;
            }
        } else {
            r.x = vis.x + vis.width - 1 - dx;
            if (r.x >= getWidth()) {
                r.x = getWidth() - 1;
            }
        }
        if (dy >= 0) {
            r.y = vis.y - dy;
            if (r.y < 0) {
                r.y = 0;
            }
        } else {
            r.y = vis.y + vis.height - 1 - dy;
            if (r.y >= getHeight()) {
                r.y = getHeight() - 1;
            }
        }
        scrollRectToVisible(r);

        lastScrolledX = e.getXOnScreen();
        lastScrolledY = e.getYOnScreen();
    }
}
