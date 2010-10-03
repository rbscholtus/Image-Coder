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
