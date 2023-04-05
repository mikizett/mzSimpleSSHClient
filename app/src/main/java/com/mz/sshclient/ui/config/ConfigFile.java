package com.mz.sshclient.ui.config;

import com.mz.sshclient.Constants;
import com.mz.sshclient.exceptions.ReadWriteConfigfileException;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class ConfigFile {
    private ConfigFile() {}

    private static final File CONFIG_FILE = new File(Constants.CONFIG_FILE_LOCATION);
    private static String storageLocation;

    public static String getStorageLocation() {
        return storageLocation;
    }

    public static void init() throws ReadWriteConfigfileException {
        if (!CONFIG_FILE.exists() && !CONFIG_FILE.isFile()) {
            final StringBuilder b = new StringBuilder();
            b.append("Do you want to use the default folder to store sessions?\n")
                    .append("Default folder is located in: " + Constants.DEFAULT_STORAGE_LOCATION);

            int result = MessageDisplayUtil.showYesNoConfirmDialog(b.toString(), "Select folder for storing sessions...");
            if (result == JOptionPane.NO_OPTION) {
                showDirectoryChooser();
            } else {
                writeConfigFile(Constants.DEFAULT_STORAGE_LOCATION);
            }
        } else {
            readConfigFile();
        }
    }

    private static void showDirectoryChooser() throws ReadWriteConfigfileException {
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
            final File selectedLocation = fileChooser.getSelectedFile();
            if (!selectedLocation.isDirectory() || !selectedLocation.canWrite()) {
                MessageDisplayUtil.showErrorMessage("The selected folder is not writable!\nPlease select another one");
                showDirectoryChooser();
            } else {
                writeConfigFile(selectedLocation.getAbsolutePath() + File.separatorChar + Constants.DEFAULT_STORAGE_PATH_NAME);
            }
        } else { // show the question again what to do
            init();
        }
    }

    private static void readConfigFile() throws ReadWriteConfigfileException {
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            storageLocation = bufferedReader.readLine();
        } catch (IOException e) {
            throw new ReadWriteConfigfileException("Cannot write storage location to file", e);
        }
    }

    private static void writeConfigFile(final String location) throws ReadWriteConfigfileException {
        if (storageLocation == null) {
            storageLocation = location;
        }

        try (final FileWriter fileWriter = new FileWriter(CONFIG_FILE)) {
            fileWriter.write(location);
            fileWriter.flush();
        } catch (IOException e) {
            throw new ReadWriteConfigfileException("Cannot write storage location to file!", e);
        }
    }

}
