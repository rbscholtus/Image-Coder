/*
 * ImagePanel.java
 */
package imagecoder;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author Barend Scholtus
 */
public class ImagePanel extends JPanel implements MouseMotionListener {

    private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
    private int lastRepaintProgress;
    public static final int MINIMUM_PROGRESS_TO_REPAINT = 5;
    private int lastScrolledX;
    private int lastScrolledY;

    public ImagePanel() {
        setLayout(null);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!images.isEmpty()) {
            g.drawImage(images.get(images.size() - 1), 0, 0, this);
        }
    }

    public void setImage(BufferedImage image) {
        if (image == null) {
            return;
        }

        images.clear();
        images.add(image);
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        revalidate();
        repaint();
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
                    if (newProgress > lastRepaintProgress + MINIMUM_PROGRESS_TO_REPAINT) {
                        repaint();
                        lastRepaintProgress = newProgress;
                    }
                }
            }
        });
        filterTask.filter(images.get(images.size()-1));
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
        firePropertyChange("mouseMoved", null, e);
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
            r.y = vis.y + vis.height - dy;
            if (r.y >= getHeight()) {
                r.y = getHeight() - 1;
            }
        }
        scrollRectToVisible(r);

        lastScrolledX = e.getXOnScreen();
        lastScrolledY = e.getYOnScreen();
    }
}
