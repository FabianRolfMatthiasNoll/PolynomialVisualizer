import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GraphPanel extends JPanel {
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 50.0;
    private final double panningSpeedFactor = 1.2;

    private Point lastMousePosition;
    private JTextField functionField;
    public JButton resetButton;
    private JButton calculateButton;
    private Polynomial polynomial;
    private List<Double> zeroPoints;
    private List<Double> extremePoints;

    public GraphPanel() {
        polynomial = new Polynomial("3x^3-4x^1+2"); // Just a default function because why not
        zeroPoints = polynomial.getZeroPoints();
        extremePoints = polynomial.getExtremePoints();
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom -= e.getPreciseWheelRotation() * 0.1;
                if (zoom < 0.1) zoom = 0.1;
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePosition = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentMousePosition = e.getPoint();
                double dx = (currentMousePosition.getX() - lastMousePosition.getX()) / scale;
                double dy = (currentMousePosition.getY() - lastMousePosition.getY()) / scale;
                offsetX += dx * panningSpeedFactor;
                offsetY -= dy * panningSpeedFactor;
                lastMousePosition = currentMousePosition;
                repaint();
            }
        });

        createFunctionField();
        createCalculateButton();
        createResetButton();
    }

    private void createFunctionField(){
        functionField = new JTextField(20);
        add(functionField);
    }

    private void createCalculateButton() {
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(e -> {
            String function = functionField.getText();
            polynomial = new Polynomial(function);
            zeroPoints = polynomial.getZeroPoints();
            extremePoints = polynomial.getExtremePoints();
            repaint();
        });
        add(calculateButton);
    }

    private void createResetButton() {
        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            offsetX = 0;
            offsetY = 0;
            zoom = 1.0;
            repaint();
        });
        add(resetButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);

        scale = 50.0 * zoom;

        // calculate grid pattern
        double step = 1.0;
        while (scale * step < 20) {
            step *= 2;
        }
        while (scale * step > 100) {
            step /= 2;
        }

        int zeroX = width / 2 + (int) (offsetX * scale);
        int zeroY = height / 2 - (int) (offsetY * scale);

        g2d.setColor(Color.WHITE);
        g2d.drawLine(0, zeroY, width, zeroY); // X axis
        g2d.drawLine(zeroX, 0, zeroX, height); // Y axis

        // Drawing a small little grid :)
        g2d.setColor(new Color(200, 200, 200, 40)); // Light grey color with transparency
        for (double x = step * Math.floor((-offsetX - width / (2.0 * scale)) / step); x <= -offsetX + width / (2.0 * scale); x += step) {
            int screenX = zeroX + (int) (x * scale);
            g2d.drawLine(screenX, 0, screenX, height);
        }
        for (double y = step * Math.floor((-offsetY - height / (2.0 * scale)) / step); y <= -offsetY + height / (2.0 * scale); y += step) {
            int screenY = zeroY - (int) (y * scale);
            g2d.drawLine(0, screenY, width, screenY);
        }

        // Function drawer
        GeneralPath path = new GeneralPath();
        for (int screenX = 0; screenX < width; screenX++) {
            double x = (screenX - zeroX) / scale;
            double y = polynomial.evaluate(x);
            int screenY = zeroY - (int) (y * scale);
            if (screenX == 0) {
                path.moveTo(screenX, screenY);
            } else {
                path.lineTo(screenX, screenY);
            }
        }
        g2d.setColor(Color.RED);
        g2d.draw(path);

        // Draw labels and scales
        g2d.setColor(Color.WHITE);
        Font originalFont = g.getFont();
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int tickSize = 3;
        for (double x = step * Math.floor((-offsetX - width / (2.0 * scale)) / step); x <= -offsetX + width / (2.0 * scale); x += step) {
            int screenX = zeroX + (int) (x * scale);
            g2d.drawLine(screenX, zeroY - tickSize, screenX, zeroY + tickSize);
            String label = String.format("%.2f", x);
            int textOffset = (x < 0) ? -2 - g2d.getFontMetrics().stringWidth(label) : 2;
            g2d.drawString(label, screenX + textOffset, zeroY - 2);
        }

        for (double y = step * Math.floor((-offsetY - height / (2.0 * scale)) / step); y <= -offsetY + height / (2.0 * scale); y += step) {
            int screenY = zeroY - (int) (y * scale);
            g2d.drawLine(zeroX - tickSize, screenY, zeroX + tickSize, screenY);
            String label = String.format("%.2f", y);
            int textOffset = (y < 0) ? g2d.getFontMetrics().getAscent() + 2 : -2;
            g2d.drawString(label, zeroX + 2, screenY + textOffset);
        }

        // Getting ZeroPoints / ExtremePoints and drawing a Information Window
        int boxX = getWidth() - 200;
        int boxY = 10;
        int boxWidth = 180;
        int boxHeight = 80;

        g2d.setColor(new Color(100, 100, 100, 200));
        g2d.fillRect(boxX, boxY, boxWidth, boxHeight);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String function = "Function: " + polynomial.polynomial;
        String zeroPointsStr = "Zero Points: " + zeroPoints.stream().map(z -> String.format("%.2f", z)).collect(Collectors.joining(", "));
        String extremePointsStr = "Extreme Points: " + extremePoints.stream().map(e -> String.format("%.2f", e)).collect(Collectors.joining(", "));
        g2d.drawString(function, boxX + 10, boxY + 20);
        g2d.drawString(zeroPointsStr, boxX + 10, boxY + 40);
        g2d.drawString(extremePointsStr, boxX + 10, boxY + 60);
    }
}
