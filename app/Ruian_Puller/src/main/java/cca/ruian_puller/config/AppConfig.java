package cca.ruian_puller.config;

import cca.ruian_puller.config.configObjects.RegionSoudrznostiBoolean;
import cca.ruian_puller.config.configObjects.StatBoolean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
    private RegionSoudrznostiBoolean regionSoudrznostiConfig = null;
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
            case HOW_OF_PROCESS_TABLES_ALL -> log.info("Processing all tables.");
            case HOW_OF_PROCESS_TABLES_SELECTED -> {
                log.info("Processing only selected tables.");
                fillObjectsToProcess(dataToProcessNode);
            }
            default -> {
                log.error("The config does not contain how to process data. Value must be one of: {} or {}",
                        HOW_OF_PROCESS_TABLES_ALL, HOW_OF_PROCESS_TABLES_SELECTED);
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

        Iterator<String> objectsName = tablesNode.fieldNames();
        while(objectsName.hasNext()) {
            String tableName = objectsName.next();
            JsonNode tableNode = tablesNode.get(tableName);
            switch (tableName) {
                case NodeConst.TABLE_STAT_NODE -> fillStatConfig(tableNode);
                case NodeConst.TABLE_REGION_SOUDRZNOSTI_NODE -> fillRegionSoudrznostiConfig(tableNode);
            }
        }
    }

    private void fillStatConfig(JsonNode statNode) {
        // Get how to process
        String howToProcess = getTextValue(statNode, NodeConst.HOW_TO_PROCESS_NODE);
        statConfig = new StatBoolean(howToProcess);
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
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

    private void fillRegionSoudrznostiConfig(JsonNode regionNode) {
        String howToProcess = getTextValue(regionNode, NodeConst.HOW_TO_PROCESS_NODE);
        regionSoudrznostiConfig = new RegionSoudrznostiBoolean(howToProcess);
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = regionNode.get(COLUMN_NODE);
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
                case RegionSoudrznostiBoolean.NAZEV -> regionSoudrznostiConfig.setNazev(true);
                case RegionSoudrznostiBoolean.NESPRAVNY -> regionSoudrznostiConfig.setNespravny(true);
                case RegionSoudrznostiBoolean.STAT -> regionSoudrznostiConfig.setStat(true);
                case RegionSoudrznostiBoolean.PLATIOD -> regionSoudrznostiConfig.setPlatiod(true);
                case RegionSoudrznostiBoolean.PLATIDO -> regionSoudrznostiConfig.setPlatido(true);
                case RegionSoudrznostiBoolean.IDTRANSAKCE -> regionSoudrznostiConfig.setIdtransakce(true);
                case RegionSoudrznostiBoolean.GLOBALNIIDNAVRHUZMENY -> regionSoudrznostiConfig.setGlobalniidnavrhuzmeny(true);
                case RegionSoudrznostiBoolean.NUTSLAU -> regionSoudrznostiConfig.setNutslau(true);
                case RegionSoudrznostiBoolean.GEOMETRIEDEFBOD -> regionSoudrznostiConfig.setGeometriedefbod(true);
                case RegionSoudrznostiBoolean.GEOMETRIEGENHRANICE -> regionSoudrznostiConfig.setGeometriegenhranice(true);
                case RegionSoudrznostiBoolean.GEOMETRIEORIHRANICE -> regionSoudrznostiConfig.setGeometrieorihranice(true);
                case RegionSoudrznostiBoolean.NESPRAVNEUDAJE -> regionSoudrznostiConfig.setNespravneudaje(true);
                case RegionSoudrznostiBoolean.DATUMVZNIKU -> regionSoudrznostiConfig.setDatumvzniku(true);
            }
        }
    }
}