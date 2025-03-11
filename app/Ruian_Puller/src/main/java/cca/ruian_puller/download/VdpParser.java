package cca.ruian_puller.download;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.download.dto.*;
import cca.ruian_puller.download.elements.*;
import cca.ruian_puller.download.geometry.GeometryParser;
import cca.ruian_puller.download.jsonObjects.*;
import cca.ruian_puller.download.service.*;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

    public void processFile(final InputStream fileIS) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fileIS);
            document.getDocumentElement().normalize();
            readData(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readData(Document document) {
        NodeList dataList = document.getElementsByTagName(ELEMENT_DATA);

        for (int i = 0; i < dataList.getLength(); i++) {
            NodeList dataStart = dataList.item(i).getChildNodes();
            for (int j = 0; j < dataStart.getLength(); j++) {
                switch (dataStart.item(j).getNodeName()) {
                    case ELEMENT_STATY:
                        readStaty(dataStart.item(j));
                        break;
                    case ELEMENT_REGIONY_SOUDRZNOSTI:
                        readRegionySoudrznosti(dataStart.item(j));
                        break;
                    case ELEMENT_VUSC:
                        readVuscs(dataStart.item(j));
                        break;
                    case ELEMENT_OKRESY:
                        readOkresy(dataStart.item(j));
                        break;
                    case ELEMENT_ORP:
                        readOrps(dataStart.item(j));
                        break;
                    case ELEMENT_POU:
                        readPous(dataStart.item(j));
                        break;
                    case ELEMENT_OBCE:
                        readObce(dataStart.item(j));
                        break;
                    case ELEMENT_CASTI_OBCE:
                        readCastiObce(dataStart.item(j));
                        break;
                    case ELEMENT_MOP:
                        readMops(dataStart.item(j));
                        break;
                    case ELEMENT_SOS:
                        readSpravniObvody(dataStart.item(j));
                        break;
                    case ELEMENT_MOMC:
                        readMomcs(dataStart.item(j));
                        break;
                    case ELEMENT_KATASTR_UZEMI:
                        readKatastrUzemis(dataStart.item(j));
                        break;
                    case ELEMENT_PARCELY:
                        readParcely(dataStart.item(j));
                        break;
                    case ELEMENT_ULICE:
                        readUlices(dataStart.item(j));
                        break;
                    case ELEMENT_STAVEBNI_OBJEKTY:
                        readStavebniObjekty(dataStart.item(j));
                        break;
                    case ELEMENT_ADRESNI_MISTA:
                        readAdresniMista(dataStart.item(j));
                        break;
                    case ELEMENT_ZSJ:
                        readZsjs(dataStart.item(j));
                        break;
                    case ELEMENT_VO:
                        readVOs(dataStart.item(j));
                        break;
                    case ELEMENT_ZANIKLE_PRVKY:
                        readZaniklePrvky(dataStart.item(j));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    //region STAT
    private void readStaty(Node statyNode) {
        List<StatDto> staty = new ArrayList<>();
        NodeList statyList = statyNode.getChildNodes();
        for (int i = 0; i < statyList.getLength(); i++) {
            if ((statyList.item(i).getNodeName()).equals(ELEMENT_STAT)) {
                StatDto stat = readStat(statyList.item(i));
                if (stat.getKod() != null) staty.add(stat);
            }
        }
        log.info("STATY: {}", staty.size());
        statService.prepareAndSave(staty, appConfig.getCommitSize());
    }

    private StatDto readStat(Node statNode) {
        StatDto stat = new StatDto();
        NodeList statData = statNode.getChildNodes();

        for (int i = 0; i < statData.getLength(); i++) {
            Node dataNode = statData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case StatTags.ELEMENT_KOD:
                    stat.setKod(Integer.parseInt(textContent));
                    break;
                case StatTags.ELEMENT_NAZEV:
                    stat.setNazev(textContent);
                    break;
                case StatTags.ELEMENT_NESPRAVNY:
                    stat.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case StatTags.ELEMENT_PLATI_OD:
                    stat.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case StatTags.ELEMENT_PLATI_DO:
                    stat.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case StatTags.ELEMENT_ID_TRANSAKCE:
                    stat.setIdtransakce(Long.parseLong(textContent));
                    break;
                case StatTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY:
                    stat.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case StatTags.ELEMENT_NUTS_LAU:
                    stat.setNutslau(textContent);
                    break;
                case StatTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) stat.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) stat.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) stat.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case StatTags.ELEMENT_NESPRAVNE_UDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    stat.setNespravneudaje(nu);
                    break;
                case StatTags.ELEMENT_DATUM_VZNIKU:
                    stat.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return stat;
    }
    //endregion

    //region REGIONY_SOUDRZNOSTI
    private void readRegionySoudrznosti(Node regionySoudrznostiNode) {
        List<RegionSoudrznostiDto> regionSoudrznostiDtos = new ArrayList<>();
        NodeList regionySoudrznosti = regionySoudrznostiNode.getChildNodes();
        for (int i = 0; i < regionySoudrznosti.getLength(); i++) {
            if ((regionySoudrznosti.item(i).getNodeName()).equals(ELEMENT_REGION_SOUDRZNOSTI)) {
                RegionSoudrznostiDto regionSoudrznostiDto = readRegionSoudrznosti(regionySoudrznosti.item(i));
                if (regionSoudrznostiDto.getKod() != null) regionSoudrznostiDtos.add(regionSoudrznostiDto);
            }
        }
        log.info("REGIONY_SOUDRZNOSTI: {}", regionSoudrznostiDtos.size());
        regionSoudrznostiService.prepareAndSave(regionSoudrznostiDtos, appConfig.getCommitSize());
    }

    private RegionSoudrznostiDto readRegionSoudrznosti(Node regionSoudrznostiNode) {
        RegionSoudrznostiDto regionSoudrznosti = new RegionSoudrznostiDto();
        NodeList regionData = regionSoudrznostiNode.getChildNodes();

        for (int i = 0; i < regionData.getLength(); i++) {
            Node dataNode = regionData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case RegionSoudrznostiTags.ELEMENT_KOD:
                    regionSoudrznosti.setKod(Integer.parseInt(textContent));
                    break;
                case RegionSoudrznostiTags.ELEMENT_NAZEV:
                    regionSoudrznosti.setNazev(textContent);
                    break;
                case RegionSoudrznostiTags.ELEMENT_NESPRAVNY:
                    regionSoudrznosti.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case RegionSoudrznostiTags.ELEMENT_STAT:
                    regionSoudrznosti.setStat(readFK(dataNode, StatTags.ELEMENT_KOD));
                    break;
                case RegionSoudrznostiTags.ELEMENT_PLATI_OD:
                    regionSoudrznosti.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case RegionSoudrznostiTags.ELEMENT_PLATI_DO:
                    regionSoudrznosti.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case RegionSoudrznostiTags.ELEMENT_ID_TRANSAKCE:
                    regionSoudrznosti.setIdtransakce(Long.parseLong(textContent));
                    break;
                case RegionSoudrznostiTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY:
                    regionSoudrznosti.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case RegionSoudrznostiTags.ELEMENT_NUTS_LAU:
                    regionSoudrznosti.setNutslau(textContent);
                    break;
                case RegionSoudrznostiTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) regionSoudrznosti.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) regionSoudrznosti.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) regionSoudrznosti.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case RegionSoudrznostiTags.ELEMENT_NESPRAVNE_UDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    regionSoudrznosti.setNespravneudaje(nu);
                    break;
                case RegionSoudrznostiTags.ELEMENT_DATUM_VZNIKU:
                    regionSoudrznosti.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }

        return regionSoudrznosti;
    }
    //endregion

    //region VUSC
    private void readVuscs(Node vuscNode) {
        List<VuscDto> vuscs = new ArrayList<>();
        NodeList vuscList = vuscNode.getChildNodes();
        for (int i = 0; i < vuscList.getLength(); i++) {
            if ((vuscList.item(i).getNodeName()).equals(ELEMENT_VUSC)) {
                VuscDto vusc = readVusc(vuscList.item(i));
                if (vusc.getKod() != null) vuscs.add(vusc);
            }
        }
        log.info("VUSC: {}", vuscs.size());
        vuscService.prepareAndSave(vuscs, appConfig.getCommitSize());
    }

    private VuscDto readVusc(Node vuscNode) {
        VuscDto vusc = new VuscDto();
        NodeList vuscData = vuscNode.getChildNodes();

        for (int i = 0; i < vuscData.getLength(); i++) {
            Node dataNode = vuscData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case VuscTags.ELEMENT_KOD:
                    vusc.setKod(Integer.parseInt(textContent));
                    break;
                case VuscTags.ELEMENT_NAZEV:
                    vusc.setNazev(textContent);
                    break;
                case VuscTags.ELEMENT_NESPRAVNY:
                    vusc.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case VuscTags.ELEMENT_REGION_SOUDRZNOSTI:
                    vusc.setRegionsoudrznosti(readFK(dataNode, RegionSoudrznostiTags.ELEMENT_KOD));
                    break;
                case VuscTags.ELEMENT_PLATI_OD:
                    vusc.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case VuscTags.ELEMENT_PLATI_DO:
                    vusc.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case VuscTags.ELEMENT_ID_TRANSAKCE:
                    vusc.setIdtransakce(Long.parseLong(textContent));
                    break;
                case VuscTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY:
                    vusc.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case VuscTags.ELEMENT_NUTS_LAU:
                    vusc.setNutslau(textContent);
                    break;
                case VuscTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) vusc.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) vusc.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) vusc.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case VuscTags.ELEMENT_NESPRAVNE_UDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    vusc.setNespravneudaje(nu);
                    break;
                case VuscTags.ELEMENT_DATUM_VZNIKU:
                    vusc.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return vusc;
    }
    //endregion

    //region Okres
    private void readOkresy(Node okresyNode) {
        List<OkresDto> okresy = new ArrayList<>();
        NodeList okresyList = okresyNode.getChildNodes();
        for (int i = 0; i < okresyList.getLength(); i++) {
            if ((okresyList.item(i).getNodeName()).equals(ELEMENT_OKRES)) {
                OkresDto okres = readOkres(okresyList.item(i));
                if (okres.getKod() != null) okresy.add(okres);
            }
        }
        log.info("OKRESY: {}", okresy.size());
        okresService.prepareAndSave(okresy, appConfig.getCommitSize());
    }

    private OkresDto readOkres(Node okresNode) {
        OkresDto okres = new OkresDto();
        NodeList okresData = okresNode.getChildNodes();

        for (int i = 0; i < okresData.getLength(); i++) {
            Node dataNode = okresData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case OkresTags.ELEMENT_KOD:
                    okres.setKod(Integer.parseInt(textContent));
                    break;
                case OkresTags.ELEMENT_NAZEV:
                    okres.setNazev(textContent);
                    break;
                case OkresTags.ELEMENT_NESPRAVNY:
                    okres.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case OkresTags.ELEMENT_KRAJ:
                    okres.setKraj(Integer.parseInt(textContent));
                    break;
                case OkresTags.ELEMENT_VUSC:
                    okres.setVusc(readFK(dataNode, VuscTags.ELEMENT_KOD));
                    break;
                case OkresTags.ELEMENT_PLATI_OD:
                    okres.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case OkresTags.ELEMENT_PLATI_DO:
                    okres.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case OkresTags.ELEMENT_ID_TRANSAKCE:
                    okres.setIdtransakce(Long.parseLong(textContent));
                    break;
                case OkresTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY:
                    okres.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case OkresTags.ELEMENT_NUTS_LAU:
                    okres.setNutslau(textContent);
                    break;
                case OkresTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) okres.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) okres.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) okres.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case OkresTags.ELEMENT_NESPRAVNE_UDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    okres.setNespravneudaje(nu);
                    break;
                case OkresTags.ELEMENT_DATUM_VZNIKU:
                    okres.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return okres;
    }
    //endregion

    //region ORP
    private void readOrps(Node orpNode) {
        List<OrpDto> orps = new ArrayList<>();
        NodeList orpList = orpNode.getChildNodes();
        for (int i = 0; i < orpList.getLength(); i++) {
            if ((orpList.item(i).getNodeName()).equals(ELEMENT_ORP)) {
                OrpDto orp = readOrp(orpList.item(i));
                if (orp.getKod() != null) orps.add(orp);
            }
        }
        log.info("ORP: {}", orps.size());
        orpService.prepareAndSave(orps, appConfig.getCommitSize());
    }

    private OrpDto readOrp(Node orpNode) {
        OrpDto orp = new OrpDto();
        NodeList orpData = orpNode.getChildNodes();

        for (int i = 0; i < orpData.getLength(); i++) {
            Node dataNode = orpData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case OrpTags.ELEMENT_KOD:
                    orp.setKod(Integer.parseInt(textContent));
                    break;
                case OrpTags.ELEMENT_NAZEV:
                    orp.setNazev(textContent);
                    break;
                case OrpTags.ELEMENT_NESPRAVNY:
                    orp.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case OrpTags.ELEMENT_SPRAVNIOBECKOD:
                    orp.setSpravniobeckod(Integer.parseInt(textContent));
                    break;
                case OrpTags.ELEMENT_VUSC:
                    orp.setVusc(readFK(dataNode, VuscTags.ELEMENT_KOD));
                    break;
                case OrpTags.ELEMENT_OKRES:
                    orp.setOkres(readFK(dataNode, OkresTags.ELEMENT_KOD));
                    break;
                case OrpTags.ELEMENT_PLATIOD:
                    orp.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case OrpTags.ELEMENT_PLATIDO:
                    orp.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case OrpTags.ELEMENT_IDTRANSAKCE:
                    orp.setIdtransakce(Long.parseLong(textContent));
                    break;
                case OrpTags.ELEMENT_GLOBALNIIDNAVRHUZMENY:
                    orp.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case OrpTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) orp.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) orp.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) orp.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case OrpTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    orp.setNespravneudaje(nu);
                    break;
                case OrpTags.ELEMENT_DATUMVZNIKU:
                    orp.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return orp;
    }
    //endregion

    //region POU
    private void readPous(Node pouNode) {
        List<PouDto> pous = new ArrayList<>();
        NodeList pouList = pouNode.getChildNodes();
        for (int i = 0; i < pouList.getLength(); i++) {
            if ((pouList.item(i).getNodeName()).equals(ELEMENT_POU)) {
                PouDto pou = readPou(pouList.item(i));
                if (pou.getKod() != null) pous.add(pou);
            }
        }
        log.info("POU: {}", pous.size());
        pouService.prepareAndSave(pous, appConfig.getCommitSize());
    }

    private PouDto readPou(Node pouNode) {
        PouDto pou = new PouDto();
        NodeList pouData = pouNode.getChildNodes();

        for (int i = 0; i < pouData.getLength(); i++) {
            Node dataNode = pouData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case PouTags.ELEMENT_KOD:
                    pou.setKod(Integer.parseInt(textContent));
                    break;
                case PouTags.ELEMENT_NAZEV:
                    pou.setNazev(textContent);
                    break;
                case PouTags.ELEMENT_NESPRAVNY:
                    pou.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case PouTags.ELEMENT_SPRAVNI_OBEC_KOD:
                    pou.setSpravniobeckod(Integer.parseInt(textContent));
                    break;
                case PouTags.ELEMENT_ORP:
                    pou.setOrp(readFK(dataNode, OrpTags.ELEMENT_KOD));
                    break;
                case PouTags.ELEMENT_PLATIOD:
                    pou.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case PouTags.ELEMENT_PLATIDO:
                    pou.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case PouTags.ELEMENT_IDTRANSAKCE:
                    pou.setIdtransakce(Long.parseLong(textContent));
                    break;
                case PouTags.ELEMENT_GLOBALNIIDNAVHRUZMENY:
                    pou.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case PouTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) pou.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) pou.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) pou.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case PouTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    pou.setNespravneudaje(nu);
                    break;
                case PouTags.ELEMENT_DATUMVZNIKU:
                    pou.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return pou;
    }
    //endregion

    //region OBCE
    private void readObce(Node obceNode) {
        List<ObecDto> obce = new ArrayList<>();
        NodeList obceList = obceNode.getChildNodes();
        for (int i = 0; i < obceList.getLength(); i++) {
            if ((obceList.item(i).getNodeName()).equals(ELEMENT_OBEC)) {
                ObecDto obec = readObec(obceList.item(i));
                if (obec.getKod() != null) obce.add(obec);
            }
        }
        log.info("OBCE: {}", obce.size());
        obecService.prepareAndSave(obce, appConfig.getCommitSize());
    }

    private ObecDto readObec(Node obecNode) {
        ObecDto obec = new ObecDto();
        NodeList obecData = obecNode.getChildNodes();

        for (int i = 0; i < obecData.getLength(); i++) {
            Node dataNode = obecData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case ObecTags.ELEMENT_KOD:
                    obec.setKod(Integer.parseInt(textContent));
                    break;
                case ObecTags.ELEMENT_NAZEV:
                    obec.setNazev(textContent);
                    break;
                case ObecTags.ELEMENT_NESPRAVNY:
                    obec.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case ObecTags.ELEMENT_STATUSKOD:
                    obec.setStatuskod(Integer.parseInt(textContent));
                    break;
                case ObecTags.ELEMENT_OKRES:
                    obec.setOkres(readFK(dataNode, OkresTags.ELEMENT_KOD));
                    break;
                case ObecTags.ELEMENT_POU:
                    obec.setPou(readFK(dataNode, PouTags.ELEMENT_KOD));
                    break;
                case ObecTags.ELEMENT_PLATIOD:
                    obec.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case ObecTags.ELEMENT_PLATIDO:
                    obec.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case ObecTags.ELEMENT_IDTRANSAKCE:
                    obec.setIdtransakce(Long.parseLong(textContent));
                    break;
                case ObecTags.ELEMENT_GLOBALNIIDNAVHRUZMENY:
                    obec.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case ObecTags.ELEMENT_MLUVNICKECHARAKTERISTIKY:
                    String mk = readMCh(dataNode);
                    obec.setMluvnickecharakteristiky(mk);
                    break;
                case ObecTags.ELEMENT_VLAJKATEXT:
                    obec.setVlajkatext(textContent);
                    break;
                case ObecTags.ELEMENT_VLAJKAOBRAZEK:
                    obec.setVlajkaobrazek(textContent.getBytes());
                    break;
                case ObecTags.ELEMENT_ZNAKTEXT:
                    obec.setZnaktext(textContent);
                    break;
                case ObecTags.ELEMENT_ZNAKOBRAZEK:
                    obec.setZnakobrazek(textContent.getBytes());
                    break;
                case ObecTags.ELEMENT_CLENENISROZSAHTYPKOD:
                    obec.setClenenismrozsahkod(Integer.parseInt(textContent));
                    break;
                case ObecTags.ELEMENT_CLENENISMTYKOD:
                    obec.setClenenismtypkod(Integer.parseInt(textContent));
                    break;
                case ObecTags.ELEMENT_NUTSLAU:
                    obec.setNutslau(textContent);
                    break;
                case ObecTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) obec.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) obec.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) obec.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case ObecTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    obec.setNespravneudaje(nu);
                    break;
                case ObecTags.ELEMENT_DATUMVZNIKU:
                    obec.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return obec;
    }
    //endregion

    //region CAST_OBCE
    private void readCastiObce(Node castiObceNode) {
        List<CastObceDto> castiObce = new ArrayList<>();
        NodeList castiObceList = castiObceNode.getChildNodes();
        for (int i = 0; i < castiObceList.getLength(); i++) {
            if ((castiObceList.item(i).getNodeName()).equals(ELEMENT_CAST_OBCE)) {
                CastObceDto castObec = readCastObce(castiObceList.item(i));
                if (castObec.getKod() != null) castiObce.add(castObec);
            }
        }
        log.info("CASTI_OBCE: {}", castiObce.size());
        castObceService.prepareAndSave(castiObce, appConfig.getCommitSize());
    }

    private CastObceDto readCastObce(Node castObceNode) {
        CastObceDto castObec = new CastObceDto();
        NodeList castObceData = castObceNode.getChildNodes();

        for (int i = 0; i < castObceData.getLength(); i++) {
            Node dataNode = castObceData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case CastObceTags.ELEMENT_KOD:
                    castObec.setKod(Integer.parseInt(textContent));
                    break;
                case CastObceTags.ELEMENT_NAZEV:
                    castObec.setNazev(textContent);
                    break;
                case CastObceTags.ELEMENT_NESPRAVNY:
                    castObec.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case CastObceTags.ELEMENT_OBEC:
                    castObec.setObec(readFK(dataNode, ObecTags.ELEMENT_KOD));
                    break;
                case CastObceTags.ELEMENT_PLATIOD:
                    castObec.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case CastObceTags.ELEMENT_PLATIDO:
                    castObec.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case CastObceTags.ELEMENT_IDTRANSAKCE:
                    castObec.setIdtransakce(Long.parseLong(textContent));
                    break;
                case CastObceTags.ELEMENT_GLOBALNIIDNAVZRZMENY:
                    castObec.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case CastObceTags.ELEMENT_MLUVNICKECHARAKTERISTIKY:
                    String mk = readMCh(dataNode);
                    castObec.setMluvnickecharakteristiky(mk);
                    break;
                case CastObceTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) castObec.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) castObec.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) castObec.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case CastObceTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    castObec.setNespravneudaje(nu);
                    break;
                case CastObceTags.ELEMENT_DATUMVZNIKU:
                    castObec.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return castObec;
    }
    //endregion

    //region MOP
    private void readMops(Node mopNode) {
        List<MopDto> mops = new ArrayList<>();
        NodeList mopList = mopNode.getChildNodes();
        for (int i = 0; i < mopList.getLength(); i++) {
            if ((mopList.item(i).getNodeName()).equals(ELEMENT_MOP)) {
                MopDto mop = readMop(mopList.item(i));
                if (mop.getKod() != null) mops.add(mop);
            }
        }
        log.info("MOP: {}", mops.size());
        mopService.prepareAndSave(mops, appConfig.getCommitSize());
    }

    private MopDto readMop(Node mopNode) {
        MopDto mop = new MopDto();
        NodeList mopData = mopNode.getChildNodes();

        for (int i = 0; i < mopData.getLength(); i++) {
            Node dataNode = mopData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case MopTags.ELEMENT_KOD:
                    mop.setKod(Integer.parseInt(textContent));
                    break;
                case MopTags.ELEMENT_NAZEV:
                    mop.setNazev(textContent);
                    break;
                case MopTags.ELEMENT_NESPRAVNY:
                    mop.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case MopTags.ELEMENT_OBEC:
                    mop.setObec(readFK(dataNode, ObecTags.ELEMENT_KOD));
                    break;
                case MopTags.ELEMENT_PLATIOD:
                    mop.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case MopTags.ELEMENT_PLATIDO:
                    mop.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case MopTags.ELEMENT_IDTRANSAKCE:
                    mop.setIdtransakce(Long.parseLong(textContent));
                    break;
                case MopTags.ELEMENT_GLOBALNIIDNAVZMENY:
                    mop.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case MopTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) mop.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) mop.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) mop.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case MopTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    mop.setNespravneudaje(nu);
                    break;
                case MopTags.ELEMENT_DATUMVZNIKU:
                    mop.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return mop;
    }
    //endregion

    //region SpravniObvod
    private void readSpravniObvody(Node spravniObvodyNode) {
        List<SpravniObvodDto> spravniObvody = new ArrayList<>();
        NodeList spravniObvodyList = spravniObvodyNode.getChildNodes();
        for (int i = 0; i < spravniObvodyList.getLength(); i++) {
            if ((spravniObvodyList.item(i).getNodeName()).equals(ELEMENT_SO)) {
                SpravniObvodDto spravniObvod = readSpravniObvod(spravniObvodyList.item(i));
                if (spravniObvod.getKod() != null) spravniObvody.add(spravniObvod);
            }
        }
        log.info("SPRAVNI_OBVODY: {}", spravniObvody.size());
        spravniObvodService.prepareAndSave(spravniObvody, appConfig.getCommitSize());
    }

    private SpravniObvodDto readSpravniObvod(Node spravniObvodNode) {
        SpravniObvodDto spravniObvod = new SpravniObvodDto();
        NodeList spravniObvodData = spravniObvodNode.getChildNodes();

        for (int i = 0; i < spravniObvodData.getLength(); i++) {
            Node dataNode = spravniObvodData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case SpravniObvodTags.ELEMENT_KOD:
                    spravniObvod.setKod(Integer.parseInt(textContent));
                    break;
                case SpravniObvodTags.ELEMENT_NAZEV:
                    spravniObvod.setNazev(textContent);
                    break;
                case SpravniObvodTags.ELEMENT_NESPRAVNY:
                    spravniObvod.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case SpravniObvodTags.ELEMENT_SPRAVNIMOMCKOD:
                    spravniObvod.setSpravnimomckod(readFK(dataNode, MomcTags.ELEMENT_KOD));
                    break;
                case SpravniObvodTags.ELEMENT_OBEC:
                    spravniObvod.setObec(readFK(dataNode, ObecTags.ELEMENT_KOD));
                    break;
                case SpravniObvodTags.ELEMENT_PLATIOD:
                    spravniObvod.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case SpravniObvodTags.ELEMENT_PLATIDO:
                    spravniObvod.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case SpravniObvodTags.ELEMENT_IDTRANSAKCE:
                    spravniObvod.setIdtransakce(Long.parseLong(textContent));
                    break;
                case SpravniObvodTags.ELEMENT_GLOBALNIIDNAVZMENY:
                    spravniObvod.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case SpravniObvodTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) spravniObvod.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) spravniObvod.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) spravniObvod.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case SpravniObvodTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    spravniObvod.setNespravneudaje(nu);
                    break;
                case SpravniObvodTags.ELEMENT_DATUMVZNIKU:
                    spravniObvod.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return spravniObvod;
    }
    //endregion

    //region Momc
    private void readMomcs(Node momcNode) {
        List<MomcDto> momcs = new ArrayList<>();
        NodeList momcList = momcNode.getChildNodes();
        for (int i = 0; i < momcList.getLength(); i++) {
            if ((momcList.item(i).getNodeName()).equals(ELEMENT_MOMC)) {
                MomcDto momc = readMomc(momcList.item(i));
                if (momc.getKod() != null) momcs.add(momc);
            }
        }
        log.info("MOMC: {}", momcs.size());
        momcService.prepareAndSave(momcs, appConfig.getCommitSize());
    }

    private MomcDto readMomc(Node momcNode) {
        MomcDto momc = new MomcDto();
        NodeList momcData = momcNode.getChildNodes();

        for (int i = 0; i < momcData.getLength(); i++) {
            Node dataNode = momcData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case MomcTags.ELEMENT_KOD:
                    momc.setKod(Integer.parseInt(textContent));
                    break;
                case MomcTags.ELEMENT_NAZEV:
                    momc.setNazev(textContent);
                    break;
                case MomcTags.ELEMENT_NESPRAVNY:
                    momc.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case MomcTags.ELEMENT_MOP:
                    momc.setMop(readFK(dataNode, MopTags.ELEMENT_KOD));
                    break;
                case MomcTags.ELEMENT_OBEC:
                    momc.setObec(readFK(dataNode, ObecTags.ELEMENT_KOD));
                    break;
                case MomcTags.ELEMENT_SPRAVNIOBVOD:
                    momc.setSpravniobvod(readFK(dataNode, SpravniObvodTags.ELEMENT_KOD));
                    break;
                case MomcTags.ELEMENT_PLATIOD:
                    momc.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case MomcTags.ELEMENT_PLATIDO:
                    momc.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case MomcTags.ELEMENT_IDTRANSAKCE:
                    momc.setIdtransakce(Long.parseLong(textContent));
                    break;
                case MomcTags.ELEMENT_GLOBALNIIDNAVZMENY:
                    momc.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case MomcTags.ELEMENT_VLAJKATEXT:
                    momc.setVlajkatext(textContent);
                    break;
                case MomcTags.ELEMENT_VLAJKAOBRAZEK:
                    momc.setVlajkaobrazek(textContent.getBytes());
                    break;
                case MomcTags.ELEMENT_ZNAKTEXT:
                    momc.setZnaktext(textContent);
                    break;
                case MomcTags.ELEMENT_ZNAKOBRAZEK:
                    momc.setZnakobrazek(textContent.getBytes());
                    break;
                case MomcTags.ELEMENT_MLUVNICKECHARAKTERISTIKY:
                    String mk = readMCh(dataNode);
                    momc.setMluvnickecharakteristiky(mk);
                    break;
                case MomcTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) momc.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) momc.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) momc.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case MomcTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    momc.setNespravneudaje(nu);
                    break;
                case MomcTags.ELEMENT_DATUMVZNIKU:
                    momc.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return momc;
    }
    //endregion

    //region KatastrUzemi
    private void readKatastrUzemis(Node katastrUzemiNode) {
        List<KatastralniUzemiDto> katastrUzemi = new ArrayList<>();
        NodeList katastrUzemiList = katastrUzemiNode.getChildNodes();
        for (int i = 0; i < katastrUzemiList.getLength(); i++) {
            if ((katastrUzemiList.item(i).getNodeName()).equals(ELEMENT_KATASTR_UZEMI)) {
                KatastralniUzemiDto katastrUzemiDto = readKatastrUzemi(katastrUzemiList.item(i));
                if (katastrUzemiDto.getKod() != null) katastrUzemi.add(katastrUzemiDto);
            }
        }
        log.info("KATASTRALNI_UZEMI: {}", katastrUzemi.size());
        katastralniUzemiService.prepareAndSave(katastrUzemi, appConfig.getCommitSize());
    }

    private KatastralniUzemiDto readKatastrUzemi(Node katastrUzemiNode) {
        KatastralniUzemiDto katastrUzemi = new KatastralniUzemiDto();
        NodeList katastrUzemiData = katastrUzemiNode.getChildNodes();

        for (int i = 0; i < katastrUzemiData.getLength(); i++) {
            Node dataNode = katastrUzemiData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case KatastralniUzemiTags.ELEMENT_KOD:
                    katastrUzemi.setKod(Integer.parseInt(textContent));
                    break;
                case KatastralniUzemiTags.ELEMENT_NAZEV:
                    katastrUzemi.setNazev(textContent);
                    break;
                case KatastralniUzemiTags.ELEMENT_NESPRAVNY:
                    katastrUzemi.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case KatastralniUzemiTags.ELEMENT_EXISTUJEDIGITALNIMAPA:
                    katastrUzemi.setExistujedigitalnimapa(Boolean.parseBoolean(textContent));
                    break;
                case KatastralniUzemiTags.ELEMENT_OBEC:
                    katastrUzemi.setObec(readFK(dataNode, ObecTags.ELEMENT_KOD));
                    break;
                case KatastralniUzemiTags.ELEMENT_PLATIOD:
                    katastrUzemi.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case KatastralniUzemiTags.ELEMENT_PLATIDO:
                    katastrUzemi.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case KatastralniUzemiTags.ELEMENT_IDTRANSAKCE:
                    katastrUzemi.setIdtransakce(Long.parseLong(textContent));
                    break;
                case KatastralniUzemiTags.ELEMENT_GLOBALNIIDNAVZMENY:
                    katastrUzemi.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case KatastralniUzemiTags.ELEMENT_RIZENIID:
                    katastrUzemi.setRizeniid(Long.parseLong(textContent));
                    break;
                case KatastralniUzemiTags.ELEMENT_MLUVNICKECHARAKTERISTIKY:
                    String mk = readMCh(dataNode);
                    katastrUzemi.setMluvnickecharakteristiky(mk);
                    break;
                case KatastralniUzemiTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) katastrUzemi.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) katastrUzemi.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) katastrUzemi.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case KatastralniUzemiTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    katastrUzemi.setNespravneudaje(nu);
                    break;
                case KatastralniUzemiTags.ELEMENT_DATUMVZNIKU:
                    katastrUzemi.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return katastrUzemi;
    }
    //endregion

    //region Parcela
    private void readParcely(Node parcelaNode) {
        List<ParcelaDto> parcely = new ArrayList<>();
        NodeList parcelaList = parcelaNode.getChildNodes();
        for (int i = 0; i < parcelaList.getLength(); i++) {
            if ((parcelaList.item(i).getNodeName()).equals(ELEMENT_PARCELA)) {
                ParcelaDto parcela = readParcela(parcelaList.item(i));
                if (parcela.getId() != null) parcely.add(parcela);
            }
        }
        log.info("PARCELY: {}", parcely.size());
        parcelaService.prepareAndSave(parcely, appConfig.getCommitSize());
    }

    private ParcelaDto readParcela(Node parcelaNode) {
        ParcelaDto parcela = new ParcelaDto();
        NodeList parcelaData = parcelaNode.getChildNodes();

        for (int i = 0; i < parcelaData.getLength(); i++) {
            Node dataNode = parcelaData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case ParcelaTags.ELEMENT_ID:
                    parcela.setId(Long.parseLong(textContent));
                    break;
                case ParcelaTags.ELEMENT_NESPRAVNY:
                    parcela.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case ParcelaTags.ELEMENT_KMENOVE_CISLO:
                    parcela.setKmenovecislo(Integer.parseInt(textContent));
                    break;
                case ParcelaTags.ELEMENT_PODODDELENICISLA:
                    parcela.setPododdelenicisla(Integer.parseInt(textContent));
                    break;
                case ParcelaTags.ELEMENT_VYEMRA_PARCELY:
                    parcela.setVymeraparcely(Long.parseLong(textContent));
                    break;
                case ParcelaTags.ELEMENT_ZPUSOBY_VYUZITI_POZEMKU:
                    parcela.setZpusobyvyuzitipozemku(Integer.parseInt(textContent));
                    break;
                case ParcelaTags.ELEMENT_DRUH_CISLOVANI_KOD:
                    parcela.setDruhcislovanikod(Integer.parseInt(textContent));
                    break;
                case ParcelaTags.ELEMENT_DRUH_POZEMKU_KOD:
                    parcela.setDruhpozemkukod(Integer.parseInt(textContent));
                    break;
                case ParcelaTags.ELEMENT_KATASTRALNI_UZEMI:
                    parcela.setKatastralniuzemi(readFK(dataNode, KatastralniUzemiTags.ELEMENT_KOD));
                    break;
                case ParcelaTags.ELEMENT_PLATI_OD:
                    parcela.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case ParcelaTags.ELEMENT_PLATI_DO:
                    parcela.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case ParcelaTags.ELEMENT_ID_TRANSAKCE:
                    parcela.setIdtransakce(Long.parseLong(textContent));
                    break;
                case ParcelaTags.ELEMENT_RIZENI_ID:
                    parcela.setRizeniid(Long.parseLong(textContent));
                    break;
                case ParcelaTags.ELEMENT_BONITOVANE_DILY:
                    String bd = readBonitovaneDily(dataNode);
                    parcela.setBonitovanedily(bd);
                    break;
                case ParcelaTags.ELEMENT_ZPUSOB_OCHRANY_POZEMKU:
                    String zo = readZpusobyOchrany(dataNode);
                    parcela.setZpusobyochranypozemku(zo);
                    break;
                case ParcelaTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) parcela.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) parcela.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) parcela.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case ParcelaTags.ELEMENT_NESPRAVNE_UDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    parcela.setNespravneudaje(nu);
                    break;
            }
        }
        return parcela;
    }
    //endregion

    //region Ulice
    private void readUlices(Node uliceNode) {
        List<UliceDto> ulice = new ArrayList<>();
        NodeList uliceList = uliceNode.getChildNodes();
        for (int i = 0; i < uliceList.getLength(); i++) {
            if ((uliceList.item(i).getNodeName()).equals(ELEMENT_ULICE)) {
                UliceDto uliceDto = readUlice(uliceList.item(i));
                if (uliceDto.getKod() != null) ulice.add(uliceDto);
            }
        }
        log.info("ULICE: {}", ulice.size());
        uliceService.prepareAndSave(ulice, appConfig.getCommitSize());
    }

    private UliceDto readUlice(Node uliceNode) {
        UliceDto ulice = new UliceDto();
        NodeList uliceData = uliceNode.getChildNodes();

        for (int i = 0; i < uliceData.getLength(); i++) {
            Node dataNode = uliceData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case UliceTags.ELEMENT_KOD:
                    ulice.setKod(Integer.parseInt(textContent));
                    break;
                case UliceTags.ELEMENT_NAZEV:
                    ulice.setNazev(textContent);
                    break;
                case UliceTags.ELEMENT_NESPRAVNY:
                    ulice.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case UliceTags.ELEMENT_OBEC:
                    ulice.setObec(readFK(dataNode, ObecTags.ELEMENT_KOD));
                    break;
                case UliceTags.ELEMENT_PLATIOD:
                    ulice.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case UliceTags.ELEMENT_PLATIDO:
                    ulice.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case UliceTags.ELEMENT_ID_TRANSAKCE:
                    ulice.setIdtransakce(Long.parseLong(textContent));
                    break;
                case UliceTags.ELEMENT_GLOBALNIIDNAVRHUZMENY:
                    ulice.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case UliceTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) ulice.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) ulice.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) ulice.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case UliceTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    ulice.setNespravneudaje(nu);
                    break;
                default:
                    break;
            }
        }
        return ulice;
    }
    //endregion

    //region StavebniObjekty
    private void readStavebniObjekty(Node stavebniObjektyNode) {
        List<StavebniObjektDto> stavebniObjekty = new ArrayList<>();
        NodeList stavebniObjektyList = stavebniObjektyNode.getChildNodes();
        for (int i = 0; i < stavebniObjektyList.getLength(); i++) {
            if ((stavebniObjektyList.item(i).getNodeName()).equals(ELEMENT_STAVEBNI_OBJEKT)) {
                StavebniObjektDto so = readStavebniObjekt(stavebniObjektyList.item(i));
                if (so.getKod() != null) stavebniObjekty.add(so);
            }
        }
        log.info("STAVEBNI_OBJEKTY: {}", stavebniObjekty.size());
        stavebniObjektService.prepareAndSave(stavebniObjekty, appConfig.getCommitSize());
    }

    private StavebniObjektDto readStavebniObjekt(Node soNode) {
        StavebniObjektDto so = new StavebniObjektDto();
        NodeList soData = soNode.getChildNodes();

        for (int i = 0; i < soData.getLength(); i++) {
            Node dataNode = soData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case StavebniObjektTags.ELEMENT_KOD:
                    so.setKod(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_NESPRAVNY:
                    so.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_CISLADOMOVNI:
                    String cd = readCisladomovni(dataNode);
                    so.setCislodomovni(cd);
                    break;
                case StavebniObjektTags.ELEMENT_IDENTIFIKACNIPARCELA:
                    so.setIdentifikacniparcela(readFKLong(dataNode, ParcelaTags.ELEMENT_ID));
                    break;
                case StavebniObjektTags.ELEMENT_TYPSTAVEBNIHOOBJEKTUKOD:
                    so.setTypstavebnihoobjektukod(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_CASTOBCE:
                    so.setCastobce(readFK(dataNode, CastObceTags.ELEMENT_KOD));
                    break;
                case StavebniObjektTags.ELEMENT_MOMC:
                    so.setMomc(readFK(dataNode, MomcTags.ELEMENT_KOD));
                    break;
                case StavebniObjektTags.ELEMENT_PLATIOD:
                    so.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case StavebniObjektTags.ELEMENT_PLATIDO:
                    so.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case StavebniObjektTags.ELEMENT_ID_TRANSAKCE:
                    so.setIdtransakce(Long.parseLong(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_GLOBALNIIDNAVRHUZMENY:
                    so.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_ISKNBUDOAID:
                    so.setIsknbudovaid(Long.parseLong(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_DOKONCENI:
                    so.setDokonceni(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case StavebniObjektTags.ELEMENT_DRUHKONSTRUKCEKOD:
                    so.setDruhkonstrukcekod(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_OBESTAVENYPROSTOR:
                    so.setObestavenyprostor(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_POCETBYTU:
                    so.setPocetbytu(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_POCETPODLAZI:
                    so.setPocetpodlazi(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_PODLAHOVAPLOCHA:
                    so.setPodlahovaplocha(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_PRIPJENIKANALIZACEKOD:
                    so.setPripojenikanalizacekod(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_PRIPJENIPLYNKOD:
                    so.setPripojeniplynkod(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_PRIPJENIVODOVODKOD:
                    so.setPripojenivodovodkod(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_VYBAVENIVYTAHEMKOD:
                    so.setVybavenivytahemkod(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_ZASTAVENAPLOCHA:
                    so.setZastavenaplocha(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_ZPUSOBVYTAPENIKOD:
                    so.setZpusobvytapenikod(Integer.parseInt(textContent));
                    break;
                case StavebniObjektTags.ELEMENT_ZPUSOBYOCHRANY:
                    String zo = readZpusobyOchrany(dataNode);
                    so.setZpusobyochrany(zo);
                    break;
                case StavebniObjektTags.ELEMENT_DETAILNITEA:
                    String dtea = readDetailniTeas(dataNode);
                    so.setDetailnitea(dtea);
                    break;
                case StavebniObjektTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) so.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) so.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) so.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case StavebniObjektTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    so.setNespravneudaje(nu);
                    break;
                default:
                    break;
            }
        }
        return so;
    }
    //endregion

    //region AdresniMisto
    private void readAdresniMista(Node adresniMistaNode) {
        List<AdresniMistoDto> adresniMista = new ArrayList<>();
        NodeList adresniMistaList = adresniMistaNode.getChildNodes();
        for (int i = 0; i < adresniMistaList.getLength(); i++) {
            if ((adresniMistaList.item(i).getNodeName()).equals(ELEMENT_ADRESNI_MISTO)) {
                AdresniMistoDto am = readAdresniMisto(adresniMistaList.item(i));
                if (am.getKod() != null) adresniMista.add(am);
            }
        }
        log.info("ADRESNI_MISTA: {}", adresniMista.size());
        adresniMistoService.prepareAndSave(adresniMista, appConfig.getCommitSize());
    }

    private AdresniMistoDto readAdresniMisto(Node adresMistoNode) {
        AdresniMistoDto adresMisto = new AdresniMistoDto();
        NodeList adresMistoData = adresMistoNode.getChildNodes();

        for (int i = 0; i < adresMistoData.getLength(); i++) {
            Node dataNode = adresMistoData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case AdresniMistoTags.ELEMENT_KOD:
                    adresMisto.setKod(Integer.parseInt(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_NESPRAVNY:
                    adresMisto.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_CISLODOMOVNI:
                    adresMisto.setCislodomovni(Integer.parseInt(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_CISLOORIENTACNI:
                    adresMisto.setCisloorientacni(Integer.parseInt(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_CISLOORIENTACNIPISMENO:
                    adresMisto.setCisloorientacnipismeno(Integer.parseInt(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_PSC:
                    adresMisto.setPsc(Integer.parseInt(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_STAVEBNIOBJEKT:
                    adresMisto.setStavebniobjekt(readFK(dataNode, StavebniObjektTags.ELEMENT_KOD));
                    break;
                case AdresniMistoTags.ELEMENT_ULICE:
                    adresMisto.setUlice(readFK(dataNode, UliceTags.ELEMENT_KOD));
                    break;
                case AdresniMistoTags.ELEMENT_VOKOD:
                    adresMisto.setVokod(Integer.parseInt(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_PLATIOD:
                    adresMisto.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case AdresniMistoTags.ELEMENT_PLATIDO:
                    adresMisto.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case AdresniMistoTags.ELEMENT_IDTRANSAKCE:
                    adresMisto.setIdtransakce(Long.parseLong(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_GLOBALNIIDNAVRHUZMENY:
                    adresMisto.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case AdresniMistoTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) adresMisto.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) adresMisto.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) adresMisto.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case AdresniMistoTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    adresMisto.setNespravneudaje(nu);
                    break;
                default:
                    break;
            }
        }
        return adresMisto;
    }
    //endregion

    //region Zjs
    private void readZsjs(Node zsjNode) {
        List<ZsjDto> zsj = new ArrayList<>();
        NodeList zjsList = zsjNode.getChildNodes();
        for (int i = 0; i < zjsList.getLength(); i++) {
            ZsjDto zsjDto = readZsj(zjsList.item(i));
            if (zsjDto.getKod() != null) zsj.add(zsjDto);
        }
        log.info("ZSJ: {}", zsj.size());
        zsjService.prepareAndSave(zsj, appConfig.getCommitSize());
    }

    private ZsjDto readZsj(Node zjsNode) {
        ZsjDto zsj = new ZsjDto();
        NodeList zsjData = zjsNode.getChildNodes();

        for(int i = 0; i < zsjData.getLength(); i++) {
            Node dataNode = zsjData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case ZsjTags.ELEMENT_KOD:
                    zsj.setKod(Integer.parseInt(textContent));
                    break;
                case ZsjTags.ELEMENT_NAZEV:
                    zsj.setNazev(textContent);
                    break;
                case ZsjTags.ELEMENT_NESPRAVNY:
                    zsj.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case ZsjTags.ELEMENT_KATASTRALNIUZEMI:
                    zsj.setKatastralniuzemi(readFK(dataNode, KatastralniUzemiTags.ELEMENT_KOD));
                    break;
                case ZsjTags.ELEMENT_PLATIOD:
                    zsj.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case ZsjTags.ELEMENT_PLATIDO:
                    zsj.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case ZsjTags.ELEMENT_ID_TRANSAKCE:
                    zsj.setIdtransakce(Long.parseLong(textContent));
                    break;
                case ZsjTags.ELEMENT_GLOBALNIIDNAVRHUZMENY:
                    zsj.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case ZsjTags.ELEMENT_MLUVNICKECHARAKTERISTIKY:
                    String mk = readMCh(dataNode);
                    zsj.setMluvnickecharakteristiky(mk);
                    break;
                case ZsjTags.ELEMENT_VYMERA:
                    zsj.setVymera(Long.parseLong(textContent));
                    break;
                case ZsjTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) zsj.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) zsj.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) zsj.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case ZsjTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    zsj.setNespravneudaje(nu);
                    break;
                case ZsjTags.ELEMENT_DATUMVZNIKU:
                    zsj.setDatumvzniku(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                default:
                    break;
            }
        }
        return zsj;
    }

    //endregion

    //region VO
    private void readVOs(Node voNode) {
        List<VODto> vos = new ArrayList<>();
        NodeList voList = voNode.getChildNodes();
        for (int i = 0; i < voList.getLength(); i++) {
            if ((voList.item(i).getNodeName()).equals(ELEMENT_VO)) {
                VODto vo = readVO(voList.item(i));
                if (vo.getIdtransakce() != null) vos.add(vo);
            }
        }
        log.info("VO: {}", vos.size());
        voService.prepareAndSave(vos, appConfig.getCommitSize());
    }

    private VODto readVO(Node voNode) {
        VODto vo = new VODto();
        NodeList voData = voNode.getChildNodes();

        for (int i = 0; i < voData.getLength(); i++) {
            Node dataNode = voData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case VOTags.ELEMENT_PLATIDO:
                    vo.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case VOTags.ELEMENT_IDTRANSAKCE:
                    vo.setIdtransakce(Long.parseLong(textContent));
                    break;
                case VOTags.ELEMENT_GLOBALNIIDNAVRHUZMENY:
                    vo.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case VOTags.ELEMENT_GEOMETRIE:
                    if (appConfig.isIncludeGeometry()) {
                        Geometry[] geom = geometryParser.readGeometry(dataNode);
                        if (geom[0] != null) vo.setGeometriedefbod(geom[0]);
                        if (geom[1] != null) vo.setGeometriegenhranice(geom[1]);
                        if (geom[2] != null) vo.setGeometrieorihranice(geom[2]);
                    }
                    break;
                case VOTags.ELEMENT_NESPRAVNEUDAJE:
                    String nu = readNespravneUdaje(dataNode);
                    vo.setNespravneudaje(nu);
                    break;
                case VOTags.ELEMENT_KOD:
                    vo.setKod(Integer.parseInt(textContent));
                    break;
                case VOTags.ELEMENT_CISLO:
                    vo.setCislo(Integer.parseInt(textContent));
                    break;
                case VOTags.ELEMENT_NESPRAVNY:
                    vo.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case VOTags.ELEMENT_OBEC:
                    vo.setObec(readFK(dataNode, ObecTags.ELEMENT_KOD));
                    break;
                case VOTags.ELEMENT_MOMC:
                    vo.setMomc(readFK(dataNode, MomcTags.ELEMENT_KOD));
                    break;
                case VOTags.ELEMENT_POZNAMKA:
                    vo.setPoznamka(textContent);
                    break;
                default:
                    break;
            }
        }
        return vo;
    }
    //endregion

    //region ZaniklePrvky
    private void readZaniklePrvky(Node zaniklePrvkyNode) {
        List<ZaniklyPrvekDto> zaniklePrvky = new ArrayList<>();
        NodeList zaniklePrvkyList = zaniklePrvkyNode.getChildNodes();
        for (int i = 0; i < zaniklePrvkyList.getLength(); i++) {
            if ((zaniklePrvkyList.item(i).getNodeName()).equals(ELEMENT_ZANIKLY_PRVEK)) {
                zaniklePrvky.add(readZaniklyPrvek(zaniklePrvkyList.item(i)));
            }
        }
        log.info("ZANIKLE_PRVKY: {}", zaniklePrvky.size());
        zaniklyPrvekService.prepareAndSave(zaniklePrvky, appConfig.getCommitSize());
    }

    private ZaniklyPrvekDto readZaniklyPrvek(Node zaniklyPrvekNode) {
        ZaniklyPrvekDto zaniklyPrvek = new ZaniklyPrvekDto();
        NodeList zaniklyPrvekData = zaniklyPrvekNode.getChildNodes();

        for (int i = 0; i < zaniklyPrvekData.getLength(); i++) {
            Node dataNode = zaniklyPrvekData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case ZaniklyPrvekTags.ELEMENT_TYP_PRVKU_KOD:
                    zaniklyPrvek.setTypPrvkuKod(textContent);
                    break;
                case ZaniklyPrvekTags.ELEMENT_PRVEK_ID:
                    zaniklyPrvek.setPrvekId(Long.parseLong(textContent));
                    break;
                case ZaniklyPrvekTags.ELEMENT_ID_TRANSAKCE:
                    zaniklyPrvek.setIdTransakce(Long.parseLong(textContent));
                    break;
                default:
                    break;
            }
        }
        return zaniklyPrvek;
    }
    //endregion

    //region JSON PARSING
    private String readMCh(Node mk) {
        NodeList mkList = mk.getChildNodes();
        // JSON FILE
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < mkList.getLength(); i++) {
            Node dataNode = mkList.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case MKTags.ELEMENT_P2:
                    jsonObject.put("Pad2", textContent);
                    break;
                case MKTags.ELEMENT_P3:
                    jsonObject.put("Pad3", textContent);
                    break;
                case MKTags.ELEMENT_P4:
                    jsonObject.put("Pad4", textContent);
                    break;
                case MKTags.ELEMENT_P5:
                    jsonObject.put("Pad5", textContent);
                    break;
                case MKTags.ELEMENT_P6:
                    jsonObject.put("Pad6", textContent);
                    break;
                case MKTags.ELEMENT_P7:
                    jsonObject.put("Pad7", textContent);
                    break;
                default:
                    break;
            }
        }
        return jsonObject.toJSONString();
    }

    private String readBonitovaneDily(Node bonitovaneDily) {
        JSONArray bonitovaneDilyList = new JSONArray();
        NodeList bonitovaneDilyData = bonitovaneDily.getChildNodes();

        for (int i = 0; i < bonitovaneDilyData.getLength(); i++) {
            if ((bonitovaneDilyData.item(i).getNodeName()).equals(BonitovanyDilTags.ELEMENT_BONITOVANY_DIL)) {
                bonitovaneDilyList.add(readBonitovanyDil(bonitovaneDilyData.item(i)));
            }
        }
        return bonitovaneDilyList.toString();
    }

    private JSONObject readBonitovanyDil(Node bonDilNode) {
        NodeList bonDilData = bonDilNode.getChildNodes();

        JSONObject bonDil = new JSONObject();;
        for(int i = 0; i < bonDilData.getLength(); i++) {
            Node dataNode = bonDilData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();
            switch (nodeName) {
                case BonitovanyDilTags.ELEMENT_VYMERA:
                    bonDil.put("Vymera", textContent);
                    break;
                case BonitovanyDilTags.ELEMENT_BONITOVANA_JEDNOTKA_KOD:
                    bonDil.put("BonitovanaJednotkaKod", textContent);
                    break;
                case BonitovanyDilTags.ELEMENT_ID_TRANSAKCE:
                    bonDil.put("IdTransakce", textContent);
                    break;
                case BonitovanyDilTags.ELEMENT_RIZENI_ID:
                    bonDil.put("RizeniId", textContent);
                    break;
                default:
                    break;
            }

        }
        return bonDil;
    }

    private String readZpusobyOchrany(Node zo) {
        NodeList zoList = zo.getChildNodes();
        JSONArray zpusobyOchrany = new JSONArray();

        for(int i = 0; i < zoList.getLength(); i++) {
            if ((zoList.item(i).getNodeName()).equals(ZpusobOchranyTags.ELEMENT_ZPUSOB_OCHRANY)) {
                zpusobyOchrany.add(readZpusobOchrany(zoList.item(i)));
            }
        }

        return zpusobyOchrany.toJSONString();
    }

    private JSONObject readZpusobOchrany(Node zo) {
        NodeList zoList = zo.getChildNodes();
        JSONObject zpusobOchrany = new JSONObject();

        for (int i = 0; i < zoList.getLength(); i++) {
            Node dataNode = zoList.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case ZpusobOchranyTags.ELEMENT_KOD:
                    zpusobOchrany.put("Kod", textContent);
                    break;
                case ZpusobOchranyTags.ELEMENT_TYP_OCHRANY_KOD:
                    zpusobOchrany.put("Nazev", textContent);
                    break;
                case ZpusobOchranyTags.ELEMENT_ID_TRANSAKCE:
                    zpusobOchrany.put("IdTransakce", textContent);
                    break;
                case ZpusobOchranyTags.ELEMENT_RIZENI_ID:
                    zpusobOchrany.put("RizeniId", textContent);
                    break;
                default:
                    break;
            }
        }
        return zpusobOchrany;
    }

    private String readDetailniTeas(Node dtea) {
        JSONArray detailniTeas = new JSONArray();
        NodeList dteaList = dtea.getChildNodes();

        for (int i = 0; i < dteaList.getLength(); i++) {
            if ((dteaList.item(i).getNodeName()).equals(DetailniTeaTags.ELEMENT_DETAILNITEA)) {
                detailniTeas.add(readDetailniTea(dteaList.item(i)));
            }
        }
        return detailniTeas.toString();
    }

    private JSONObject readDetailniTea(Node dteaNode) {
        NodeList dteaData = dteaNode.getChildNodes();
        JSONObject detailniTea = new JSONObject();

        for (int i = 0; i < dteaData.getLength(); i++) {
            Node dataNode = dteaData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case DetailniTeaTags.ELEMENT_KOD:
                    detailniTea.put("Kod", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_PLATI_OD:
                    detailniTea.put("PlatiOd", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_NESPRAVNY:
                    detailniTea.put("Nespravny", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY:
                    detailniTea.put("GlobalniIdNavrhZmeny", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_DRUH_KONSTRUKCE_KOD:
                    detailniTea.put("DruhKonstrukceKod", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_POCET_BYTU:
                    detailniTea.put("PocetBytu", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_POCET_PODLAZI:
                    detailniTea.put("PocetPodlazi", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_PRIPOJENI_KANALIZACE_KOD:
                    detailniTea.put("PripojeniKanalizaceKod", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_PRIPOJENI_PLYN_KOD:
                    detailniTea.put("PripojeniPlynKod", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_PRIPOJENI_VODOVOD_KOD:
                    detailniTea.put("PripojeniVodovodKod", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_ZPUSOB_VYTAPENI_KOD:
                    detailniTea.put("ZpusobVytapeniKod", textContent);
                    break;
                case DetailniTeaTags.ELEMENT_ADRESNI_MISTO_KOD:
                    detailniTea.put("AdresniMistoKod", textContent);
                    break;
                default:
                    break;
            }
        }
        return detailniTea;
    }

    private String readCisladomovni(Node cd) {
        JSONObject cislodomovni = new JSONObject();
        NodeList cdList = cd.getChildNodes();

        for (int i = 0; i < cdList.getLength(); i++) {
            if ((cdList.item(i).getNodeName()).equals(CislaDomovniTags.ELEMENT_CISLO_DOMOVNI)) {
                cislodomovni.put("CisloDomovni" + i, cdList.item(i).getTextContent());
            }
        }
        return cislodomovni.toString();
    }

    private String readNespravneUdaje(Node nu) {
        JSONObject nespravneUdaje = new JSONObject();
        NodeList nuList = nu.getChildNodes();

        for (int i = 0; i < nuList.getLength(); i++) {
            Node dataNode = nuList.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case NespravneUdajeTags.ELEMENT_NESPRAVNY_UDAJ:
                    nespravneUdaje.put("NespravnyUdaj", textContent);
                    break;
                case NespravneUdajeTags.ELEMENT_NAZEV_UDAJE:
                    nespravneUdaje.put("NazevUdaje", textContent);
                    break;
                case NespravneUdajeTags.ELEMENT_OZNACENO_DNE:
                    nespravneUdaje.put("OznacenoDne", textContent);
                    break;
                case NespravneUdajeTags.ELEMENT_OZNACENO_INFO:
                    nespravneUdaje.put("OznacenoInfo", textContent);
                    break;
                default:
                    break;
            }
        }
        return nespravneUdaje.toString();
    }
    //endregion

    //region FK PARSING
    private Integer readFK(Node datanode, String fkName) {
        NodeList fkList = datanode.getChildNodes();

        for (int i = 0; i < fkList.getLength(); i++) {
            Node dataNode = fkList.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            if (nodeName.equals(fkName)) {
                return Integer.parseInt(textContent);
            }
        }
        return null;
    }

    private Long readFKLong(Node datanode, String fkName) {
        NodeList fkList = datanode.getChildNodes();

        for (int i = 0; i < fkList.getLength(); i++) {
            Node dataNode = fkList.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            if (nodeName.equals(fkName)) {
                return Long.parseLong(textContent);
            }
        }
        return null;
    }
    //endregion
}