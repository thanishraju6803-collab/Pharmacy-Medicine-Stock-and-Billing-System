
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BillingPanel extends JPanel {

    private final JComboBox<MedicineOption> medicineBox =
        new JComboBox<>();

    private final JTextField quantityField =
        new JTextField(6);

    private final DefaultTableModel cartModel =
        new DefaultTableModel(
            new String[] {
                "Medicine ID", "Medicine", "Quantity",
                "Unit Price", "Line Total"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

    private final JTable cartTable = new JTable(cartModel);
    private final JLabel totalLabel = new JLabel("Grand Total: ₹0.00");

    private final Map<Integer, Integer> cart =
        new LinkedHashMap<>();

    private final BillingDAO billingDAO = new BillingDAO();

    public BillingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel(
            "PHARMACY BILLING",
            SwingConstants.CENTER
        );
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshButton = new JButton("Load Medicines");
        JButton addButton = new JButton("Add to Bill");
        JButton removeButton = new JButton("Remove Selected");
        JButton completeButton = new JButton("Complete Bill");

        topPanel.add(new JLabel("Medicine:"));
        topPanel.add(medicineBox);
        topPanel.add(new JLabel("Quantity:"));
        topPanel.add(quantityField);
        topPanel.add(addButton);
        topPanel.add(removeButton);
        topPanel.add(refreshButton);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(completeButton, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadMedicines());
        addButton.addActionListener(e -> addToCart());
        removeButton.addActionListener(e -> removeSelected());
        completeButton.addActionListener(e -> completeBill());

        loadMedicines();
    }

    private void loadMedicines() {
        medicineBox.removeAllItems();

        String sql = "SELECT medicine_id, medicine_name, "
            + "unit_price, quantity, expiry_date "
            + "FROM medicines ORDER BY medicine_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MedicineOption option = new MedicineOption(
                    rs.getInt("medicine_id"),
                    rs.getString("medicine_name"),
                    rs.getBigDecimal("unit_price"),
                    rs.getInt("quantity"),
                    rs.getDate("expiry_date").toLocalDate().toString()
                );

                medicineBox.addItem(option);
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void addToCart() {
        try {
            MedicineOption medicine =
                (MedicineOption) medicineBox.getSelectedItem();

            if (medicine == null) {
                throw new IllegalArgumentException(
                    "No medicine available. Load medicines first."
                );
            }

            int quantity = Integer.parseInt(
                quantityField.getText().trim()
            );

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
                );
            }

            if (java.time.LocalDate.parse(medicine.expiryDate)
                    .isBefore(java.time.LocalDate.now())) {
                throw new IllegalArgumentException(
                    "This medicine has expired."
                );
            }

            int alreadyAdded = cart.getOrDefault(medicine.id, 0);
            int requestedTotal = alreadyAdded + quantity;

            if (requestedTotal > medicine.stock) {
                throw new IllegalArgumentException(
                    "Insufficient stock. Available: "
                    + medicine.stock
                );
            }

            cart.put(medicine.id, requestedTotal);
            refreshCart();
            quantityField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid whole-number quantity."
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this, ex.getMessage()
            );
        }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            MedicineOption medicine = findMedicine(entry.getKey());

            if (medicine == null) {
                continue;
            }

            int quantity = entry.getValue();
            BigDecimal lineTotal = medicine.price.multiply(
                BigDecimal.valueOf(quantity)
            );

            total = total.add(lineTotal);

            cartModel.addRow(new Object[] {
                medicine.id,
                medicine.name,
                quantity,
                medicine.price,
                lineTotal
            });
        }

        totalLabel.setText("Grand Total: ₹" + total);
    }

    private MedicineOption findMedicine(int id) {
        for (int i = 0; i < medicineBox.getItemCount(); i++) {
            MedicineOption option = medicineBox.getItemAt(i);

            if (option.id == id) {
                return option;
            }
        }

        return null;
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Select a cart item to remove."
            );
            return;
        }

        int id = (int) cartModel.getValueAt(row, 0);
        cart.remove(id);
        refreshCart();
    }

    private void completeBill() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Add medicines to the bill first."
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Complete this bill and update stock?",
            "Confirm Sale",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int saleId = billingDAO.createBill(
                new LinkedHashMap<>(cart)
            );

            JOptionPane.showMessageDialog(
                this,
                "Bill completed successfully!\nBill number: "
                + saleId + "\n" + totalLabel.getText()
            );

            cart.clear();
            refreshCart();
            loadMedicines();

        } catch (SQLException | IllegalArgumentException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            ex.getMessage(),
            "Billing Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private static class MedicineOption {
        final int id;
        final String name;
        final BigDecimal price;
        final int stock;
        final String expiryDate;

        MedicineOption(int id, String name, BigDecimal price,
                       int stock, String expiryDate) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.expiryDate = expiryDate;
        }

        @Override
        public String toString() {
            return id + " - " + name + " (Stock: " + stock + ")";
        }
    }
}
