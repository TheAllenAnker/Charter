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
    private OriginalDataCanvas originalDataCurveCanvas;

    static final int MAX_NUMBER_OF_VALUE = 120;

    // frame start point
    // FRAME_X and FRAME_Y is also the left and top margin of the coordinates respectively
    static final int FRAME_X = 40;
    private static final int FRAME_Y = 180;
    static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 250;

    // origin of coordinates
    private static final int BOTTOM_MARGIN = 50;
    static final int ORIGIN_X = FRAME_X;
    static final int ORIGIN_Y = (FRAME_Y + FRAME_HEIGHT) - BOTTOM_MARGIN;

    // x and y axises end points
    static final int XAxis_X = FRAME_X + FRAME_WIDTH;
    static final int XAxis_Y = ORIGIN_Y;
    static final int YAxis_X = ORIGIN_X;
    static final int YAxis_Y = FRAME_Y - 150;
    // interval in x and y axises
    static final int TIME_INTERVAL = 50;
    static final int DATA_INTERVAL = 50;
    static final int X_VALUE_MARGIN = 20;
    static final int Y_VALUE_MARGIN = 30;

    private boolean newFile = true;
    private File file;
    private long readFrom = 0;

    public CharterFrame() {
        super("Charter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(300, 200, 1600, 1200);
        setLayout(new BorderLayout());

        // build synchronized list
        values = Collections.synchronizedList(new ArrayList<>());
        originalDataCurveCanvas = new OriginalDataCanvas(values);
        originalDataCurveCanvas.setBounds(0, 0, 800, 600);
        originalDataCurveCanvas.setBorder(new TitledBorder(""));
        add(originalDataCurveCanvas);

        JPanel upperPanel = new JPanel();
        JButton chooseFileButton = new JButton("Choose File");
        JButton startButton = new JButton("Start");
        JButton resumeButton = new JButton("Resume");
        JButton pauseButton = new JButton("Pause");
        JButton stopButton = new JButton("Stop");
        JTextField startPosition = new JTextField("0", 10);
        startPosition.setHorizontalAlignment(JTextField.RIGHT);
        startButton.setEnabled(false);
        resumeButton.setEnabled(false);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        upperPanel.add(chooseFileButton);
        upperPanel.add(startButton);
        upperPanel.add(pauseButton);
        upperPanel.add(resumeButton);
        upperPanel.add(stopButton);
        upperPanel.add(new JLabel("Starts From:"));
        upperPanel.add(startPosition);
        upperPanel.add(new JLabel("th 2 Bytes"));
        add(upperPanel, BorderLayout.NORTH);
        setVisible(true);

        chooseFileButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileHidingEnabled(true);
            jfc.showDialog(new JLabel(), "Choose File");
            if (jfc.getSelectedFile() != null) {
                file = jfc.getSelectedFile();
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                stopButton.setEnabled(false);
            }
        });

        startButton.addActionListener(e -> {
            String startPosStr = startPosition.getText();
            try {
                readFrom = Long.parseLong(startPosStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Start Position must be integer",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
            chooseFileButton.setEnabled(false);
            newFile = false;
            try {
                // get the file and starting reading and drawing
                DrawingThread drawingThread = new DrawingThread(file, readFrom);
                drawingThread.start();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        });

        stopButton.addActionListener(e -> {
            newFile = true;
            stopButton.setEnabled(false);
            resumeButton.setEnabled(false);
            pauseButton.setEnabled(false);
            chooseFileButton.setEnabled(true);
            startPosition.setText("0");
            values = Collections.synchronizedList(new ArrayList<>());
            originalDataCurveCanvas.setValues(values);
            originalDataCurveCanvas.repaint();
        });

        resumeButton.addActionListener(e -> {
            resumeButton.setEnabled(false);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
            newFile = false;
            try {
                DrawingThread drawingThread = new DrawingThread(file, readFrom);
                drawingThread.start();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        });

        pauseButton.addActionListener(e -> {
            stopButton.setEnabled(false);
            resumeButton.setEnabled(true);
            newFile = true;
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
        private long readFrom;

        DrawingThread(File file) throws FileNotFoundException {
            this.raf = new RandomAccessFile(file, "r");
            this.readFrom = 0;
        }

        DrawingThread(File file, long start) throws FileNotFoundException {
            this.raf = new RandomAccessFile(file, "r");
            this.readFrom = start;
        }

        @Override
        public void run() {
            try {
                byte[] data = new byte[2];
                raf.seek(readFrom);
                while (raf.read(data) != -1) {
                    if (newFile) {
                        readFrom = raf.getFilePointer();
                        break;
                    }
                    int value = 0;
                    value += (data[0] & 0x000000ff) << 8;
                    value += (data[1] & 0x000000ff);
                    int presentedY = ORIGIN_Y - (value >> 8);
                    addValue(presentedY);
                    System.out.println(value);
                    originalDataCurveCanvas.repaint();
                    Thread.sleep(100);
                }
                System.out.println("Thread dead");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        new CharterFrame();
    }
}
