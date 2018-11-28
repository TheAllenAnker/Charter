package com.allen_anker.charter.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharterFrame extends JFrame {
    // data container
    private List<Integer> values;
    private OringinalDataCanvas originalDataCurveCanvas;

    public static final int MAX_NUMBER_OF_VALUE = 120;

    // frame start point
    // FRAME_X and FRAME_Y is also the left and top margin of the coordinates respectively
    public static final int FRAME_X = 40;
    public static final int FRAME_Y = 180;
    public static final int FRAME_WIDTH = 600;
    public static final int FRAME_HEIGHT = 250;

    // origin of coordinates
    public static final int BOTTOM_MARGIN = 50;
    public static final int ORIGIN_X = FRAME_X;
    public static final int ORIGIN_Y = FRAME_Y + FRAME_HEIGHT - BOTTOM_MARGIN;

    // x and y axises end points
    public static final int XAxis_X = FRAME_X + FRAME_WIDTH;
    public static final int XAxis_Y = ORIGIN_Y;
    public static final int YAxis_X = ORIGIN_X;
    public static final int YAxis_Y = FRAME_Y - 150;
    // interval in x and y axises
    public static final int TIME_INTERVAL = 50;
    public static final int DATA_INTERVAL = 50;
    public static final int X_VALUE_MARGIN = 20;
    public static final int Y_VALUE_MARGIN = 30;

    public boolean isReading = true;

    public CharterFrame() {
        super("Charter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(300, 200, 1600, 1200);
        setLayout(new BorderLayout());

        // build synchronized list
        values = Collections.synchronizedList(new ArrayList<>());
        originalDataCurveCanvas = new OringinalDataCanvas(values);
        originalDataCurveCanvas.setBounds(0, 0, 800, 600);
        originalDataCurveCanvas.setBorder(new TitledBorder(""));
        add(originalDataCurveCanvas);

        JPanel uppperPanel = new JPanel();
        JButton chooseFileButton = new JButton("Choose File");
        JButton resumeButton = new JButton("Resume");
        JButton stopButton = new JButton("Stop");
        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        uppperPanel.add(chooseFileButton);
        uppperPanel.add(resumeButton);
        uppperPanel.add(stopButton);
        uppperPanel.add(new JLabel("Starts From:"));
        uppperPanel.add(new JTextField("Start position"));
        uppperPanel.add(new JLabel("th 2 Bytes"));
        add(uppperPanel, BorderLayout.NORTH);
        setVisible(true);

        stopButton.addActionListener(e -> {
            if (isReading) {
                isReading = false;
                stopButton.setEnabled(false);
                resumeButton.setEnabled(true);
            }
        });

        resumeButton.addActionListener(e -> {
            if (!isReading) {
                isReading = true;
                resumeButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        chooseFileButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileHidingEnabled(true);
            jfc.showDialog(new JLabel(), "Choose File");
            File file = jfc.getSelectedFile();
            if (file != null) {
                stopButton.setEnabled(true);
                try {
                    // get the file and starting reading and drawing
                    DrawingThread drawingThread = new DrawingThread(file);
                    drawingThread.start();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    private void addValue(int value) {
        // use the same list to store the input data
        if (values.size() > MAX_NUMBER_OF_VALUE) {
            values.remove(0);
        }
        values.add(value);
    }

    private class DrawingThread extends Thread {
        private RandomAccessFile raf;
        private FileInputStream fis;

        public DrawingThread(File file) throws FileNotFoundException {
            this.fis = new FileInputStream(file);
            this.raf = new RandomAccessFile(file, "r");
        }

        @Override
        public void run() {
            try {
                byte[] data = new byte[2];
                while (fis.read(data) != -1 && isReading) {
                    int value = 0;
                    value += (data[0] & 0x000000ff) << 8;
                    value += (data[1] & 0x000000ff);
                    int presentedY = ORIGIN_Y - (value >> 8);
                    addValue(presentedY);
                    System.out.println(value);
                    originalDataCurveCanvas.repaint();
                    Thread.sleep(100);
                }
            } catch (InterruptedException | IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public List<Integer> getValues() {
        return values;
    }

    public static void main(String[] args) {
        new CharterFrame();
    }
}
