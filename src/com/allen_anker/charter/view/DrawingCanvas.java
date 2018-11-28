package com.allen_anker.charter.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DrawingCanvas extends JPanel {
    private List<Integer> values;

    public DrawingCanvas(List<Integer> values) {
        this.values = values;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        Color c = new Color(200, 70, 0);
        g.setColor(c);
        super.paintComponent(g);

        // drawing configurations
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // draw the current data read in the list
        int xDelta = CharterFrame.FRAME_WIDTH / CharterFrame.MAX_NUMBER_OF_VALUE;
        for (int i = 0; i < values.size() - 1; ++i) {
            g2D.drawLine(CharterFrame.FRAME_X + xDelta * i, values.get(i),
                    CharterFrame.FRAME_X + xDelta * (i + 1), values.get(i + 1));
        }
        // set drawing configuration (roughness)
        g2D.setStroke(new BasicStroke(Float.parseFloat("2.0F")));
        // draw x-axis and the arrow
        g.drawLine(CharterFrame.ORIGIN_X, CharterFrame.ORIGIN_Y, CharterFrame.XAxis_X, CharterFrame.XAxis_Y);
        g.drawLine(CharterFrame.XAxis_X, CharterFrame.XAxis_Y, CharterFrame.XAxis_X - 5, CharterFrame.XAxis_Y - 5);
        g.drawLine(CharterFrame.XAxis_X, CharterFrame.XAxis_Y, CharterFrame.XAxis_X - 5, CharterFrame.XAxis_Y + 5);

        // draw y-axis and the arrow
        g.drawLine(CharterFrame.ORIGIN_X, CharterFrame.ORIGIN_Y, CharterFrame.YAxis_X, CharterFrame.YAxis_Y);
        g.drawLine(CharterFrame.YAxis_X, CharterFrame.YAxis_Y, CharterFrame.YAxis_X - 5, CharterFrame.YAxis_Y + 5);
        g.drawLine(CharterFrame.YAxis_X, CharterFrame.YAxis_Y, CharterFrame.YAxis_X + 5, CharterFrame.YAxis_Y + 5);

        // set drawing configurations
        g.setColor(Color.BLUE);
        g2D.setStroke(new BasicStroke(Float.parseFloat("1.0f")));

        // draw the values on x-axis
        for (int i = CharterFrame.ORIGIN_X, j = 0; i <= CharterFrame.XAxis_X; i += CharterFrame.TIME_INTERVAL, j += CharterFrame.TIME_INTERVAL) {
            g.drawString("" + j, i, CharterFrame.ORIGIN_Y + CharterFrame.X_VALUE_MARGIN);
        }
        g.drawString("Number of 2Bytes", CharterFrame.XAxis_X + 5, CharterFrame.XAxis_Y + 5);
        g.drawString(CharterFrame.MAX_NUMBER_OF_VALUE * 2 + " Bytes in Window", CharterFrame.XAxis_X + 5, CharterFrame.XAxis_Y - 8);

        // draw the values on y-axis
        for (int i = CharterFrame.ORIGIN_Y, j = 0; i > CharterFrame.YAxis_Y; i -= CharterFrame.DATA_INTERVAL, j += CharterFrame.TIME_INTERVAL) {
            g.drawString((j << 8) + "", CharterFrame.ORIGIN_X - CharterFrame.Y_VALUE_MARGIN, i);
        }
        g.drawString("Value of the Two Bytes", CharterFrame.YAxis_X - 5, CharterFrame.YAxis_Y - 5);
        // draw the grid
        g.setColor(Color.BLACK);
        for (int i = CharterFrame.ORIGIN_Y; i > CharterFrame.YAxis_Y; i -= CharterFrame.DATA_INTERVAL) {
            g.drawLine(CharterFrame.ORIGIN_X, i, CharterFrame.XAxis_X, i);
        }
        for (int i = CharterFrame.ORIGIN_X; i <= CharterFrame.XAxis_X; i += CharterFrame.TIME_INTERVAL) {
            g.drawLine(i, CharterFrame.ORIGIN_Y, i, CharterFrame.ORIGIN_Y - 6 * CharterFrame.DATA_INTERVAL);
        }
    }
}
