/*
 * ImageFilterTask.java
 */
package imagecoder;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * @author Barend Scholtus
 */
public abstract class ImageFilterTask extends SwingWorker {

    protected BufferedImage image;
    protected Rectangle imageBounds;
    private boolean warnings = false;

    /**
     * Applies this filter to image. After setting the image and its bounds,
     * it executes the task, which implies calling doInBackground() and done().
     * @param image the image that must be filtered
     */
    public void filter(BufferedImage image) {
        this.image = image;
        imageBounds = new Rectangle(image.getWidth(), image.getHeight());
        execute();
    }

    /**
     * Does the filtering in the background. This method is called by
     * filter(BufferedImage).
     * @return null
     * @throws Exception
     */
    @Override
    protected Object doInBackground() throws Exception {
        setProgress(0);
        filter();
        return null;
    }

    /**
     * Notifies listeners that the filtering is done. Called automatically at
     * the end of the task.
     */
    @Override
    public void done() {
        setProgress(100);
    }

    /**
     * Performs the filtering. This is called by filter(BufferedImage). This
     * must be overridden in subclasses to implement a filter.
     */
    protected abstract void filter();

    public static int limit(final int val, final int min, final int max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        }
        return val;
    }

    public boolean isWarnings() {
        return warnings;
    }

    public void setWarnings(boolean warnings) {
        this.warnings = warnings;
    }

    private void showWarningMessage(String str) {
        if (warnings) {
            JOptionPane.showMessageDialog(null, "Warning", str,
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /* Helper methods to make filter() easier to write. */
    public void setProgress(int units, int maxUnits) {
        units = limit(units, 0, maxUnits);
        setProgress(100 * units / maxUnits);
    }

    public void progress(int units, int maxUnits) {
        setProgress(units, maxUnits);
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int width() {
        return getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int height() {
        return getHeight();
    }

    public void setRGB(int x, int y, int rgb) {
        if (imageBounds.contains(x, y)) {
            image.setRGB(x, y, rgb);
        } else {
            showWarningMessage("Coordinate [" + x + "," + y + "] is out of the image's bounds");
        }
    }

    public void rgb(int x, int y, int rgb) {
        setRGB(x, y, rgb);
    }

    public int getRGB(int x, int y) {
        if (imageBounds.contains(x, y)) {
            return image.getRGB(x, y);
        } else {
            showWarningMessage("Coordinate [" + x + "," + y + "] is out of the image's bounds");
            return 0;
        }
    }

    public int rgb(int x, int y) {
        return getRGB(x, y);
    }

    public int getA(int x, int y) {
        if (imageBounds.contains(x, y)) {
            return (image.getRGB(x, y) >> 24) & 0xff;
        } else {
            showWarningMessage("Coordinate [" + x + "," + y + "] is out of the image's bounds");
            return 0;
        }
    }

    public int getR(int x, int y) {
        if (imageBounds.contains(x, y)) {
            return (image.getRGB(x, y) >> 16) & 0xff;
        } else {
            showWarningMessage("Coordinate [" + x + "," + y + "] is out of the image's bounds");
            return 0;
        }
    }

    public int getG(int x, int y) {
        if (imageBounds.contains(x, y)) {
            return (image.getRGB(x, y) >> 8) & 0xff;
        } else {
            showWarningMessage("Coordinate [" + x + "," + y + "] is out of the image's bounds");
            return 0;
        }
    }

    public int getB(int x, int y) {
        if (imageBounds.contains(x, y)) {
            return image.getRGB(x, y) & 0xff;
        } else {
            showWarningMessage("Coordinate [" + x + "," + y + "] is out of the image's bounds");
            return 0;
        }
    }

    public int alpha(int x, int y) {
        return getA(x, y);
    }

    public int red(int x, int y) {
        return getR(x, y);
    }

    public int green(int x, int y) {
        return getG(x, y);
    }

    public int blue(int x, int y) {
        return getB(x, y);
    }

    public int makeARGB(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int makeRGB(int r, int g, int b) {
        if (r < 0 || r > 0xff) {
            showWarningMessage("Color value " + r + " for red is not in the range [0,255]");
            r = limit(r, 0, 0xff);
        }
        if (g < 0 || g > 0xff) {
            showWarningMessage("Color value " + g + " for green is not in the range [0,255]");
            g = limit(g, 0, 0xff);
        }
        if (b < 0 || b > 0xff) {
            showWarningMessage("Color value " + b + " for blue is not in the range [0,255]");
            b = limit(b, 0, 0xff);
        }
        return (0xff << 24) | (r << 16) | (g << 8) | b;
    }

    public int makeRGB(int r, int g, double b) {
        return makeRGB(r, g, (int) b);
    }

    public int makeRGB(int r, double g, int b) {
        return makeRGB(r, (int) g, b);
    }

    public int makeRGB(int r, double g, double b) {
        return makeRGB(r, (int) g, (int) b);
    }

    public int makeRGB(double r, int g, int b) {
        return makeRGB((int) r, g, b);
    }

    public int makeRGB(double r, int g, double b) {
        return makeRGB((int) r, g, (int) b);
    }

    public int makeRGB(double r, double g, int b) {
        return makeRGB((int) r, (int) g, b);
    }

    public int makeRGB(double r, double g, double b) {
        return makeRGB((int) r, (int) g, (int) b);
    }

//    public int makeRGB(int c) {
//        if (c < 0 || c > 0xff) {
//            showWarningMessage("Color value " + c + " is not in the range [0,255]");
//        }
//        c = limit(c, 0, 0xff);
//        return (0xff << 24) | (c << 16) | (c << 8) | c;
//    }
//
//    public int makeRGB(float c) {
//        return makeRGB((int) c);
//    }
}
