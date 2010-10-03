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

import info.clearthought.layout.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.*;
import java.net.URI;
import javassist.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.sf.robocode.ui.editor.*;
import util.*;

/**
 *
 * @author Barend Scholtus
 */
public class ControlsFrame extends MasterJFrame implements ActionListener {

    // controls panel
    private ImageFileChooser chooser;
    private JButton openImageButton = new JButton(new ImageIcon(getClass().getResource("images/open.png")));
    private JButton saveImageAsButton = new JButton(new ImageIcon(getClass().getResource("images/save.png")));
    private JButton revertButton = new JButton(new ImageIcon(getClass().getResource("images/revert.png")));
    private JButton undoButton = new JButton(new ImageIcon(getClass().getResource("images/undo.png")));
    private JButton renderButton = new JButton(new ImageIcon(getClass().getResource("images/render.png")));
    private JButton wwwButton = new JButton(new ImageIcon(getClass().getResource("images/www.png")));
    private JLabel imageSizeLabel = new JLabel(makeSizeString(0, 0),
            new ImageIcon(getClass().getResource("images/size.png")), SwingConstants.LEADING);
    private JLabel mouseXYLabel = new JLabel(makeXYString(0, 0),
            new ImageIcon(getClass().getResource("images/mouse.png")), SwingConstants.LEADING);
    private JLabel emptyLabel = new JLabel(" ");
    private JLabel mouseColorLabel = new JLabel(makeColorString(0),
            new ImageIcon(getClass().getResource("images/color.png")), SwingConstants.LEADING);
    private JProgressBar progressBar = new JProgressBar();
    // code panel
    private JEditorPane codePane = new JEditorPane();
    private JScrollPane codeScrollPane = new JScrollPane(codePane);
    // reference to the image frame and panel
    private ImageFrame imageFrame;
    private ImageLabel imagePanel;
    // class generator
    private int sequenceNumber = 0;
    // background task
    private ImageFilterTask task;
    private static final String INTRO_TEXT = "// Welcome to Image Coder!\n"
            + "//\n"
            + "// This is Image Coder's main window. You type snippets of Java code here to\n"
            + "// manipulate the image in the Image View. For examples and a list of commands,\n"
            + "// please visit http://github.com/rbscholtus/Image-Coder/wiki\n"
            + "//\n"
            + "// Before you start, you have to open an image though. Dp this by pressing the Open\n"
            + "// image icon, or by dragging an image in one of the windows.\n"
            + "\n// EXAMPLE: make image greyscale\n"
            + "for (int x = 0; x < width(); x++) {\n"
            + "    setProgress(x, width());\n"
            + "    for (int y = 0; y < height(); y++) {\n"
            + "        int avg = ( red(x,y) + green(x,y) + blue(x,y) ) / 3;\n"
            + "        setRGB(x, y, makeRGB(avg, avg, avg));\n"
            + "    }\n"
            + "}\n";
    private static final String WWW_URI = "http://github.com/rbscholtus/Image-Coder/wiki/Examples";

    public ControlsFrame(ImageFrame iFrame) {
        setTitle("Image Coder");

        this.imageFrame = iFrame;
        this.imagePanel = imageFrame.getImageLabel();

        initLayout();
        initEditor();
        initImagePanelCallbacks();

        // frames behavior
        setTransferHandler(new FileDropHandler(this));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addSlaveJFrame(imageFrame);
        imageFrame.setTransferHandler(new FileDropHandler(this));

        progressBar.setValue(progressBar.getMinimum());
        progressBar.setIndeterminate(false);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == openImageButton) {
            openImageEvent();
        } else if (src == saveImageAsButton) {
            saveImageAsEvent();
        } else if (src == renderButton) {
            new Thread() {

                @Override
                public void run() {
                    createAndFilter();
                }
            }.start();
        } else if (src == undoButton) {
            undoEvent();
        } else if (src == revertButton) {
            revertEvent();
        } else if (src == wwwButton) {
            try {
                Desktop.getDesktop().browse(URI.create(WWW_URI));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error",
                        "Could not access your browser, or action not allowed.\n" + ex.getMessage(),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openImageEvent() {
        if (chooser == null) {
            chooser = new ImageFileChooser();
        }
        File file = null;

        int ret = chooser.showOpenDialog(this);
        switch (ret) {
            case JFileChooser.APPROVE_OPTION:
                file = chooser.getSelectedFile();
                break;
            case JFileChooser.ERROR_OPTION:
                JOptionPane.showMessageDialog(this, "Error while opening file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }

        if (file == null) {
            return;
        }

        try {
            openImage(file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while reading file:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openImage(File file) throws Exception {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new Exception("Cannot read this type of image.");
        }
        imageSizeLabel.setText(makeSizeString(image.getWidth(), image.getHeight()));
        imageFrame.setImage(image);
        imageFrame.setTitle(file.getName());
        updateButtons();
    }

    private void saveImageAsEvent() {
        try {
            BufferedImage image = imagePanel.getLastImage();
            if (image == null) {
                throw new Exception("There is no image that can be saved.");
            }

            if (chooser == null) {
                chooser = new ImageFileChooser();
            }

            int ret = chooser.showSaveDialog(this);
            File file = chooser.getSelectedFile();
            if (ret != JFileChooser.APPROVE_OPTION || file == null) {
                return;
            }

            String ext = ImageFileChooser.getExtension(file);
            if (ext == null || !ImageFileChooser.canWriteSuffix(ext)
                    || !ImageIO.write(image, ext, file)) {
                throw new Exception("This file type (" + ext + ") is not supported by your Java system.");
            }

            imageFrame.setTitle(file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error during writing:\n" + e.getMessage()
                    + "\n\n(There may be a permissions error, or disk is full?)",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createAndFilter() {
        // buttons off, change cursor
        updateButtons(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // create the filter
        try {
            progressBar.setIndeterminate(true);
            String code = codePane.getText().trim();
            if (code.isEmpty()) {
                throw new Exception("Nothing to do. Write some code first.");
            }
            task = compileCode(code);
        } catch (CannotCompileException cce) {
            String mess = cce.getMessage();
            if (mess.startsWith("[source error]")) {
                mess = mess.substring(15);
            }
            JOptionPane.showMessageDialog(this, mess,
                    "Error in source", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "General error", JOptionPane.ERROR_MESSAGE);
        }

        if (task == null) {
            restoreUI();
            return;
        }

        // create a copy of last image
        try {
            imagePanel.addCopyOfLast();
        } catch (OutOfMemoryError e) {
            if (imagePanel.canUndo()) {
                int ret = JOptionPane.showConfirmDialog(this, "Out of memory:\n"
                        + e.getMessage()
                        + "\n\nPurge history?",
                        "Out of memory", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (ret == JOptionPane.OK_OPTION) {
                    imagePanel.purgeUndoList();
                    JOptionPane.showMessageDialog(this, "Undo list purged. Please retry.",
                            "Message", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Out of memory.",
                        "Out of memory", JOptionPane.ERROR_MESSAGE);
            }
            restoreUI();
            return;
        }

        // apply filter to it
        progressBar.setIndeterminate(false);
        progressBar.setValue(progressBar.getMinimum());
        task.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (task == null) {
                    return;
                }
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue(task.getProgress());
                } else if ("state".equals(evt.getPropertyName())
                        && SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                    Toolkit.getDefaultToolkit().beep();
                    progressBar.setValue(progressBar.getMaximum());
                    imagePanel.repaint();
                    restoreUI();
                    task = null;
                }
            }
        });
        imagePanel.applyFilter(task);

        restoreUI();
    }

    private ImageFilterTask compileCode(String userCode) throws Exception {
        // get code
        String filterCode = makeMethod(userCode);

        // prepare ClassPool
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        CtClass imageBase = cp.get("imagecoder.ImageFilterTask");

        // create new Filter class
        String filterClassName = "Filter" + (sequenceNumber++);
        CtClass filterCtClass = cp.makeClass(filterClassName, imageBase);
        CtMethod filterCtMethod = CtNewMethod.make(filterCode, filterCtClass);
        filterCtClass.addMethod(filterCtMethod);
        ImageFilterTask filter = (ImageFilterTask) filterCtClass.toClass().newInstance();

        return filter;
    }

    private String makeMethod(String userCode) {
        StringBuilder sb = new StringBuilder(30 + userCode.length());
        return sb.append("public void filter() {").append(userCode).append('}').toString();
    }

    private void undoEvent() {
        imagePanel.undo();
        updateButtons();
    }

    private void revertEvent() {
        imagePanel.revert();
        updateButtons();
    }

    private void restoreUI() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(progressBar.getMinimum());
        updateButtons();
        setCursor(null);
    }

    private void updateButtons() {
        updateButtons(true);
    }

    private void updateButtons(boolean enabled) {
        openImageButton.setEnabled(enabled && imagePanel != null);

        if (!enabled || imagePanel == null || !imagePanel.hasImage()) {
            saveImageAsButton.setEnabled(false);
            renderButton.setEnabled(false);
            undoButton.setEnabled(false);
            revertButton.setEnabled(false);
        } else {
            saveImageAsButton.setEnabled(true);
            renderButton.setEnabled(imagePanel.canApplyFilter());
            undoButton.setEnabled(imagePanel.canUndo());
            revertButton.setEnabled(imagePanel.canRevert());
        }
    }

    private void initLayout() {
        setMinimumSize(new Dimension(450, 240));
        setPreferredSize(new Dimension(640, 300));

        Color c = getContentPane().getBackground().darker();

        openImageButton.setToolTipText("Open image...");
        openImageButton.addActionListener(this);
        saveImageAsButton.setToolTipText("Save image as...");
        saveImageAsButton.addActionListener(this);
        renderButton.setToolTipText("Apply code to image");
        renderButton.addActionListener(this);
        undoButton.setToolTipText("Undo last step");
        undoButton.addActionListener(this);
        revertButton.setToolTipText("Revert to original");
        revertButton.addActionListener(this);
        wwwButton.setToolTipText("Find Image Coder on the web");
        wwwButton.addActionListener(this);

        JToolBar toolbar = new JToolBar();
        toolbar.add(openImageButton);
        toolbar.add(saveImageAsButton);
        toolbar.addSeparator();
        toolbar.add(revertButton);
        toolbar.add(undoButton);
        toolbar.addSeparator();
        toolbar.add(renderButton);
        toolbar.addSeparator();
        toolbar.add(wwwButton);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, c));
        updateButtons();

        codeScrollPane.setBorder(null);

        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, c),
                BorderFactory.createEmptyBorder(1, 3, 1, 3)));

        imageSizeLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, c));
        mouseXYLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, c));
        mouseColorLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, c));
        emptyLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, c));

        double PREF = TableLayout.PREFERRED;
        double FILL = TableLayout.FILL;
        ComponentArranger.arrange(3, 0, new Object[]{
                    statusPanel, 100, 100, 110, FILL, 125,
                    PREF, imageSizeLabel, mouseXYLabel, mouseColorLabel, emptyLabel, progressBar
                });

        add(toolbar, BorderLayout.NORTH);
        add(codeScrollPane);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void initEditor() {
        codePane.setFont(new Font("monospaced", 0, 12));
        RobocodeEditorKit editorKit = new RobocodeEditorKit();
        codePane.setEditorKitForContentType("text/java", editorKit);
        codePane.setContentType("text/java");
        codePane.setText(INTRO_TEXT);
        codePane.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isMetaDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        JavaDocument d = (JavaDocument) codePane.getDocument();
                        d.undo();
                    } else if (e.getKeyCode() == KeyEvent.VK_Y) {
                        JavaDocument d = (JavaDocument) codePane.getDocument();
                        d.redo();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        new Thread() {

                            @Override
                            public void run() {
                                createAndFilter();
                            }
                        }.start();
                    }
                }
            }
        });
    }

    private void initImagePanelCallbacks() {
        // mouse XY postion
        imagePanel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("mouseMoved".equals(evt.getPropertyName())) {
                    // XY pos
                    MouseEvent e = (MouseEvent) evt.getOldValue();
                    mouseXYLabel.setText(makeXYString(e.getX() + 1, e.getY() + 1));
                    // color under mouse
                    int color = (Integer) evt.getNewValue();
                    mouseColorLabel.setText(makeColorString(color));
                }
            }
        });
    }

    public static String makeXYString(int x, int y) {
        return new StringBuilder(17).append(x).append(", ").append(y).toString();
    }

    public static String makeSizeString(int w, int h) {
        return new StringBuilder(17).append(w).append(" x ").append(h).toString();
    }

    public static String makeColorString(int color) {
        return new StringBuilder(17).append(color >>> 16 & 0xff).append(", ").append(color >>> 8 & 0xff).append(", ").append(color & 0xff).toString();
    }
}
