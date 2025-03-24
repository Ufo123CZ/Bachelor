package cca.ruian_puller.config;

import cca.ruian_puller.config.configObjects.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Field;
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
    private VuscBoolean vuscConfig = null;
    private OkresBoolean okresConfig = null;
    private OrpBoolean orpConfig = null;
    private PouBoolean pouConfig = null;
    private ObecBoolean obecConfig = null;
    private CastObceBoolean castObceConfig = null;
    private MopBoolean mopConfig = null;
    private SpravniObvodBoolean spravniObvodConfig = null;
    private MomcBoolean momcConfig = null;
    private KatastralniUzemiBoolean katastralniUzemiConfig = null;
    private ParcelaBoolean parcelaConfig = null;
    private UliceBoolean uliceConfig = null;
    private StavebniObjektBoolean stavebniObjektConfig = null;
    private AdresniMistoBoolean adresniMistoConfig = null;
    private ZsjBoolean zsjConfig = null;
    private VOBoolean voConfig = null;
    private ZaniklyPrvekBoolean zaniklyPrvekConfig = null;


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
        log.info("===============================================================");
    }

    private boolean includeGeometry(JsonNode mainNode) {
        JsonNode specialInfoNode = mainNode.get(NodeConst.ADDITIONAL_OPTIONS_NODE);
        return specialInfoNode != null && getBooleanValue(specialInfoNode, NodeConst.INCLUDE_GEOMETRY_NODE);
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
                case NodeConst.TABLE_VUSC_NODE -> fillVuscConfig(tableNode);
                case NodeConst.TABLE_OKRES_NODE -> fillOkresConfig(tableNode);
                case NodeConst.TABLE_ORP_NODE -> fillOrpConfig(tableNode);
                case NodeConst.TABLE_POU_NODE -> fillPouConfig(tableNode);
                case NodeConst.TABLE_OBEC_NODE -> fillObecConfig(tableNode);
                case NodeConst.TABLE_CAST_OBCE_NODE -> fillCastObceConfig(tableNode);
                case NodeConst.TABLE_MOP_NODE -> fillMopConfig(tableNode);
                case NodeConst.TABLE_SPRAVNI_OBVOD_NODE -> fillSpravniObvodConfig(tableNode);
                case NodeConst.TABLE_MOMC_NODE -> fillMomcConfig(tableNode);
                case NodeConst.TABLE_KATASTRALNI_UZEMI_NODE -> fillKatastralniUzemiConfig(tableNode);
                case NodeConst.TABLE_PARCELA_NODE -> fillParcelaConfig(tableNode);
                case NodeConst.TABLE_ULICE_NODE -> fillUliceConfig(tableNode);
                case NodeConst.TABLE_STAVEBNI_OBJEKT_NODE -> fillStavebniObjektConfig(tableNode);
                case NodeConst.TABLE_ADRESNI_MISTO_NODE -> fillAdresniMistoConfig(tableNode);
                case NodeConst.TABLE_ZSJ_NODE -> fillZsjConfig(tableNode);
                case NodeConst.TABLE_VO_NODE -> fillVOConfig(tableNode);
                case NodeConst.TABLE_ZANIKLY_PRVEK_NODE -> fillZaniklyPrvekConfig(tableNode);
            }
        }
    }

    //region Fill objects
    private void fillStatConfig(JsonNode statNode) {
        // Get how to process
        String howToProcess = getTextValue(statNode, NodeConst.HOW_TO_PROCESS_NODE);
        statConfig = new StatBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Stat will be processed.");
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

        // Log what will be processed
        try {
            printTrueBooleanFields(statConfig, "Stat", statConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Stat.");
            exit(1);
        }

    }

    private void fillRegionSoudrznostiConfig(JsonNode regionSoudrznostiNode) {
        String howToProcess = getTextValue(regionSoudrznostiNode, NodeConst.HOW_TO_PROCESS_NODE);
        regionSoudrznostiConfig = new RegionSoudrznostiBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of RegionSoudrznosti will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = regionSoudrznostiNode.get(COLUMN_NODE);
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
                default -> {}
            }
        }

        // Log what will be processed
        try {
        printTrueBooleanFields(regionSoudrznostiConfig, "RegionSoudrznosti", regionSoudrznostiConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of RegionSoudrznosti.");
            exit(1);
        }
    }

    private void fillVuscConfig (JsonNode vuscNode) {
        // Get how to process
        String howToProcess = getTextValue(vuscNode, NodeConst.HOW_TO_PROCESS_NODE);
        vuscConfig = new VuscBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Vusc will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = vuscNode.get(COLUMN_NODE);
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
                case VuscBoolean.NAZEV -> vuscConfig.setNazev(true);
                case VuscBoolean.NESPRAVNY -> vuscConfig.setNespravny(true);
                case VuscBoolean.REGIONSOUDRZNOSTI -> vuscConfig.setRegionsoudrznosti(true);
                case VuscBoolean.PLATIOD -> vuscConfig.setPlatiod(true);
                case VuscBoolean.PLATIDO -> vuscConfig.setPlatido(true);
                case VuscBoolean.IDTRANSAKCE -> vuscConfig.setIdtransakce(true);
                case VuscBoolean.GLOBALNIIDNAVRHUZMENY -> vuscConfig.setGlobalniidnavrhuzmeny(true);
                case VuscBoolean.NUTSLAU -> vuscConfig.setNutslau(true);
                case VuscBoolean.GEOMETRIEDEFBOD -> vuscConfig.setGeometriedefbod(true);
                case VuscBoolean.GEOMETRIEGENHRANICE -> vuscConfig.setGeometriegenhranice(true);
                case VuscBoolean.GEOMETRIEORIHRANICE -> vuscConfig.setGeometrieorihranice(true);
                case VuscBoolean.NESPRAVNEUDAJE -> vuscConfig.setNespravneudaje(true);
                case VuscBoolean.DATUMVZNIKU -> vuscConfig.setDatumvzniku(true);
                default -> {
                }
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(vuscConfig, "Vusc", vuscConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Vusc.");
            exit(1);
        }
    }

    private void fillOkresConfig(JsonNode okresNode) {
        // Get how to process
        String howToProcess = getTextValue(okresNode, NodeConst.HOW_TO_PROCESS_NODE);
        okresConfig = new OkresBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Okres will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = okresNode.get(COLUMN_NODE);
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
                case OkresBoolean.NAZEV -> okresConfig.setNazev(true);
                case OkresBoolean.NESPRAVNY -> okresConfig.setNespravny(true);
                case OkresBoolean.KRAJ -> okresConfig.setKraj(true);
                case OkresBoolean.VUSC -> okresConfig.setVusc(true);
                case OkresBoolean.PLATIOD -> okresConfig.setPlatiod(true);
                case OkresBoolean.PLATIDO -> okresConfig.setPlatido(true);
                case OkresBoolean.IDTRANSAKCE -> okresConfig.setIdtransakce(true);
                case OkresBoolean.GLOBALNIIDNAVRHUZMENY -> okresConfig.setGlobalniidnavrhuzmeny(true);
                case OkresBoolean.NUTSLAU -> okresConfig.setNutslau(true);
                case OkresBoolean.GEOMETRIEDEFBOD -> okresConfig.setGeometriedefbod(true);
                case OkresBoolean.GEOMETRIEGENHRANICE -> okresConfig.setGeometriegenhranice(true);
                case OkresBoolean.GEOMETRIEORIHRANICE -> okresConfig.setGeometrieorihranice(true);
                case OkresBoolean.NESPRAVNEUDAJE -> okresConfig.setNespravneudaje(true);
                case OkresBoolean.DATUMVZNIKU -> okresConfig.setDatumvzniku(true);
                default -> {
                }
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(okresConfig, "Okres", okresConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Okres.");
            exit(1);
        }
    }

    private void fillOrpConfig(JsonNode orpNode) {
        // Get how to process
        String howToProcess = getTextValue(orpNode, NodeConst.HOW_TO_PROCESS_NODE);
        orpConfig = new OrpBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Orp will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = orpNode.get(COLUMN_NODE);
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
                case OrpBoolean.NAZEV -> orpConfig.setNazev(true);
                case OrpBoolean.NESPRAVNY -> orpConfig.setNespravny(true);
                case OrpBoolean.SPRAVNI_OBEC_KOD -> orpConfig.setSpravniobeckod(true);
                case OrpBoolean.VUSC -> orpConfig.setVusc(true);
                case OrpBoolean.OKRES -> orpConfig.setOkres(true);
                case OrpBoolean.PLATIOD -> orpConfig.setPlatiod(true);
                case OrpBoolean.PLATIDO -> orpConfig.setPlatido(true);
                case OrpBoolean.IDTRANSAKCE -> orpConfig.setIdtransakce(true);
                case OrpBoolean.GLOBALNIIDNAVRHUZMENY -> orpConfig.setGlobalniidnavrhuzmeny(true);
                case OrpBoolean.GEOMETRIEDEFBOD -> orpConfig.setGeometriedefbod(true);
                case OrpBoolean.GEOMETRIEGENHRANICE -> orpConfig.setGeometriegenhranice(true);
                case OrpBoolean.GEOMETRIEORIHRANICE -> orpConfig.setGeometrieorihranice(true);
                case OrpBoolean.NESPRAVNEUDAJE -> orpConfig.setNespravneudaje(true);
                case OrpBoolean.DATUMVZNIKU -> orpConfig.setDatumvzniku(true);
                default -> {
                }
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(orpConfig, "Orp", orpConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Orp.");
            exit(1);
        }
    }

    private void fillPouConfig(JsonNode pouNode) {
        // Get how to process
        String howToProcess = getTextValue(pouNode, NodeConst.HOW_TO_PROCESS_NODE);
        pouConfig = new PouBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Pou will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = pouNode.get(COLUMN_NODE);
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
                case PouBoolean.NAZEV -> pouConfig.setNazev(true);
                case PouBoolean.NESPRAVNY -> pouConfig.setNespravny(true);
                case PouBoolean.SPRAVNIOBECKOD -> pouConfig.setSpravniobeckod(true);
                case PouBoolean.ORP -> pouConfig.setOrp(true);
                case PouBoolean.PLATIOD -> pouConfig.setPlatiod(true);
                case PouBoolean.PLATIDO -> pouConfig.setPlatido(true);
                case PouBoolean.IDTRANSAKCE -> pouConfig.setIdtransakce(true);
                case PouBoolean.GLOBALNIIDNAVRHUZMENY -> pouConfig.setGlobalniidnavrhuzmeny(true);
                case PouBoolean.GEOMETRIEDEFBOD -> pouConfig.setGeometriedefbod(true);
                case PouBoolean.GEOMETRIEGENHRANICE -> pouConfig.setGeometriegenhranice(true);
                case PouBoolean.GEOMETRIEORIHRANICE -> pouConfig.setGeometrieorihranice(true);
                case PouBoolean.NESPRAVNEUDAJE -> pouConfig.setNespravneudaje(true);
                case PouBoolean.DATUMVZNIKU -> pouConfig.setDatumvzniku(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(pouConfig, "Pou", pouConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Pou.");
            exit(1);
        }
    }

    private void fillObecConfig(JsonNode obecNode) {
        // Get how to process
        String howToProcess = getTextValue(obecNode, NodeConst.HOW_TO_PROCESS_NODE);
        obecConfig = new ObecBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Obec will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = obecNode.get(COLUMN_NODE);
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
                case ObecBoolean.NAZEV -> obecConfig.setNazev(true);
                case ObecBoolean.NESPRAVNY -> obecConfig.setNespravny(true);
                case ObecBoolean.STATUSKOD -> obecConfig.setStatuskod(true);
                case ObecBoolean.OKRES -> obecConfig.setOkres(true);
                case ObecBoolean.POU -> obecConfig.setPou(true);
                case ObecBoolean.PLATIOD -> obecConfig.setPlatiod(true);
                case ObecBoolean.PLATIDO -> obecConfig.setPlatido(true);
                case ObecBoolean.IDTRANSAKCE -> obecConfig.setIdtransakce(true);
                case ObecBoolean.GLOBALNIIDNAVRHUZMENY -> obecConfig.setGlobalniidnavrhuzmeny(true);
                case ObecBoolean.MLUVNICKECHARAKTERISTIKY -> obecConfig.setMluvnickecharakteristiky(true);
                case ObecBoolean.VLAJKATEXT -> obecConfig.setVlajkatext(true);
                case ObecBoolean.VLAJKAOBRAZEK -> obecConfig.setVlajkaobrazek(true);
                case ObecBoolean.ZNAKTEXT -> obecConfig.setZnaktext(true);
                case ObecBoolean.ZNAKOBRAZEK -> obecConfig.setZnakobrazek(true);
                case ObecBoolean.CLENENISMROZSAHKOD -> obecConfig.setClenenismrozsahkod(true);
                case ObecBoolean.CLENENISMTYPKOD -> obecConfig.setClenenismtypkod(true);
                case ObecBoolean.NUTSLAU -> obecConfig.setNutslau(true);
                case ObecBoolean.GEOMETRIEDEFBOD -> obecConfig.setGeometriedefbod(true);
                case ObecBoolean.GEOMETRIEGENHRANICE -> obecConfig.setGeometriegenhranice(true);
                case ObecBoolean.GEOMETRIEORIHRANICE -> obecConfig.setGeometrieorihranice(true);
                case ObecBoolean.NESPRAVNEUDAJE -> obecConfig.setNespravneudaje(true);
                case ObecBoolean.DATUMVZNIKU -> obecConfig.setDatumvzniku(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(obecConfig, "Obec", obecConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Obec.");
            exit(1);
        }
    }

    private void fillCastObceConfig(JsonNode castObceNode) {
        // Get how to process
        String howToProcess = getTextValue(castObceNode, NodeConst.HOW_TO_PROCESS_NODE);
        castObceConfig = new CastObceBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of CastObce will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = castObceNode.get(COLUMN_NODE);
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
                case CastObceBoolean.NAZEV -> castObceConfig.setNazev(true);
                case CastObceBoolean.NESPRAVNY -> castObceConfig.setNespravny(true);
                case CastObceBoolean.OBEC -> castObceConfig.setObec(true);
                case CastObceBoolean.PLATIOD -> castObceConfig.setPlatiod(true);
                case CastObceBoolean.PLATIDO -> castObceConfig.setPlatido(true);
                case CastObceBoolean.IDTRANSAKCE -> castObceConfig.setIdtransakce(true);
                case CastObceBoolean.GLOBALNIIDNAVRHUZMENY -> castObceConfig.setGlobalniidnavrhuzmeny(true);
                case CastObceBoolean.MLUVNICKECHARAKTERISTIKY -> castObceConfig.setMluvnickecharakteristiky(true);
                case CastObceBoolean.GEOMETRIEDEFBOD -> castObceConfig.setGeometriedefbod(true);
                case CastObceBoolean.NESPRAVNEUDAJE -> castObceConfig.setNespravneudaje(true);
                case CastObceBoolean.DATUMVZNIKU -> castObceConfig.setDatumvzniku(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(castObceConfig, "CastObce", castObceConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of CastObce.");
            exit(1);
        }
    }

    private void fillMopConfig(JsonNode mopNode) {
        // Get how to process
        String howToProcess = getTextValue(mopNode, NodeConst.HOW_TO_PROCESS_NODE);
        mopConfig = new MopBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Mop will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = mopNode.get(COLUMN_NODE);
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
                case MopBoolean.NAZEV -> mopConfig.setNazev(true);
                case MopBoolean.NESPRAVNY -> mopConfig.setNespravny(true);
                case MopBoolean.OBEC -> mopConfig.setObec(true);
                case MopBoolean.PLATIOD -> mopConfig.setPlatiod(true);
                case MopBoolean.PLATIDO -> mopConfig.setPlatido(true);
                case MopBoolean.IDTRANSAKCE -> mopConfig.setIdtransakce(true);
                case MopBoolean.GLOBALNIIDNAVRHUZMENY -> mopConfig.setGlobalniidnavrhuzmeny(true);
                case MopBoolean.GEOMETRIEDEFBOD -> mopConfig.setGeometriedefbod(true);
                case MopBoolean.NESPRAVNEUDAJE -> mopConfig.setNespravneudaje(true);
                case MopBoolean.DATUMVZNIKU -> mopConfig.setDatumvzniku(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(mopConfig, "Mop", mopConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Mop.");
            exit(1);
        }
    }

    private void fillSpravniObvodConfig(JsonNode spravniObvodNode) {
        // Get how to process
        String howToProcess = getTextValue(spravniObvodNode, NodeConst.HOW_TO_PROCESS_NODE);
        spravniObvodConfig = new SpravniObvodBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of SpravniObvod will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = spravniObvodNode.get(COLUMN_NODE);
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
                case SpravniObvodBoolean.NAZEV -> spravniObvodConfig.setNazev(true);
                case SpravniObvodBoolean.NESPRAVNY -> spravniObvodConfig.setNespravny(true);
                case SpravniObvodBoolean.SPRAVNIMOMCKOD -> spravniObvodConfig.setSpravnimomckod(true);
                case SpravniObvodBoolean.OBEC -> spravniObvodConfig.setObec(true);
                case SpravniObvodBoolean.PLATIOD -> spravniObvodConfig.setPlatiod(true);
                case SpravniObvodBoolean.PLATIDO -> spravniObvodConfig.setPlatido(true);
                case SpravniObvodBoolean.IDTRANSAKCE -> spravniObvodConfig.setIdtransakce(true);
                case SpravniObvodBoolean.GLOBALNIIDNAVRHUZMENY -> spravniObvodConfig.setGlobalniidnavrhuzmeny(true);
                case SpravniObvodBoolean.GEOMETRIEDEFBOD -> spravniObvodConfig.setGeometriedefbod(true);
                case SpravniObvodBoolean.GEOMETRIEORIHRANICE -> spravniObvodConfig.setGeometrieorihranice(true);
                case SpravniObvodBoolean.NESPRAVNEUDAJE -> spravniObvodConfig.setNespravneudaje(true);
                case SpravniObvodBoolean.DATUMVZNIKU -> spravniObvodConfig.setDatumvzniku(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(spravniObvodConfig, "SpravniObvod", spravniObvodConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of SpravniObvod.");
            exit(1);
        }
    }

    private void fillMomcConfig(JsonNode momcNode) {
        // Get how to process
        String howToProcess = getTextValue(momcNode, NodeConst.HOW_TO_PROCESS_NODE);
        momcConfig = new MomcBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Momc will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = momcNode.get(COLUMN_NODE);
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
                case MomcBoolean.NAZEV -> momcConfig.setNazev(true);
                case MomcBoolean.NESPRAVNY -> momcConfig.setNespravny(true);
                case MomcBoolean.MOP -> momcConfig.setMop(true);
                case MomcBoolean.OBEC -> momcConfig.setObec(true);
                case MomcBoolean.SPRAVNIOBVOD -> momcConfig.setSpravniobvod(true);
                case MomcBoolean.PLATIOD -> momcConfig.setPlatiod(true);
                case MomcBoolean.PLATIDO -> momcConfig.setPlatido(true);
                case MomcBoolean.IDTRANSAKCE -> momcConfig.setIdtransakce(true);
                case MomcBoolean.GLOBALNIIDNAVRHUZMENY -> momcConfig.setGlobalniidnavrhuzmeny(true);
                case MomcBoolean.VLAJKATEXT -> momcConfig.setVlajkatext(true);
                case MomcBoolean.VLAJKAOBRAZEK -> momcConfig.setVlajkaobrazek(true);
                case MomcBoolean.ZNAKTEXT -> momcConfig.setZnaktext(true);
                case MomcBoolean.MLUVNICKECHARAKTERISTIKY -> momcConfig.setMluvnickecharakteristiky(true);
                case MomcBoolean.ZNAKOBRAZEK -> momcConfig.setZnakobrazek(true);
                case MomcBoolean.GEOMETRIEDEFBOD -> momcConfig.setGeometriedefbod(true);
                case MomcBoolean.GEOMETRIEORIHRANICE -> momcConfig.setGeometrieorihranice(true);
                case MomcBoolean.NESPRAVNEUDAJE -> momcConfig.setNespravneudaje(true);
                case MomcBoolean.DATUMVZNIKU -> momcConfig.setDatumvzniku(true);
                default -> {
                }
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(momcConfig, "Momc", momcConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Momc.");
            exit(1);
        }
    }

    private void fillKatastralniUzemiConfig(JsonNode katastralniUzemiNode) {
        // Get how to process
        String howToProcess = getTextValue(katastralniUzemiNode, NodeConst.HOW_TO_PROCESS_NODE);
        katastralniUzemiConfig = new KatastralniUzemiBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of KatastralniUzemi will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = katastralniUzemiNode.get(COLUMN_NODE);
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
                case KatastralniUzemiBoolean.NAZEV -> katastralniUzemiConfig.setNazev(true);
                case KatastralniUzemiBoolean.NESPRAVNY -> katastralniUzemiConfig.setNespravny(true);
                case KatastralniUzemiBoolean.EXISTUJEDIGITALNIMAPA -> katastralniUzemiConfig.setExistujedigitalnimapa(true);
                case KatastralniUzemiBoolean.OBEC -> katastralniUzemiConfig.setObec(true);
                case KatastralniUzemiBoolean.PLATIOD -> katastralniUzemiConfig.setPlatiod(true);
                case KatastralniUzemiBoolean.PLATIDO -> katastralniUzemiConfig.setPlatido(true);
                case KatastralniUzemiBoolean.IDTRANSAKCE -> katastralniUzemiConfig.setIdtransakce(true);
                case KatastralniUzemiBoolean.GLOBALNIIDNAVRHUZMENY -> katastralniUzemiConfig.setGlobalniidnavrhuzmeny(true);
                case KatastralniUzemiBoolean.RIZENIID -> katastralniUzemiConfig.setRizeniid(true);
                case KatastralniUzemiBoolean.MLUVNICKECHARAKTERISTIKY -> katastralniUzemiConfig.setMluvnickecharakteristiky(true);
                case KatastralniUzemiBoolean.GEOMETRIEDEFBOD -> katastralniUzemiConfig.setGeometriedefbod(true);
                case KatastralniUzemiBoolean.GEOMETRIEGENHRANICE -> katastralniUzemiConfig.setGeometriegenhranice(true);
                case KatastralniUzemiBoolean.NESPRAVNEUDAJE -> katastralniUzemiConfig.setNespravneudaje(true);
                case KatastralniUzemiBoolean.DATUMVZNIKU -> katastralniUzemiConfig.setDatumvzniku(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(katastralniUzemiConfig, "KatastralniUzemi", katastralniUzemiConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of KatastralniUzemi.");
            exit(1);
        }
    }

    private void fillParcelaConfig(JsonNode parcelaNode) {
        // Get how to process
        String howToProcess = getTextValue(parcelaNode, NodeConst.HOW_TO_PROCESS_NODE);
        parcelaConfig = new ParcelaBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Parcela will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = parcelaNode.get(COLUMN_NODE);
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
                case ParcelaBoolean.NESPRAVNY -> parcelaConfig.setNespravny(true);
                case ParcelaBoolean.KMENOVECISLO -> parcelaConfig.setKmenovecislo(true);
                case ParcelaBoolean.PODODDELENICISLA -> parcelaConfig.setPododdelenicisla(true);
                case ParcelaBoolean.VYMERAPARCELY -> parcelaConfig.setVymeraparcely(true);
                case ParcelaBoolean.ZPUSOBYVYUZITIPOZEMKU -> parcelaConfig.setZpusobyvyuzitipozemku(true);
                case ParcelaBoolean.DRUHCISLOVANIKOD -> parcelaConfig.setDruhcislovanikod(true);
                case ParcelaBoolean.DRUHPOZEMKUKOD -> parcelaConfig.setDruhpozemkukod(true);
                case ParcelaBoolean.KATASTRALNI_UZEMI -> parcelaConfig.setKatastralniuzemi(true);
                case ParcelaBoolean.PLATIOD -> parcelaConfig.setPlatiod(true);
                case ParcelaBoolean.PLATIDO -> parcelaConfig.setPlatido(true);
                case ParcelaBoolean.IDTRANSAKCE -> parcelaConfig.setIdtransakce(true);
                case ParcelaBoolean.RIZENIID -> parcelaConfig.setRizeniid(true);
                case ParcelaBoolean.BONITOVANEDILY -> parcelaConfig.setBonitovanedily(true);
                case ParcelaBoolean.ZPUSOBYOCHRANYPOZEMKU -> parcelaConfig.setZpusobyochranypozemku(true);
                case ParcelaBoolean.GEOMETRIEDEFBOD -> parcelaConfig.setGeometriedefbod(true);
                case ParcelaBoolean.GEOMETRIEORIHRANICE -> parcelaConfig.setGeometrieorihranice(true);
                case ParcelaBoolean.NESPRAVNEUDAJE -> parcelaConfig.setNespravneudaje(true);
                default -> {
                }
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(parcelaConfig, "Parcela", parcelaConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Parcela.");
            exit(1);
        }
    }

    private void fillUliceConfig(JsonNode uliceNode) {
        // Get how to process
        String howToProcess = getTextValue(uliceNode, NodeConst.HOW_TO_PROCESS_NODE);
        uliceConfig = new UliceBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Ulice will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = uliceNode.get(COLUMN_NODE);
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
                case UliceBoolean.NAZEV -> uliceConfig.setNazev(true);
                case UliceBoolean.NESPRAVNY -> uliceConfig.setNespravny(true);
                case UliceBoolean.OBEC -> uliceConfig.setObec(true);
                case UliceBoolean.PLATIOD -> uliceConfig.setPlatiod(true);
                case UliceBoolean.PLATIDO -> uliceConfig.setPlatido(true);
                case UliceBoolean.IDTRANSAKCE -> uliceConfig.setIdtransakce(true);
                case UliceBoolean.GLOBALNIIDNAVRHUZMENY -> uliceConfig.setGlobalniidnavrhuzmeny(true);
                case UliceBoolean.GEOMETRIEDEFBOD -> uliceConfig.setGeometriedefbod(true);
                case UliceBoolean.GEOMETRIEDEFCARA -> uliceConfig.setGeometriedefcara(true);
                case UliceBoolean.NESPRAVNEUDAJE -> uliceConfig.setNespravneudaje(true);
                default -> {
                }
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(uliceConfig, "Ulice", uliceConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Ulice.");
            exit(1);
        }
    }

    private void fillStavebniObjektConfig(JsonNode stavebniObjektNode) {
        // Get how to process
        String howToProcess = getTextValue(stavebniObjektNode, NodeConst.HOW_TO_PROCESS_NODE);
        stavebniObjektConfig = new StavebniObjektBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of StavebniObjekt will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = stavebniObjektNode.get(COLUMN_NODE);
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
                case StavebniObjektBoolean.NESPRAVNY -> stavebniObjektConfig.setNespravny(true);
                case StavebniObjektBoolean.CISLODOMOVNI -> stavebniObjektConfig.setCislodomovni(true);
                case StavebniObjektBoolean.IDENTIFIKACNIPARCELA -> stavebniObjektConfig.setIdentifikacniparcela(true);
                case StavebniObjektBoolean.TYPSTAVEBNIHOOBJEKTUKOD -> stavebniObjektConfig.setTypstavebnihoobjektukod(true);
                case StavebniObjektBoolean.CASTOBCE -> stavebniObjektConfig.setCastobce(true);
                case StavebniObjektBoolean.MOMC -> stavebniObjektConfig.setMomc(true);
                case StavebniObjektBoolean.PLATIOD -> stavebniObjektConfig.setPlatiod(true);
                case StavebniObjektBoolean.PLATIDO -> stavebniObjektConfig.setPlatido(true);
                case StavebniObjektBoolean.IDTRANSAKCE -> stavebniObjektConfig.setIdtransakce(true);
                case StavebniObjektBoolean.GLOBALNIIDNAVRHUZMENY -> stavebniObjektConfig.setGlobalniidnavrhuzmeny(true);
                case StavebniObjektBoolean.ISKNBUDOVAID -> stavebniObjektConfig.setIsknbudovaid(true);
                case StavebniObjektBoolean.DOKONCENI -> stavebniObjektConfig.setDokonceni(true);
                case StavebniObjektBoolean.DRUHKONSTRUKCEKOD -> stavebniObjektConfig.setDruhkonstrukcekod(true);
                case StavebniObjektBoolean.OBESTAVENYPROSTOR -> stavebniObjektConfig.setObestavenyprostor(true);
                case StavebniObjektBoolean.POCETBYTU -> stavebniObjektConfig.setPocetbytu(true);
                case StavebniObjektBoolean.POCETPODLAZI -> stavebniObjektConfig.setPocetpodlazi(true);
                case StavebniObjektBoolean.PODLAHOVAPLOCHA -> stavebniObjektConfig.setPodlahovaplocha(true);
                case StavebniObjektBoolean.PRIPPOJENIKANALIZACEKOD -> stavebniObjektConfig.setPripojenikanalizacekod(true);
                case StavebniObjektBoolean.PRIPPOJENIPLYNKOD -> stavebniObjektConfig.setPripojeniplynkod(true);
                case StavebniObjektBoolean.PRIPPOJENIVODOVODKOD -> stavebniObjektConfig.setPripojenivodovodkod(true);
                case StavebniObjektBoolean.VYBAVENIVYTAHEMKOD -> stavebniObjektConfig.setVybavenivytahemkod(true);
                case StavebniObjektBoolean.ZASTAVENAPLOCHA -> stavebniObjektConfig.setZastavenaplocha(true);
                case StavebniObjektBoolean.ZPUSOBVYTAPENIKOD -> stavebniObjektConfig.setZpusobvytapenikod(true);
                case StavebniObjektBoolean.ZPUSOBYOCHRANY -> stavebniObjektConfig.setZpusobyochrany(true);
                case StavebniObjektBoolean.DETAILNITEA -> stavebniObjektConfig.setDetailnitea(true);
                case StavebniObjektBoolean.GEOMETRIEDEFBOD -> stavebniObjektConfig.setGeometriedefbod(true);
                case StavebniObjektBoolean.GEOMETRIEORIHRANICE -> stavebniObjektConfig.setGeometrieorihranice(true);
                case StavebniObjektBoolean.NESPRAVNEUDAJE -> stavebniObjektConfig.setNespravneudaje(true);
                default -> {
                }
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(stavebniObjektConfig, "StavebniObjekt", stavebniObjektConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of StavebniObjekt.");
            exit(1);
        }
    }

    private void fillAdresniMistoConfig(JsonNode adresniMistoNode) {
        // Get how to process
        String howToProcess = getTextValue(adresniMistoNode, NodeConst.HOW_TO_PROCESS_NODE);
        adresniMistoConfig = new AdresniMistoBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of AdresniMisto will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = adresniMistoNode.get(COLUMN_NODE);
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
                case AdresniMistoBoolean.NESPRAVNY -> adresniMistoConfig.setNespravny(true);
                case AdresniMistoBoolean.CISLODOMOVNI -> adresniMistoConfig.setCislodomovni(true);
                case AdresniMistoBoolean.CISLOORIENTACNI -> adresniMistoConfig.setCisloorientacni(true);
                case AdresniMistoBoolean.CISLOORIENTACNIPISMENO -> adresniMistoConfig.setCisloorientacnipismeno(true);
                case AdresniMistoBoolean.PSC -> adresniMistoConfig.setPsc(true);
                case AdresniMistoBoolean.STAVEBNI_OBJEKT -> adresniMistoConfig.setStavebniobjekt(true);
                case AdresniMistoBoolean.ULICE -> adresniMistoConfig.setUlice(true);
                case AdresniMistoBoolean.VOKOD -> adresniMistoConfig.setVokod(true);
                case AdresniMistoBoolean.PLATIOD -> adresniMistoConfig.setPlatiod(true);
                case AdresniMistoBoolean.PLATIDO -> adresniMistoConfig.setPlatido(true);
                case AdresniMistoBoolean.IDTRANSAKCE -> adresniMistoConfig.setIdtransakce(true);
                case AdresniMistoBoolean.GLOBALNIIDNAVRHUZMENY -> adresniMistoConfig.setGlobalniidnavrhuzmeny(true);
                case AdresniMistoBoolean.GEOMETRIEDEFBOD -> adresniMistoConfig.setGeometriedefbod(true);
                case AdresniMistoBoolean.NESPRAVNEUDAJE -> adresniMistoConfig.setNespravneudaje(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(adresniMistoConfig, "AdresniMisto", adresniMistoConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of AdresniMisto.");
            exit(1);
        }
    }

    private void fillZsjConfig(JsonNode zsjNode) {
        // Get how to process
        String howToProcess = getTextValue(zsjNode, NodeConst.HOW_TO_PROCESS_NODE);
        zsjConfig = new ZsjBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Zsj will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = zsjNode.get(COLUMN_NODE);
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
                case ZsjBoolean.NAZEV -> zsjConfig.setNazev(true);
                case ZsjBoolean.NESPRAVNY -> zsjConfig.setNespravny(true);
                case ZsjBoolean.KATASTRALNIUZEMI -> zsjConfig.setKatastralniuzemi(true);
                case ZsjBoolean.PLATIOD -> zsjConfig.setPlatiod(true);
                case ZsjBoolean.PLATIDO -> zsjConfig.setPlatido(true);
                case ZsjBoolean.IDTRANSAKCE -> zsjConfig.setIdtransakce(true);
                case ZsjBoolean.GLOBALNIIDNAVRHUZMENY -> zsjConfig.setGlobalniidnavrhuzmeny(true);
                case ZsjBoolean.MLUVNICKECHARAKTERISTIKY -> zsjConfig.setMluvnickecharakteristiky(true);
                case ZsjBoolean.VYMERA -> zsjConfig.setVymera(true);
                case ZsjBoolean.CHARAKTERZSJKOD -> zsjConfig.setCharakterzsjkod(true);
                case ZsjBoolean.GEOMETRIEDEFBOD -> zsjConfig.setGeometriedefbod(true);
                case ZsjBoolean.GEOMETRIEORIHRANICE -> zsjConfig.setGeometrieorihranice(true);
                case ZsjBoolean.NESPRAVNEUDAJE -> zsjConfig.setNespravneudaje(true);
                case ZsjBoolean.DATUMVZNIKU -> zsjConfig.setDatumvzniku(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(zsjConfig, "Zsj", zsjConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Zsj.");
            exit(1);
        }
    }

    private void fillVOConfig(JsonNode voNode) {
        // Get how to process
        String howToProcess = getTextValue(voNode, NodeConst.HOW_TO_PROCESS_NODE);
        voConfig = new VOBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of Vo will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = voNode.get(COLUMN_NODE);
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
                case VOBoolean.PLATIOD -> voConfig.setPlatiod(true);
                case VOBoolean.PLATIDO -> voConfig.setPlatido(true);
                case VOBoolean.IDTRANSAKCE -> voConfig.setIdtransakce(true);
                case VOBoolean.GLOBALNIIDNAVRHUZMENY -> voConfig.setGlobalniidnavrhuzmeny(true);
                case VOBoolean.GEOMETRIEDEFBOD -> voConfig.setGeometriedefbod(true);
                case VOBoolean.GEOMETRIEGENHRANICE -> voConfig.setGeometriegenhranice(true);
                case VOBoolean.GEOMETRIEORIHRANICE -> voConfig.setGeometrieorihranice(true);
                case VOBoolean.NESPRAVNEUDAJE -> voConfig.setNespravneudaje(true);
                case VOBoolean.CISLO -> voConfig.setCislo(true);
                case VOBoolean.NESPRAVNY -> voConfig.setNespravny(true);
                case VOBoolean.OBEC -> voConfig.setObec(true);
                case VOBoolean.MOMC -> voConfig.setMomc(true);
                case VOBoolean.POZNAMKA -> voConfig.setPoznamka(true);
                default -> {}
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(voConfig, "Vo", voConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of Vo.");
            exit(1);
        }
    }

    private void fillZaniklyPrvekConfig(JsonNode zaniklyprvekNode) {
        // Get how to process
        String howToProcess = getTextValue(zaniklyprvekNode, NodeConst.HOW_TO_PROCESS_NODE);
        zaniklyPrvekConfig = new ZaniklyPrvekBoolean(howToProcess);

        log.info("--------------------------------------------------------------");
        // Check if contain all columns
        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            log.info("All columns of ZaniklyPrvek will be processed.");
            return;
        }
        // Find node columns
        JsonNode columnsNode = zaniklyprvekNode.get(COLUMN_NODE);
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
                case ZaniklyPrvekBoolean.TYPPRVKUKOD -> zaniklyPrvekConfig.setTypprvkukod(true);
                case ZaniklyPrvekBoolean.IDTRANSAKCE -> zaniklyPrvekConfig.setIdtransakce(true);
                default -> {
                }
            }
        }
        // Log what will be processed
        try {
            printTrueBooleanFields(zaniklyPrvekConfig, "ZaniklyPrvek", zaniklyPrvekConfig.getHowToProcess());
        } catch (IllegalAccessException e) {
            log.error("Error while printing true fields of ZaniklyPrvek.");
            exit(1);
        }
    }
    //endregion

    public static void printTrueBooleanFields(Object obj, String nameObject, String howToProcess) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        log.info("{} will be processed by mode {}", nameObject, howToProcess);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
                field.setAccessible(true); // Access private fields
                Object value = field.get(obj);
                if (Boolean.TRUE.equals(value)) {
                    log.info("Column {} will be processed.", field.getName());
                }
            }
        }
    }
}