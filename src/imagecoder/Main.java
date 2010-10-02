/*
 * Main.java
 */
package imagecoder;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
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

        DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        System.out.println("" + mode.getHeight());
        controls.setLocation(mode.getWidth() / 2 - controls.getWidth() / 2,
                150 + mode.getHeight() / 2 - controls.getHeight() / 2);
        controls.setVisible(true);

        viewer.setLocation(-150 + mode.getWidth() / 2 - viewer.getWidth() / 2,
                -150 + mode.getHeight() / 2 - viewer.getHeight() / 2);
        viewer.setVisible(true);
    }
}
