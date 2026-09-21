import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CashierDashboard extends JFrame {
    private int currentUserId;
    private String currentUserName;

    private JComboBox<String> comboMedicines;
    private JTextField txtQuantity, txtPrice, txtStock;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblTotal;
    private double grandTotal = 0.0;

    public CashierDashboard(int userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;

        setTitle("PIMS - Cashier Point of Sale (POS) [" + userName + "]");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        JLabel lblHeader = new JLabel("  HealthFirst Pharmacy - POS Module", JLabel.LEFT);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        headerPanel.add(lblHeader, BorderLayout.CENTER);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Input & Table Panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Item Selection & Stock Check"));

        inputPanel.add(new JLabel("Select Medicine:"));
        comboMedicines = new JComboBox<>();
        loadMedicineDropdown();
        inputPanel.add(comboMedicines);

        inputPanel.add(new JLabel("Unit Price (ZAR):"));
        txtPrice = new JTextField();
        txtPrice.setEditable(false);
        inputPanel.add(txtPrice);

        inputPanel.add(new JLabel("Available Stock:"));
        txtStock = new JTextField();
        txtStock.setEditable(false);
        inputPanel.add(txtStock);

        inputPanel.add(new JLabel("Quantity to Sell:"));
        txtQuantity = new JTextField();
        inputPanel.add(txtQuantity);

        JButton btnAddToCart = new JButton("Add to Cart");
        JButton btnClearInput = new JButton("Clear Input");
        inputPanel.add(btnAddToCart);
        inputPanel.add(btnClearInput);

        centerPanel.add(inputPanel, BorderLayout.NORTH);

        cartModel = new DefaultTableModel(new String[]{"Medicine ID", "Name", "Price", "Qty", "Subtotal"}, 0);
        cartTable = new JTable(cartModel);
        centerPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Total: R 0.00  ");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCheckout = new JButton("Checkout & Print Bill");
        JButton btnClearCart = new JButton("Clear Cart");
        btnPanel.add(btnClearCart);
        btnPanel.add(btnCheckout);

        bottomPanel.add(lblTotal, BorderLayout.WEST);
        bottomPanel.add(btnPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event Listeners
        comboMedicines.addActionListener(e -> updateMedicineDetails());
        btnAddToCart.addActionListener(e -> addToCart());
        btnClearCart.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> processCheckout());
        btnClearInput.addActionListener(e -> txtQuantity.setText(""));

        if (comboMedicines.getItemCount() > 0) {
            updateMedicineDetails();
        }
    }

    private void loadMedicineDropdown() {
        comboMedicines.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT medicine_id, name FROM medicines WHERE quantity_in_stock > 0")) {
            while (rs.next()) {
                comboMedicines.addItem(rs.getInt("medicine_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + e.getMessage());
        }
    }

    private void updateMedicineDetails() {
        String selected = (String) comboMedicines.getSelectedItem();
        if (selected == null) return;
        int medId = Integer.parseInt(selected.split(" - ")[0]);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT price, quantity_in_stock FROM medicines WHERE medicine_id = ?")) {
            pstmt.setInt(1, medId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                txtPrice.setText(String.format("%.2f", rs.getDouble("price")));
                txtStock.setText(String.valueOf(rs.getInt("quantity_in_stock")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addToCart() {
        String selected = (String) comboMedicines.getSelectedItem();
        if (selected == null) return;

        int medId = Integer.parseInt(selected.split(" - ")[0]);
        String medName = selected.split(" - ")[1];

        try {
            double price = Double.parseDouble(txtPrice.getText());
            int stock = Integer.parseInt(txtStock.getText());
            int qty = Integer.parseInt(txtQuantity.getText().trim());

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.");
                return;
            }
            if (qty > stock) {
                JOptionPane.showMessageDialog(this, "Requested quantity exceeds available stock!");
                return;
            }

            double subtotal = price * qty;
            cartModel.addRow(new Object[]{medId, medName, price, qty, subtotal});
            grandTotal += subtotal;
            lblTotal.setText(String.format("Total: R %.2f  ", grandTotal));
            txtQuantity.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric quantity.");
        }
    }

    private void clearCart() {
        cartModel.setRowCount(0);
        grandTotal = 0.0;
        lblTotal.setText("Total: R 0.00  ");
    }

    private void processCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String insertSale = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
            PreparedStatement pstmtSale = conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS);
            pstmtSale.setDouble(1, grandTotal);
            pstmtSale.setInt(2, currentUserId);
            pstmtSale.executeUpdate();

            ResultSet rsKeys = pstmtSale.getGeneratedKeys();
            int saleId = 0;
            if (rsKeys.next()) {
                saleId = rsKeys.getInt(1);
            }

            String insertItem = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
            String updateStock = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?";

            PreparedStatement pstmtItem = conn.prepareStatement(insertItem);
            PreparedStatement pstmtStock = conn.prepareStatement(updateStock);

            StringBuilder billReceipt = new StringBuilder();
            billReceipt.append("========== HEALTHFIRST PHARMACY ==========\n");
            billReceipt.append("Sale ID: ").append(saleId).append("\n");
            billReceipt.append("Cashier: ").append(currentUserName).append("\n");
            billReceipt.append("------------------------------------------\n");
            billReceipt.append(String.format("%-20s %-5s %-10s\n", "Item", "Qty", "Price"));
            billReceipt.append("------------------------------------------\n");

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int medId = (Integer) cartModel.getValueAt(i, 0);
                String name = (String) cartModel.getValueAt(i, 1);
                double price = (Double) cartModel.getValueAt(i, 2);
                int qty = (Integer) cartModel.getValueAt(i, 3);
                double subtotal = (Double) cartModel.getValueAt(i, 4);

                pstmtItem.setInt(1, saleId);
                pstmtItem.setInt(2, medId);
                pstmtItem.setInt(3, qty);
                pstmtItem.setDouble(4, price);
                pstmtItem.addBatch();

                pstmtStock.setInt(1, qty);
                pstmtStock.setInt(2, medId);
                pstmtStock.addBatch();

                billReceipt.append(String.format("%-20s %-5d R%-10.2f\n", name, qty, subtotal));
            }

            pstmtItem.executeBatch();
            pstmtStock.executeBatch();

            conn.commit();

            billReceipt.append("------------------------------------------\n");
            billReceipt.append(String.format("TOTAL AMOUNT: R %.2f\n", grandTotal));
            billReceipt.append("==========================================\n");
            billReceipt.append("        Thank you for your visit!         ");

            JTextArea textArea = new JTextArea(billReceipt.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Generated Bill / Receipt", JOptionPane.INFORMATION_MESSAGE);

            clearCart();
            loadMedicineDropdown();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Checkout Failed: " + e.getMessage());
        }
    }
}