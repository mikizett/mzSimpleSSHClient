package com.mz.sshclient.ui.components.tabs.terminal;

import com.mz.sshclient.model.SessionFolderModel;
import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.services.ServiceRegistry;
import com.mz.sshclient.services.exceptions.PasswordStorageException;
import com.mz.sshclient.services.interfaces.IPasswordStorageService;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import com.mz.sshclient.ui.utils.UIUtils;
import com.mz.sshclient.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public final class PasswordStorageHandler {

    private static final Logger LOG = LogManager.getLogger(PasswordStorageHandler.class);

    private static final PasswordStorageHandler INSTANCE = new PasswordStorageHandler();

    private final ISessionDataService sessionDataService = ServiceRegistry.get(ISessionDataService.class);
    private final IPasswordStorageService passwordStorageService = ServiceRegistry.get(IPasswordStorageService.class);

    public static PasswordStorageHandler getHandler() {
        return INSTANCE;
    }

    private String getMessageToStoreSessionsWithMasterPassword() {
        return new StringBuilder("Please add a master password to store sessions with passwords.\n\n")
                .append("This password will be used\n").append("to unlock the password storage file.\n\n")
                .append("If you forget the master password the password storage can't be used!\n")
                .append("In this case you have to add the passwords again!\n\n")
                .append("The passwords are BASE64 encoded and\n").append("the password storage file is AES encrypted.\n")
                .append(" ")
                .toString();
    }

    private String getMessageToReadSessionsWithMasterPassword() {
        return new StringBuilder("Please add the master password\n")
                .append("to unlock the password storage file.\n").append(" ")
                .toString();
    }

    private MasterPasswordAnswer showMasterPasswordDialog(final String title) {
        final JPasswordField passwordField = new JPasswordField(30);
        UIUtils.addAncestorAndFocusListenerToPasswordField(passwordField);

        int result = JOptionPane.showOptionDialog(
                mzSimpleSshClientMain.MAIN_FRAME,
                new Object[] {
                        title,
                        passwordField
                },
                "Master password",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null);

        return new MasterPasswordAnswer(result, passwordField.getPassword());
    }

    public void unlockPasswordStorage(final SessionFolderModel sessionFolderModel) {
        if (passwordStorageService.existStorageFile() && !passwordStorageService.isUnlockedPasswordStorage()) {
            while (true) {
                try {
                    final MasterPasswordAnswer answer = showMasterPasswordDialog(getMessageToReadSessionsWithMasterPassword());
                    if (answer.getAnswerType() == JOptionPane.YES_OPTION) {
                        char[] password = answer.getPassword();
                        final char[] passwordEncoded = Utils.encodeStringAsCharArray(new String(password));
                        passwordStorageService.unlockPasswordStorage(passwordEncoded);

                        passwordStorageService.setPasswordsToModel(sessionFolderModel);
                        // used to have a comparison
                        passwordStorageService.setPasswordsToModel(sessionDataService.getDefaultSessionModel().getFolder());

                        // reset passwd
                        password = new char[] {'0'};
                        break;
                    } else {
                        final String message = new StringBuilder("If you don't add the master password, you can't use the stored passwords for the sessions.\n\n")
                                .append("Do you want to try again?")
                                .append("\n").append(" ")
                                .toString();
                        final int result = MessageDisplayUtil.showYesNoConfirmDialog(mzSimpleSshClientMain.MAIN_FRAME, message, "Question");
                        if (result != JOptionPane.YES_OPTION) {
                            break;
                        }
                    }
                } catch (PasswordStorageException e) {
                    final String message = new StringBuilder("Wrong master password!\n\n")
                            .append("If you don't add the master password you won't be able\n")
                            .append("to use the sessions with the stored passwords!\n\n")
                            .append("Try again?\n").append(" ")
                            .toString();
                    final int result = MessageDisplayUtil.showYesNoConfirmDialog(mzSimpleSshClientMain.MAIN_FRAME, message, "Wrong password");
                    if (result != JOptionPane.YES_OPTION) {
                        break;
                    }
                }
            }
        }
    }

    public void storePassword(final SessionItemModel sessionItemModel) {
        if (StringUtils.isNotBlank(sessionItemModel.getPassword())) {
            final boolean passwordStorageFileExist = passwordStorageService.existStorageFile();
            if (passwordStorageFileExist && passwordStorageService.isUnlockedPasswordStorage()) {
                try {
                    passwordStorageService.storePassword(sessionItemModel);
                } catch (PasswordStorageException e) {
                    LOG.error(e);
                    MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
                }
            } else {
                final String message = passwordStorageFileExist
                        ? getMessageToReadSessionsWithMasterPassword()
                        : getMessageToStoreSessionsWithMasterPassword();

                final MasterPasswordAnswer answer = showMasterPasswordDialog(message);
                if (answer.getAnswerType() != JOptionPane.YES_OPTION) {
                    MessageDisplayUtil.showMessage(mzSimpleSshClientMain.MAIN_FRAME, "The sessions won't be stored with passwords!");
                } else {
                    if (!passwordStorageFileExist) {
                        char[] password = answer.getPassword();
                        final char[] passwordEncoded = Utils.encodeStringAsCharArray(new String(password));
                        try {
                            passwordStorageService.addMasterPassword(passwordEncoded);

                            // reset passwd
                            password = new char[]{'0'};
                        } catch (PasswordStorageException e) {
                            LOG.error(e);
                            MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
                        }
                    }

                    try {
                        passwordStorageService.storePassword(sessionItemModel);
                    } catch (PasswordStorageException e) {
                        LOG.error(e);
                        MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
                    }
                }
            }
        }
    }


    @AllArgsConstructor
    @Getter
    public static final class MasterPasswordAnswer {
        private int answerType;
        private char[] password;
    }

}
