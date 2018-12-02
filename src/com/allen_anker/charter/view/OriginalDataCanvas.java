package com.allen_anker.charter.view;

import java.awt.*;
import java.util.List;

public class OriginalDataCanvas extends DrawingCanvas {

    public OriginalDataCanvas(List<Integer> values) {
        super(values);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString("The Raw Data", CharterFrame.YAxis_X + CharterFrame.FRAME_WIDTH / 2 - 50,
                CharterFrame.YAxis_Y + 20);
    }
}
