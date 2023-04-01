package com.mz.sshclient.ui.components.tabs.sftp.transfer;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ui.utils.AWTInvokerUtils;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.util.function.Consumer;

public class TransferProgressPanel extends JPanel {

    private final JProgressBar prg;
    private final JButton stop;
    private int dragSource;
    private final AlphaComposite alphaComposite = AlphaComposite.SrcOver
            .derive(0.65f);
    private final AlphaComposite alphaComposite1 = AlphaComposite.SrcOver
            .derive(1.0f);
    private final Box b12 = Box.createVerticalBox();
    private Consumer<Boolean> stopCallback;

    private final JWindow window;

    public TransferProgressPanel() {
        window = new JWindow(mzSimpleSshClientMain.MAIN_FRAME);

        BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(layout);
        setOpaque(false);
        Box b1 = Box.createHorizontalBox();
        b1.setOpaque(true);
        b1.setMaximumSize(new Dimension(300, 50));
        b1.setAlignmentX(Box.LEFT_ALIGNMENT);
        prg = new JProgressBar();
        b1.add(prg);
        b1.add(Box.createHorizontalStrut(10));
        stop = new JButton("Stop");
        stop.addActionListener(e -> {
            if (stopCallback != null)
                stopCallback.accept(Boolean.TRUE);
        });
        b1.add(stop);
        add(Box.createVerticalGlue());
        JLabel label = new JLabel("Copying files");
        label.setAlignmentX(Box.LEFT_ALIGNMENT);
        b12.add(label);
        b12.add(b1);
        b12.setBorder(new EmptyBorder(10, 10, 10, 10));
        b12.setAlignmentX(Box.CENTER_ALIGNMENT);
        add(b12);
        add(Box.createVerticalGlue());
        addMouseListener(new MouseAdapter() {
        });
        addMouseMotionListener(new MouseAdapter() {
        });
        addKeyListener(new KeyAdapter() {
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                requestFocusInWindow();
            }
        });
        setFocusTraversalKeysEnabled(false);

        window.getContentPane().add(this);
        window.setLocationRelativeTo(mzSimpleSshClientMain.MAIN_FRAME);
        window.pack();
    }

    public void clear() {
        prg.setValue(0);
    }

    public void setProgress(int prg) {
        this.prg.setValue(prg);
    }

    public int getSource() {
        return dragSource;
    }

    public void setSource(int source) {
        this.dragSource = source;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(alphaComposite);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        int x = b12.getX();
        int y = b12.getY();
        int w = b12.getWidth();
        int h = b12.getHeight();
        g2.setComposite(alphaComposite1);
        g2.setColor(getBackground());
        g.fillRoundRect(x - 10, y - 10, w + 20, h + 20, 5, 5);
    }

    /**
     * @return the stopCallback
     */
    public Consumer<Boolean> getStopCallback() {
        return stopCallback;
    }

    /**
     * @param stopCallback the stopCallback to set
     */
    public void setStopCallback(Consumer<Boolean> stopCallback) {
        this.stopCallback = stopCallback;
    }

    @Override
    public void setVisible(boolean aFlag) {
        //SwingUtilities.invokeLater(() -> {
            if (!window.isVisible() && aFlag) {
                window.setVisible(true);
            } else {
                window.setVisible(false);
            }
        //});
    }

    public static void main(String[] args) {
        TransferProgressPanel bla = new TransferProgressPanel();
        bla.setVisible(true);
    }
}
