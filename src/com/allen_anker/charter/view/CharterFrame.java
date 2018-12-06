package com.allen_anker.charter.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharterFrame extends JFrame {
    // data container
    private List<Integer> values;
    private List<Integer> averageValues;
    private int currentAverage = 0;
    private int dataSize = 0;

    static final int MAX_NUMBER_OF_VALUE = 120;

    // frame start point
    // FRAME_X and FRAME_Y is also the left and top margin of the coordinates respectively
    static final int FRAME_X = 40;
    private static final int FRAME_Y = 150;
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

    private static final int DRAWING_INTERVAL = 100;

    // control the reading process, pause/resume
    private boolean readingSignal = true;
    private File file;
    // where to start and end when a file is chosen to be read
    private long readFrom = 0;
    private long endsAt = 0;

    private OriginalDataCanvas originalDataCurveCanvas;
    private AverageDataCanvas averageDataCurveCanvas;

    // components in the main frame
    private JPanel upperPanel;
    private JButton chooseFileButton;
    private JButton startButton;
    private JButton resumeButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JTextField startPosition;
    private JTextField endPosition;

    private JPanel canvasPanel;
//    private JPanel rawSliderPanel;
//    private JLabel rawSliderTitle;
//    private JPanel averageSliderPanel;
//    private JLabel averageSliderTitle;

    private JPanel bottomPanel;
    private JLabel fileLengthLabel;

    public CharterFrame() {
        super("Charter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(300, 200, 1600, 1200);
        setLayout(new BorderLayout());

        // build synchronized list
        values = Collections.synchronizedList(new ArrayList<>());
        averageValues = Collections.synchronizedList(new ArrayList<>());

        // init all the components
        originalDataCurveCanvas = new OriginalDataCanvas(values);
        averageDataCurveCanvas = new AverageDataCanvas(averageValues);
        originalDataCurveCanvas.setBounds(0, 0, 800, 500);
        averageDataCurveCanvas.setBounds(0, 0, 800, 500);
        originalDataCurveCanvas.setBorder(new TitledBorder(""));
        averageDataCurveCanvas.setBorder(new TitledBorder(""));

        upperPanel = new JPanel();
        chooseFileButton = new JButton("Choose File");
        startButton = new JButton("Start");
        resumeButton = new JButton("Resume");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        startPosition = new JTextField("0", 10);
        endPosition = new JTextField("", 10);
        startPosition.setHorizontalAlignment(JTextField.RIGHT);
        endPosition.setHorizontalAlignment(JTextField.RIGHT);
        startButton.setEnabled(false);
        resumeButton.setEnabled(false);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        upperPanel.add(chooseFileButton);
        upperPanel.add(startButton);
        upperPanel.add(pauseButton);
        upperPanel.add(resumeButton);
        upperPanel.add(stopButton);
        upperPanel.add(new JLabel("Starts from:"));
        upperPanel.add(startPosition);
        upperPanel.add(new JLabel("th Byte, "));
        upperPanel.add(new JLabel("Ends at:"));
        upperPanel.add(endPosition);
        upperPanel.add(new JLabel("th Byte"));
        add(upperPanel, BorderLayout.NORTH);

        canvasPanel = new JPanel();
        canvasPanel.setLayout(new GridLayout(2, 2));
        canvasPanel.add(originalDataCurveCanvas);
        canvasPanel.add(averageDataCurveCanvas);
        add(canvasPanel, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        fileLengthLabel = new JLabel();
        bottomPanel.add(new JLabel("Current File Length: "));
        bottomPanel.add(fileLengthLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

        chooseFileButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileHidingEnabled(true);
            jfc.showDialog(new JLabel(), "Choose File");
            if (jfc.getSelectedFile() != null) {
                file = jfc.getSelectedFile();
                fileLengthLabel.setText(file.length() + " bytes");
                endPosition.setText("" + file.length());
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                stopButton.setEnabled(false);
            }
        });

        startButton.addActionListener(e -> {
            String startPosStr = startPosition.getText();
            String endsPosStr = endPosition.getText();
            try {
                readFrom = Long.parseLong(startPosStr);
                endsAt = Long.parseLong(endsPosStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Start/End Position must be integer",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            startPosition.setEditable(false);
            endPosition.setEditable(false);
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
            chooseFileButton.setEnabled(false);
            readingSignal = false;
            try {
                // get the file and starting reading and drawing
                DrawingThread drawingThread = new DrawingThread(file, readFrom);
                drawingThread.start();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        });

        stopButton.addActionListener(e -> {
            readingSignal = true;
            stopButton.setEnabled(false);
            resumeButton.setEnabled(false);
            pauseButton.setEnabled(false);
            chooseFileButton.setEnabled(true);
            startPosition.setEditable(true);
            endPosition.setEditable(true);
            startPosition.setText("0");
            values = Collections.synchronizedList(new ArrayList<>());
            averageValues = Collections.synchronizedList(new ArrayList<>());
            currentAverage = 0;
            dataSize = 0;
            originalDataCurveCanvas.setValues(values);
            averageDataCurveCanvas.setValues(averageValues);
            originalDataCurveCanvas.repaint();
            averageDataCurveCanvas.repaint();
        });

        resumeButton.addActionListener(e -> {
            resumeButton.setEnabled(false);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
            readingSignal = false;
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
            readingSignal = true;
        });
    }

    private void addValue(int value) {
        // use the same list to store the input data
        if (values.size() > MAX_NUMBER_OF_VALUE) {
            values.remove(0);
        }
        values.add(value);
    }

    private void addAverageValue(int value) {
        if (averageValues.size() > MAX_NUMBER_OF_VALUE) {
            averageValues.remove(0);
        }
        dataSize++;
        averageValues.add(value);
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
                // continue reading until the file end
                while (raf.read(data) != -1) {
                    // if the reading process is paused, save the current position for later restart
                    if (readingSignal) {
                        saveStart(raf.getFilePointer());
                        break;
                    }
                    // the reading process must not read beyond the end position which is set before start reading
                    if (raf.getFilePointer() >= endsAt) {
                        setConfiguresWhenReachEnd();
                        break;
                    }
                    int value = 0;
                    value += (data[0] & 0xff) << 8;
                    value += (data[1] & 0xff);
                    currentAverage = ((currentAverage * dataSize) + value) / (dataSize + 1);
                    int presentedY = ORIGIN_Y - (value >> 8);
                    int presentedAverageY = ORIGIN_Y - (currentAverage >> 8);
                    addValue(presentedY);
                    addAverageValue(presentedAverageY);
                    originalDataCurveCanvas.repaint();
                    averageDataCurveCanvas.repaint();
                    Thread.sleep(DRAWING_INTERVAL);
                }
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

    // when the file reading pointer read to the end, this method is called
    private void setConfiguresWhenReachEnd() {
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        chooseFileButton.setEnabled(true);
        clearCache();
    }

    private void clearCache() {
        values.clear();
        averageValues.clear();
    }

    private void saveStart(long pos) {
        readFrom = pos;
    }

    public static void main(String[] args) {
        new CharterFrame();
    }
}
