package com.allen_anker.charter.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharterFrame extends JFrame {
    // data container
    private List<Integer> values;
    private static final int MAX_COUNT_OF_DATA = 80;

    private MyCanvas dataCurveCanvas = new MyCanvas();
    // frame start point
    // FRAME_X and FRAME_Y is also the left and top margin of the coordinates respectively
    private final int FRAME_X = 120;
    private final int FRAME_Y = 300;
    private final int FRAME_WIDTH = 600;
    private final int FRAME_HEIGHT = 250;

    // origin of coordinates
    private final int BOTTOM_MARGIN = 50;
    private final int ORIGIN_X = FRAME_X;
    private final int ORIGIN_Y = FRAME_Y + FRAME_HEIGHT - BOTTOM_MARGIN;

    // x and y axises end points
    private final int XAxis_X = FRAME_X + FRAME_WIDTH;
    private final int XAxis_Y = ORIGIN_Y;
    private final int YAxis_X = ORIGIN_X;
    private final int YAxis_Y = FRAME_Y - 150;
    // interval in x and y axises
    private final int TIME_INTERVAL = 50;
    private final int DATA_INTERVAL = 50;

    public CharterFrame() {
        super("Charter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(300, 200, 900, 600);
        setLayout(new BorderLayout());

        dataCurveCanvas.setBorder(new TitledBorder(""));
        add(dataCurveCanvas, BorderLayout.CENTER);

        JPanel uppperPanel = new JPanel();
        JButton chooseFileButton = new JButton("Choose File");
        uppperPanel.add(chooseFileButton);
        uppperPanel.add(new JTextField());
        add(uppperPanel, BorderLayout.NORTH);
        setVisible(true);

        // build synchronized list
        values = Collections.synchronizedList(new ArrayList<>());
        chooseFileButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileHidingEnabled(true);
            jfc.showDialog(new JLabel(), "Choose File");
            File file = jfc.getSelectedFile();
            if (file != null) {
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                    FileInputStream finalFis = fis;
                    new Thread(() -> {
                        try {
                            int length;
                            byte[] data = new byte[2];
                            while ((length = finalFis.read(data)) != -1) {
                                int value = 0;
                                value += (data[0] & 0x000000ff) << 8;
                                value += (data[1] & 0x000000ff);
                                int presentedY = ORIGIN_Y - (value >> 8);
                                addValue(presentedY);
                                System.out.println(value);
                                dataCurveCanvas.repaint();
                                Thread.sleep(100);
                            }
                        } catch (InterruptedException | IOException e1) {
                            e1.printStackTrace();
                        } finally {
                            try {
                                finalFis.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }).start();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    public void addValue(int value) {
        // use the same list to store the input data
        if (values.size() > MAX_COUNT_OF_DATA) {
            values.remove(0);
        }
        values.add(value);
    }

    // canvas for representing data
    class MyCanvas extends JPanel {
        private static final long serialVersionUID = 1L;

        public void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;

            Color c = new Color(200, 70, 0);
            g.setColor(c);
            super.paintComponent(g);

            // drawing configurations
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // draw the current data read in the list
            int xDelta = FRAME_WIDTH / MAX_COUNT_OF_DATA;
            int length = values.size();
            for (int i = 0; i < values.size() - 1; ++i) {
                g2D.drawLine(FRAME_X + xDelta * (i), values.get(i),
                        FRAME_X + xDelta * (i + 1), values.get(i + 1));
            }
            // set drawing configuration (roughness)
            g2D.setStroke(new BasicStroke(Float.parseFloat("2.0F")));
            // draw x-axis and the arrow
            g.drawLine(ORIGIN_X, ORIGIN_Y, XAxis_X, XAxis_Y);
            g.drawLine(XAxis_X, XAxis_Y, XAxis_X - 5, XAxis_Y - 5);
            g.drawLine(XAxis_X, XAxis_Y, XAxis_X - 5, XAxis_Y + 5);

            // draw y-axis and the arrow
            g.drawLine(ORIGIN_X, ORIGIN_Y, YAxis_X, YAxis_Y);
            g.drawLine(YAxis_X, YAxis_Y, YAxis_X - 5, YAxis_Y + 5);
            g.drawLine(YAxis_X, YAxis_Y, YAxis_X + 5, YAxis_Y + 5);

            // set drawing configurations
            g.setColor(Color.BLUE);
            g2D.setStroke(new BasicStroke(Float.parseFloat("1.0f")));

            // draw the values on x-axis
            for (int i = ORIGIN_X, j = 0; i < XAxis_X; i += TIME_INTERVAL, j += TIME_INTERVAL) {
                g.drawString(" " + j, i - 10, ORIGIN_Y + 20);
            }
            g.drawString("Stream", XAxis_X + 5, XAxis_Y + 5);

            // draw teh values on y-axis
            for (int i = ORIGIN_Y, j = 0; i > YAxis_Y; i -= DATA_INTERVAL, j += TIME_INTERVAL) {
                g.drawString(j + " ", ORIGIN_X - 30, i + 3);
            }
            g.drawString("Value", YAxis_X - 5, YAxis_Y - 5);
            /*
            // draw the grid
            g.setColor(Color.BLACK);
            for (int i = ORIGIN_Y; i > YAxis_Y; i -= DATA_INTERVAL) {
                g.drawLine(ORIGIN_X, i, ORIGIN_X + 10 * TIME_INTERVAL, i);
            }
            for (int i = ORIGIN_X; i < XAxis_X; i += TIME_INTERVAL) {
                g.drawLine(i, ORIGIN_Y, i, ORIGIN_Y - 6 * DATA_INTERVAL);
            }
            */
        }
    }

    public static void main(String[] args) {
        new CharterFrame();
    }
}
