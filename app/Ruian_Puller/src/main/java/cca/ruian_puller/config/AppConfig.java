package cca.ruian_puller.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter @Setter @Configuration
public class AppConfig {
    private DatabaseConfig database;
}