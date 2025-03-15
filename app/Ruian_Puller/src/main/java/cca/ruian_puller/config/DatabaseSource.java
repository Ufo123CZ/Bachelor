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
import java.util.List;
import java.util.Properties;

@Configuration
@Log4j2
public class DatabaseSource extends ConfigAbstract {
    public DatabaseSource(ObjectMapper objectMapper, @Value("${config.file.path}") String configFilePath) throws IOException {
        super(objectMapper, configFilePath);
    }

    @Bean
    public DataSource dataSource() {
        List<String> dbConfig = getDatabaseConfig(configNode);

        log.info("Database type: {}", dbConfig.get(0));
        log.info("Connecting to database at {}", dbConfig.get(1));
        log.info("Database username: {}", dbConfig.get(2));

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(getDriverClassName(dbConfig.get(0)));
        if (dbConfig.get(0).equals(NodeConst.MSSQL)) {

            dataSource.setUrl(dbConfig.get(1) + NodeConst.CERTIFICATE);
        } else {
            dataSource.setUrl(dbConfig.get(1));
        }
        dataSource.setUsername(dbConfig.get(2));
        dataSource.setPassword(dbConfig.get(3));

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("cca.ruian_puller.download.dto");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        String dbType = getDatabaseConfig(configNode).get(0).toLowerCase();
        switch (dbType) {
            case NodeConst.POSTGRESQL:
                properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                break;
            case NodeConst.MSSQL:
                properties.setProperty("hibernate.dialect", "org.hibernate.spatial.dialect.sqlserver.SqlServer2012SpatialDialect");
                break;
            case NodeConst.ORACLE:
                properties.setProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
        em.setJpaProperties(properties);

        return em;
    }

    private String getDriverClassName(String dbType) {
        return switch (dbType.toLowerCase()) {
            case NodeConst.POSTGRESQL -> "org.postgresql.Driver";
            case NodeConst.MSSQL -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case NodeConst.ORACLE -> "oracle.jdbc.OracleDriver";
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    private List<String> getDatabaseConfig(JsonNode mainNode) {
        JsonNode databaseNode = mainNode.get(NodeConst.DATABASE_NODE);
        return List.of(
                getTextValue(databaseNode, NodeConst.DATABASE_TYPE_NODE),
                getTextValue(databaseNode, NodeConst.DATABASE_URL_NODE),
                getTextValue(databaseNode, NodeConst.DATABASE_USERNAME_NODE),
                getTextValue(databaseNode, NodeConst.DATABASE_PASSWORD_NODE)
        );
    }
}
