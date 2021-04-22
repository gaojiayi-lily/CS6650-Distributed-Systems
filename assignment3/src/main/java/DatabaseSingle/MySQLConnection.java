package DatabaseSingle;
// Connection success for server GET / POST method, throughput too low
// Consider the DB Connection manager in lab7

import DatabaseSingle.DBUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLConnection {

    private Connection conn;

    public Connection CustomerConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance(); // com.mysql.jdbc.Driver (old version)
            conn = DriverManager.getConnection(DBUtils.URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPurchaseRecord(int storeId, int customerId, String date) {
        if (conn == null) {
            System.err.println("DB connection failed!");
            return;
        }

        try {
            //System.out.println("DB connection success!");
            String sql = "INSERT INTO Records (StoreId, CustomerId, Date) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, storeId);
            stmt.setInt(2, customerId);
            stmt.setString(3, date);
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void unsetPurchaseRecord(int storeId, int customerId, String date) {
        if (conn == null) {
            System.err.println("DB connection failed!");
            return;
        }

        try {
            String sql = "DELETE FROM Records WHERE StoreId = ? AND CustomerId = ? AND Date = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, storeId);
            stmt.setInt(2, customerId);
            stmt.setString(3, date);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
