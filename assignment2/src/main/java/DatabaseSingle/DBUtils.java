package DatabaseSingle;

public class DBUtils {
    private static final String HOSTNAME = "rds-mysql-marketpurchaserecord.cbwxmklql9rh.us-east-1.rds.amazonaws.com";
    private static final String PORT_NUM = "3306"; // change it to your mysql port number
    public static final String DB_NAME = "PurchaseRecord";
    private static final String USERNAME = "masterUsername";
    private static final String PASSWORD = "CS6650database.";
    public static final String URL = "jdbc:mysql://"
            + HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
            + "?user=" + USERNAME + "&password=" + PASSWORD
            + "&autoReconnect=true&serverTimezone=UTC";
}