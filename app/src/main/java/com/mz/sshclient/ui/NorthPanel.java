package com.mz.sshclient.ui;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ui.config.AppSettingsDialog;
import com.mz.sshclient.utils.AwesomeFontEnum;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FlowLayout;

public class NorthPanel extends JPanel {

    public NorthPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton appConfigButton = new JButton();
        appConfigButton.setText(AwesomeFontEnum.FA_COG.getAwesomeFontType());
        appConfigButton.addActionListener(e -> openAppConfigDialog());
        add(appConfigButton);
    }

    private void openAppConfigDialog() {
        final AppSettingsDialog dlg = new AppSettingsDialog();
        dlg.setLocationRelativeTo(mzSimpleSshClientMain.MAIN_FRAME);
        dlg.setVisible(true);
    }
}
