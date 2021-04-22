package DatabaseSingle;
// Connection success for server GET / POST method, throughput too low
// Consider the DB Connection manager in lab7

import DatabaseSingle.DBUtils;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {

    public void TableCreation() {
        try {
            // Step 1 Connect to MySQL
            System.out.println("Connecting to " + DBUtils.URL);

            // Reflection (allow third party), call itself & register in DriverManager
            Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
            Connection conn = DriverManager.getConnection(DBUtils.URL);

            if (conn == null) {
                return;
            }

            // Step 2 Drop table in case they exist
            Statement stmt = conn.createStatement();
            String sql = "DROP TABLE IF EXISTS Records";
            stmt.executeUpdate(sql);

            // Step 3 Create new tables
            sql = "CREATE TABLE Records ("
                    + "RecordId INT auto_increment,"
                    + "StoreId INT,"
                    + "CustomerId INT,,"
                    + "Date VARCHAR(255),"
                    + "CONSTRAINT pk_Records_RecordId PRIMARY KEY(RecordId)"
                    + ")";
            stmt.executeUpdate(sql);

            System.out.println("Table creation done successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}