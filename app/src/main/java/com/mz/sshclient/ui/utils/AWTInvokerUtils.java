package com.mz.sshclient.ui.utils;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.concurrent.Executors;

public final class AWTInvokerUtils {

    private AWTInvokerUtils() {}

    public static void invokeLaterShowWindow(Window window) {
        SwingUtilities.invokeLater(() -> window.setVisible(true));
    }

    public static void invokeExclusivelyNotInEventDispatcher(final Runnable runnable) {
        if (runnable != null) {
            if (SwingUtilities.isEventDispatchThread()) {
                Executors.newSingleThreadExecutor().submit(runnable);
            } else {
                SwingUtilities.invokeLater(runnable);
            }
        }
    }

    public static void invokeInSeparateThread(final Runnable runnable) {
        if (runnable != null) {
            Executors.newSingleThreadExecutor().submit(runnable);
        }
    }

    public static void invokeLater(final Runnable runnable) {
        if (runnable != null) {
            SwingUtilities.invokeLater(runnable);
        }
    }

}
