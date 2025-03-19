package cca.ruian_puller.config;

import cca.ruian_puller.config.configObjects.StatBoolean;
import cca.ruian_puller.download.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cca.ruian_puller.config.NodeConst.*;
import static java.lang.System.exit;

@Getter
@Configuration
@Log4j2
public class AppConfig extends ConfigAbstract {

    private final boolean includeGeometry;
    private final int commitSize;

    private final String howToProcessTables;

    private StatBoolean statConfig = null;
//    private RegionBoolean regionSoudrznostiConfigInstance;
//    private VuscBoolean vuscConfigInstance;
//    private OkresBoolean okresConfigInstance;
//    private OrpBoolean orpConfigInstance;
//    private PouBoolean pouConfigInstance;
//    private ObecBoolean obecConfigInstance;
//    private CastObceBoolean castObceConfigInstance;
//    private final MopDto mopConfigInstance;
//    private final SpravniObvodDto spravniObvodConfigInstance;
//    private final MomcDto momcConfigInstance;
//    private final KatastralniUzemiDto katastralniUzemiConfigInstance;
//    private final ParcelaDto parcelaConfigInstance;
//    private final UliceDto uliceConfigInstance;
//    private final StavebniObjektDto stavebniObjektConfigInstance;
//    private final AdresniMistoDto adresniMistoConfigInstance;
//    private final ZsjDto zsjConfigInstance;
//    private final ZaniklyPrvekDto zaniklyPrvekConfigInstance;


    public AppConfig(ObjectMapper objectMapper, @Value("${config.file.path}") String configFilePath) throws IOException {
        super(objectMapper, configFilePath);

        // Load the configuration
        includeGeometry = includeGeometry(configNode);
        commitSize = getCommitSize(configNode);
        JsonNode dataToProcessNode = configNode.get(NodeConst.DATA_TO_PROCESS);
        if (dataToProcessNode == null) {
            log.error("The configuration file does not contain how to process data Node.");
            exit(1);
        }
        howToProcessTables = getTextValue(dataToProcessNode, HOW_TO_PROCESS_NODE);
        switch (howToProcessTables) {
            case HOW_OF_PROCESS_TABLES_VAL_1 -> log.info("Processing all tables.");
            case HOW_OF_PROCESS_TABLES_VAL_2 -> {
                log.info("Processing only selected tables.");
                fillObjectsToProcess(dataToProcessNode);
            }
            default -> {
                log.error("The config does not contain how to process data. Value must be one of: {} or {}",
                        HOW_OF_PROCESS_TABLES_VAL_1, HOW_OF_PROCESS_TABLES_VAL_2);
                exit(1);
            }
        }
    }

    private boolean includeGeometry(JsonNode mainNode) {
        JsonNode specialInfoNode = mainNode.get(NodeConst.ADDITIONAL_OPTIONS_NODE);
        return specialInfoNode != null ? getBooleanValue(specialInfoNode, NodeConst.INCLUDE_GEOMETRY_NODE) : false;
    }

    private int getCommitSize(JsonNode mainNode) {
        JsonNode specialInfoNode = mainNode.get(NodeConst.ADDITIONAL_OPTIONS_NODE);
        return specialInfoNode != null ? getIntValue(specialInfoNode, NodeConst.COMMIT_SIZE_NODE) : 1000;
    }

    private void fillObjectsToProcess(JsonNode mainNode) {
        JsonNode tablesNode = mainNode.get(NodeConst.TABLES_NODE);
        if (tablesNode == null) {
            log.error("The configuration file does not contain the required node: " + NodeConst.TABLES_NODE);
            exit(1);
        }

        List<JsonNode> tables = mainNode.findValues(NodeConst.TABLES_NODE);

        for(JsonNode node : tables) {
            // Get node name to string
            String tableName = node.fieldNames().next();
            JsonNode tableNode = node.get(tableName);
            switch (tableName) {
                case NodeConst.TABLE_STAT_NODE -> fillStatConfig(tableNode);
//                case NodeConst.TABLE_REGION_SOUDRZNOSTI_NODE -> regionSoudrznostiConfigInstance = fillRegionSoudrznostiConfig(node);
//                case NodeConst.TABLE_VUSC_NODE -> vuscConfigInstance = fillVuscConfig(node);
//                case NodeConst.TABLE_OKRES_NODE -> okresConfigInstance = fillOkresConfig(node);
//                case NodeConst.TABLE_ORP_NODE -> orpConfigInstance = fillOrpConfig(node);
//                case NodeConst.TABLE_POU_NODE -> pouConfigInstance = fillPouConfig(node);
//                case NodeConst.TABLE_OBEC_NODE -> obecConfigInstance = fillObecConfig(node);
//                case NodeConst.TABLE_CAST_OBCE_NODE -> castObceConfigInstance = fillCastObceConfig(node);
                default -> {
                    log.error("The configuration file does contain invalid table name.");
                    exit(1);
                }
            }
        }
    }

    private void fillStatConfig(JsonNode statNode) {
        // Get how to process
        String howToProcess = getTextValue(statNode, NodeConst.HOW_TO_PROCESS_NODE);
        statConfig = new StatBoolean(howToProcess);
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_VAL_1)) {
            log.info("All columns will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = statNode.get(COLUMN_NODE);
        if (columnsNode == null) {
            log.error("The configuration file does not contain the required node: " + COLUMN_NODE);
            exit(1);
        }

        // Get column names from array in columnsNode
        List<String> columnNames = new ArrayList<>();
        columnsNode.forEach(column -> columnNames.add(column.asText()));
        // Fill columns
        for (String column : columnNames) {
            switch (column) {
                case StatBoolean.KOD -> statConfig.setKod(true);
                case StatBoolean.NAZEV -> statConfig.setNazev(true);
                case StatBoolean.NESPRAVNY -> statConfig.setNespravny(true);
                case StatBoolean.PLATIOD -> statConfig.setPlatiod(true);
                case StatBoolean.PLATIDO -> statConfig.setPlatido(true);
                case StatBoolean.IDTRANSAKCE -> statConfig.setIdtransakce(true);
                case StatBoolean.GLOBALNIIDNAVRHUZMENY -> statConfig.setGlobalniidnavrhuzmeny(true);
                case StatBoolean.NUTSLAU -> statConfig.setNutslau(true);
                case StatBoolean.GEOMETRIEDEFBOD -> statConfig.setGeometriedefbod(true);
                case StatBoolean.GEOMETRIEGENHRANICE -> statConfig.setGeometriegenhranice(true);
                case StatBoolean.GEOMETRIEORIHRANICE -> statConfig.setGeometrieorihranice(true);
                case StatBoolean.NESPRAVNEUDAJE -> statConfig.setNespravneudaje(true);
                case StatBoolean.DATUMVZNIKU -> statConfig.setDatumvzniku(true);
                default -> {}
            }
        }
    }
}