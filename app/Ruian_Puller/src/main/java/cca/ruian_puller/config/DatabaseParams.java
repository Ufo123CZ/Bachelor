package cca.ruian_puller.config;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DatabaseParams {
    private String type;
    private String url;
    private String username;
    private String password;

    public DatabaseParams(String type, String url, String username, String password) {
        this.type = type;
        this.url = url;
        this.username = username;
        this.password = password;
    }
}
