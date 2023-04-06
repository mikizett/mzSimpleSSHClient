package com.mz.sshclient.ui.laf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.Font;
import java.io.InputStream;

public final class LAF {

    private LAF() {}

    private static final Logger LOG = LogManager.getLogger(LAF.class);

    public static void setNimbusLookAndFeel() {
        setLookAndFeel(NimbusLookAndFeel.class.getName());
    }

    public static void setMetalLookAndFeel() {
        setLookAndFeel(MetalLookAndFeel.class.getName());
    }

    public static void setSystemLookAndFeel() {
        setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private static void setLookAndFeel(final String laf) {
        try {
            CustomUIDefaults.read();

            UIManager.setLookAndFeel(laf);

            CustomUIDefaults.write();

            UIManager.getDefaults().put("iconFont", loadFontAwesome());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ex) {
            LOG.error("Could not set LAF: " + laf, ex);
        }
    }

    private static Font loadFontAwesome() {
        try (InputStream is = LAF.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf")) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(Font.PLAIN, 14f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
