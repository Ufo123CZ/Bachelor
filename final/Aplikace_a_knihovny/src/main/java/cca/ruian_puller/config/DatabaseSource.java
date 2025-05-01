package cca.ruian_puller.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

import static cca.ruian_puller.config.NodeConst.DB_NAME_MSSQL;

/**
 * DatabaseSource is a Spring configuration class that sets up the DataSource and EntityManagerFactory beans
 * for the application. It reads the database configuration from a JSON file and configures the DataSource
 * accordingly.
 */
@Configuration
@Log4j2
public class DatabaseSource extends ConfigAbstract {
    public DatabaseSource(ObjectMapper objectMapper, @Value("${config.file.path}") String configFilePath) throws IOException {
        super(objectMapper, configFilePath);
    }

    /**
     * Creates a DataSource bean for the application.
     * The DataSource is configured based on the database type specified in the configuration file.
     *
     * @return a DataSource object configured for the specified database type
     */
    @Bean
    public DataSource dataSource() {
        DbParams dbConfig = getDatabaseConfig(configNode);

        log.info("Database type: {}", dbConfig.getType());
        log.info("Connecting to database at {}", dbConfig.getUrl());
        log.info("Database name: {}", dbConfig.getName());
        log.info("Database username: {}", dbConfig.getUsername());

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(getDriverClassName(dbConfig.getType()));
        switch (dbConfig.getType().toLowerCase()) {
            case NodeConst.POSTGRESQL -> dataSource.setUrl(dbConfig.getUrl() + "/" + dbConfig.getName());
            case NodeConst.MSSQL -> dataSource.setUrl(dbConfig.getUrl() + DB_NAME_MSSQL + dbConfig.getName() + NodeConst.CERTIFICATE);
            case NodeConst.ORACLE -> dataSource.setUrl(dbConfig.getUrl() + "/" + dbConfig.getName());
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbConfig.getType());
        }
        dataSource.setUsername(dbConfig.getUsername());
        dataSource.setPassword(dbConfig.getPassword());

        return dataSource;
    }

    /**
     * Creates a LocalContainerEntityManagerFactoryBean for the application.
     * This factory bean is responsible for creating the EntityManagerFactory used by JPA.
     *
     * @param dataSource the DataSource to be used by the EntityManagerFactory
     * @return a LocalContainerEntityManagerFactoryBean configured for the specified DataSource
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("cca.ruian_puller.download.dto");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        DbParams dbParams = getDatabaseConfig(configNode);
        String dbType = dbParams.getType();
        switch (dbType) {
            case NodeConst.POSTGRESQL -> properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            case NodeConst.MSSQL -> properties.setProperty("hibernate.dialect", "org.hibernate.spatial.dialect.sqlserver.SqlServer2012SpatialDialect");
            case NodeConst.ORACLE -> properties.setProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
        em.setJpaProperties(properties);

        return em;
    }

    /**
     * Returns the driver class name for the specified database type.
     *
     * @param dbType the type of the database (e.g., PostgreSQL, MSSQL, Oracle)
     * @return the driver class name for the specified database type
     */
    private String getDriverClassName(String dbType) {
        return switch (dbType.toLowerCase()) {
            case NodeConst.POSTGRESQL -> "org.postgresql.Driver";
            case NodeConst.MSSQL -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case NodeConst.ORACLE -> "oracle.jdbc.OracleDriver";
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    /**
     * Retrieves the database configuration from the JSON configuration file.
     *
     * @param mainNode the root node of the JSON configuration
     * @return a DbParams object containing the database configuration
     */
    private DbParams getDatabaseConfig(JsonNode mainNode) {
        JsonNode databaseNode = mainNode.get(NodeConst.DATABASE_NODE);
        return new DbParams(
                databaseNode.get(NodeConst.DATABASE_TYPE_NODE).asText(),
                databaseNode.get(NodeConst.DATABASE_URL_NODE).asText(),
                databaseNode.get(NodeConst.DATABASE_NAME_NODE).asText(),
                databaseNode.get(NodeConst.DATABASE_USERNAME_NODE).asText(),
                databaseNode.get(NodeConst.DATABASE_PASSWORD_NODE).asText()
        );
    }
}
