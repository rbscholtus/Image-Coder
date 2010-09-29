/*
 * ImageFrame.java
 */
package imagecoder;

import java.awt.Dimension;
import javax.swing.*;

/**
 * @author Barend Scholtus
 */
public class ImageFrame extends JFrame {

    private ImagePanel imagePanel = new ImagePanel();
    private JScrollPane imageScrollPane = new JScrollPane(imagePanel);

    public ImageFrame() {
        setTitle("Image View");
        setContentPane(imageScrollPane);
        setMinimumSize(new Dimension(320, 240));
        setPreferredSize(new Dimension(640, 480));
        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public JScrollPane getImageScrollPane() {
        return imageScrollPane;
    }

    public ImagePanel getImagePanel() {
        return imagePanel;
    }
}
