/*
 * Main.java
 */
package imagecoder;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import javax.swing.*;

/**
 *
 * @author Barend Scholtus
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not change Look & Feel: continue");
        }

        final ImageFrame viewer = new ImageFrame();
        final ControlsFrame controls = new ControlsFrame(viewer);

        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        p.translate(-100 - viewer.getWidth()/2, -100 - viewer.getHeight() / 2);
        viewer.setLocation(p);
        p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        p.translate(100 - controls.getWidth()/2, 100 - controls.getHeight()/2);
        controls.setLocation(p);

        viewer.setVisible(true);
        controls.setVisible(true);
    }
}
