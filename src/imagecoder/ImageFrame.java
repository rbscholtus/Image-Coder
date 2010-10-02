/*
 * ImageFrame.java
 */
package imagecoder;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * @author Barend Scholtus
 */
public class ImageFrame extends JFrame {

    private ImageLabel imageLabel = new ImageLabel();
    private JScrollPane imageScrollPane = new JScrollPane(imageLabel);
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 768;

    public ImageFrame() {
        super("Image View");
        setContentPane(imageScrollPane);
        setMinimumSize(new Dimension(320, 240));
        pack();
    }

    public JScrollPane getImageScrollPane() {
        return imageScrollPane;
    }

    public ImageLabel getImageLabel() {
        return imageLabel;
    }

    void setImage(BufferedImage image) {
        imageLabel.setImage(image);
        imageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        imageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        pack();
        if (getWidth() > MAX_WIDTH || getHeight() > MAX_HEIGHT) {
            setSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
        }
        imageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        imageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
}
