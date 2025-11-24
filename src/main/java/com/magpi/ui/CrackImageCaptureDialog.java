package com.magpi.ui;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple dialog that shows a live webcam preview and lets the user capture
 * a single image of a crack. The captured image is saved under
 *  ~/MagPi/CrackImages and the absolute file path is returned to the caller.
 */
public class CrackImageCaptureDialog extends JDialog {

    private static final String IMAGE_DIR =
            Paths.get(System.getProperty("user.home"), "MagPi", "CrackImages").toString();

    static {
        // Ensure image directory exists
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        // Try to load OpenCV native library once
        try {
            // org.openpnp.opencv dependency auto-loads; keep explicit load as safety
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV loaded for crack image capture: " + Core.VERSION);
        } catch (Throwable t) {
            System.err.println("Failed to load OpenCV for crack image capture: " + t.getMessage());
        }
    }

    private final int partNumber;
    private JLabel previewLabel;
    private JButton captureButton;
    private JButton cancelButton;

    private volatile boolean running = false;
    private VideoCapture camera;
    private Thread cameraThread;

    private String capturedPath;

    public CrackImageCaptureDialog(Window owner, int partNumber) {
        super(owner, "Capture Crack Image - Part " + partNumber, ModalityType.APPLICATION_MODAL);
        this.partNumber = partNumber;
        buildUI();
        pack();
        setLocationRelativeTo(owner);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopCamera();
            }
        });
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        previewLabel = new JLabel();
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.BLACK);
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(480, 360));

        add(previewLabel, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        captureButton = new JButton("Capture");
        cancelButton = new JButton("Cancel");

        captureButton.addActionListener(e -> onCapture());
        cancelButton.addActionListener(e -> onCancel());

        south.add(cancelButton);
        south.add(captureButton);

        add(south, BorderLayout.SOUTH);
    }

    private void startCamera() {
        camera = new VideoCapture();
        if (!camera.open(0)) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open webcam (device 0).",
                    "Camera Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        running = true;
        cameraThread = new Thread(() -> {
            Mat frame = new Mat();
            while (running) {
                if (camera.read(frame) && !frame.empty()) {
                    BufferedImage image = matToBufferedImage(frame);
                    if (image != null) {
                        SwingUtilities.invokeLater(() -> previewLabel.setIcon(new ImageIcon(image)));
                    }
                }
                try {
                    Thread.sleep(40); // ~25 fps
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (camera != null && camera.isOpened()) {
                camera.release();
            }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    private void stopCamera() {
        running = false;
        if (cameraThread != null) {
            cameraThread.interrupt();
            try {
                cameraThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
    }

    private void onCapture() {
        if (camera == null || !camera.isOpened()) {
            JOptionPane.showMessageDialog(this,
                    "Camera is not running.",
                    "Capture Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Mat frame = new Mat();
        if (!camera.read(frame) || frame.empty()) {
            JOptionPane.showMessageDialog(this,
                    "Failed to capture image from camera.",
                    "Capture Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        BufferedImage image = matToBufferedImage(frame);
        if (image == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to convert captured frame to image.",
                    "Capture Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("Part%d_%s.png", partNumber, timestamp);
        Path path = Paths.get(IMAGE_DIR, fileName);

        try {
            javax.imageio.ImageIO.write(image, "png", path.toFile());
            capturedPath = path.toAbsolutePath().toString();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save image: " + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        stopCamera();
        dispose();
    }

    private void onCancel() {
        capturedPath = null;
        stopCamera();
        dispose();
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] bytes = new byte[bufferSize];
        mat.get(0, 0, bytes);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(bytes, 0, targetPixels, 0, bytes.length);
        return image;
    }

    /**
     * Shows the dialog, starts the camera and blocks until closed.
     * @return absolute path to the captured image, or null if the user cancelled.
     */
    public String showAndCapture() {
        startCamera();
        setVisible(true);
        return capturedPath;
    }

    /**
     * Helper used by callers (e.g. TablePage) to capture an image for a part.
     */
    public static String captureForPart(Window owner, int partNumber) {
        CrackImageCaptureDialog dlg = new CrackImageCaptureDialog(owner, partNumber);
        return dlg.showAndCapture();
    }
}
