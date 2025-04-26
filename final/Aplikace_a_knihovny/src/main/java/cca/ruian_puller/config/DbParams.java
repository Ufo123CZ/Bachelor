package cca.ruian_puller.config;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DbParams {
    // Elements from config file for database connection
    public String type;
    public String url;
    public String name;
    public String username;
    public String password;

    /**
     * Constructor for DbParams.
     *
     * @param type     the type of the database (e.g., PostgreSQL, MSSQL, Oracle)
     * @param url      the URL of the database
     * @param name     the name of the database
     * @param username the username for database connection
     * @param password the password for database connection
     */
    public DbParams(String type, String url, String name, String username, String password) {
        this.type = type;
        this.url = url;
        this.name = name;
        this.username = username;
        this.password = password;
    }
}
