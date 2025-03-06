package cca.ruian_puller.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static java.lang.System.exit;

@Getter
@Configuration
@Log4j2
public class AppConfig extends ConfigAbstract {

    private final boolean includeGeometry;
    private final int commitSize;
    private final String sourceSearch;
    private final String sourceList;


    public AppConfig(ObjectMapper objectMapper, @Value("${config.file.path}") String configFilePath) throws IOException {
        super(objectMapper, configFilePath);

        // Load the configuration
        includeGeometry = includeGeometry(configNode);
        commitSize = getCommitSize(configNode);
        sourceSearch = null;
        sourceList = null;
    }

    private boolean includeGeometry(JsonNode mainNode) {
        JsonNode specialInfoNode = mainNode.get(NodeConst.ADDITIONAL_OPTIONS_NODE);
        JsonNode databaseNode = mainNode.get(NodeConst.DATABASE_NODE);
        if (specialInfoNode != null && !getTextValue(databaseNode, NodeConst.DATABASE_TYPE_NODE).equals(NodeConst.ORACLE)) {
            return  getBooleanValue(specialInfoNode, NodeConst.INCLUDE_GEOMETRY_NODE);
        }
        return false;
    }

    private int getCommitSize(JsonNode mainNode) {
        JsonNode specialInfoNode = mainNode.get(NodeConst.ADDITIONAL_OPTIONS_NODE);
        return specialInfoNode != null ? getIntValue(specialInfoNode, NodeConst.COMMIT_SIZE_NODE) : 1000;
    }
}