/*
 * ControlsFrame.java
 */
package imagecoder;

import info.clearthought.layout.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import javassist.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.sf.robocode.ui.editor.*;

/**
 *
 * @author Barend Scholtus
 */
public class ControlsFrame extends JFrame implements ActionListener {

    // controls panel
    private ImageFileChooser chooser;
    private JButton openImageButton = new JButton("Open image");
    private JButton saveImageAsButton = new JButton("Save image as");
    private JButton renderButton = new JButton("Render");
    private JButton undoButton = new JButton("Undo");
    private JButton revertButton = new JButton("Revert");
    private JLabel mouseXYLabel = new JLabel(makeXYString(0, 0));
    private JLabel mouseColorLabel = new JLabel(makeColorString(0));
    private JProgressBar progressBar = new JProgressBar();
    // code panel
    private JEditorPane codePane = new JEditorPane();
    private JScrollPane codeScrollPane = new JScrollPane(codePane);
    // reference to the image frame and panel
    private ImageFrame imageFrame;
    private ImagePanel imagePanel;
    // class generator
    private int sequenceNumber = 0;
    // background task
    private ImageFilterTask task;

    public ControlsFrame(ImageFrame iFrame) {
        setTitle("Image Coder");

        this.imageFrame = iFrame;
        this.imagePanel = imageFrame.getImagePanel();

        final double PREF = TableLayout.PREFERRED;
        final double FILL = TableLayout.FILL;
        ComponentArranger.arrange(0, 0, new Object[]{
                    getContentPane(), 5, FILL, 5, PREF, 5,
                    5, null, null, null, null, null,
                    PREF, null, codeScrollPane, null, openImageButton, null,
                    PREF, null, codeScrollPane, null, saveImageAsButton, null,
                    10, null, codeScrollPane, null, null, null,
                    PREF, null, codeScrollPane, null, renderButton, null,
                    PREF, null, codeScrollPane, null, undoButton, null,
                    PREF, null, codeScrollPane, null, revertButton, null,
                    FILL, null, codeScrollPane, null, null, null,
                    PREF, null, codeScrollPane, null, mouseColorLabel, null,
                    5, null, codeScrollPane, null, null, null,
                    PREF, null, codeScrollPane, null, mouseXYLabel, null,
                    5, null, codeScrollPane, null, null, null,
                    PREF, null, codeScrollPane, null, progressBar, null,
                    5, null, null, null, null, null
                });
        setMinimumSize(new Dimension(320, 240));
        setPreferredSize(new Dimension(800, 360));
        pack();

        codePane.setFont(new Font("monospaced", 0, 12));
        RobocodeEditorKit editorKit = new RobocodeEditorKit();
        codePane.setEditorKitForContentType("text/java", editorKit);
        codePane.setContentType("text/java");
        codePane.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && !e.isShiftDown() && !e.isAltDown()
                        && !e.isAltGraphDown() && !e.isMetaDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        JavaDocument d = (JavaDocument) codePane.getDocument();
                        d.undo();
                    } else if (e.getKeyCode() == KeyEvent.VK_Y) {
                        JavaDocument d = (JavaDocument) codePane.getDocument();
                        d.redo();
                    }
                }
            }
        });

        Dimension d = progressBar.getPreferredSize();
        if (d != null) {
            d.width = 50;
        }
        progressBar.setPreferredSize(d);
        progressBar.setVisible(false);

        openImageButton.addActionListener(this);
        saveImageAsButton.addActionListener(this);
        renderButton.addActionListener(this);
        undoButton.addActionListener(this);
        revertButton.addActionListener(this);

        updateButtons();

        // mouse XY postion
        mouseColorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mouseXYLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePanel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("mouseMoved".equals(evt.getPropertyName())) {
                    // XY pos
                    MouseEvent e = (MouseEvent) evt.getOldValue();
                    mouseXYLabel.setText(makeXYString(e.getX(), e.getY()));
                    // color under mouse
                    int color = (Integer) evt.getNewValue();
                    mouseColorLabel.setText(makeColorString(color));
                }
            }
        });

        // frames behavior
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            private long lastActivationTime;

            @Override
            public void windowIconified(WindowEvent e) {
                int state = imageFrame.getExtendedState();
                if ((state & JFrame.ICONIFIED) == 0) {
                    state |= JFrame.ICONIFIED;
                    imageFrame.setExtendedState(state);
                }
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                int state = imageFrame.getExtendedState();
                if ((state & JFrame.ICONIFIED) != 0) {
                    state &= ~JFrame.ICONIFIED;
                    imageFrame.setExtendedState(state);
                    toFront();
                    requestFocus();
                }
            }

            @Override
            public void windowActivated(WindowEvent e) {
                if (System.currentTimeMillis() > lastActivationTime + 500) {
                    lastActivationTime = System.currentTimeMillis();
                    int state = imageFrame.getExtendedState();
                    if ((state & JFrame.ICONIFIED) == 0) {
                        imageFrame.toFront();
                        imageFrame.requestFocus();
                        toFront();
                        requestFocus();
                    }
                }
            }
        });
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
        }

        updateButtons();
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
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new Exception("Cannot read this type of image.");
            }
            imagePanel.setImage(image);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while reading file:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveImageAsEvent() {
        try {
            BufferedImage image = imagePanel.getLastImage();
            if (image == null) {
                throw new Exception("There is no image that can be saved.");
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
        try {
            // buttons off, progressbar on
            saveUI();

            // create the filter
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(false);
            task = compileCode(codePane.getText());

            // create a copy of last image
            imagePanel.addCopyOfLast();

            // apply filter to it
            progressBar.setValue(progressBar.getMinimum());
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(false);
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
        } catch (CannotCompileException cce) {
            JOptionPane.showMessageDialog(this,
                    "There is a \"Cannot Compile\" error:\n" + cce.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            restoreUI();
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while compiling or executing your code:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            restoreUI();
        }
    }

    private ImageFilterTask compileCode(String userCode) throws Exception {
        // get code
        String filterCode = makeMethod(userCode);

        // prepare ClassPool
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        CtClass imageBase = cp.get("imagecoder.ImageFilterTask");

        // create new Filter class, if none
        String filterClassName = "Filter" + (sequenceNumber++);
        ImageFilterTask filter;// = filters.get(filterClassName);
        CtClass filterCtClass = cp.makeClass(filterClassName, imageBase);
        CtMethod filterCtMethod = CtNewMethod.make(filterCode, filterCtClass);
        filterCtClass.addMethod(filterCtMethod);
        filter = (ImageFilterTask) filterCtClass.toClass().newInstance();

        return filter;
    }

    private String makeMethod(String userCode) {
        StringBuilder sb = new StringBuilder(30 + userCode.length());
        return sb.append("public void filter() {").append(userCode).append('}').toString();
    }

    private void undoEvent() {
        imagePanel.undo();
    }

    private void revertEvent() {
        imagePanel.revert();
    }

    private void saveUI() {
        updateButtons(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        progressBar.setVisible(true);
    }

    private void restoreUI() {
        updateButtons();
        setCursor(null);
        progressBar.setVisible(false);
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

    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static String makeXYString(int x, int y) {
        return new StringBuilder(17).append("pos=[").append(x).append(',').append(y).append(']').toString();
    }

    public static String makeColorString(int color) {
        return new StringBuilder(17).append("rgb=[").append(color >>> 16 & 0xff).append(',').append(color >>> 8 & 0xff).append(',').append(color & 0xff).append(']').toString();
    }
}
