package cca.ruian_puller.download;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.download.dto.*;
import cca.ruian_puller.download.elements.*;
import cca.ruian_puller.download.geometry.GeometryParser;
import cca.ruian_puller.download.jsonObjects.*;
import cca.ruian_puller.download.service.*;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cca.ruian_puller.download.VdpParserConst.*;

@Component
@Log4j2
public class VdpParser {
    //region Autowired services
    @Autowired
    private GeometryParser geometryParser;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private AdresniMistoService adresniMistoService;
    @Autowired
    private CastObceService castObceService;
    @Autowired
    private KatastralniUzemiService katastralniUzemiService;
    @Autowired
    private MomcService momcService;
    @Autowired
    private MopService mopService;
    @Autowired
    private ObecService obecService;
    @Autowired
    private OkresService okresService;
    @Autowired
    private OrpService orpService;
    @Autowired
    private ParcelaService parcelaService;
    @Autowired
    private PouService pouService;
    @Autowired
    private RegionSoudrznostiService regionSoudrznostiService;
    @Autowired
    private SpravniObvodService spravniObvodService;
    @Autowired
    private StatService statService;
    @Autowired
    private StavebniObjektService stavebniObjektService;
    @Autowired
    private UliceService uliceService;
    @Autowired
    private VOService voService;
    @Autowired
    private VuscService vuscService;
    @Autowired
    private ZaniklyPrvekService zaniklyPrvekService;
    @Autowired
    private ZsjService zsjService;
    //endregion

    XMLInputFactory factory;
    XMLStreamReader reader;

    public void processFile(final InputStream fileIS) {
        try {
            factory = XMLInputFactory.newInstance();
            reader = factory.createXMLStreamReader(fileIS);
            readRoot();
            readData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readRoot() throws XMLStreamException {
        while(reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = reader.getLocalName();
                if (name.equals(ELEMENT_DATA)) {
                    break;
                }
            }
        }
    }

    private void readData() throws XMLStreamException {
        while(reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                String name = reader.getLocalName();
                switch (name) {
                    case ELEMENT_STATY -> readStaty();
                    case ELEMENT_REGIONY_SOUDRZNOSTI -> readRegionySoudrznosti();
                    case ELEMENT_VUSC -> readVuscs();
                    case ELEMENT_OKRESY -> readOkresy();
                    case ELEMENT_ORP -> readOrps();
                    case ELEMENT_POU -> readPous();
                    case ELEMENT_OBCE -> readObce();
                    case ELEMENT_SOS -> readSpravniObvody();
                    case ELEMENT_MOP -> readMops();
                    case ELEMENT_MOMC -> readMomcs();
                    case ELEMENT_CASTI_OBCI -> readCastiObce();
                    case ELEMENT_KATASTRALNI_UZEMI -> readKatastrUzemis();
                    case ELEMENT_PARCELY -> readParcely();
                    case ELEMENT_ULICE -> readUlices();
                    case ELEMENT_STAVEBNI_OBJEKTY -> readStavebniObjekty();
                    case ELEMENT_ADRESNI_MISTA -> readAdresniMista();
                    case ELEMENT_ZSJ -> readZsjs();
                    case ELEMENT_VO -> readVOs();
                    case ELEMENT_ZANIKLE_PRVKY -> readZaniklePrvky();
                    default -> {}
                }
            }
        }
    }

    //region STAT
    private void readStaty() throws XMLStreamException {
        List<StatDto> statDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_STAT)) {
                StatDto statDto = readStat();
                if (statDto.getKod() != null) statDtos.add(statDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_STATY)) {
                break;
            }
        }
        log.info("Found {} Stat objects.", statDtos.size());
        if (appConfig.getStatConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Stat config is null. Skipping the saving of Stat objects.");
            return;
        }
        statService.prepareAndSave(statDtos, appConfig);
    }

    private StatDto readStat() throws XMLStreamException {
        StatDto statDto = new StatDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_STAT)) break;
                continue;
            }

            switch (name) {
                case StatTags.ELEMENT_KOD ->
                        statDto.setKod(Integer.parseInt(reader.getElementText()));
                case StatTags.ELEMENT_NAZEV ->
                        statDto.setNazev(reader.getElementText());
                case StatTags.ELEMENT_NESPRAVNY ->
                        statDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case StatTags.ELEMENT_PLATI_OD ->
                        statDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case StatTags.ELEMENT_PLATI_DO ->
                        statDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case StatTags.ELEMENT_ID_TRANSAKCE ->
                        statDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case StatTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        statDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case StatTags.ELEMENT_NUTS_LAU ->
                        statDto.setNutslau(reader.getElementText());
                case StatTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        statDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case StatTags.ELEMENT_GEN_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        statDto.setGeometriegenhranice(geometryParser.readGeneralizovaneHranice(reader));
                }
                case StatTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        statDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case StatTags.ELEMENT_NESPRAVNE_UDAJE ->
                        statDto.setNespravneudaje(readNespravneUdaje(StatTags.ELEMENT_NESPRAVNE_UDAJE));
                case StatTags.ELEMENT_DATUM_VZNIKU ->
                        statDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return statDto;
    }
    //endregion

    //region REGIONY_SOUDRZNOSTI
    private void readRegionySoudrznosti() throws XMLStreamException {
        List<RegionSoudrznostiDto> regionSoudrznostiDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_REGION_SOUDRZNOSTI)) {
                RegionSoudrznostiDto regionSoudrznostiDto = readRegionSoudrznosti();
                if (regionSoudrznostiDto.getKod() != null) regionSoudrznostiDtos.add(regionSoudrznostiDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_REGIONY_SOUDRZNOSTI)) {
                break;
            }
        }
        log.info("Found {} RegionSoudrznosti objects.", regionSoudrznostiDtos.size());
        if (appConfig.getRegionSoudrznostiConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("RegionSoudrznosti config is null. Skipping the saving of RegionSoudrznosti objects.");
            return;
        }
        regionSoudrznostiService.prepareAndSave(regionSoudrznostiDtos, appConfig);
    }

    private RegionSoudrznostiDto readRegionSoudrznosti() throws XMLStreamException {
        RegionSoudrznostiDto regionSoudrznostiDto = new RegionSoudrznostiDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_REGION_SOUDRZNOSTI)) break;
                continue;
            }

            switch (name) {
                case RegionSoudrznostiTags.ELEMENT_KOD ->
                        regionSoudrznostiDto.setKod(Integer.parseInt(reader.getElementText()));
                case RegionSoudrznostiTags.ELEMENT_NAZEV ->
                        regionSoudrznostiDto.setNazev(reader.getElementText());
                case RegionSoudrznostiTags.ELEMENT_NESPRAVNY ->
                        regionSoudrznostiDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case RegionSoudrznostiTags.ELEMENT_STAT ->
                        regionSoudrznostiDto.setStat(readFK(StatTags.ELEMENT_KOD));
                case RegionSoudrznostiTags.ELEMENT_PLATI_OD ->
                        regionSoudrznostiDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case RegionSoudrznostiTags.ELEMENT_PLATI_DO ->
                        regionSoudrznostiDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case RegionSoudrznostiTags.ELEMENT_ID_TRANSAKCE ->
                        regionSoudrznostiDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case RegionSoudrznostiTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        regionSoudrznostiDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case RegionSoudrznostiTags.ELEMENT_NUTS_LAU ->
                        regionSoudrznostiDto.setNutslau(reader.getElementText());
                case RegionSoudrznostiTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        regionSoudrznostiDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case RegionSoudrznostiTags.ELEMENT_GEN_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        regionSoudrznostiDto.setGeometriegenhranice(geometryParser.readGeneralizovaneHranice(reader));
                }
                case RegionSoudrznostiTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        regionSoudrznostiDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case RegionSoudrznostiTags.ELEMENT_NESPRAVNE_UDAJE ->
                        regionSoudrznostiDto.setNespravneudaje(readNespravneUdaje(RegionSoudrznostiTags.ELEMENT_NESPRAVNE_UDAJE));
                case RegionSoudrznostiTags.ELEMENT_DATUM_VZNIKU ->
                        regionSoudrznostiDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return regionSoudrznostiDto;
    }
    //endregion

    //region VUSC
    private void readVuscs() throws XMLStreamException {
        List<VuscDto> vuscDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_VUSC)) {
                VuscDto vuscDto = readVusc();
                if (vuscDto.getKod() != null) vuscDtos.add(vuscDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_VUSC)) {
                break;
            }
        }
        log.info("Found {} Vusc objects.", vuscDtos.size());
        if (appConfig.getVuscConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Vusc config is null. Skipping the saving of Vusc objects.");
            return;
        }
        vuscService.prepareAndSave(vuscDtos, appConfig);
    }

    private VuscDto readVusc() throws XMLStreamException {
        VuscDto vuscDto = new VuscDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_VUSC)) break;
                continue;
            }
            switch (name) {
                case VuscTags.ELEMENT_KOD ->
                        vuscDto.setKod(Integer.parseInt(reader.getElementText()));
                case VuscTags.ELEMENT_NAZEV ->
                        vuscDto.setNazev(reader.getElementText());
                case VuscTags.ELEMENT_NESPRAVNY ->
                        vuscDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case VuscTags.ELEMENT_REGION_SOUDRZNOSTI ->
                        vuscDto.setRegionsoudrznosti(readFK(RegionSoudrznostiTags.ELEMENT_KOD));
                case VuscTags.ELEMENT_PLATI_OD ->
                        vuscDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case VuscTags.ELEMENT_PLATI_DO ->
                        vuscDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case VuscTags.ELEMENT_ID_TRANSAKCE ->
                        vuscDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case VuscTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        vuscDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case VuscTags.ELEMENT_NUTS_LAU ->
                        vuscDto.setNutslau(reader.getElementText());
                case VuscTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        vuscDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case VuscTags.ELEMENT_GEN_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        vuscDto.setGeometriegenhranice(geometryParser.readGeneralizovaneHranice(reader));
                }
                case VuscTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        vuscDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case VuscTags.ELEMENT_NESPRAVNE_UDAJE ->
                        vuscDto.setNespravneudaje(readNespravneUdaje(VuscTags.ELEMENT_NESPRAVNE_UDAJE));
                case VuscTags.ELEMENT_DATUM_VZNIKU ->
                        vuscDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return vuscDto;
    }
    //endregion

    //region OKRES
    private void readOkresy() throws XMLStreamException {
        List<OkresDto> okresy = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_OKRES)) {
                OkresDto okres = readOkres();
                if (okres.getKod() != null) okresy.add(okres);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_OKRESY)) {
                break;
            }
        }
        log.info("Found {} Okres objects.", okresy.size());
        if (appConfig.getOkresConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Okres config is null. Skipping the saving of Okres objects.");
            return;
        }
        okresService.prepareAndSave(okresy, appConfig);
    }

    private OkresDto readOkres() throws XMLStreamException {
        OkresDto okres = new OkresDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_OKRES)) break;
                continue;
            }
            switch (name) {
                case OkresTags.ELEMENT_KOD ->
                        okres.setKod(Integer.parseInt(reader.getElementText()));
                case OkresTags.ELEMENT_NAZEV ->
                        okres.setNazev(reader.getElementText());
                case OkresTags.ELEMENT_NESPRAVNY ->
                        okres.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case OkresTags.ELEMENT_KRAJ ->
                        okres.setKraj(Integer.parseInt(reader.getElementText()));
                case OkresTags.ELEMENT_VUSC ->
                        okres.setVusc(readFK(VuscTags.ELEMENT_KOD));
                case OkresTags.ELEMENT_PLATI_OD ->
                        okres.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case OkresTags.ELEMENT_PLATI_DO ->
                        okres.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case OkresTags.ELEMENT_ID_TRANSAKCE ->
                        okres.setIdtransakce(Long.parseLong(reader.getElementText()));
                case OkresTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        okres.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case OkresTags.ELEMENT_NUTS_LAU ->
                        okres.setNutslau(reader.getElementText());
                case OkresTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        okres.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case OkresTags.ELEMENT_GEN_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        okres.setGeometriegenhranice(geometryParser.readGeneralizovaneHranice(reader));
                }
                case OkresTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        okres.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case OkresTags.ELEMENT_NESPRAVNE_UDAJE ->
                        okres.setNespravneudaje(readNespravneUdaje(OkresTags.ELEMENT_NESPRAVNE_UDAJE));
                case OkresTags.ELEMENT_DATUM_VZNIKU ->
                        okres.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {
                }
            }
        }
        return okres;
    }
    //endregion

    //region ORP
    private void readOrps() throws XMLStreamException {
        List<OrpDto> orpDtos = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && name.equals(ELEMENT_ORP)) {
                OrpDto orpDto = readOrp();
                if (orpDto.getKod() != null) orpDtos.add(orpDto);
            } else if (event == XMLStreamConstants.END_ELEMENT && name.equals(ELEMENT_ORP)) {
                break;
            }
        }
        log.info("Found {} Orp objects.", orpDtos.size());
        if (appConfig.getOrpConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Orp config is null. Skipping the saving of Orp objects.");
            return;
        }
        orpService.prepareAndSave(orpDtos, appConfig);
    }

    private OrpDto readOrp() throws XMLStreamException {
        OrpDto orpDto = new OrpDto();

        while(reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_ORP)) break;
                continue;
            }
            switch (name) {
                case OrpTags.ELEMENT_KOD ->
                        orpDto.setKod(Integer.parseInt(reader.getElementText()));
                case OrpTags.ELEMENT_NAZEV ->
                        orpDto.setNazev(reader.getElementText());
                case OrpTags.ELEMENT_NESPRAVNY ->
                        orpDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case OrpTags.ELEMENT_SPRAVNI_OBEC_KOD ->
                        orpDto.setSpravniobeckod(Integer.parseInt(reader.getElementText()));
                case OrpTags.ELEMENT_VUSC ->
                        orpDto.setVusc(readFK(VuscTags.ELEMENT_KOD));
                case OrpTags.ELEMENT_OKRES ->
                        orpDto.setOkres(readFK(OkresTags.ELEMENT_KOD));
                case OrpTags.ELEMENT_PLATI_OD ->
                        orpDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case OrpTags.ELEMENT_PLATI_DO ->
                        orpDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case OrpTags.ELEMENT_ID_TRANSAKCE ->
                        orpDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case OrpTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        orpDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case OrpTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        orpDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case OrpTags.ELEMENT_GEN_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        orpDto.setGeometriegenhranice(geometryParser.readGeneralizovaneHranice(reader));
                }
                case OrpTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        orpDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case OrpTags.ELEMENT_NESPRAVNE_UDAJE ->
                        orpDto.setNespravneudaje(readNespravneUdaje(OrpTags.ELEMENT_NESPRAVNE_UDAJE));
                case OrpTags.ELEMENT_DATUM_VZNIKU ->
                        orpDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return orpDto;
    }
    //endregion

    //region POU
    private void readPous() throws XMLStreamException {
        List<PouDto> pouDtos = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_POU)) {
                PouDto pouDto = readPou();
                if (pouDto.getKod() != null) pouDtos.add(pouDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_POU)) {
                break;
            }
        }

        log.info("Found {} Pou objects.", pouDtos.size());
        if (appConfig.getPouConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Pou config is null. Skipping the saving of Pou objects.");
            return;
        }
        pouService.prepareAndSave(pouDtos, appConfig);
    }

    private PouDto readPou() throws XMLStreamException {
        PouDto pouDto = new PouDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_POU)) break;
                continue;
            }
            switch (name) {
                case PouTags.ELEMENT_KOD ->
                        pouDto.setKod(Integer.parseInt(reader.getElementText()));
                case PouTags.ELEMENT_NAZEV ->
                        pouDto.setNazev(reader.getElementText());
                case PouTags.ELEMENT_NESPRAVNY ->
                        pouDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case PouTags.ELEMENT_SPRAVNI_OBEC_KOD ->
                        pouDto.setSpravniobeckod(Integer.parseInt(reader.getElementText()));
                case PouTags.ELEMENT_ORP ->
                        pouDto.setOrp(readFK(OrpTags.ELEMENT_KOD));
                case PouTags.ELEMENT_PLATI_OD ->
                        pouDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case PouTags.ELEMENT_PLATI_DO ->
                        pouDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case PouTags.ELEMENT_ID_TRANSAKCE ->
                        pouDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case PouTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        pouDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case PouTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        pouDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case PouTags.ELEMENT_GEN_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        pouDto.setGeometriegenhranice(geometryParser.readGeneralizovaneHranice(reader));
                }
                case PouTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        pouDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case PouTags.ELEMENT_NESPRAVNE_UDAJE ->
                        pouDto.setNespravneudaje(readNespravneUdaje(PouTags.ELEMENT_NESPRAVNE_UDAJE));
                case PouTags.ELEMENT_DATUM_VZNIKU ->
                        pouDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return pouDto;
    }
    //endregion

    //region OBCE
    private void readObce() throws XMLStreamException {
        List<ObecDto> obecDtos = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_OBEC)) {
                ObecDto obecDto = readObec();
                if (obecDto.getKod() != null) obecDtos.add(obecDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_OBCE)) {
                break;
            }
        }
        log.info("Found {} Obec objects.", obecDtos.size());
        if (appConfig.getObecConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Obec config is null. Skipping the saving of Obec objects.");
            return;
        }
        obecService.prepareAndSave(obecDtos, appConfig);
    }

    private ObecDto readObec() throws XMLStreamException {
        ObecDto obecDto = new ObecDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_OBEC)) break;
                continue;
            }
            switch (name) {
                case ObecTags.ELEMENT_KOD ->
                        obecDto.setKod(Integer.parseInt(reader.getElementText()));
                case ObecTags.ELEMENT_NAZEV ->
                        obecDto.setNazev(reader.getElementText());
                case ObecTags.ELEMENT_NESPRAVNY ->
                        obecDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case ObecTags.ELEMENT_STATUSKOD ->
                        obecDto.setStatuskod(Integer.parseInt(reader.getElementText()));
                case ObecTags.ELEMENT_OKRES ->
                        obecDto.setOkres(readFK(OkresTags.ELEMENT_KOD));
                case ObecTags.ELEMENT_POU ->
                        obecDto.setPou(readFK(PouTags.ELEMENT_KOD));
                case ObecTags.ELEMENT_PLATI_OD ->
                        obecDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case ObecTags.ELEMENT_PLATI_DO ->
                        obecDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case ObecTags.ELEMENT_ID_TRANSAKCE ->
                        obecDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case ObecTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        obecDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case ObecTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY ->
                        obecDto.setMluvnickecharakteristiky(readMCh(ObecTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY));
                case ObecTags.ELEMENT_VLAJKA_TEXT ->
                        obecDto.setVlajkatext(reader.getElementText());
                case ObecTags.ELEMENT_VLAJKA_OBRAZEK ->
                        obecDto.setVlajkaobrazek(reader.getElementText().getBytes());
                case ObecTags.ELEMENT_ZNAK_TEXT ->
                        obecDto.setZnaktext(reader.getElementText());
                case ObecTags.ELEMENT_ZNAK_OBRAZEK ->
                        obecDto.setZnakobrazek(reader.getElementText().getBytes());
                case ObecTags.ELEMENT_CLENENI_SM_ROZSAH_KOD ->
                        obecDto.setClenenismrozsahkod(Integer.parseInt(reader.getElementText()));
                case ObecTags.ELEMENT_CLENENI_SMT_TYP_KOD ->
                        obecDto.setClenenismtypkod(Integer.parseInt(reader.getElementText()));
                case ObecTags.ELEMENT_NUTS_LAU ->
                        obecDto.setNutslau(reader.getElementText());
                case ObecTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        obecDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case ObecTags.ELEMENT_GEN_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        obecDto.setGeometriegenhranice(geometryParser.readGeneralizovaneHranice(reader));
                }
                case ObecTags.ELEMENT_NESPRAVNE_UDAJE ->
                        obecDto.setNespravneudaje(readNespravneUdaje(ObecTags.ELEMENT_NESPRAVNE_UDAJE));
                case ObecTags.ELEMENT_DATUM_VZNIKU ->
                        obecDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return obecDto;
    }
    //endregion

    //region SRAVNI_OBVODY
    private void readSpravniObvody() throws XMLStreamException {
        List<SpravniObvodDto> spravniObvodDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_SO)) {
                SpravniObvodDto spravniObvodDto = readSpravniObvod();
                if (spravniObvodDto.getKod() != null) spravniObvodDtos.add(spravniObvodDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_SOS)) {
                break;
            }
        }
        log.info("Found {} SpravniObvod objects.", spravniObvodDtos.size());
        if (appConfig.getSpravniObvodConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("SpravniObvod config is null. Skipping the saving of SpravniObvod objects.");
            return;
        }
        spravniObvodService.prepareAndSave(spravniObvodDtos, appConfig);
    }

    private SpravniObvodDto readSpravniObvod() throws XMLStreamException {
        SpravniObvodDto spravniObvodDto = new SpravniObvodDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_SO)) break;
                continue;
            }
            switch (name) {
                case SpravniObvodTags.ELEMENT_KOD ->
                        spravniObvodDto.setKod(Integer.parseInt(reader.getElementText()));
                case SpravniObvodTags.ELEMENT_NAZEV ->
                        spravniObvodDto.setNazev(reader.getElementText());
                case SpravniObvodTags.ELEMENT_NESPRAVNY ->
                        spravniObvodDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case SpravniObvodTags.ELEMENT_SPRAVNI_MOMC_KOD ->
                        spravniObvodDto.setSpravnimomckod(Integer.parseInt(reader.getElementText()));
                case SpravniObvodTags.ELEMENT_OBEC ->
                        spravniObvodDto.setObec(readFK(ObecTags.ELEMENT_KOD));
                case SpravniObvodTags.ELEMENT_PLATI_OD ->
                        spravniObvodDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case SpravniObvodTags.ELEMENT_PLATI_DO ->
                        spravniObvodDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case SpravniObvodTags.ELEMENT_ID_TRANSAKCE ->
                        spravniObvodDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case SpravniObvodTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        spravniObvodDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case SpravniObvodTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        spravniObvodDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case SpravniObvodTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        spravniObvodDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case SpravniObvodTags.ELEMENT_NESPRAVNE_UDAJE ->
                        spravniObvodDto.setNespravneudaje(readNespravneUdaje(SpravniObvodTags.ELEMENT_NESPRAVNE_UDAJE));
                case SpravniObvodTags.ELEMENT_DATUM_VZNIKU ->
                        spravniObvodDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return spravniObvodDto;

    }
    //endregion

    //region MOP
    private void readMops() throws XMLStreamException {
        List<MopDto> mopDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_MOP)) {
                MopDto mopDto = readMop();
                if (mopDto.getKod() != null) mopDtos.add(mopDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_MOP)) {
                break;
            }
        }
        log.info("Found {} Mop objects.", mopDtos.size());
        if (appConfig.getMopConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Mop config is null. Skipping the saving of Mop objects.");
            return;
        }
        mopService.prepareAndSave(mopDtos, appConfig);
    }

    private MopDto readMop() throws XMLStreamException {
        MopDto mopDto = new MopDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_MOP)) break;
                continue;
            }
            switch (name) {
                case MopTags.ELEMENT_KOD ->
                        mopDto.setKod(Integer.parseInt(reader.getElementText()));
                case MopTags.ELEMENT_NAZEV ->
                        mopDto.setNazev(reader.getElementText());
                case MopTags.ELEMENT_NESPRAVNY ->
                        mopDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case MopTags.ELEMENT_OBEC ->
                        mopDto.setObec(readFK(ObecTags.ELEMENT_KOD));
                case MopTags.ELEMENT_PLATI_OD ->
                        mopDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case MopTags.ELEMENT_PLATI_DO ->
                        mopDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case MopTags.ELEMENT_ID_TRANSAKCE ->
                        mopDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case MopTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        mopDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case MopTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        mopDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case MopTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        mopDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case MopTags.ELEMENT_NESPRAVNE_UDAJE ->
                        mopDto.setNespravneudaje(readNespravneUdaje(MopTags.ELEMENT_NESPRAVNE_UDAJE));
                case MopTags.ELEMENT_DATUM_VZNIKU ->
                        mopDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return mopDto;
    }
    //endregion

    //region MOMC
    private void readMomcs() throws XMLStreamException {
        List<MomcDto> momcDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_MOMC)) {
                MomcDto momcDto = readMomc();
                if (momcDto.getKod() != null) momcDtos.add(momcDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_MOMC)) {
                break;
            }
        }
        log.info("Found {} Momc objects.", momcDtos.size());
        if (appConfig.getMomcConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Momc config is null. Skipping the saving of Momc objects.");
            return;
        }
        momcService.prepareAndSave(momcDtos, appConfig);
    }

    private MomcDto readMomc() throws XMLStreamException {
        MomcDto momcDto = new MomcDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_MOMC)) break;
                continue;
            }
            switch (name) {
                case MomcTags.ELEMENT_KOD ->
                        momcDto.setKod(Integer.parseInt(reader.getElementText()));
                case MomcTags.ELEMENT_NAZEV ->
                        momcDto.setNazev(reader.getElementText());
                case MomcTags.ELEMENT_NESPRAVNY ->
                        momcDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case MomcTags.ELEMENT_MOP ->
                        momcDto.setMop(readFK(MopTags.ELEMENT_KOD));
                case MomcTags.ELEMENT_OBEC ->
                        momcDto.setObec(readFK(ObecTags.ELEMENT_KOD));
                case MomcTags.ELEMENT_SPRAVNI_OBVOD ->
                        momcDto.setSpravniobvod(readFK(SpravniObvodTags.ELEMENT_KOD));
                case MomcTags.ELEMENT_PLATI_OD ->
                        momcDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case MomcTags.ELEMENT_PLATI_DO ->
                        momcDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case MomcTags.ELEMENT_ID_TRANSAKCE ->
                        momcDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case MomcTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        momcDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case MomcTags.ELEMENT_VLAJKA_TEXT ->
                        momcDto.setVlajkatext(reader.getElementText());
                case MomcTags.ELEMENT_VLAJKA_OBRAZEK ->
                        momcDto.setVlajkaobrazek(reader.getElementText().getBytes());
                case MomcTags.ELEMENT_ZNAK_TEXT ->
                        momcDto.setZnaktext(reader.getElementText());
                case MomcTags.ELEMENT_ZNAK_OBRAZEK ->
                        momcDto.setZnakobrazek(reader.getElementText().getBytes());
                case MomcTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY ->
                        momcDto.setMluvnickecharakteristiky(readMCh(MomcTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY));
                case MomcTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        momcDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case MomcTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        momcDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case MomcTags.ELEMENT_NESPRAVNE_UDAJE ->
                        momcDto.setNespravneudaje(readNespravneUdaje(MomcTags.ELEMENT_NESPRAVNE_UDAJE));
                case MomcTags.ELEMENT_DATUM_VZNIKU ->
                        momcDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return momcDto;
    }
    //endregion

    //region CAST_OBCE
    private void readCastiObce() throws XMLStreamException {
        List<CastObceDto> castObceDtos = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_CAST_OBCE)) {
                CastObceDto castObceDto = readCastObce();
                if (castObceDto.getKod() != null) castObceDtos.add(castObceDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_CASTI_OBCI)) {
                break;
            }
        }


        log.info("Found {} CastObce objects", castObceDtos.size());
        if (appConfig.getCastObceConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("CastObce config is null. Skipping the saving of CastObce objects.");
            return;
        }

        castObceService.prepareAndSave(castObceDtos, appConfig);
    }

    private CastObceDto readCastObce() throws XMLStreamException {
        CastObceDto castObceDto = new CastObceDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_CAST_OBCE)) break;
                continue;
            }
            switch (name) {
                case CastObceTags.ELEMENT_KOD ->
                        castObceDto.setKod(Integer.parseInt(reader.getElementText()));
                case CastObceTags.ELEMENT_NAZEV ->
                        castObceDto.setNazev(reader.getElementText());
                case CastObceTags.ELEMENT_NESPRAVNY ->
                        castObceDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case CastObceTags.ELEMENT_OBEC ->
                        castObceDto.setObec(readFK(ObecTags.ELEMENT_KOD));
                case CastObceTags.ELEMENT_PLATI_OD ->
                        castObceDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case CastObceTags.ELEMENT_PLATI_DO ->
                        castObceDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case CastObceTags.ELEMENT_ID_TRANSAKCE ->
                        castObceDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case CastObceTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        castObceDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case CastObceTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY ->
                        castObceDto.setMluvnickecharakteristiky(readMCh(CastObceTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY));
                case CastObceTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        castObceDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case CastObceTags.ELEMENT_NESPRAVNE_UDAJE ->
                        castObceDto.setNespravneudaje(readNespravneUdaje(CastObceTags.ELEMENT_NESPRAVNE_UDAJE));
                case CastObceTags.ELEMENT_DATUM_VZNIKU ->
                        castObceDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return castObceDto;
    }
    //endregion

    //region KATASTRALNI_UZEMI
    private void readKatastrUzemis() throws XMLStreamException {
        List<KatastralniUzemiDto> katastralniUzemiDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_KATASTRALNI_UZEMI)) {
                KatastralniUzemiDto katastralniUzemiDto = readKatastrUzemi();
                if (katastralniUzemiDto.getKod() != null) katastralniUzemiDtos.add(katastralniUzemiDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_KATASTRALNI_UZEMI)) {
                break;
            }
        }
        log.info("Found {} KatastralniUzemi objects", katastralniUzemiDtos.size());
        if (appConfig.getKatastralniUzemiConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("KatastralniUzemi config is null. Skipping the saving of KatastralniUzemi objects.");
            return;
        }
        katastralniUzemiService.prepareAndSave(katastralniUzemiDtos, appConfig);
    }

    private KatastralniUzemiDto readKatastrUzemi() throws XMLStreamException {
        KatastralniUzemiDto katastralniUzemiDto = new KatastralniUzemiDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_KATASTRALNI_UZEMI)) break;
                continue;
            }
            switch (name) {
                case KatastralniUzemiTags.ELEMENT_KOD ->
                        katastralniUzemiDto.setKod(Integer.parseInt(reader.getElementText()));
                case KatastralniUzemiTags.ELEMENT_NAZEV ->
                        katastralniUzemiDto.setNazev(reader.getElementText());
                case KatastralniUzemiTags.ELEMENT_NESPRAVNY ->
                        katastralniUzemiDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case KatastralniUzemiTags.ELEMENT_EXISTUJE_DIGITALNI_MAPA ->
                        katastralniUzemiDto.setExistujedigitalnimapa(Boolean.parseBoolean(reader.getElementText()));
                case KatastralniUzemiTags.ELEMENT_OBEC ->
                        katastralniUzemiDto.setObec(readFK(ObecTags.ELEMENT_KOD));
                case KatastralniUzemiTags.ELEMENT_PLATI_OD ->
                        katastralniUzemiDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case KatastralniUzemiTags.ELEMENT_PLATI_DO ->
                        katastralniUzemiDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case KatastralniUzemiTags.ELEMENT_ID_TRANSAKCE ->
                        katastralniUzemiDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case KatastralniUzemiTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        katastralniUzemiDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case KatastralniUzemiTags.ELEMENT_RIZENI_ID ->
                        katastralniUzemiDto.setRizeniid(Long.parseLong(reader.getElementText()));
                case KatastralniUzemiTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY ->
                        katastralniUzemiDto.setMluvnickecharakteristiky(readMCh(KatastralniUzemiTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY));
                case KatastralniUzemiTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        katastralniUzemiDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case KatastralniUzemiTags.ELEMENT_GEN_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        katastralniUzemiDto.setGeometriegenhranice(geometryParser.readGeneralizovaneHranice(reader));
                }
                case KatastralniUzemiTags.ELEMENT_NESPRAVNEUDAJE ->
                        katastralniUzemiDto.setNespravneudaje(readNespravneUdaje(KatastralniUzemiTags.ELEMENT_NESPRAVNEUDAJE));
                case KatastralniUzemiTags.ELEMENT_DATUM_VZNIKU ->
                        katastralniUzemiDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return katastralniUzemiDto;
    }
    //endregion

    //region Parcela
    private void readParcely() throws XMLStreamException {
        List<ParcelaDto> parcelaDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_PARCELA)) {
                ParcelaDto parcelaDto = readParcela();
                if (parcelaDto.getId() != null) parcelaDtos.add(parcelaDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_PARCELY)) {
                break;
            }
        }
        log.info("Found {} Parcela objects", parcelaDtos.size());
        if (appConfig.getParcelaConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Parcela config is null. Skipping the saving of Parcela objects.");
            return;
        }
        parcelaService.prepareAndSave(parcelaDtos, appConfig);
    }

    private ParcelaDto readParcela() throws XMLStreamException {
        ParcelaDto parcelaDto = new ParcelaDto();

        while(reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_PARCELA)) break;
                continue;
            }

            switch (name) {
                case ParcelaTags.ELEMENT_ID ->
                        parcelaDto.setId(Long.parseLong(reader.getElementText()));
                case ParcelaTags.ELEMENT_NESPRAVNY ->
                        parcelaDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case ParcelaTags.ELEMENT_KMENOVE_CISLO ->
                        parcelaDto.setKmenovecislo(Integer.parseInt(reader.getElementText()));
                case ParcelaTags.ELEMENT_PODODDELENI_CISLA ->
                        parcelaDto.setPododdelenicisla(Integer.parseInt(reader.getElementText()));
                case ParcelaTags.ELEMENT_VYEMRA_PARCELY ->
                        parcelaDto.setVymeraparcely(Long.parseLong(reader.getElementText()));
                case ParcelaTags.ELEMENT_ZPUSOBY_VYUZITI_POZEMKU ->
                        parcelaDto.setZpusobyvyuzitipozemku(Integer.parseInt(reader.getElementText()));
                case ParcelaTags.ELEMENT_DRUH_CISLOVANI_KOD ->
                        parcelaDto.setDruhcislovanikod(Integer.parseInt(reader.getElementText()));
                case ParcelaTags.ELEMENT_DRUH_POZEMKU_KOD ->
                        parcelaDto.setDruhpozemkukod(Integer.parseInt(reader.getElementText()));
                case ParcelaTags.ELEMENT_KATASTRALNI_UZEMI ->
                        parcelaDto.setKatastralniuzemi(readFK(KatastralniUzemiTags.ELEMENT_KOD));
                case ParcelaTags.ELEMENT_PLATI_OD ->
                        parcelaDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case ParcelaTags.ELEMENT_PLATI_DO ->
                        parcelaDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case ParcelaTags.ELEMENT_ID_TRANSAKCE ->
                        parcelaDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case ParcelaTags.ELEMENT_RIZENI_ID ->
                        parcelaDto.setRizeniid(Long.parseLong(reader.getElementText()));
                case ParcelaTags.ELEMENT_BONITOVANE_DILY ->
                        parcelaDto.setBonitovanedily(readBonitovaneDily(ParcelaTags.ELEMENT_BONITOVANE_DILY));
                case ParcelaTags.ELEMENT_ZPUSOB_OCHRANY_POZEMKU ->
                        parcelaDto.setZpusobyochranypozemku(readZpusobyOchrany(ParcelaTags.ELEMENT_ZPUSOB_OCHRANY_POZEMKU));
                case ParcelaTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        parcelaDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case ParcelaTags.ELEMENT_ORI_HRANICE -> {
                    if (appConfig.isIncludeGeometry())
                        parcelaDto.setGeometrieorihranice(geometryParser.readOriginalniHranice(reader));
                }
                case ParcelaTags.ELEMENT_NESPRAVNE_UDAJE ->
                        parcelaDto.setNespravneudaje(readNespravneUdaje(ParcelaTags.ELEMENT_NESPRAVNE_UDAJE));
                default -> {
                }
            }
        }
        return parcelaDto;
    }
    //endregion

    //region Ulice
    private void readUlices() throws XMLStreamException {
        List<UliceDto> uliceDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_ULICE)) {
                UliceDto uliceDto = readUlice();
                if (uliceDto.getKod() != null) uliceDtos.add(uliceDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_ULICE)) {
                break;
            }
        }
        log.info("Found {} Ulice objects", uliceDtos.size());
        if (appConfig.getUliceConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Ulice config is null. Skipping the saving of Ulice objects.");
            return;
        }
        uliceService.prepareAndSave(uliceDtos, appConfig);
    }

    private UliceDto readUlice() throws XMLStreamException {
        UliceDto uliceDto = new UliceDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_ULICE)) break;
                continue;
            }

            switch (name) {
                case UliceTags.ELEMENT_KOD -> uliceDto.setKod(Integer.parseInt(reader.getElementText()));
                case UliceTags.ELEMENT_NAZEV -> uliceDto.setNazev(reader.getElementText());
                case UliceTags.ELEMENT_NESPRAVNY ->
                        uliceDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case UliceTags.ELEMENT_OBEC -> uliceDto.setObec(readFK(ObecTags.ELEMENT_KOD));
                case UliceTags.ELEMENT_PLATI_OD ->
                        uliceDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case UliceTags.ELEMENT_PLATI_DO ->
                        uliceDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case UliceTags.ELEMENT_ID_TRANSAKCE -> uliceDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case UliceTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        uliceDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case UliceTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        uliceDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case UliceTags.ELEMENT_DEF_CARA -> {
                    if (appConfig.isIncludeGeometry())
                        uliceDto.setGeometriedefcara(geometryParser.readDefinicniCara(reader));
                }
                case UliceTags.ELEMENT_NESPRAVNE_UDAJE ->
                        uliceDto.setNespravneudaje(readNespravneUdaje(UliceTags.ELEMENT_NESPRAVNE_UDAJE));
                default -> {}
            }
        }
        return uliceDto;
    }
    //endregion

    //region StavebniObjekty
    private void readStavebniObjekty() throws XMLStreamException {
        List<StavebniObjektDto> stavebniObjektDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_STAVEBNI_OBJEKT)) {
                StavebniObjektDto stavebniObjektDto = readStavebniObjekt();
                if (stavebniObjektDto.getKod() != null) stavebniObjektDtos.add(stavebniObjektDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_STAVEBNI_OBJEKTY)) {
                break;
            }
        }
        log.info("Found {} StavebniObjekt objects", stavebniObjektDtos.size());
        if (appConfig.getStavebniObjektConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("StavebniObjekt config is null. Skipping the saving of StavebniObjekt objects.");
            return;
        }
        stavebniObjektService.prepareAndSave(stavebniObjektDtos, appConfig);
    }

    private StavebniObjektDto readStavebniObjekt() throws XMLStreamException {
        StavebniObjektDto stavebniObjektDto = new StavebniObjektDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_STAVEBNI_OBJEKT)) break;
                continue;
            }
            switch (name) {
                case StavebniObjektTags.ELEMENT_KOD ->
                        stavebniObjektDto.setKod(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_NESPRAVNY ->
                        stavebniObjektDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_CISLA_DOMOVNI ->
                        stavebniObjektDto.setCislodomovni(readCisladomovni(StavebniObjektTags.ELEMENT_CISLA_DOMOVNI));
                case StavebniObjektTags.ELEMENT_IDENTIFIKACNI_PARCELA ->
                        stavebniObjektDto.setIdentifikacniparcela(readFKLong(ParcelaTags.ELEMENT_ID));
                case StavebniObjektTags.ELEMENT_TYP_STAVEBNIHO_OBJEKTU_KOD ->
                        stavebniObjektDto.setTypstavebnihoobjektukod(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_CAST_OBCE ->
                        stavebniObjektDto.setCastobce(readFK(CastObceTags.ELEMENT_KOD));
                case StavebniObjektTags.ELEMENT_MOMC -> stavebniObjektDto.setMomc(readFK(MomcTags.ELEMENT_KOD));
                case StavebniObjektTags.ELEMENT_PLATI_OD ->
                        stavebniObjektDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case StavebniObjektTags.ELEMENT_PLATI_DO ->
                        stavebniObjektDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case StavebniObjektTags.ELEMENT_ID_TRANSAKCE ->
                        stavebniObjektDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        stavebniObjektDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_ISKN_BUDOVA_ID ->
                        stavebniObjektDto.setIsknbudovaid(Long.parseLong(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_DOKONCENI ->
                        stavebniObjektDto.setDokonceni(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case StavebniObjektTags.ELEMENT_DRUH_KONSTRUKCE_KOD ->
                        stavebniObjektDto.setDruhkonstrukcekod(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_OBESTAVENY_PROSTOR ->
                        stavebniObjektDto.setObestavenyprostor(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_POCET_BYTU ->
                        stavebniObjektDto.setPocetbytu(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_POCET_PODLAZI ->
                        stavebniObjektDto.setPocetpodlazi(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_PODLAHOVA_PLOCHA ->
                        stavebniObjektDto.setPodlahovaplocha(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_PRIPOJENI_KANALIZACE_KOD ->
                        stavebniObjektDto.setPripojenikanalizacekod(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_PRIPOJENI_PLYN_KOD ->
                        stavebniObjektDto.setPripojeniplynkod(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_PRIPOJENI_VODOVOD_KOD ->
                        stavebniObjektDto.setPripojenivodovodkod(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_VYBAVENI_VYTAHEM_KOD ->
                        stavebniObjektDto.setVybavenivytahemkod(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_ZASTAVENA_PLOCHA ->
                        stavebniObjektDto.setZastavenaplocha(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_ZPUSOB_VYTAPENI_KOD ->
                        stavebniObjektDto.setZpusobvytapenikod(Integer.parseInt(reader.getElementText()));
                case StavebniObjektTags.ELEMENT_ZPUSOBY_OCHRANY ->
                        stavebniObjektDto.setZpusobyochrany(readZpusobyOchrany(StavebniObjektTags.ELEMENT_ZPUSOBY_OCHRANY));
                case StavebniObjektTags.ELEMENT_DETAILNI_TEA ->
                        stavebniObjektDto.setDetailnitea(readDetailniTeas(StavebniObjektTags.ELEMENT_DETAILNI_TEA));
                case StavebniObjektTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        stavebniObjektDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case StavebniObjektTags.ELEMENT_NESPRAVNE_UDAJE ->
                        stavebniObjektDto.setNespravneudaje(readNespravneUdaje(StavebniObjektTags.ELEMENT_NESPRAVNE_UDAJE));
                case StavebniObjektTags.ELEMENT_NEZJISTENE_UDAJE -> readNeyjisteneUdaje();
                default -> {}
            }
        }
        return stavebniObjektDto;
    }
    //endregion

    //region AdresniMisto
    private void readAdresniMista() throws XMLStreamException {
        List<AdresniMistoDto> adresniMistoDtos = new ArrayList<>();
        while (reader.hasNext()) {
            try {
                int event = reader.next();
                if (event == XMLStreamReader.CHARACTERS) continue;
                String name = reader.getLocalName();
                if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_ADRESNI_MISTO)) {
                    AdresniMistoDto adresniMisto = readAdresniMisto();
                    if (adresniMisto.getKod() != null) adresniMistoDtos.add(adresniMisto);
                } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_ADRESNI_MISTA)) {
                    break;
                }
            } catch (XMLStreamException e) {
                log.error("Error while reading AdresniMisto: {}", e.getMessage());
            }
        }
        log.info("Found {} AdresniMisto", adresniMistoDtos.size());
        if (appConfig.getAdresniMistoConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("AdresniMisto is not in the list of tables to process");
            return;
        }
        adresniMistoService.prepareAndSave(adresniMistoDtos, appConfig);
    }

    private AdresniMistoDto readAdresniMisto() throws XMLStreamException {
        AdresniMistoDto adresniMistoDto = new AdresniMistoDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_ADRESNI_MISTO)) break;
                continue;
            }
            switch (name) {
                case AdresniMistoTags.ELEMENT_KOD -> adresniMistoDto.setKod(Integer.parseInt(reader.getElementText()));
                case AdresniMistoTags.ELEMENT_NESPRAVNY ->
                        adresniMistoDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case AdresniMistoTags.ELEMENT_CISLO_DOMOVNI ->
                        adresniMistoDto.setCislodomovni(Integer.parseInt(reader.getElementText()));
                case AdresniMistoTags.ELEMENT_CISLO_ORIENTACNI ->
                        adresniMistoDto.setCisloorientacni(Integer.parseInt(reader.getElementText()));
                case AdresniMistoTags.ELEMENT_CISLO_ORIENTACNI_PISMENO ->
                        adresniMistoDto.setCisloorientacnipismeno(reader.getElementText());
                case AdresniMistoTags.ELEMENT_PSC -> adresniMistoDto.setPsc(Integer.parseInt(reader.getElementText()));
                case AdresniMistoTags.ELEMENT_STAVEBNI_OBJEKT ->
                        adresniMistoDto.setStavebniobjekt(readFK(StavebniObjektTags.ELEMENT_KOD));
                case AdresniMistoTags.ELEMENT_ULICE -> adresniMistoDto.setUlice(readFK(UliceTags.ELEMENT_KOD));
                case AdresniMistoTags.ELEMENT_VO_KOD ->
                        adresniMistoDto.setVokod(Integer.parseInt(reader.getElementText()));
                case AdresniMistoTags.ELEMENT_PLATI_OD ->
                        adresniMistoDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case AdresniMistoTags.ELEMENT_PLATI_DO ->
                        adresniMistoDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case AdresniMistoTags.ELEMENT_ID_TRANSAKCE ->
                        adresniMistoDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case AdresniMistoTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        adresniMistoDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case AdresniMistoTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        adresniMistoDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case AdresniMistoTags.ELEMENT_NESPRAVNE_UDAJE ->
                        adresniMistoDto.setNespravneudaje(readNespravneUdaje(AdresniMistoTags.ELEMENT_NESPRAVNE_UDAJE));
                default -> {
                }
            }
        }
        return adresniMistoDto;
    }
    //endregion

    //region Zjs
    private void readZsjs() throws XMLStreamException {
        List<ZsjDto> zsjDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_ZSJ)) {
                ZsjDto zsjDto = readZsj();
                if (zsjDto.getKod() != null) zsjDtos.add(zsjDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_ZSJ)) {
                break;
            }
        }
        log.info("Found {} Zsj objects", zsjDtos.size());
        if (appConfig.getZsjConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("Zsj config is null. Skipping the saving of Zsj objects.");
            return;
        }
        zsjService.prepareAndSave(zsjDtos, appConfig);
    }

    private ZsjDto readZsj() throws XMLStreamException {
        ZsjDto zsjDto = new ZsjDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_ZSJ)) break;
                continue;
            }
            switch (name) {
                case ZsjTags.ELEMENT_KOD ->
                        zsjDto.setKod(Integer.parseInt(reader.getElementText()));
                case ZsjTags.ELEMENT_NAZEV ->
                        zsjDto.setNazev(reader.getElementText());
                case ZsjTags.ELEMENT_NESPRAVNY ->
                        zsjDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case ZsjTags.ELEMENT_KATASTRALNI_UZEMI ->
                        zsjDto.setKatastralniuzemi(readFK(KatastralniUzemiTags.ELEMENT_KOD));
                case ZsjTags.ELEMENT_PLATI_OD ->
                        zsjDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case ZsjTags.ELEMENT_PLATI_DO ->
                        zsjDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case ZsjTags.ELEMENT_ID_TRANSAKCE ->
                        zsjDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case ZsjTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        zsjDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case ZsjTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY ->
                        zsjDto.setMluvnickecharakteristiky(readMCh(ZsjTags.ELEMENT_MLUVNICKE_CHARAKTERISTIKY));
                case ZsjTags.ELEMENT_VYMERA ->
                        zsjDto.setVymera(Long.parseLong(reader.getElementText()));
                case ZsjTags.ELEMENT_CHARAKTER_ZSJ_KOD ->
                        zsjDto.setCharakterzsjkod(Integer.parseInt(reader.getElementText()));
                case ZsjTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry()) zsjDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case ZsjTags.ELEMENT_NESPRAVNE_UDAJE ->
                        zsjDto.setNespravneudaje(readNespravneUdaje(ZsjTags.ELEMENT_NESPRAVNE_UDAJE));
                case ZsjTags.ELEMENT_DATUM_VZNIKU ->
                        zsjDto.setDatumvzniku(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                default -> {}
            }
        }
        return zsjDto;
    }

    //endregion

    //region VO
    private void readVOs() throws XMLStreamException {
        List<VODto> voDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_VO)) {
                VODto voDto = readVO();
                if (voDto.getIdtransakce() != null) voDtos.add(voDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_VO)) {
                break;
            }
        }
        log.info("Found {} VO objects", voDtos.size());
        if (appConfig.getVoConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("VO config is null. Skipping the saving of VO objects.");
            return;
        }
        voService.prepareAndSave(voDtos, appConfig);
    }

    private VODto readVO() throws XMLStreamException {
        VODto voDto = new VODto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_VO)) break;
                continue;
            }
            switch (name) {
                case VOTags.ELEMENT_PLATI_OD ->
                        voDto.setPlatiod(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case VOTags.ELEMENT_PLATI_DO ->
                        voDto.setPlatido(LocalDateTime.parse(reader.getElementText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                case VOTags.ELEMENT_ID_TRANSAKCE ->
                        voDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                case VOTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                        voDto.setGlobalniidnavrhuzmeny(Long.parseLong(reader.getElementText()));
                case VOTags.ELEMENT_DEF_BOD -> {
                    if (appConfig.isIncludeGeometry())
                        voDto.setGeometriedefbod(geometryParser.readDefinicniBod(reader));
                }
                case VOTags.ELEMENT_NESPRAVNE_UDAJE ->
                        voDto.setNespravneudaje(readNespravneUdaje(VOTags.ELEMENT_NESPRAVNE_UDAJE));
                case VOTags.ELEMENT_KOD ->
                        voDto.setKod(Integer.parseInt(reader.getElementText()));
                case VOTags.ELEMENT_CISLO ->
                        voDto.setCislo(Integer.parseInt(reader.getElementText()));
                case VOTags.ELEMENT_NESPRAVNY ->
                        voDto.setNespravny(Boolean.parseBoolean(reader.getElementText()));
                case VOTags.ELEMENT_OBEC ->
                        voDto.setObec(readFK(ObecTags.ELEMENT_KOD));
                case VOTags.ELEMENT_MOMC ->
                        voDto.setMomc(readFK(MomcTags.ELEMENT_KOD));
                case VOTags.ELEMENT_POZNAMKA ->
                        voDto.setPoznamka(reader.getElementText());
                default -> {}
            }
        }
        return voDto;
    }
    //endregion

    //region ZaniklePrvky
    private void readZaniklePrvky() throws XMLStreamException {
        List<ZaniklyPrvekDto> zaniklyPrvekDtos = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.START_ELEMENT && name.equals(ELEMENT_ZANIKLY_PRVEK)) {
                ZaniklyPrvekDto zaniklyPrvekDto = readZaniklyPrvek();
                if (zaniklyPrvekDto.getTypprvkukod() != null) zaniklyPrvekDtos.add(zaniklyPrvekDto);
            } else if (event == XMLStreamReader.END_ELEMENT && name.equals(ELEMENT_ZANIKLE_PRVKY)) {
                break;
            }
        }
        log.info("Found {} ZaniklyPrvek objects", zaniklyPrvekDtos.size());
        if (appConfig.getZaniklyPrvekConfig() == null && !appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
            log.info("ZaniklyPrvek config is null. Skipping the saving of ZaniklyPrvek objects.");
            return;
        }
        zaniklyPrvekService.prepareAndSave(zaniklyPrvekDtos, appConfig);
    }

    private ZaniklyPrvekDto readZaniklyPrvek() throws XMLStreamException {
        ZaniklyPrvekDto zaniklyPrvekDto = new ZaniklyPrvekDto();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(ELEMENT_ZANIKLY_PRVEK)) break;
                continue;
            }
            switch (name) {
                case ZaniklyPrvekTags.ELEMENT_TYP_PRVKU_KOD ->
                        zaniklyPrvekDto.setTypprvkukod(reader.getElementText());
                case ZaniklyPrvekTags.ELEMENT_PRVEK_ID ->
                        zaniklyPrvekDto.setPrvekid(Long.parseLong(reader.getElementText()));
                case ZaniklyPrvekTags.ELEMENT_ID_TRANSAKCE ->
                        zaniklyPrvekDto.setIdtransakce(Long.parseLong(reader.getElementText()));
                default -> {}
            }
        }
        return zaniklyPrvekDto;
    }
    //endregion

    //region JSON PARSING
    private String readMCh(String endElement) throws XMLStreamException {
        JSONObject jsonObject = new JSONObject();

        while(reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(endElement)) {
                break;
            }
            switch (name) {
                case MKTags.ELEMENT_P2 ->
                    jsonObject.put(MKTags.ELEMENT_P2, reader.getElementText());
                case MKTags.ELEMENT_P3 ->
                    jsonObject.put(MKTags.ELEMENT_P3, reader.getElementText());
                case MKTags.ELEMENT_P4 ->
                    jsonObject.put(MKTags.ELEMENT_P4, reader.getElementText());
                case MKTags.ELEMENT_P5 ->
                    jsonObject.put(MKTags.ELEMENT_P5, reader.getElementText());
                case MKTags.ELEMENT_P6 ->
                    jsonObject.put(MKTags.ELEMENT_P6, reader.getElementText());
                case MKTags.ELEMENT_P7 ->
                    jsonObject.put(MKTags.ELEMENT_P7, reader.getElementText());
                default -> {}
            }
        }
        return jsonObject.toJSONString();
    }

    private String readBonitovaneDily(String endElement) throws XMLStreamException {
        JSONArray bonitovaneDilyList = new JSONArray();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(endElement)) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT && name.equals(BonitovanyDilTags.ELEMENT_BONITOVANY_DIL)) {
                bonitovaneDilyList.add(readBonitovanyDil());
            }
        }

        return bonitovaneDilyList.toString();
    }

    private JSONObject readBonitovanyDil() throws XMLStreamException {
        JSONObject bonDil = new JSONObject();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(BonitovanyDilTags.ELEMENT_BONITOVANY_DIL)) {
                break;
            }
            switch (name) {
                case BonitovanyDilTags.ELEMENT_VYMERA ->
                        bonDil.put(BonitovanyDilTags.ELEMENT_VYMERA, reader.getElementText());
                case BonitovanyDilTags.ELEMENT_BONITOVANA_JEDNOTKA_KOD ->
                        bonDil.put(BonitovanyDilTags.ELEMENT_BONITOVANA_JEDNOTKA_KOD, reader.getElementText());
                case BonitovanyDilTags.ELEMENT_ID_TRANSAKCE ->
                        bonDil.put(BonitovanyDilTags.ELEMENT_ID_TRANSAKCE, reader.getElementText());
                case BonitovanyDilTags.ELEMENT_RIZENI_ID ->
                        bonDil.put(BonitovanyDilTags.ELEMENT_RIZENI_ID, reader.getElementText());
                default -> {}
            }
        }
        return bonDil;
    }

    private String readZpusobyOchrany(String endElement) throws XMLStreamException {
        JSONArray zpusobyOchrany = new JSONArray();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(endElement)) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT && name.equals(ZpusobOchranyTags.ELEMENT_ZPUSOB_OCHRANY)) {
                zpusobyOchrany.add(readZpusobOchrany());
            }
        }
        return zpusobyOchrany.toJSONString();
    }

    private JSONObject readZpusobOchrany() throws XMLStreamException {
        JSONObject zpusobOchrany = new JSONObject();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(ZpusobOchranyTags.ELEMENT_ZPUSOB_OCHRANY)) {
                break;
            }
            switch (name) {
                case ZpusobOchranyTags.ELEMENT_KOD ->
                        zpusobOchrany.put(ZpusobOchranyTags.ELEMENT_KOD, reader.getElementText());
                case ZpusobOchranyTags.ELEMENT_TYP_OCHRANY_KOD ->
                        zpusobOchrany.put(ZpusobOchranyTags.ELEMENT_TYP_OCHRANY_KOD, reader.getElementText());
                case ZpusobOchranyTags.ELEMENT_ID_TRANSAKCE ->
                        zpusobOchrany.put(ZpusobOchranyTags.ELEMENT_ID_TRANSAKCE, reader.getElementText());
                case ZpusobOchranyTags.ELEMENT_RIZENI_ID ->
                        zpusobOchrany.put(ZpusobOchranyTags.ELEMENT_RIZENI_ID, reader.getElementText());
                default -> {
                }
            }
        }

        return zpusobOchrany;
    }

    private String readDetailniTeas(String endElement) throws XMLStreamException {
        JSONArray detailniTeas = new JSONArray();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(endElement)) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT && name.equals(DetailniTeaTags.ELEMENT_DETAILNI_TEA)) {
                detailniTeas.add(readDetailniTea());
            }
        }
        return detailniTeas.toString();
    }

    private JSONObject readDetailniTea() throws XMLStreamException {
        JSONObject detailniTea = new JSONObject();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(DetailniTeaTags.ELEMENT_DETAILNI_TEA)) {
                break;
            }
            switch (name) {
                case DetailniTeaTags.ELEMENT_KOD ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_KOD, reader.getElementText());
                case DetailniTeaTags.ELEMENT_PLATI_OD ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_PLATI_OD, reader.getElementText());
                case DetailniTeaTags.ELEMENT_NESPRAVNY ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_NESPRAVNY, reader.getElementText());
                case DetailniTeaTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY, reader.getElementText());
                case DetailniTeaTags.ELEMENT_DRUH_KONSTRUKCE_KOD ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_DRUH_KONSTRUKCE_KOD, reader.getElementText());
                case DetailniTeaTags.ELEMENT_POCET_BYTU ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_POCET_BYTU, reader.getElementText());
                case DetailniTeaTags.ELEMENT_POCET_PODLAZI ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_POCET_PODLAZI, reader.getElementText());
                case DetailniTeaTags.ELEMENT_PRIPOJENI_KANALIZACE_KOD ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_PRIPOJENI_KANALIZACE_KOD, reader.getElementText());
                case DetailniTeaTags.ELEMENT_PRIPOJENI_PLYN_KOD ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_PRIPOJENI_PLYN_KOD, reader.getElementText());
                case DetailniTeaTags.ELEMENT_PRIPOJENI_VODOVOD_KOD ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_PRIPOJENI_VODOVOD_KOD, reader.getElementText());
                case DetailniTeaTags.ELEMENT_ZPUSOB_VYTAPENI_KOD ->
                    detailniTea.put(DetailniTeaTags.ELEMENT_ZPUSOB_VYTAPENI_KOD, reader.getElementText());
                case DetailniTeaTags.ELEMENT_ADRESNI_MISTO_KOD -> {
                    if (event == XMLStreamConstants.START_ELEMENT)
                        detailniTea.put(DetailniTeaTags.ELEMENT_ADRESNI_MISTO_KOD, readFK(AdresniMistoTags.ELEMENT_KOD));
                }
                default -> {}
            }
        }
        return detailniTea;
    }

    private String readCisladomovni(String endElement) throws XMLStreamException {
        JSONObject cislodomovni = new JSONObject();

        int iterator = 0;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(endElement)) {
                break;
            }
            if (name.equals(CislaDomovniTags.ELEMENT_CISLO_DOMOVNI)) {
                cislodomovni.put(CislaDomovniTags.ELEMENT_CISLO_DOMOVNI + ++iterator, reader.getElementText());
            }
        }
        return cislodomovni.toString();
    }

    private String readNespravneUdaje(String endElement) throws XMLStreamException {
        JSONObject nespravneUdaje = new JSONObject();

        while(reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && name.equals(endElement)) {
                break;
            }
            if (reader.isStartElement()) {
                String elementName = reader.getLocalName();
                switch (elementName) {
                    case NespravneUdajeTags.ELEMENT_NESPRAVNY_UDAJ ->
                        nespravneUdaje.put(NespravneUdajeTags.ELEMENT_NESPRAVNY_UDAJ, reader.getElementText());
                    case NespravneUdajeTags.ELEMENT_NAZEV_UDAJE ->
                        nespravneUdaje.put(NespravneUdajeTags.ELEMENT_NAZEV_UDAJE, reader.getElementText());
                    case NespravneUdajeTags.ELEMENT_OZNACENO_DNE ->
                        nespravneUdaje.put(NespravneUdajeTags.ELEMENT_OZNACENO_DNE, reader.getElementText());
                    case NespravneUdajeTags.ELEMENT_OZNACENO_INFO ->
                        nespravneUdaje.put(NespravneUdajeTags.ELEMENT_OZNACENO_INFO, reader.getElementText());
                    default -> {}
                }
            }
        }
        return nespravneUdaje.toString();
    }
    //endregion

    //region FK PARSING
    private Integer readFK(String fkName) throws XMLStreamException {
        int event = reader.next();
        if (event == XMLStreamReader.CHARACTERS) event = reader.next();
        if (event == XMLStreamConstants.START_ELEMENT) {
            String name = reader.getLocalName();
            if (name.equals(fkName)) {
                return Integer.parseInt(reader.getElementText());
            }
        }
        return null;
    }

    private Long readFKLong(String fkName) throws XMLStreamException {
        int event = reader.next();
        if (event == XMLStreamReader.CHARACTERS) event = reader.next();
        if (event == XMLStreamConstants.START_ELEMENT) {
            String name = reader.getLocalName();
            if (name.equals(fkName)) {
                return Long.parseLong(reader.getElementText());
            }
        }
        return null;
    }
    //endregion

    private void readNeyjisteneUdaje() throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.CHARACTERS) continue;
            String name = reader.getLocalName();
            if (event == XMLStreamReader.END_ELEMENT) {
                if (name.equals(StavebniObjektTags.ELEMENT_NEZJISTENE_UDAJE)) return;
            }
        }
    }
}