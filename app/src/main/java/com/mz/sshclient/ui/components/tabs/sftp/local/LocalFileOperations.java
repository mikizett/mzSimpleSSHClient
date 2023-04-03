package com.mz.sshclient.ui.components.tabs.sftp.local;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.local.LocalFileSystem;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import com.mz.sshclient.utils.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFileOperations {

    private static final Logger LOG = LogManager.getLogger(LocalFileOperations.class);

    public boolean rename(String oldName, String newName) {
        boolean result = false;
        try {
            Files.move(Paths.get(oldName), Paths.get(newName));
            result = true;
        } catch (IOException e) {
            LOG.error(e);
        }
        return result;
    }

    public boolean newFile(String folder) {
        String text = JOptionPane.showInputDialog("New file");
        if (StringUtils.isEmpty(text)) {
            return false;
        }
        LocalFileSystem fs = new LocalFileSystem();
        try {
            fs.createFile(PathUtils.combine(folder, text, File.separator));
            return true;
        } catch (Exception e) {
            LOG.error(e);
            MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, "Unable to create new file");
        }
        return false;
    }

    public boolean newFolder(String folder) {
        String text = JOptionPane.showInputDialog("New folder name");
        if (text == null || text.length() < 1) {
            return false;
        }
        IFileSystem fs = new LocalFileSystem();
        try {
            fs.mkdir(PathUtils.combine(folder, text, fs.getSeparator()));
            return true;
        } catch (Exception e) {
            LOG.error(e);
            MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, "Unable to create new folder");
        }
        return false;
    }

}
