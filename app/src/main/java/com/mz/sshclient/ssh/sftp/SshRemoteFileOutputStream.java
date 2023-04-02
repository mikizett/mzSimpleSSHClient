package com.mz.sshclient.ssh.sftp;

import net.schmizz.sshj.sftp.RemoteFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class SshRemoteFileOutputStream extends OutputStream {

    private static final Logger LOG = LogManager.getLogger(SshRemoteFileOutputStream.class);

    private int bufferCapacity;
    private final RemoteFile remoteFile;
    private final OutputStream remoteFileOutputStream;

    public SshRemoteFileOutputStream(RemoteFile remoteFile, int remoteMaxPacketSize) {
        this.remoteFile = remoteFile;
        this.bufferCapacity = remoteMaxPacketSize - this.remoteFile.getOutgoingPacketOverhead();
        this.remoteFileOutputStream = this.remoteFile.new RemoteFileOutputStream(0, 16);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        remoteFileOutputStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        remoteFileOutputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        LOG.debug(getClass().getName() + " closing");
        try {
            remoteFile.close();
        } catch (Exception e) {
            LOG.error(e);
        }
        try {
            remoteFileOutputStream.close();
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    @Override
    public void flush() throws IOException {
        LOG.debug(getClass().getName() + " flushing");
        remoteFileOutputStream.flush();
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

}
