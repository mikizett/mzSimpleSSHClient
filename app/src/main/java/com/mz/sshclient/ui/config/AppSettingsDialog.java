package com.mz.sshclient.ui.config;

import com.mz.sshclient.mzSimpleSshClientMain;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

public class AppSettingsDialog extends JDialog {

    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private final JList<String> navList;

    private GlobalSettingsPanel globalSettingsPanel;

    public AppSettingsDialog() {
        super(mzSimpleSshClientMain.MAIN_FRAME);

        setTitle("App Settings");
        setModal(true);
        setSize(800, 600);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout, true);

        DefaultListModel<String> navModel = new DefaultListModel<>();
        navModel.addElement("Global Settings");

        navList = new JList<>(navModel);
        navList.setCellRenderer(new CellRenderer());

        navList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = navList.getSelectedIndex();
                if (index != -1) {
                    String value = navModel.get(index);
                    cardLayout.show(cardPanel, value);
                    this.revalidate();
                    this.repaint(0);
                }
            }
        });
        navList.setSelectedIndex(0);

        JScrollPane scrollPane = new JScrollPane(navList);
        scrollPane.setPreferredSize(new Dimension(150, 200));
        scrollPane.setBorder(new MatteBorder(0, 0, 0, 1, Color.gray));

        globalSettingsPanel = new GlobalSettingsPanel();
        cardPanel.add(globalSettingsPanel, "Global");

        Box bottomBox = Box.createHorizontalBox();
        bottomBox.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Color.gray),
                new EmptyBorder(10, 10, 10, 10)));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            dispose();
            setVisible(false);
        });

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            globalSettingsPanel.saveToFile();

            dispose();
            setVisible(false);
        });

        bottomBox.add(Box.createHorizontalGlue());
        bottomBox.add(btnCancel);
        bottomBox.add(Box.createHorizontalStrut(5));
        bottomBox.add(btnSave);

        add(scrollPane, BorderLayout.WEST);
        add(cardPanel);
        add(bottomBox, BorderLayout.SOUTH);
    }

    static class CellRenderer extends JLabel implements ListCellRenderer<String> {
        private final Color defaultBackgroundColor;
        private final Color defaultForegroundColor;
        private final Font font;

        public CellRenderer() {
            defaultBackgroundColor = getBackground();
            defaultForegroundColor = getForeground();
            font = getFont();

            setBorder(new EmptyBorder(15, 15, 15, 15));
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list,
                String value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            setText(value);
            if (isSelected) {
                setBackground(Color.decode("#557394"));
                setForeground(AppSettings.isDarkMode() ? defaultForegroundColor : Color.white);

                setFont(font.deriveFont(Font.BOLD, font.getSize() + 1));
            } else {
                setBackground(defaultBackgroundColor);
                setForeground(defaultForegroundColor);

                setFont(font.deriveFont(Font.PLAIN, font.getSize()));
            }
            return this;
        }
    };
}
