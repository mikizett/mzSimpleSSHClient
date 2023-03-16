package com.mz.sshclient.ui.utils;

import com.mz.sshclient.Constants;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.io.File;

public final class UIUtils {
    private UIUtils() {}

    private static final File CONFIG_FILE = new File(Constants.CONFIG_FILE_LOCATION);

    private static void setLookAndFeel(final String laf) {
        try {
            UIManager.setLookAndFeel(laf);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
}
