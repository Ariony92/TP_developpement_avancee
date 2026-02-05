package tp_avancee_dev.tp_avancee.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {

    private static final String URL = "jdbc:postgresql://localhost:5432/MasterAnnonce";
    private static final String USER = "tpavancee";
    private static final String PASSWD = "";

    private static Connection connect;

    private ConnectionDB() throws ClassNotFoundException {
        try {
            Class.forName("org.postgresql.Driver");
            connect = DriverManager.getConnection(URL, USER, PASSWD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getInstance() throws ClassNotFoundException {
        try {
            if (connect == null || connect.isClosed()) {
                new ConnectionDB();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connect;
    }
}
