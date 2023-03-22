package com.mz.sshclient.ui.laf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public final class LAF {

    private LAF() {}

    private static final Logger LOG = LogManager.getLogger(LAF.class);

    public static void setNimbusLookAndFeel() {
        setLookAndFeel(NimbusLookAndFeel.class.getName());
    }

    public static void setMetalLookAndFeel() {
        CustomUIDefaults.read();
        setLookAndFeel(MetalLookAndFeel.class.getName());
        CustomUIDefaults.write();
    }

    public static void setSystemLookAndFeel() {
        setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private static void setLookAndFeel(final String laf) {
        try {
            UIManager.setLookAndFeel(laf);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ex) {
            LOG.error("Could not set LAF: " + laf, ex);
        }
    }

}
