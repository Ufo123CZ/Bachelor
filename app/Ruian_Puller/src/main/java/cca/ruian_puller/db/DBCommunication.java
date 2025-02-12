package cca.ruian_puller.db;

import cca.ruian_puller.utils.LoggerUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBCommunication {

    Connection connection = null;

    // Singleton
    private static DBCommunication instance = null;
    public static DBCommunication getInstance() {
        if (instance == null) {
            instance = new DBCommunication();
        }
        return instance;
    }

    public boolean connect(String dbUrl, String dbUsername, String dbPassword) {
        // Connect to the database
        // use jdbc to connect to the database
        // use dbUrl, dbUsername, dbPassword to connect to the database
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            LoggerUtil.LOGGER.info("Connected to the database.");
            return true;
        } catch (SQLException e) {
            LoggerUtil.LOGGER.error("Error connecting to the database: {}", e.getMessage());
            return false;
        }
    }

    public boolean sendQuery(String base, String place, String info, String condORvals) {
        // Send query to the database
        // use jdbc to send query to the database
        String query;
        switch (base) {
            case SQLConst.SELECT -> query = querySelect(place, info, condORvals);
            case SQLConst.INSERT -> query = queryInsert(place, info, condORvals);
            case SQLConst.UPDATE -> query = queryUpdate(place, info, condORvals);
            default -> {
                LoggerUtil.LOGGER.error("Unknown query type: {}", base);
                return false;
            }
        }

        if (query == null) {
            LoggerUtil.LOGGER.error("Error creating query.");
            return false;
        }

        // use query to send query to the database
        try {
            connection.createStatement().execute(query);
        } catch (SQLException e) {
            LoggerUtil.LOGGER.error("Error sending query to the database: {}", e.getMessage());
            return false;
        }
        // print the result
        LoggerUtil.LOGGER.info("Query sent to the database: {}", query);
        return true;
    }


    private String querySelect(String place, String data, String condition) {
        // Select query
        StringBuilder query = new StringBuilder();
        query.append(SQLConst.SELECT).append(" ")
                .append(data).append(" ")
                .append(SQLConst.FROM).append(" ")
                .append(place).append(" ")
                .append(SQLConst.WHERE).append(" ")
                .append(condition).append(";");
        // print the result
        LoggerUtil.LOGGER.info("Select query sent to the database: {}", query);
        return null;
    }

    private String queryInsert(String place, String data, String values) {
        // Insert query
        StringBuilder query = new StringBuilder();
        query.append(SQLConst.INSERT).append(" ")
                .append(SQLConst.INTO).append(" ")
                .append(place).append(" ").append("(").append(values).append(") ")
                .append(SQLConst.VALUES).append(" ")
                .append("(").append(values).append(")").append(";");
        // print the result
        LoggerUtil.LOGGER.info("Insert query sent to the database: {}", query);
        return null;
    }

    private String queryUpdate(String place, String info, String condition) {
        // Update query
        StringBuilder query = new StringBuilder();
        query.append(SQLConst.UPDATE).append(" ")
                .append(place).append(" ")
                .append(SQLConst.SET).append(" ")
                .append(info).append(" ")
                .append(SQLConst.WHERE).append(" ")
                .append(condition).append(";");
        // print the result
        LoggerUtil.LOGGER.info("Update query sent to the database: {}", query);
        return null;
    }
}
