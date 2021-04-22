package DatabasePool;

import java.sql.*;
import DatabasePool.DBCPDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.*;

public class PurchaseRecord {
    //private static BasicDataSource dataSource;
    private static HikariDataSource dataSource;

    public PurchaseRecord() {
        dataSource = DBCPDataSource.getDataSource();
    }

    public void createPurchaseRecord(int storeId, int customerId, String date) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "INSERT INTO Records (StoreId, CustomerId, Date) VALUES (?, ?, ?)";

        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setInt(1, storeId);
            preparedStatement.setInt(2, customerId);
            preparedStatement.setString(3, date);

            // execute insert SQL statement
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
