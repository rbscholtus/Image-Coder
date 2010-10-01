/*
 * MasterFrameBehavior.java
 */
package util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JFrame;

/**
 *
 * @author Barend Scholtus
 */
public class MasterFrameBehavior extends WindowAdapter {

    private long lastActivationTime;
    private JFrame masterFrame;
    private ArrayList<JFrame> slaveFrames = new ArrayList<JFrame>();

    public MasterFrameBehavior(JFrame masterFrame) {
        this.masterFrame = masterFrame;
    }

    @Override
    public void windowIconified(WindowEvent e) {
        for (JFrame slave : slaveFrames) {
            int state = slave.getExtendedState();
            if ((state & JFrame.ICONIFIED) == 0) {
                state |= JFrame.ICONIFIED;
                slave.setExtendedState(state);
            }
        }
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        boolean anySlaveDeiconified = false;

        for (JFrame slave : slaveFrames) {
            int state = slave.getExtendedState();
            if ((state & JFrame.ICONIFIED) == JFrame.ICONIFIED) {
                state &= ~JFrame.ICONIFIED;
                slave.setExtendedState(state);
                anySlaveDeiconified = true;
            }
        }

        if (anySlaveDeiconified) {
            masterFrame.toFront();
            masterFrame.requestFocus();
        }
    }

    @Override
    public void windowActivated(WindowEvent e) {

        // if window is activated again within 500 ms,
        // it is our own request for focus
        if (System.currentTimeMillis() < lastActivationTime + 500) {
            return;
        }

        // activate each slave frame that isn't iconified
        for (JFrame slave: slaveFrames)  {
            int state = slave.getExtendedState();
            if ((state & JFrame.ICONIFIED) == 0) {
                slave.toFront();
                slave.requestFocus();
            }
        }

        // reactivate ourselves
        masterFrame.toFront();
        masterFrame.requestFocus();
        lastActivationTime = System.currentTimeMillis();
    }

    /**
     *
     * @param slave
     * @return
     */
    public boolean addSlaveJFrame(JFrame slave) {
        if (slave == null) {
            throw new IllegalArgumentException("slave cannot be null");
        }

        if (slaveFrames.contains(slave)) {
            return false;
        }

        return slaveFrames.add(slave);
    }

    /**
     *
     * @param slave
     * @return
     */
    public boolean removeSlaveJFrame(JFrame slave) {
        if (slave == null) {
            throw new IllegalArgumentException("slave cannot be null");
        }

        return slaveFrames.remove(slave);
    }
}
