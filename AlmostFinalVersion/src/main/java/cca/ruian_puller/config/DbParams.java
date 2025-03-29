package cca.ruian_puller.config;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DbParams {
    public String type;
    public String url;
    public String name;
    public String username;
    public String password;

    public DbParams(String type, String url, String name, String username, String password) {
        this.type = type;
        this.url = url;
        this.name = name;
        this.username = username;
        this.password = password;
    }
}
