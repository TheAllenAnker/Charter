package com.allen_anker.charter.view;

import java.awt.*;
import java.util.List;

public class AverageDataCanvas extends DrawingCanvas {
    public AverageDataCanvas(List<Integer> values) {
        super(values);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString("The Current Average Data", CharterFrame.YAxis_X + CharterFrame.FRAME_WIDTH / 2 - 70,
                CharterFrame.YAxis_Y + 20);
    }
}
