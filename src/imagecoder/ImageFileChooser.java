/*
 * ImageFileChooser.java
 */
package imagecoder;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Barend Scholtus
 */
public class ImageFileChooser extends JFileChooser {

    private ImageReaderFileFilter fileReaderFilter = new ImageReaderFileFilter();
    private ImageWriterFileFilter fileWriterFilter = new ImageWriterFileFilter();

    public ImageFileChooser() {

        // create File chooser
        setMultiSelectionEnabled(false);
        setFileFilter(fileReaderFilter);

        // get the user directory and set it in the chooser
        try {
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                File file = new File(userDir);
                if (file.exists() && file.isDirectory()) {
                    setCurrentDirectory(file);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Not allowed to access User directory at this time.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        setFileFilter(fileReaderFilter);
        return super.showOpenDialog(parent);
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        setFileFilter(fileWriterFilter);
        return super.showSaveDialog(parent);
    }

    /** Returns a f's extension. */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static boolean canReadSuffix(String suffix) {
        return ImageIO.getImageReadersBySuffix(suffix).hasNext();
    }

    public static boolean canWriteSuffix(String suffix) {
        return ImageIO.getImageWritersBySuffix(suffix).hasNext();
    }

    static class ImageReaderFileFilter extends FileFilter {
        private String descr;

        public ImageReaderFileFilter() {
            buildDescr();
        }

        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            }
            String ext = getExtension(pathname);
            return (ext != null && canReadSuffix(ext));
        }

        public String getDescription() {
            return descr;
        }

        private void buildDescr() {
            StringBuilder sb = new StringBuilder("Readable formats [");
            String[] suffixes = ImageIO.getReaderFileSuffixes();
            for (int i = 0; i < suffixes.length; i++) {
                if (i != 0) {
                    sb.append(' ');
                }
                sb.append('.').append(suffixes[i]);
            }
            sb.append(']');
            descr = sb.toString();
        }
    }

    static class ImageWriterFileFilter extends FileFilter {
        private String descr;

        public ImageWriterFileFilter() {
            buildDescr();
        }

        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            }
            String ext = getExtension(pathname);
            return (ext != null && canReadSuffix(ext));
        }

        public String getDescription() {
            return descr;
        }

        private void buildDescr() {
            StringBuilder sb = new StringBuilder("Writable formats [");
            String[] suffixes = ImageIO.getWriterFileSuffixes();
            for (int i = 0; i < suffixes.length; i++) {
                if (i != 0) {
                    sb.append(' ');
                }
                sb.append('.').append(suffixes[i]);
            }
            sb.append(']');
            descr = sb.toString();
        }
    }
}
