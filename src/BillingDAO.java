
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Map;

public class BillingDAO {

    public int createBill(Map<Integer, Integer> cart)
            throws SQLException {

        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException(
                "Please add at least one medicine to the bill."
            );
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                BigDecimal grandTotal = BigDecimal.ZERO;

                // Lock and validate each medicine row.
                String checkSql =
                    "SELECT unit_price, quantity, expiry_date "
                    + "FROM medicines WHERE medicine_id = ? "
                    + "FOR UPDATE";

                try (PreparedStatement ps =
                         con.prepareStatement(checkSql)) {

                    for (Map.Entry<Integer, Integer> item
                            : cart.entrySet()) {

                        int medicineId = item.getKey();
                        int requested = item.getValue();

                        if (requested <= 0) {
                            throw new IllegalArgumentException(
                                "Quantity must be greater than zero."
                            );
                        }

                        ps.setInt(1, medicineId);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                    "Medicine ID " + medicineId
                                    + " was not found."
                                );
                            }

                            int available = rs.getInt("quantity");
                            LocalDate expiry = rs.getDate(
                                "expiry_date"
                            ).toLocalDate();

                            if (expiry.isBefore(LocalDate.now())) {
                                throw new IllegalArgumentException(
                                    "Expired medicine cannot be sold. ID: "
                                    + medicineId
                                );
                            }

                            if (available < requested) {
                                throw new IllegalArgumentException(
                                    "Insufficient stock for medicine ID "
                                    + medicineId + ". Available: "
                                    + available
                                );
                            }

                            BigDecimal price =
                                rs.getBigDecimal("unit_price");

                            grandTotal = grandTotal.add(
                                price.multiply(
                                    BigDecimal.valueOf(requested)
                                )
                            );
                        }
                    }
                }

                // Save the bill and obtain its generated ID.
                int saleId;

                String saleSql =
                    "INSERT INTO sales (customer_id, grand_total) "
                    + "VALUES (NULL, ?)";

                try (PreparedStatement ps = con.prepareStatement(
                        saleSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setBigDecimal(1, grandTotal);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                "Could not generate bill number."
                            );
                        }
                        saleId = keys.getInt(1);
                    }
                }

                String itemSql =
                    "INSERT INTO sale_items "
                    + "(sale_id, medicine_id, quantity, "
                    + "unit_price, line_total) "
                    + "VALUES (?, ?, ?, ?, ?)";

                String stockSql =
                    "UPDATE medicines SET quantity = quantity - ? "
                    + "WHERE medicine_id = ? AND quantity >= ?";

                try (PreparedStatement itemPs =
                         con.prepareStatement(itemSql);
                     PreparedStatement stockPs =
                         con.prepareStatement(stockSql)) {

                    for (Map.Entry<Integer, Integer> item
                            : cart.entrySet()) {

                        int medicineId = item.getKey();
                        int quantity = item.getValue();

                        // Read the price again while holding the row lock.
                        BigDecimal price;

                        try (PreparedStatement pricePs =
                                 con.prepareStatement(
                                     "SELECT unit_price FROM medicines "
                                     + "WHERE medicine_id = ?")) {

                            pricePs.setInt(1, medicineId);

                            try (ResultSet rs = pricePs.executeQuery()) {
                                if (!rs.next()) {
                                    throw new SQLException(
                                        "Medicine not found."
                                    );
                                }
                                price = rs.getBigDecimal("unit_price");
                            }
                        }

                        BigDecimal lineTotal = price.multiply(
                            BigDecimal.valueOf(quantity)
                        );

                        itemPs.setInt(1, saleId);
                        itemPs.setInt(2, medicineId);
                        itemPs.setInt(3, quantity);
                        itemPs.setBigDecimal(4, price);
                        itemPs.setBigDecimal(5, lineTotal);
                        itemPs.executeUpdate();

                        stockPs.setInt(1, quantity);
                        stockPs.setInt(2, medicineId);
                        stockPs.setInt(3, quantity);

                        if (stockPs.executeUpdate() != 1) {
                            throw new SQLException(
                                "Stock update failed for medicine ID "
                                + medicineId
                            );
                        }
                    }
                }

                con.commit();
                return saleId;

            } catch (SQLException | RuntimeException ex) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection closes automatically.
                }
            }
        }
    }
}
