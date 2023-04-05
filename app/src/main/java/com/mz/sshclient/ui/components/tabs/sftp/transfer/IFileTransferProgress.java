package com.mz.sshclient.ui.components.tabs.sftp.transfer;

public interface IFileTransferProgress {

    void init(long totalSize, long files, FileTransfer fileTransfer);

    void progress(long processedBytes, long totalBytes, long processedCount, long totalCount, FileTransfer fileTransfer);

    void error(String cause, FileTransfer fileTransfer);

    void done(FileTransfer fileTransfer);

}
