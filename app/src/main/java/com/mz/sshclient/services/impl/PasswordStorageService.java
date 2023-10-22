package com.mz.sshclient.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mz.sshclient.model.session.SessionFolderModel;
import com.mz.sshclient.model.session.SessionItemModel;
import com.mz.sshclient.services.exceptions.PasswordStorageException;
import com.mz.sshclient.services.interfaces.IPasswordStorageService;
import com.mz.sshclient.ui.config.AppConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

public class PasswordStorageService implements IPasswordStorageService {

    private static final Logger LOG = LogManager.getLogger(PasswordStorageService.class);

    private static KeyStore keyStore;

    private final File passwordStorageFile = new File(AppConfig.getPasswordStorageFilePath());
    private Map<String, char[]> passwordMap = new HashMap<>(0);

    private KeyStore.PasswordProtection passwordProtection;

    private volatile boolean unlockedPasswordStorage = false;

    public PasswordStorageService() throws PasswordStorageException {
        try {
            keyStore = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException e) {
            throw new PasswordStorageException("Could not instantiate keyStore", e);
        }
    }

    private char[] serializePasswordMap(Map<String, char[]> map) throws PasswordStorageException {
        final CharArrayWriter writer = new CharArrayWriter();
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(writer, map);
        } catch (IOException e) {
            throw new PasswordStorageException("Could not write password");
        }
        return writer.toCharArray();
    }

    private Map<String, char[]> deserializePasswordMap(char[] chars) throws PasswordStorageException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(new CharArrayReader(chars), new TypeReference<>() {});
        } catch (IOException e) {
            throw new PasswordStorageException("Could not read passwords from password storage file");
        }
    }

    private void saveKeyStore() throws PasswordStorageException {
        try {
            final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBE");
            final SecretKey generatedSecret = secretKeyFactory.generateSecret(new PBEKeySpec(serializePasswordMap(this.passwordMap)));

            keyStore.setEntry("passwords", new KeyStore.SecretKeyEntry(generatedSecret), passwordProtection);

            LOG.debug("Password protection: " + passwordProtection.getProtectionAlgorithm());

            // check if file does not exist
            // in this case we have to create the folders
            if (!passwordStorageFile.exists() || !passwordStorageFile.isFile()) {
                boolean result = new File(AppConfig.getPasswordStorageLocation()).mkdirs();
                if (!result) {
                    throw new PasswordStorageException("Could not create folder for the password storage file: <" + passwordStorageFile.getName() + ">");
                }
            }

            try (OutputStream out = new FileOutputStream(passwordStorageFile)) {
                keyStore.store(out, passwordProtection.getPassword());
            } catch (IOException | CertificateException e) {
                throw new PasswordStorageException("Could not save password to password storage file: <" + passwordStorageFile.getName() + ">");
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | KeyStoreException e) {
            throw new PasswordStorageException("Could not save password to password storage file: <" + passwordStorageFile.getName() + ">");
        }
    }

    private void loadPasswords() throws PasswordStorageException {
        try {
            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
            final KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) keyStore.getEntry("passwords", passwordProtection);
            final PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);
            final char[] chars = keySpec.getPassword();
            this.passwordMap = deserializePasswordMap(chars);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | InvalidKeySpecException e) {
            throw new PasswordStorageException("Could not load passwords from password storage file");
        }
    }

    private char[] getStoredPassword(final String id) {
        return this.passwordMap.get(id);
    }

    public void storePassword(final String id, final char[] password) {
        this.passwordMap.put(id, password);
    }

    @Override
    public boolean existStorageFile() {
        return passwordStorageFile.isFile() && passwordStorageFile.exists() && passwordStorageFile.canWrite();
    }

    @Override
    public void setPasswordsToModel(final SessionFolderModel model) {
        if (model != null && existStorageFile()) {
            model.getItems().forEach((item) -> {
                final char[] password = getStoredPassword(item.getId());
                if (password != null) {
                    item.setPassword(new String(password));
                }
            });

            model.getFolders().forEach((folder) -> setPasswordsToModel(folder));
        }
    }

    @Override
    public void storePassword(SessionItemModel sessionItemModel) throws PasswordStorageException {
        if (StringUtils.isNotBlank(sessionItemModel.getPassword())) {
            storePassword(sessionItemModel.getId(), sessionItemModel.getPassword().toCharArray());
            saveKeyStore();
        }
    }

    @Override
    public void unlockPasswordStorage(char[] masterPassword) throws PasswordStorageException {
        if (!unlockedPasswordStorage) {
            passwordProtection = new KeyStore.PasswordProtection(masterPassword, "PBEWithHmacSHA256AndAES_256", null);
            if (!existStorageFile()) {
                try {
                    keyStore.load(null, passwordProtection.getPassword());
                    unlockedPasswordStorage = true;
                } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                    throw new PasswordStorageException("Could not set master password protection");
                }
            } else {
                try (InputStream in = new FileInputStream(passwordStorageFile)) {
                    keyStore.load(in, passwordProtection.getPassword());
                    loadPasswords();
                    unlockedPasswordStorage = true;
                } catch (FileNotFoundException e) {
                    throw new PasswordStorageException("Could not read password storage file: <" + passwordStorageFile.getName() + ">");
                } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                    throw new PasswordStorageException("Could not set master password protection");
                }
            }
        }
    }

    @Override
    public void addMasterPassword(char[] masterPassword) throws PasswordStorageException {
        unlockPasswordStorage(masterPassword);

        passwordProtection = new KeyStore.PasswordProtection(masterPassword, "PBEWithHmacSHA256AndAES_256", null);

        saveKeyStore();
    }

    @Override
    public boolean isUnlockedPasswordStorage() {
        return unlockedPasswordStorage;
    }
}
