package com.mz.sshclient.ui.components.tabs.sftp.transfer;

import com.mz.sshclient.ssh.sftp.ConflictAction;
import com.mz.sshclient.ssh.sftp.SFtpConnector;
import com.mz.sshclient.ssh.sftp.SshRemoteFileInputStream;
import com.mz.sshclient.ssh.sftp.SshRemoteFileOutputStream;
import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;
import com.mz.sshclient.ssh.sftp.filesystem.IFileSystem;
import com.mz.sshclient.ssh.sftp.filesystem.InputTransferChannel;
import com.mz.sshclient.ssh.sftp.filesystem.OutputTransferChannel;
import com.mz.sshclient.ssh.sftp.filesystem.sftp.SshFileSystem;
import com.mz.sshclient.utils.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileTransfer implements Runnable, AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(FileTransfer.class);

    private static final int BUF_SIZE = Short.MAX_VALUE;
    private final IFileSystem sourceFs;
    private final IFileSystem targetFs;
    private final FileInfo[] files;
    private final String targetFolder;
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);
    private final SFtpConnector instance;
    private long totalSize;
    private FileTransferProgress callback;
    private long processedBytes;
    private int processedFilesCount;
    private long totalFiles;
    private ConflictAction conflictAction = ConflictAction.PROMPT; // 0 -> overwrite, 1 -> auto rename, 2

    public FileTransfer(
            IFileSystem sourceFs,
            IFileSystem targetFs,
            FileInfo[] files,
            String targetFolder,
            FileTransferProgress callback,
            ConflictAction defaultConflictAction,
            SFtpConnector instance
    ) {
        this.sourceFs = sourceFs;
        this.targetFs = targetFs;
        this.files = files;
        this.targetFolder = targetFolder;
        this.callback = callback;
        this.conflictAction = defaultConflictAction;
        this.instance = instance;
        if (defaultConflictAction == ConflictAction.CANCEL) {
            throw new IllegalArgumentException("defaultConflictAction can not be ConflictAction.Cancel");
        }
    }

    private void transfer(String targetFolder) throws Exception {
        List<FileInfoHolder> fileList = new ArrayList<>();
        List<FileInfo> list = targetFs.list(targetFolder);
        List<FileInfo> dupList = new ArrayList<>();

        if (this.conflictAction == ConflictAction.PROMPT) {
            this.conflictAction = checkForConflict(dupList);
            if (dupList.size() > 0 && this.conflictAction == ConflictAction.CANCEL) {
                LOG.debug("Operation cancelled by user");
                return;
            }
        }

        totalSize = 0;
        for (FileInfo file : files) {
            if (stopFlag.get()) {
                return;
            }

            String proposedName = null;
            if (isDuplicate(list, file.getName())) {
                if (this.conflictAction == ConflictAction.AUTORENAME) {
                    proposedName = generateNewName(list, file.getName());
                } else if (this.conflictAction == ConflictAction.SKIP) {
                    continue;
                }
            }

            if (file.getType() == FileType.Directory || file.getType() == FileType.DirLink) {
                fileList.addAll(createFileList(file, targetFolder, proposedName));
            } else {
                fileList.add(new FileInfoHolder(file, targetFolder, proposedName));
                totalSize += file.getSize();
            }
        }
        totalFiles = fileList.size();

        callback.init(totalSize, totalFiles, this);
        InputTransferChannel inc = sourceFs.inputTransferChannel();
        OutputTransferChannel outc = targetFs.outputTransferChannel();
        for (FileInfoHolder file : fileList) {
            if (stopFlag.get()) {
                return;
            }
            copyFile(file.info, file.targetPath, file.proposedName, inc, outc);
            processedFilesCount++;
        }
    }

    @Override
    public void run() {
        try {
            try {
                transfer(this.targetFolder);
                callback.done(this);
            } catch (AccessDeniedException e) {
                LOG.error(e);
                if (targetFs instanceof SshFileSystem) {
                    throw e;
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            if (stopFlag.get()) {
                callback.done(this);
                return;
            }
            callback.error("Error", this);
        }
    }

    private List<FileInfoHolder> createFileList(FileInfo folder, String target, String proposedName) throws Exception {
        if (stopFlag.get()) {
            throw new Exception("Interrupted");
        }
        String folderTarget = PathUtils.combineUnix(target, proposedName == null ? folder.getName() : proposedName);
        targetFs.mkdir(folderTarget);
        List<FileInfoHolder> fileInfoHolders = new ArrayList<>();
        List<FileInfo> list = sourceFs.list(folder.getPath());
        for (FileInfo file : list) {
            if (stopFlag.get()) {
                throw new Exception("Interrupted");
            }
            if (file.getType() == FileType.Directory) {
                fileInfoHolders.addAll(createFileList(file, folderTarget, null));
            } else if (file.getType() == FileType.File) {
                fileInfoHolders.add(new FileInfoHolder(file, folderTarget, null));
                totalSize += file.getSize();
            }
        }
        return fileInfoHolders;
    }

    private synchronized void copyFile(
            FileInfo file,
            String targetDirectory,
            String proposedName,
            InputTransferChannel inc,
            OutputTransferChannel outc
    ) throws Exception {

        String outPath = PathUtils.combine(targetDirectory, proposedName == null ? file.getName() : proposedName,
                outc.getSeparator());
        String inPath = file.getPath();
        try (InputStream in = inc.getInputStream(inPath); OutputStream out = outc.getOutputStream(outPath)) {
            long len = inc.getSize(inPath);

            int bufferCapacity = BUF_SIZE;
            if (in instanceof SshRemoteFileInputStream && out instanceof SshRemoteFileOutputStream) {
                bufferCapacity = Math.min(((SshRemoteFileInputStream) in).getBufferCapacity(),
                        ((SshRemoteFileOutputStream) out).getBufferCapacity());
            } else if (in instanceof SshRemoteFileInputStream) {
                bufferCapacity = ((SshRemoteFileInputStream) in).getBufferCapacity();
            } else if (out instanceof SshRemoteFileOutputStream) {
                bufferCapacity = ((SshRemoteFileOutputStream) out).getBufferCapacity();
            }

            byte[] buf = new byte[bufferCapacity];

            while (len > 0 && !stopFlag.get()) {
                int x = in.read(buf);
                if (x == -1)
                    throw new IOException("Unexpected EOF");
                out.write(buf, 0, x);
                len -= x;
                processedBytes += x;
                callback.progress(processedBytes, totalSize, processedFilesCount, totalFiles, this);
            }
            out.flush();
        }
    }

    public void stop() {
        stopFlag.set(true);
    }

    @Override
    public void close() {
        stopFlag.set(true);
    }

    public FileInfo[] getFiles() {
        return files;
    }

    public String getTargetFolder() {
        return this.targetFolder;
    }

    private ConflictAction checkForConflict(List<FileInfo> dupList) throws Exception {
        List<FileInfo> fileList = targetFs.list(targetFolder);
        for (FileInfo file : files) {
            for (FileInfo file1 : fileList) {
                if (file.getName().equals(file1.getName())) {
                    dupList.add(file);
                }
            }
        }

        ConflictAction action = ConflictAction.CANCEL;
        if (!dupList.isEmpty()) {

            DefaultComboBoxModel<ConflictAction> conflictOptionsCmb = new DefaultComboBoxModel<>(ConflictAction.values());
            conflictOptionsCmb.removeAllElements();
            for (ConflictAction conflictActionCmb : ConflictAction.values()) {
                if (conflictActionCmb.getKey() < 3) {
                    conflictOptionsCmb.addElement(conflictActionCmb);
                }
            }
            JComboBox<ConflictAction> cmbs = new JComboBox<>(conflictOptionsCmb);

            if (JOptionPane.showOptionDialog(null,
                    new Object[]{"Some file with the same name already exists. Please choose an action", cmbs},
                    "Action required", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                    null) == JOptionPane.YES_OPTION) {
                action = (ConflictAction) cmbs.getSelectedItem();
            }
        }

        return action;
    }

    private boolean isDuplicate(List<FileInfo> list, String name) {
        for (FileInfo s : list) {
            if (s.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public String generateNewName(List<FileInfo> list, String name) {
        while (isDuplicate(list, name)) {
            name = "Copy-of-" + name;
        }
        return name;
    }

    public String getSourceName() {
        return this.sourceFs.getName();
    }

    public String getTargetName() {
        return this.targetFs.getName();
    }

    public void setCallback(FileTransferProgress callback) {
        this.callback = callback;
    }

    public IFileSystem getSourceFs() {
        return sourceFs;
    }

    public IFileSystem getTargetFs() {
        return targetFs;
    }

    static class FileInfoHolder {
        FileInfo info;
        String targetPath;
        String proposedName;

        public FileInfoHolder(FileInfo info, String targetPath, String proposedName) {
            this.info = info;
            this.targetPath = targetPath;
            this.proposedName = proposedName;
        }
    }

}
