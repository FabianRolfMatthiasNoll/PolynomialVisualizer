import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.stream.Collectors;

public class GraphPanel extends JPanel {
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 50.0;

    private Point lastMousePosition;
    private JTextField functionField;
    public JButton resetButton;
    private Polynomial polynomial;
    private List<Double> zeroPoints;
    private List<Double> extremePoints;
    private class ZoomListener extends MouseAdapter {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            zoom -= e.getPreciseWheelRotation() * 0.1;
            if (zoom < 0.1) zoom = 0.1;
            repaint();
        }
    }

    private class PanListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            lastMousePosition = e.getPoint();
        }
    }

    private class PanMotionListener extends MouseAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            Point currentMousePosition = e.getPoint();
            double dx = (currentMousePosition.getX() - lastMousePosition.getX()) / scale;
            double dy = (currentMousePosition.getY() - lastMousePosition.getY()) / scale;
            double panningSpeedFactor = 1.2;
            offsetX += dx * panningSpeedFactor;
            offsetY -= dy * panningSpeedFactor;
            lastMousePosition = currentMousePosition;
            repaint();
        }
    }

    private class CalculateActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String function = functionField.getText();
            polynomial = new Polynomial(function);
            zeroPoints = polynomial.getZeroPoints();
            extremePoints = polynomial.getExtremePoints();
            repaint();
        }
    }

    private class ResetActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            offsetX = 0;
            offsetY = 0;
            zoom = 1.0;
            repaint();
        }
    }

    private class DeriveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            polynomial.derive();
            repaint();
        }
    }

    public GraphPanel() {
        polynomial = new Polynomial("3x^3-4x^1+2");
        zeroPoints = polynomial.getZeroPoints();
        extremePoints = polynomial.getExtremePoints();

        addMouseWheelListener(new ZoomListener());
        addMouseListener(new PanListener());
        addMouseMotionListener(new PanMotionListener());

        createResetButton();
        createFunctionField();
        createCalculateButton();
        createDeriveButton();
    }

    private void createFunctionField() {
        functionField = new JTextField(20);
        add(functionField);
    }

    private void createCalculateButton() {
        JButton calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(new CalculateActionListener());
        add(calculateButton);
    }

    private void createResetButton() {
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ResetActionListener());
        add(resetButton);
    }
    private void createDeriveButton() {
        JButton deriveButton = new JButton("Derive");
        deriveButton.addActionListener(new DeriveActionListener());
        add(deriveButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        clearBackground(g2d, width, height);
        updateScale();
        double step = calculateGridStep();
        int zeroX = width / 2 + (int) (offsetX * scale);
        int zeroY = height / 2 - (int) (offsetY * scale);

        drawAxes(g2d, width, height, zeroX, zeroY);
        drawGrid(g2d, width, height, zeroX, zeroY, step);
        drawFunction(g2d, width, zeroX, zeroY);
        drawLabelsAndScales(g2d, width, height, zeroX, zeroY, step);
        drawInformationWindow(g2d);
    }

    private void clearBackground(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
    }

    private void updateScale() {
        scale = 50.0 * zoom;
    }

    private double calculateGridStep() {
        double step = 1.0;
        while (scale * step < 20) {
            step *= 2;
        }
        while (scale * step > 100) {
            step /= 2;
        }
        return step;
    }

    private void drawAxes(Graphics2D g2d, int width, int height, int zeroX, int zeroY) {
        g2d.setColor(Color.WHITE);
        g2d.drawLine(0, zeroY, width, zeroY); // X axis
        g2d.drawLine(zeroX, 0, zeroX, height); // Y axis
    }

    private void drawGrid(Graphics2D g2d, int width, int height, int zeroX, int zeroY, double step) {
        g2d.setColor(new Color(200, 200, 200, 40)); // Light grey color with transparency

        for (double x = step * Math.floor((-offsetX - width / (2.0 * scale)) / step); x <= -offsetX + width / (2.0 * scale); x += step) {
            int screenX = zeroX + (int) (x * scale);
            g2d.drawLine(screenX, 0, screenX, height);
        }

        for (double y = step * Math.floor((-offsetY - height / (2.0 * scale)) / step); y <= -offsetY + height / (2.0 * scale); y += step) {
            int screenY = zeroY - (int) (y * scale);
            g2d.drawLine(0, screenY, width, screenY);
        }
    }

    private void drawFunction(Graphics2D g2d, int width, int zeroX, int zeroY) {
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
    }

    private void drawLabelsAndScales(Graphics2D g2d, int width, int height, int zeroX, int zeroY, double step) {
        g2d.setColor(Color.WHITE);
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
    }

    private void drawInformationWindow(Graphics2D g2d) {
        int boxX = getWidth() - 190;
        int boxY = getHeight() - 90;
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
