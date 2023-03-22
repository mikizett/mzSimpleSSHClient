package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.IInteractiveResponseProvider;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import com.mz.sshclient.ui.utils.UIUtils;
import com.mz.sshclient.utils.Utils;
import net.schmizz.sshj.userauth.password.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.util.Collections;
import java.util.List;

public class InteractiveResponseProvider implements IInteractiveResponseProvider {

    private static final Logger LOG = LogManager.getLogger(InteractiveResponseProvider.class);

    private boolean retry = true;

    private char[] cachedEncodedPassword;

    public InteractiveResponseProvider(final SessionItemModel sessionItemModel) {
        if (sessionItemModel != null) {
            cachedEncodedPassword = sessionItemModel.getPassword().toCharArray();
        }
    }

    @Override
    public List<String> getSubmethods() {
        return Collections.emptyList();
    }

    @Override
    public void init(Resource resource, String name, String instruction) {
        LOG.debug("ChallengeResponseProvider init - resource: " + resource + " name: " + name + " instruction: " + instruction);

        if (StringUtils.isNotBlank(name) || StringUtils.isNotBlank(instruction)) {
            MessageDisplayUtil.showMessage(mzSimpleSshClientMain.MAIN_FRAME, name + "\n" + instruction);
        }
    }

    @Override
    public char[] getResponse(String prompt, boolean echo) {
        LOG.debug("prompt: " + prompt + " echo: " + echo);

        if (cachedEncodedPassword != null) {
            return cachedEncodedPassword;
        }

        if (echo) {
            final String answer = JOptionPane.showInputDialog(prompt);
            if (answer != null) {
                cachedEncodedPassword = Utils.encodeStringAsCharArray(answer);
                return cachedEncodedPassword;
            }
        } else {
            final JPasswordField passwordField = new JPasswordField(30);
            UIUtils.addAncestorAndFocusListenerToPasswordField(passwordField);
            int answer = JOptionPane.showOptionDialog(
                    mzSimpleSshClientMain.MAIN_FRAME,
                    new Object[] {
                            prompt,
                            passwordField
                    },
                    "Input",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null, null, null
            );
            if (answer == JOptionPane.OK_OPTION) {
                cachedEncodedPassword = Utils.encodeCharArrayAsCharArray(passwordField.getPassword());
                return cachedEncodedPassword;
            }
        }
        retry = false;
        return null;
    }

    @Override
    public boolean shouldRetry() {
        return retry;
    }

}
