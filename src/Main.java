
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Main extends JFrame {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final DefaultTableModel tableModel;
    private final JTable medicineTable;

    public Main() {
        setTitle("Pharmacy Medicine Stock and Billing System");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
            "PHARMACY MEDICINE MANAGEMENT",
            SwingConstants.CENTER
        );
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        add(heading, BorderLayout.NORTH);

        String[] columns = {
            "ID", "Medicine Name", "Batch No", "Category",
            "Unit Price", "Quantity", "Expiry Date", "Reorder Level"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        medicineTable = new JTable(tableModel);
        add(new JScrollPane(medicineTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Add Medicine");
         JButton refreshButton = new JButton("Refresh");
         JButton updateButton = new JButton("Update Selected");
         JButton deleteButton = new JButton("Delete Selected");
        JButton searchButton = new JButton("Search by Name");
        JButton lowStockButton = new JButton("Low Stock Alerts");

        buttons.add(addButton);
        buttons.add(refreshButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(searchButton);
        buttons.add(lowStockButton);
        updateButton.addActionListener(e -> updateMedicine());
        lowStockButton.addActionListener(e -> showLowStock());

        add(buttons, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addMedicine());
        refreshButton.addActionListener(e -> loadMedicines());
        deleteButton.addActionListener(e -> deleteSelectedMedicine());
        searchButton.addActionListener(e -> searchMedicine());

        loadMedicines();
    }

    private void loadMedicines() {
        try {
            tableModel.setRowCount(0);

            ArrayList<Medicine> medicines =
                medicineDAO.getAllMedicines();

            for (Medicine m : medicines) {
                tableModel.addRow(new Object[] {
                    m.getMedicineId(),
                    m.getMedicineName(),
                    m.getBatchNo(),
                    m.getCategory(),
                    m.getUnitPrice(),
                    m.getQuantity(),
                    m.getExpiryDate(),
                    m.getReorderLevel()
                });
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void addMedicine() {
        try {
            String name = ask("Medicine name:");
            if (name == null) return;
            if (name.isBlank()) {
                throw new IllegalArgumentException(
                    "Medicine name cannot be empty."
                );
            }

            String batch = ask("Batch number:");
            if (batch == null) return;
            if (batch.isBlank()) {
                throw new IllegalArgumentException(
                    "Batch number cannot be empty."
                );
            }

            String category = ask("Category:");
            if (category == null) return;

            double price = Double.parseDouble(
                ask("Unit price (e.g. 20.00):")
            );

            int quantity = Integer.parseInt(
                ask("Available quantity:")
            );

            String expiry = ask("Expiry date (YYYY-MM-DD):");
            if (expiry == null) return;

            java.time.LocalDate expiryDate =
                java.time.LocalDate.parse(expiry);

            int reorder = Integer.parseInt(
                ask("Reorder level:")
            );

            if (price < 0 || quantity < 0 || reorder < 0) {
                throw new IllegalArgumentException(
                    "Price, quantity and reorder level must not be negative."
                );
            }

            if (expiryDate.isBefore(java.time.LocalDate.now())) {
                throw new IllegalArgumentException(
                    "Expiry date cannot be in the past."
                );
            }

            Medicine m = new Medicine(
                0, name.trim(), batch.trim(), category.trim(),
                price, quantity, expiry, reorder
            );

            if (medicineDAO.addMedicine(m)) {
                JOptionPane.showMessageDialog(
                    this, "Medicine added successfully!"
                );
                loadMedicines();
            }
        } catch (SQLException | IllegalArgumentException ex) {
            showError(ex);
        }
    }

    private String ask(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    private void deleteSelectedMedicine() {
        int row = medicineTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a medicine first."
            );
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Delete the selected medicine?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            if (medicineDAO.deleteMedicine(id)) {
                JOptionPane.showMessageDialog(
                    this, "Medicine deleted successfully!"
                );
                loadMedicines();
            } else {
                JOptionPane.showMessageDialog(
                    this, "Medicine not found."
                );
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void searchMedicine() {
        String keyword = JOptionPane.showInputDialog(
            this, "Enter medicine name to search:"
        );

        if (keyword == null) return;

        String search = keyword.trim().toLowerCase();

        try {
            tableModel.setRowCount(0);

            for (Medicine m : medicineDAO.getAllMedicines()) {
                if (m.getMedicineName().toLowerCase()
                        .contains(search)) {
                    tableModel.addRow(new Object[] {
                        m.getMedicineId(),
                        m.getMedicineName(),
                        m.getBatchNo(),
                        m.getCategory(),
                        m.getUnitPrice(),
                        m.getQuantity(),
                        m.getExpiryDate(),
                        m.getReorderLevel()
                    });
                }
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this, "No matching medicines found."
                );
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }

    
private void updateMedicine() {
    int row = medicineTable.getSelectedRow();

    if (row == -1) {
        JOptionPane.showMessageDialog(
            this, "Select a medicine to update."
        );
        return;
    }

    try {
        int id = (int) tableModel.getValueAt(row, 0);

        String name = ask("Medicine name:");
        if (name == null) return;
        if (name.isBlank()) {
            throw new IllegalArgumentException(
                "Medicine name cannot be empty."
            );
        }

        String batch = ask("Batch number:");
        if (batch == null) return;
        if (batch.isBlank()) {
            throw new IllegalArgumentException(
                "Batch number cannot be empty."
            );
        }

        String category = ask("Category:");
        if (category == null) return;

        String priceText = ask("Unit price:");
        if (priceText == null) return;
        double price = Double.parseDouble(priceText);

        String quantityText = ask("Quantity:");
        if (quantityText == null) return;
        int quantity = Integer.parseInt(quantityText);

        String expiry = ask("Expiry date (YYYY-MM-DD):");
        if (expiry == null) return;
        java.time.LocalDate date =
            java.time.LocalDate.parse(expiry);

        String reorderText = ask("Reorder level:");
        if (reorderText == null) return;
        int reorder = Integer.parseInt(reorderText);

        if (price < 0 || quantity < 0 || reorder < 0) {
            throw new IllegalArgumentException(
                "Price, quantity and reorder level cannot be negative."
            );
        }

        if (date.isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException(
                "Expiry date cannot be in the past."
            );
        }

        Medicine medicine = new Medicine(
            id, name.trim(), batch.trim(), category.trim(),
            price, quantity, expiry, reorder
        );

        if (medicineDAO.updateMedicine(medicine)) {
            JOptionPane.showMessageDialog(
                this, "Medicine updated successfully!"
            );
            loadMedicines();
        } else {
            JOptionPane.showMessageDialog(
                this, "No medicine record was updated."
            );
        }

    } catch (SQLException | IllegalArgumentException ex) {
        showError(ex);
    }
}

private void showLowStock() {
    try {
        tableModel.setRowCount(0);

        for (Medicine m : medicineDAO.getAllMedicines()) {
            if (m.getQuantity() <= m.getReorderLevel()) {
                tableModel.addRow(new Object[] {
                    m.getMedicineId(),
                    m.getMedicineName(),
                    m.getBatchNo(),
                    m.getCategory(),
                    m.getUnitPrice(),
                    m.getQuantity(),
                    m.getExpiryDate(),
                    m.getReorderLevel()
                });
            }
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                this, "No low-stock medicines found."
            );
        }

    } catch (SQLException ex) {
        showError(ex);
    }
}

}
