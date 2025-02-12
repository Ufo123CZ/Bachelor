package cca.ruian_puller.config;

public class AppConfig {
    private ServerConfig server;
    private DatabaseConfig database;

    public AppConfig() {}

    //region Getters and Setters
    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }
    //endregion
}