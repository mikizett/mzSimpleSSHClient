package com.mz.sshclient.ui.utils;

import javax.swing.SwingUtilities;
import java.awt.Window;

public final class AWTInvokerUtils {

    private AWTInvokerUtils() {}

    public static void invokeLaterShowWindow(Window window) {
        SwingUtilities.invokeLater(() -> window.setVisible(true));
    }

}
