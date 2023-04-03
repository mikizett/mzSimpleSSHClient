package com.mz.sshclient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import com.jediterm.terminal.ArrayTerminalDataStream;
import com.jediterm.terminal.TerminalMode;
import com.jediterm.terminal.emulator.JediEmulator;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.mz.sshclient.exceptions.ReadWriteConfigfileException;
import com.mz.sshclient.ui.config.AppConfig;
import com.mz.sshclient.ui.config.ConfigFile;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import com.newsrx.gui.TeeStream;

public class JediTermTest {

    static {
        Locale.setDefault(Locale.US);

        System.setProperty("java.net.useSystemProxies", "true");

        try {
            ConfigFile.init();
        } catch (ReadWriteConfigfileException e) {
            MessageDisplayUtil.showErrorMessage("Error occurred during read/write config file:\n" + e.getMessage());
            System.exit(-1);
        }

        System.setProperty("logPath", AppConfig.getLogFileLocation());
    }

    private static final Logger LOG = LogManager.getLogger(JediTermTest.class);

    private static final char ESC = 27;

    private static void termInit() throws InterruptedException, IOException {

        SettingsProvider settingsProvider = new DefaultSettingsProvider() {
            @Override
            public float getTerminalFontSize() {
                return 18;
            }
        };

        JFrame jframe = new JFrame();
        JediTermWidget tw = new JediTermWidget(settingsProvider);
        tw.getTerminal().setModeEnabled(TerminalMode.AutoNewLine, true);

        jframe.add(tw);

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(1280, 720);
        jframe.setVisible(true);

        JediEmulator je = new JediEmulator(new ArrayTerminalDataStream(new char[0]), tw.getTerminal());
        OutputStream terminalOutputStream = new OutputStream() {
            @Override
            public void write(int ch) throws IOException {
                je.processChar((char) ch, tw.getTerminal());
            }
        };

        PrintStream terminalPrintStream = new PrintStream(terminalOutputStream);
        PrintStream stdout = System.out;
        //TeeStream tee_stdout = new TeeStream(stdout, terminalPrintStream);
        //System.setOut(tee_stdout);
        System.out.print(ESC + "%G"); //set to UTF-8 mode
        System.out.print(ESC + "[31;44m"); //colors don't change?
        System.out.println("HELLO");
        System.out.print(ESC + "[2J"); //nothing is erased?
        System.out.print(ESC + "[32;43m"); //colors don't change?
        System.out.println("TEST");
        System.out.flush();
        System.setOut(stdout);
        Thread.sleep(1000);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        //termInit();

        loadSpinner();
    }

    private static void loadSpinner() {
        JWindow window = new JWindow();
        window.getContentPane().add(new JLabel("Loading", SwingConstants.CENTER));
        window.setBounds(500, 150, 300, 200);
        window.setVisible(true);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        window.setVisible(false);
        JFrame frame = new JFrame();
        frame.add(new JLabel("Welcome Swing application..."));
        frame.setVisible(true);
        frame.setSize(300, 200);
        window.dispose();
    }


}
