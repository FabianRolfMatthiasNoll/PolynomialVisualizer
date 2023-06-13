import javax.swing.*;
import java.awt.*;

public class FunctionGrapher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Function Grapher");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GraphPanel graphPanel = new GraphPanel();
            frame.add(graphPanel);

            frame.setVisible(true);
        });
    }
}
