package com.mz.sshclient.ssh;

import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;
import net.schmizz.sshj.transport.TransportException;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class SshTtyConnector implements TtyConnector {

    private InputStreamReader inputStreamReader;
    private InputStream inputStream;
    private OutputStream outputStream;
    private SessionChannel shell;
    private Session channel;

    private Dimension myPendingTermSize;
    private Dimension myPendingPixelSize;

    private boolean initiated = false;
    private boolean canceled = false;
    private boolean stopped = false;

    private final SshClient sshClient;

    public SshTtyConnector(final SshClient sshClient) {
        this.sshClient = sshClient;
    }

    private void resizeImmediately() {
        if (myPendingTermSize != null && myPendingPixelSize != null) {
            setPtySize(shell, myPendingTermSize.width, myPendingTermSize.height, myPendingPixelSize.width,
                    myPendingPixelSize.height);
            myPendingTermSize = null;
            myPendingPixelSize = null;
        }
    }

    private void setPtySize(Session.Shell shell, int col, int row, int wp, int hp) {
        System.out.println("Exec pty resized:- col: " + col + " row: " + row + " wp: " + wp + " hp: " + hp);
        if (shell != null) {
            try {
                shell.changeWindowDimensions(col, row, wp, hp);
            } catch (TransportException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean init(Questioner q) {
        try {
            this.channel = sshClient.openSession();
            this.channel.setAutoExpand(true);

            this.channel.allocatePTY("xterm-256color", 80, 24, 0, 0, Collections.emptyMap());

            try {
                this.channel.setEnvVar("LANG", "en_US.UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Cannot set environment variable Lang: " + e.getMessage());
            }


            this.shell = (SessionChannel) this.channel.startShell();

            inputStream = shell.getInputStream();
            outputStream = shell.getOutputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            resizeImmediately();
            System.out.println("Initiated");

            initiated = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            initiated = false;
            canceled = true;
            return false;
        }
    }

    @Override
    public void close() {
        try {
            stopped = true;
            System.out.println("Terminal wrapper disconnecting");
            sshClient.disconnect();
        } catch (Exception e) {
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return 0;
    }

    @Override
    public void write(byte[] bytes) throws IOException {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void write(String string) throws IOException {

    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }
}
