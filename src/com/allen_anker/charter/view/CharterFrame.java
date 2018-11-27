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
    private static final int MAX_COUNT_OF_DATA = 50;

    private MyCanvas dataCurveCanvas = new MyCanvas();
    // frame start point
    private final int FRAME_X = 120;
    private final int FRAME_Y = 300;
    private final int FRAME_WIDTH = 600;
    private final int FRAME_HEIGHT = 250;

    // origin of coordinates
    private final int ORIGIN_X = FRAME_X;
    private final int ORIGIN_Y = FRAME_Y + FRAME_HEIGHT - 50;

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

        values = Collections.synchronizedList(new ArrayList<>());// 防止引起线程异常
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
                            while ((length = finalFis.read()) != -1) {
                                System.out.println(length);
                                addValue(500 - length);
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
            int xDelta = XAxis_X / MAX_COUNT_OF_DATA;
            int length = values.size() - 10;

            for (int i = 0; i < length - 1; ++i) {
                g2D.drawLine(xDelta * (MAX_COUNT_OF_DATA - length + i), values.get(i),
                        xDelta * (MAX_COUNT_OF_DATA - length + i + 1), values.get(i + 1));
            }
            // 画坐标轴
            g2D.setStroke(new BasicStroke(Float.parseFloat("2.0F")));// 轴线粗度
            // X轴以及方向箭头
            g.drawLine(ORIGIN_X, ORIGIN_Y, XAxis_X, XAxis_Y);// x轴线的轴线
            g.drawLine(XAxis_X, XAxis_Y, XAxis_X - 5, XAxis_Y - 5);// 上边箭头
            g.drawLine(XAxis_X, XAxis_Y, XAxis_X + 5, XAxis_Y + 5);// 下边箭头

            // Y轴以及方向箭头
            g.drawLine(ORIGIN_X, ORIGIN_Y, YAxis_X, YAxis_Y);
            g.drawLine(YAxis_X, YAxis_Y, YAxis_X - 5, YAxis_Y + 5);
            g.drawLine(YAxis_X, YAxis_Y, YAxis_X + 5, YAxis_Y + 5);

            // 画X轴上的时间刻度（从坐标轴原点起，每隔TIME_INTERVAL(时间分度)像素画一时间点，到X轴终点止）
            g.setColor(Color.BLUE);
            g2D.setStroke(new BasicStroke(Float.parseFloat("1.0f")));

            // X轴刻度依次变化情况
            for (int i = ORIGIN_X, j = 0; i < XAxis_X; i += TIME_INTERVAL, j += TIME_INTERVAL) {
                g.drawString(" " + j, i - 10, ORIGIN_Y + 20);
            }
            g.drawString("Stream", XAxis_X + 5, XAxis_Y + 5);

            // 画Y轴上血压刻度（从坐标原点起，每隔10像素画一压力值，到Y轴终点止）
            for (int i = ORIGIN_Y, j = 0; i > YAxis_Y; i -= DATA_INTERVAL, j += TIME_INTERVAL) {
                g.drawString(j + " ", ORIGIN_X - 30, i + 3);
            }
            g.drawString("Data", YAxis_X - 5, YAxis_Y - 5);// 血压刻度小箭头值
            /*
            // 画网格线
            g.setColor(Color.BLACK);
            // 坐标内部横线
            for (int i = ORIGIN_Y; i > YAxis_Y; i -= DATA_INTERVAL) {
                g.drawLine(ORIGIN_X, i, ORIGIN_X + 10 * TIME_INTERVAL, i);
            }
            // 坐标内部竖线
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
