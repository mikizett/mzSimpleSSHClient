package com.mz.sshclient.ui.laf;

import org.apache.commons.lang3.SystemUtils;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import java.util.HashMap;
import java.util.Map;

public class CustomUIDefaults {

    private static final String[] FOCUS_INPUT_MAPS = {
            "EditorPane.focusInputMap",
            "FormattedTextField.focusInputMap",
            "PasswordField.focusInputMap",
            "TextField.focusInputMap",
            "TextPane.focusInputMap",
            "TextArea.focusInputMap",
            "Table.ancestorInputMap",
            "Tree.focusInputMap"
    };

    private static final Map<String, Object> map = new HashMap<>(0);

    public static void read() {
        if (SystemUtils.IS_OS_MAC) {
            final UIDefaults uiDefaults = UIManager.getDefaults();
            for (String k : FOCUS_INPUT_MAPS) {
                map.put(k, uiDefaults.get(k));
            }
        }
    }

    public static void write() {
        if (SystemUtils.IS_OS_MAC) {
            final UIDefaults uiDefaults = UIManager.getDefaults();
            map.keySet().stream().forEach((key) -> {
                uiDefaults.put(key, map.get(key));
            });
        }
    }

}
