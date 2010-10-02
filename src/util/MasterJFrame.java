/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.JFrame;

/**
 *
 * @author Barend
 */
public class MasterJFrame extends JFrame implements WindowListener {

    private long lastActivationTime;
    private ArrayList<JFrame> slaveFrames = new ArrayList<JFrame>();
    private static final long MIN_ACTIVATIONTIME_GAP = 1000;

    public MasterJFrame() {
        this.addWindowListener(this);
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
            toFront();
            requestFocus();
        }
    }

    @Override
    public void windowActivated(WindowEvent e) {

        // if window is activated again within x ms,
        // it is our own request for focus
        if (System.currentTimeMillis() < lastActivationTime + MIN_ACTIVATIONTIME_GAP) {
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
        toFront();
        requestFocus();
        lastActivationTime = System.currentTimeMillis();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
