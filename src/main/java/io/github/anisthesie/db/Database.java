package io.github.anisthesie.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://botusers.cdzxgdwbo6b4.us-east-2.rds.amazonaws.com:3306/Users";

    private static final String USER = "admin";
    private static final String PASS = "fiverr2021";

    public static final String USERS_TABLE = "Users_table";

    public static Connection connection = null;

    public static void initDatabase() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected to Database");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        try {
            return connection != null && connection.isValid(5);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

}
