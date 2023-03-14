package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.ssh.IInteractiveResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.awt.Window;
import java.util.Collections;
import java.util.List;

public class InteractiveResponseProvider implements IInteractiveResponseProvider {

    private static final Logger LOG = LogManager.getLogger(InteractiveResponseProvider.class);

    private boolean retry = true;

    private final Window parentWindow;

    public InteractiveResponseProvider(final Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    @Override
    public List<String> getSubmethods() {
        return Collections.emptyList();
    }

    @Override
    public void init(Resource resource, String name, String instruction) {
        LOG.debug("ChallengeResponseProvider init - resource: " + resource + " name: " + name + " instruction: " + instruction);

        if (StringUtils.isNotBlank(name) || StringUtils.isNotBlank(instruction)) {
            JOptionPane.showMessageDialog(parentWindow, name + "\n" + instruction);
        }
    }

    @Override
    public char[] getResponse(String prompt, boolean echo) {
        LOG.debug("prompt: " + prompt + " echo: " + echo);

        if (echo) {
            final String answer = JOptionPane.showInputDialog(prompt);
            if (answer != null) {
                return answer.toCharArray();
            }
        } else {
            final JPasswordField passwordField = new JPasswordField(30);
            int answer = JOptionPane.showOptionDialog(
                    parentWindow,
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
                return passwordField.getPassword();
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
