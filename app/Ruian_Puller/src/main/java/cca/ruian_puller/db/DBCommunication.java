package cca.ruian_puller.db;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

@Getter
@Component
@Log4j2
public class DBCommunication {

    private static Connection connection = null;

    public static boolean connect(String dbUrl, String dbUsername, String dbPassword) {
        // Connect to the database
        // use jdbc to connect to the database
        // use dbUrl, dbUsername, dbPassword to connect to the database
        try {
            Properties props = new Properties();
            props.setProperty("user", dbUsername);
            props.setProperty("password", dbPassword);
            connection = DriverManager.getConnection(dbUrl, props);
            log.info("Connected to the database.");
            return true;
        } catch (SQLException e) {
            log.error("Error connecting to the database: {}", e.getMessage());
            return false;
        }
    }

    public boolean sendQuery(String base, String place, String info, ArrayList<String> values) {
        // Send query to the database
        // use jdbc to send query to the database
        // use query to send query to the database
        try {
            switch (base) {
//                case SQLConst.SELECT -> querySelect(place, info, condORvals);
                case SQLConst.INSERT -> queryInsert(place, info, values);
//                case SQLConst.UPDATE -> queryUpdate(place, info, condORvals);
                default -> {
                    log.error("Unknown query type: {}", base);
                    return false;
                }
            }
        } catch (SQLException e) {
            log.error("Error sending query to the database: {}", e.getMessage());
            return false;
        }
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
        return query.toString();
    }

    private void queryInsert(String place, String data, ArrayList<String> values) throws SQLException {
    // Construct the SQL query with the table name directly
    String sql = "INSERT INTO " + place + " (cislo1, cislo2, cislo3, cislo4) VALUES (?, ?, ?, ?)";

    PreparedStatement stmt = connection.prepareStatement(sql);

    // Set the values
    for (int i = 0; i < values.size(); i++) {
        String value = values.get(i);
        // Check if the value is numeric or not
        if (value.matches("-?\\d+(\\.\\d+)?")) {
            stmt.setInt(i + 1, Integer.parseInt(value));
        } else {
            stmt.setString(i + 1, value);
        }
    }

    // Execute the query
    int rowsInserted = stmt.executeUpdate();
    if (rowsInserted > 0) {
        log.info("A new row was inserted successfully!");
    }
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
        return query.toString();
    }
}
