package cca.ruian_puller.config;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AppConfig {
    private DatabaseConfig database;

    public AppConfig(DatabaseConfig database) {
        this.database = database;
    }
}