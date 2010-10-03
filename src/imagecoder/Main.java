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
import javax.swing.*;

/**
 * Start-up code for the Image Coder program.
 * It switches Swing to the System Look And Feel, and creates a controller frame
 * and image viewer frame.
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
