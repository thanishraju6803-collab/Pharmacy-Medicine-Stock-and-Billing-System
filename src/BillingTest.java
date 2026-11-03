
import javax.swing.*;

public class BillingTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pharmacy Billing Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 550);
            frame.setLocationRelativeTo(null);
            frame.add(new BillingPanel());
            frame.setVisible(true);
        });
    }
}
