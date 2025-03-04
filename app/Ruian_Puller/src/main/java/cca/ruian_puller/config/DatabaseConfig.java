package cca.ruian_puller.config;

import cca.ruian_puller.utils.Consts;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

//@Getter @Setter
//public class DatabaseConfig {
//    private String type;
//    private String url;
//    private String username;
//    private String password;
//
//    public DatabaseConfig(String type, String url, String username, String password) {
//        this.type = type;
//        this.url = url;
//        this.username = username;
//        this.password = password;
//    }
//}

@Configuration
@Log4j2
public class DatabaseConfig {

    private final ConfigReader configReader;

    public DatabaseConfig(ConfigReader configReader) {
        this.configReader = configReader;
    }

    @Bean
    public DataSource dataSource() {
        // Read the configuration
        DatabaseParams dbConfig = configReader.readDBConfig("src/main/resources/config.json");

        if (dbConfig == null) {
            log.error("Failed to load database configuration.");
            throw new RuntimeException("Database configuration is missing!");
        }

        log.info("Connecting to database at {}", dbConfig.getUrl());
        log.info("Database type: {}", dbConfig.getType());
        log.info("Database username: {}", dbConfig.getUsername());

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(getDriverClassName(dbConfig.getType()));
        dataSource.setUrl(dbConfig.getUrl());
        dataSource.setUsername(dbConfig.getUsername());
        dataSource.setPassword(dbConfig.getPassword());

        return dataSource;
    }

    private String getDriverClassName(String dbType) {
        return switch (dbType.toLowerCase()) {
            case "postgresql" -> "org.postgresql.Driver";
            case "mssql" -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "oracle" -> "oracle.jdbc.OracleDriver";
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }
}