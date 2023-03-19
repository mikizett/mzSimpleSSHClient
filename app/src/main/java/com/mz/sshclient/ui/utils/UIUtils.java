package com.mz.sshclient.ui.utils;

import com.mz.sshclient.Constants;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFileChooser;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UIUtils {

    private static final Logger LOG = LogManager.getLogger(UIUtils.class);

    private UIUtils() {}

    private static void setLookAndFeel(final String laf) {
        try {
            UIManager.setLookAndFeel(laf);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LOG.error("Could not set LAF: " + laf, ex);
        }
    }

    /**
     * Set the Nimbus look and feel
     * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    public static void setNimbusLookAndFeel() {
        setLookAndFeel(NimbusLookAndFeel.class.getName());
    }

    public static void setMetalLookAndFeel() {
        setLookAndFeel(MetalLookAndFeel.class.getName());
    }

    public static void setSystemLookAndFeel() {
        setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }


    public static void selectSessionStorageLocation() {
        final FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Only directories";
            }
        };

        final JFileChooser fileChooser = new JFileChooser(new File(Constants.USER_HOME));
        fileChooser.setDialogTitle("Select folder where to store sessions...");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogType(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDragEnabled(false);
        fileChooser.addChoosableFileFilter(fileFilter);
        fileChooser.setFileFilter(fileFilter);

        int selectedResult = fileChooser.showOpenDialog(null);
        if (selectedResult == JFileChooser.APPROVE_OPTION) {

        }
    }

    public static String showFileChooserPrivateKey(final Component parentComponent) {
        String result = null;

        final JFileChooser fileChooser = new JFileChooser(getSshHomeFolder());
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(parentComponent)) == JFileChooser.APPROVE_OPTION) {
            result = fileChooser.getSelectedFile().getAbsolutePath();
        }

        return result;
    }

    public static void addAncestorAndFocusListenerToPasswordField(final JPasswordField comp) {
        comp.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorRemoved(final AncestorEvent event) {}

            @Override
            public void ancestorMoved(final AncestorEvent event) {}

            @Override
            public void ancestorAdded(final AncestorEvent event) {
                comp.requestFocusInWindow();
            }
        });

        comp.addFocusListener(new FocusListener() {
            private boolean isFirstTime = true;

            @Override
            public void focusGained(final FocusEvent e) {}

            @Override
            public void focusLost(final FocusEvent e) {
                if (isFirstTime) {
                    comp.requestFocusInWindow();
                    isFirstTime = false;
                }
            }
        });
    }

    private static String getSshHomeFolder() {
        String pathAsString = new StringBuilder(SystemUtils.USER_HOME).append(File.separatorChar).append(".ssh").toString();
        final Path path = Paths.get(pathAsString);
        if (!Files.exists(path)) {
            pathAsString = SystemUtils.USER_HOME;
        }
        return pathAsString;
    }

}
