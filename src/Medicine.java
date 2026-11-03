
public class Medicine {
    private int medicineId;
    private String medicineName;
    private String batchNo;
    private String category;
    private double unitPrice;
    private int quantity;
    private String expiryDate;
    private int reorderLevel;

    public Medicine(int medicineId, String medicineName,
            String batchNo, String category, double unitPrice,
            int quantity, String expiryDate, int reorderLevel) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.batchNo = batchNo;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.reorderLevel = reorderLevel;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public String getCategory() {
        return category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }
}

