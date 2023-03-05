package com.mz.sshclient;

import com.mz.sshclient.exceptions.ReadWriteConfigfileException;
import com.mz.sshclient.ui.config.ConfigFile;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import com.mz.sshclient.ui.utils.UIUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Locale;
import java.util.concurrent.Executors;

public class mzSimpleSSHClientMain {

    static {
        Locale.setDefault(Locale.US);

        System.setProperty("java.net.useSystemProxies", "true");

        try {
            ConfigFile.init();
        } catch (ReadWriteConfigfileException e) {
            MessageDisplayUtil.showErrorMessage("Error occurred during read/write config file:\n" + e.getMessage());
            System.exit(-1);
        }

        final String location = ConfigFile.getSessionsLocation();
        System.setProperty("logPath", location + File.separatorChar + Constants.LOG_PATH_NAME);
    }

    private static final Logger LOG = LogManager.getLogger(mzSimpleSSHClientMain.class);

    private static void initBountyCastleCryptographySecurityProvider() {
        Security.addProvider(new BouncyCastleProvider());

        Security.setProperty("networkaddress.cache.ttl", "1");
        Security.setProperty("networkaddress.cache.negative.ttl", "1");
        Security.setProperty("crypto.policy", "unlimited");
    }

    private static void checkMaxLenAES() {
        try {
            int maxKeySize = Cipher.getMaxAllowedKeyLength("AES");

            LOG.debug("cryptography maxKeySize: " + maxKeySize);

            if (maxKeySize < Integer.MAX_VALUE) {
                MessageDisplayUtil.showErrorMessage("Unlimited cryptography is not enabled in JVM");
            }
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Could not get the max allowed cryptography key length", e);
        }
    }

    private static void preloadJediTermLib() {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Class.forName("com.jediterm.terminal.ui.JediTermWidget");
            } catch (ClassNotFoundException e) {
                LOG.warn("Could not preload jedi-term-lib", e);
            }
        });
    }

    public static void main(String[] args) {
        //UiUtils.setNimbusLookAndFeel();
        UIUtils.setMetalLookAndFeel();
        initBountyCastleCryptographySecurityProvider();
        checkMaxLenAES();
        preloadJediTermLib();
    }
}
