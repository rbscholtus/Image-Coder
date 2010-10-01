/*
 * FileDropHandler.java
 */
package imagecoder;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author Shannon Hickey
 * @author Barend Scholtus
 */
public class FileDropHandler extends TransferHandler {

    ControlsFrame controlsFrame;

    public FileDropHandler(ControlsFrame controlsFrame) {
        this.controlsFrame = controlsFrame;
    }

    @Override
    public boolean canImport(TransferSupport supp) {
        // we only support drops (not clipboard paste)
        if (!supp.isDrop()) {
            return false;
        }

        // return false if the drop doesn't contain a list of files
        if (!supp.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }

        // if COPY isn't supported, reject the transfer
        if ((COPY & supp.getSourceDropActions()) != COPY) {
            return false;
        }

        // COPY is supported, choose COPY and accept the transfer
        supp.setDropAction(COPY);
        return true;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        // fetch the Transferable
        Transferable t = support.getTransferable();

        try {
            // fetch the data from the Transferable
            Object data = t.getTransferData(DataFlavor.javaFileListFlavor);

            // data of type javaFileListFlavor is a list of files
            List fileList = (List) data;

            // loop through the files in the file list
            if (fileList.isEmpty()) {
                return false;
            }

            // controls panel opens the file
            controlsFrame.openImage((File) fileList.get(0));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(controlsFrame,
                    "Error while reading file:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
