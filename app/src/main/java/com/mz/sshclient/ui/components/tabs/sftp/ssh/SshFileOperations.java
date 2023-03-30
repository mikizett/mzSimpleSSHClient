package com.mz.sshclient.ui.components.tabs.sftp.ssh;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.sftp.SFtpConnector;
import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.utils.PathUtils;
import com.mz.sshclient.utils.SudoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SshFileOperations {

    private static final Logger LOG = LogManager.getLogger(SshFileOperations.class);

    public static void delete(List<FileInfo> files, SFtpConnector instance) throws Exception {
        StringBuilder sb = new StringBuilder("rm -rf ");

        for (FileInfo file : files) {
            sb.append("\"" + file.getPath() + "\" ");
        }

        if (instance.exec(sb.toString(), new AtomicBoolean(false)) != 0) {
            throw new FileNotFoundException("Operation failed");
        }
    }

    public boolean moveTo(
            SFtpConnector instance,
            List<FileInfo> files,
            String targetFolder,
            IFileSystem fs,
            String password
    ) throws Exception {
        List<FileInfo> fileList = fs.list(targetFolder);
        List<FileInfo> dupList = new ArrayList<>();
        for (FileInfo file : files) {
            for (FileInfo file1 : fileList) {
                if (file.getName().equals(file1.getName())) {
                    dupList.add(file);
                }
            }
        }

        int action = -1;
        if (!dupList.isEmpty()) {
            JComboBox<String> cmbs = new JComboBox<>(
                    new String[] {
                            "Auto rename",
                            "Overwrite"
                    });
            if (JOptionPane.showOptionDialog(null,
                    new Object[] {
                            "Some file with the same name already exists. Please choose an action",
                            cmbs
                    }, "Action required", JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null,
                    null) == JOptionPane.YES_OPTION) {
                action = cmbs.getSelectedIndex();
            } else {
                return false;
            }
        }

        StringBuilder command = new StringBuilder();
        for (FileInfo fileInfo : files) {
            if (fileInfo.getType() == FileType.DirLink
                    || fileInfo.getType() == FileType.Directory) {
                command.append("mv ");
            } else {
                command.append("mv -T ");
            }
            command.append("\"" + fileInfo.getPath() + "\" ");
            if (dupList.contains(fileInfo) && action == 0) {
                command.append("\""
                        + PathUtils.combineUnix(targetFolder,
                        getUniqueName(fileList, fileInfo.getName()))
                        + "\"; ");
            } else {
                command.append("\"" + PathUtils.combineUnix(targetFolder,
                        fileInfo.getName()) + "\"; ");
            }
        }

        if (instance.exec(command.toString(), new AtomicBoolean(false)) != 0) {
            int ret = SudoUtils.runSudo(command.toString(), instance, password);
            if (ret != -1) {
                return ret == 0;
            }

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }
        } else {
            return true;
        }
        return false;
    }

    public boolean copyTo(
            SFtpConnector instance,
            List<FileInfo> files,
            String targetFolder,
            IFileSystem fs,
            String password
    ) throws Exception {
        List<FileInfo> fileList = fs.list(targetFolder);
        List<FileInfo> dupList = new ArrayList<>();
        for (FileInfo file : files) {
            for (FileInfo file1 : fileList) {
                if (file.getName().equals(file1.getName())) {
                    dupList.add(file);
                }
            }
        }

        int action = -1;
        if (!dupList.isEmpty()) {
            JComboBox<String> cmbs = new JComboBox<>(
                    new String[] {
                            "Auto rename",
                            "Overwrite"
                    });
            if (JOptionPane.showOptionDialog(null, new Object[] {
                            "Some file with the same name already exists. Please choose an action",
                            cmbs
                    }, "Action required", JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null,
                    null) == JOptionPane.YES_OPTION) {
                action = cmbs.getSelectedIndex();
            } else {
                return false;
            }
        }

        StringBuilder command = new StringBuilder();
        for (FileInfo fileInfo : files) {
            if (fileInfo.getType() == FileType.DirLink
                    || fileInfo.getType() == FileType.Directory) {
                command.append("cp -rf ");
            } else {
                command.append("cp -Tf ");
            }
            command.append("\"" + fileInfo.getPath() + "\" ");
            if (dupList.contains(fileInfo) && action == 0) {
                command.append("\""
                        + PathUtils.combineUnix(targetFolder,
                        getUniqueName(fileList, fileInfo.getName()))
                        + "\"; ");
            } else {
                command.append("\"" + PathUtils.combineUnix(targetFolder,
                        fileInfo.getName()) + "\"; ");
            }
        }

        if (instance.exec(command.toString(), new AtomicBoolean(false)) != 0) {
            int ret = SudoUtils.runSudo(command.toString(), instance, password);
            if (ret != -1) {
                return ret == 0;
            }

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }
        } else {
            return true;
        }
        return false;
    }

    private String getUniqueName(List<FileInfo> list, String name) {
        while (true) {
            boolean found = false;
            for (FileInfo f : list) {
                if (name.equals(f.getName())) {
                    name = "Copy of " + name;
                    found = true;
                    break;
                }
            }
            if (!found)
                break;
        }
        return name;
    }

    public boolean rename(
            String oldName,
            String newName,
            IFileSystem fs,
            SFtpConnector instance, String password) {
        try {
            fs.rename(oldName, newName);
            return true;
        } catch (AccessDeniedException e) {
            LOG.error(e);

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }
            return false;
        } catch (Exception e) {
            LOG.error(e);

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }
            return false;
        }
    }

    public boolean delete(
            FileInfo[] targetList,
            IFileSystem fs,
            SFtpConnector instance,
            String password
    ) {
        try {
            try {
                delete(Arrays.asList(targetList), instance);
                return true;
            } catch (FileNotFoundException e) {
                LOG.error(e);
                throw e;
            } catch (Exception e) {
                LOG.error(e);
                for (FileInfo s : targetList) {
                    fs.delete(s);
                }
                return true;
            }
        } catch (FileNotFoundException | AccessDeniedException e) {
            LOG.error(e);

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }
            return false;

        } catch (Exception e) {
            LOG.error(e);
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Error deleting file");
            }
            return false;
        }
    }

    public boolean newFile(FileInfo[] files, IFileSystem fs, String folder, SFtpConnector instance, String password) {
        String text = JOptionPane.showInputDialog("New file");
        if (text == null || text.length() < 1) {
            return false;
        }
        boolean alreadyExists = false;
        for (FileInfo f : files) {
            if (f.getName().equals(text)) {
                alreadyExists = true;
                break;
            }
        }
        if (alreadyExists) {
            JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "File with same name already exists");
            return false;
        }
        try {
            fs.createFile(PathUtils.combineUnix(folder, text));
            return true;
        } catch (AccessDeniedException e) {
            LOG.error(e);

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }

            return false;
        } catch (Exception e) {
            LOG.error(e);
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }
        }
        return false;
    }

    public boolean newFolder(FileInfo[] files, String folder, IFileSystem fs, SFtpConnector instance, String password) {
        String text = JOptionPane.showInputDialog("New folder name");
        if (text == null || text.length() < 1) {
            return false;
        }
        boolean alreadyExists = false;
        for (FileInfo f : files) {
            if (f.getName().equals(text)) {
                alreadyExists = true;
                break;
            }
        }
        if (alreadyExists) {
            JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "File with same name already exists");
            return false;
        }
        try {
            fs.mkdir(PathUtils.combineUnix(folder, text));
            return true;
        } catch (AccessDeniedException e) {
            LOG.error(e);

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }
            return false;

        } catch (Exception e) {
            LOG.error(e);

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(mzSimpleSshClientMain.MAIN_FRAME, "Operation failed");
            }
        }
        return false;
    }

}
