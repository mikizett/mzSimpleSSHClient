package com.mz.sshclient.ui.components.common.animation;

import org.apache.commons.lang3.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;


public class ConnectAnimationComponent extends JComponent {

    private static final String DEFAULT_TEXT = "Connecting ";
    private static final int MAX_DOTS = 8;

    private final JWindow window = new JWindow();
    private final String title;

    private final Font defaultFont;

    private Timer timer;

    private boolean stopped = false;

    private int dots = 1;
    private int x;

    private int width;
    private int height;

    public ConnectAnimationComponent(final Window parent, final String title) {
        this.title = StringUtils.isBlank(title) ? DEFAULT_TEXT : title;

        ((JPanel) window.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.RED, 8));
        window.getContentPane().add(this, SwingConstants.CENTER);

        defaultFont = new Font(getFont().getName(), Font.ITALIC | Font.BOLD, 22);
        window.setFont(defaultFont);

        final FontMetrics fontMetrics = window.getFontMetrics(defaultFont);
        int width = fontMetrics.charsWidth(this.title.toCharArray(), 0, this.title.length());
        int height = fontMetrics.getHeight();

        this.width = width + 115;
        this.height = height + 40;

        window.setBounds(new Rectangle(this.width, this.height));
        window.setLocationRelativeTo(parent);

        timer = new Timer(500, e -> repaint());

        //window.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(defaultFont);
        g2.setColor(Color.BLACK);
        g2.drawString(title, 5, height / 2);

        if (dots == 1) {
            x = g2.getFontMetrics().charsWidth(title.toCharArray(), 0, title.length()) + 10;
        } else {
            x += 10;
        }

        if (dots > MAX_DOTS) {
            dots = 1;
            x = g2.getFontMetrics().charsWidth(title.toCharArray(), 0, title.length()) + 10;
        }

        for (int i = 0; i < dots; i++) {
            g2.fillOval(x, (height / 2) - 10, 10, 10);
        }

        ++dots;
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            timer.start();
            window.setVisible(true);
        });
    }

    public void stop() {
        SwingUtilities.invokeLater(() -> {
            if (!stopped) {
                stopped = true;
                timer.stop();
                window.setVisible(false);
                window.dispose();
            }
        });
    }

    public boolean isRunning() {
        return !stopped;
    }

    public static void main(String[] args) {
        final com.mz.sshclient.ui.components.common.animation.ConnectAnimationComponent c = new com.mz.sshclient.ui.components.common.animation.ConnectAnimationComponent(new JFrame(), "Connecting SSH");
        c.start();
    }
}
/*
public class ConnectAnimationComponent extends JComponent implements Runnable {

    private static final String DEFAULT_TEXT = "Connecting ";
    private static final int MAX_DOTS = 8;

    private final JWindow window = new JWindow();
    private final String title;
    private final Thread th = new Thread(this, ConnectAnimationComponent.class.getName());

    private final Font defaultFont;

    private boolean stopped = false;

    private int dots = 1;
    private int x;

    private int width;
    private int height;

    public ConnectAnimationComponent(final Window parent, final String title) {
        this.title = StringUtils.isBlank(title) ? DEFAULT_TEXT : title;

        ((JPanel) window.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.RED, 8));
        window.getContentPane().add(this, SwingConstants.CENTER);

        defaultFont = new Font(getFont().getName(), Font.ITALIC | Font.BOLD, 22);
        window.setFont(defaultFont);

        final FontMetrics fontMetrics = window.getFontMetrics(defaultFont);
        int width = fontMetrics.charsWidth(this.title.toCharArray(), 0, this.title.length());
        int height = fontMetrics.getHeight();

        this.width = width + 115;
        this.height = height + 40;

        window.setBounds(new Rectangle(this.width, this.height));
        window.setLocationRelativeTo(parent);

        //window.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(defaultFont);
        g2.setColor(Color.BLACK);
        g2.drawString(title, 5, height / 2);

        if (dots == 1) {
            x = g2.getFontMetrics().charsWidth(title.toCharArray(), 0, title.length()) + 10;
        } else {
            x += 10;
        }

        if (dots > MAX_DOTS) {
            dots = 1;
            x = g2.getFontMetrics().charsWidth(title.toCharArray(), 0, title.length()) + 10;
        }

        for (int i = 0; i < dots; i++) {
            g2.fillOval(x, (height / 2) - 10, 10, 10);
        }

        ++dots;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                Thread.sleep(500);
                repaint();
            } catch (InterruptedException e) {}
        }
    }

    public void start() {
        if (!window.isVisible()) {
            window.setVisible(true);
        }
        th.start();
    }

    public void stop() {
        if (!stopped) {
            stopped = true;
            th.interrupt();
            window.setVisible(false);
            window.dispose();
        }
    }

    public boolean isRunning() {
        return !stopped;
    }

    public static void main(String[] args) {
        final ConnectAnimationComponent c = new ConnectAnimationComponent(new JFrame(), "Connecting SSH");
        c.start();
    }

}*/
