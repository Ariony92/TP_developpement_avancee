package org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {


    private static final String PASSWD = ""; // personne aura le MDP
    private static final String URL = "jdbc:postgresql://database-etudiants:5432/zkhan";
    private static final String USER = "zkhan";


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

