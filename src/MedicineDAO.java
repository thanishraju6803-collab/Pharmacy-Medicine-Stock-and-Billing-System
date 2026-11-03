
import java.sql.*;
import java.util.ArrayList;

public class MedicineDAO {

    public boolean addMedicine(Medicine m) throws SQLException {
        String sql = "INSERT INTO medicines "
            + "(medicine_name, batch_no, category, unit_price, "
            + "quantity, expiry_date, reorder_level) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, m.getMedicineName());
            ps.setString(2, m.getBatchNo());
            ps.setString(3, m.getCategory());
            ps.setDouble(4, m.getUnitPrice());
            ps.setInt(5, m.getQuantity());
            ps.setDate(6, Date.valueOf(m.getExpiryDate()));
            ps.setInt(7, m.getReorderLevel());

            return ps.executeUpdate() > 0;
        }
    }

    public ArrayList<Medicine> getAllMedicines()
            throws SQLException {

        ArrayList<Medicine> list = new ArrayList<>();

        String sql = "SELECT * FROM medicines ORDER BY medicine_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Medicine(
                    rs.getInt("medicine_id"),
                    rs.getString("medicine_name"),
                    rs.getString("batch_no"),
                    rs.getString("category"),
                    rs.getDouble("unit_price"),
                    rs.getInt("quantity"),
                    rs.getDate("expiry_date").toString(),
                    rs.getInt("reorder_level")
                ));
            }
        }

        return list;
    }

    public boolean deleteMedicine(int id) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
     
    
public boolean updateMedicine(Medicine m) throws SQLException {
    String sql = "UPDATE medicines SET medicine_name=?, "
        + "batch_no=?, category=?, unit_price=?, quantity=?, "
        + "expiry_date=?, reorder_level=? WHERE medicine_id=?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, m.getMedicineName());
        ps.setString(2, m.getBatchNo());
        ps.setString(3, m.getCategory());
        ps.setDouble(4, m.getUnitPrice());
        ps.setInt(5, m.getQuantity());
        ps.setDate(6, java.sql.Date.valueOf(m.getExpiryDate()));
        ps.setInt(7, m.getReorderLevel());
        ps.setInt(8, m.getMedicineId());

        return ps.executeUpdate() > 0;
    }
 }

    
}
