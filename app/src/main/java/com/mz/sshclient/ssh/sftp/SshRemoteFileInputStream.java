package com.mz.sshclient.ssh.sftp;

import net.schmizz.sshj.sftp.RemoteFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class SshRemoteFileInputStream extends InputStream {

    private static final Logger LOG = LogManager.getLogger(SshRemoteFileInputStream.class);

    private final RemoteFile remoteFile;
    private final InputStream in;
    private final int bufferCapacity;

    public SshRemoteFileInputStream(RemoteFile remoteFile, int localMaxPacketSize) {
        this.remoteFile = remoteFile;
        this.bufferCapacity = localMaxPacketSize;
        this.in = this.remoteFile.new ReadAheadRemoteFileInputStream(16);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.in.read(b, off, len);
    }

    @Override
    public int read() throws IOException {
        return this.in.read();
    }

    @Override
    public void close() {
        try {
            this.remoteFile.close();
        } catch (Exception e) {
            LOG.error(e);
        }
        try {
            this.in.close();
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

}
