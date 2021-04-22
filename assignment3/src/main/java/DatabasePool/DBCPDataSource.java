package DatabasePool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.*;

public class DBCPDataSource {
    //private static BasicDataSource dataSource;

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

//    private static final String HOST_NAME = "rds-mysql-marketpurchaserecord.cbwxmklql9rh.us-east-1.rds.amazonaws.com";
//    private static final String PORT = "3306";
//    private static final String DATABASE = "PurchaseRecord";
//    private static final String USERNAME = "masterUsername";
//    private static final String PASSWORD = "CS6650database.";

    // TODO: Using a more powerful RDS connection
    private static final String HOST_NAME = "rds-mysql-marketpurchaserecord.cbwxmklql9rh.us-east-1.rds.amazonaws.com";
    private static final String PORT = "3306";
    private static final String DATABASE = "PurchaseRecord";
    private static final String USERNAME = "masterUsername";
    private static final String PASSWORD = "CS6650database.";

    static {
//        dataSource = new HikariDataSource();
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
//        dataSource.setUrl(url);
//        dataSource.setUsername(USERNAME);
//        dataSource.setPassword(PASSWORD);
//        dataSource.setInitialSize(10);
//        dataSource.setMaxTotal(60);

        String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
        config.setJdbcUrl( url );
        config.setUsername( USERNAME );
        config.setPassword( PASSWORD );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        dataSource = new HikariDataSource( config );
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}