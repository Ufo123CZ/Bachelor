package cca.ruian_puller.download;

import cca.ruian_puller.download.dto.*;
import cca.ruian_puller.download.elements.*;
import lombok.extern.log4j.Log4j2;
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

    private BufferedWriter writer;

    public void processFile(final InputStream fileIS) {
        try {
            writer = new BufferedWriter(new FileWriter("logs/output.txt"));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fileIS);
            document.getDocumentElement().normalize();

            writer.write("Root element: " + document.getDocumentElement().getNodeName() + "\n");

            List<String> data = new ArrayList<>();

            readData(document);

            writer.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readData(Document document) throws IOException {
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
                    case ELEMENT_SOVY:
                        readSpravniObvody(dataStart.item(j));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    //region STAT
    private void readStaty(Node statyNode) throws IOException {
        List<StatDto> stats = new ArrayList<>();
        NodeList staty = statyNode.getChildNodes();
        for (int i = 0; i < staty.getLength(); i++) {
            if ((staty.item(i).getNodeName()).equals(ELEMENT_STAT)) {
                stats.add(readStat(staty.item(i)));
            }
        }
        writer.write("STATY: " + stats.size() + "\n");
        for (StatDto stat : stats) {
            writer.write(stat + "\n");
        }
    }

    private StatDto readStat(Node statNode) {
        StatDto stat = new StatDto();
        NodeList statData = statNode.getChildNodes();

        for (int i = 0; i < statData.getLength(); i++) {
            Node dataNode = statData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case Stat_Tags.ELEMENT_KOD:
                    stat.setKod(Integer.parseInt(textContent));
                    break;
                case Stat_Tags.ELEMENT_NAZEV:
                    stat.setNazev(textContent);
                    break;
                case Stat_Tags.ELEMENT_NESPRAVNY:
                    stat.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case Stat_Tags.ELEMENT_PLATI_OD:
                    stat.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case Stat_Tags.ELEMENT_PLATI_DO:
                    stat.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case Stat_Tags.ELEMENT_ID_TRANSAKCE:
                    stat.setIdtransakce(Long.parseLong(textContent));
                    break;
                case Stat_Tags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY:
                    stat.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case Stat_Tags.ELEMENT_NUTS_LAU:
                    stat.setNutsLau(textContent);
                    break;
                case Stat_Tags.ELEMENT_GEOMETRIE:
                    stat.setGeometrie(textContent);
                    break;
                case Stat_Tags.ELEMENT_NESPRAVNE_UDAJE:
                    stat.setNespravneudaje(textContent);
                    break;
                case Stat_Tags.ELEMENT_DATUM_VZNIKU:
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
    private void readRegionySoudrznosti(Node regionySoudrznostiNode) throws IOException {
        List<RSDto> rsDtos = new ArrayList<>();
        NodeList regionySoudrznosti = regionySoudrznostiNode.getChildNodes();
        for (int i = 0; i < regionySoudrznosti.getLength(); i++) {
            if ((regionySoudrznosti.item(i).getNodeName()).equals(ELEMENT_REGION_SOUDRZNOSTI)) {
                rsDtos.add(readRegionSoudrznosti(regionySoudrznosti.item(i)));
            }
        }
        writer.write("REGIONY_SOUDRZNOSTI: " + rsDtos.size() + "\n");
        for (RSDto rs : rsDtos) {
            writer.write(rs + "\n");
        }
    }

    private RSDto readRegionSoudrznosti(Node regionSoudrznostiNode) {
        RSDto regionSoudrznosti = new RSDto();
        NodeList regionData = regionSoudrznostiNode.getChildNodes();

        for (int i = 0; i < regionData.getLength(); i++) {
            Node dataNode = regionData.item(i);
            String nodeName = dataNode.getNodeName();
            String textContent = dataNode.getTextContent();

            switch (nodeName) {
                case RS_Tags.ELEMENT_KOD:
                    regionSoudrznosti.setKod(Integer.parseInt(textContent));
                    break;
                case RS_Tags.ELEMENT_NAZEV:
                    regionSoudrznosti.setNazev(textContent);
                    break;
                case RS_Tags.ELEMENT_NESPRAVNY:
                    regionSoudrznosti.setNespravny(Boolean.parseBoolean(textContent));
                    break;
                case RS_Tags.ELEMENT_STAT:
                    regionSoudrznosti.setStat(Integer.parseInt(textContent));
                    break;
                case RS_Tags.ELEMENT_PLATI_OD:
                    regionSoudrznosti.setPlatiod(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case RS_Tags.ELEMENT_PLATI_DO:
                    regionSoudrznosti.setPlatido(LocalDateTime.parse(textContent, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                case RS_Tags.ELEMENT_ID_TRANSAKCE:
                    regionSoudrznosti.setIdtransakce(Long.parseLong(textContent));
                    break;
                case RS_Tags.ELEMENT_GLOBALNI_ID_NAVRHU_ZMENY:
                    regionSoudrznosti.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case RS_Tags.ELEMENT_NUTS_LAU:
                    regionSoudrznosti.setNutslau(textContent);
                    break;
                case RS_Tags.ELEMENT_GEOMETRIE:
                    regionSoudrznosti.setGeometrie(textContent);
                    break;
                case RS_Tags.ELEMENT_NESPRAVNE_UDAJE:
                    regionSoudrznosti.setNespravneudaje(textContent);
                    break;
                case RS_Tags.ELEMENT_DATUM_VZNIKU:
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
    private void readVuscs(Node vuscNode) throws IOException {
        List<VuscDto> vuscs = new ArrayList<>();
        NodeList vuscList = vuscNode.getChildNodes();
        for (int i = 0; i < vuscList.getLength(); i++) {
            if ((vuscList.item(i).getNodeName()).equals(ELEMENT_VUSC)) {
                vuscs.add(readVusc(vuscList.item(i)));
            }
        }
        writer.write("VUSC: " + vuscs.size() + "\n");
        for (VuscDto stat : vuscs) {
            writer.write(stat + "\n");
        }
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
                    vusc.setGeometrie(textContent);
                    break;
                case VuscTags.ELEMENT_NESPRAVNE_UDAJE:
                    vusc.setNespravneudaje(textContent);
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
    private void readOkresy(Node okresyNode) throws IOException {
        List<OkresDto> okresy = new ArrayList<>();
        NodeList okresyList = okresyNode.getChildNodes();
        for (int i = 0; i < okresyList.getLength(); i++) {
            if ((okresyList.item(i).getNodeName()).equals(ELEMENT_OKRES)) {
                okresy.add(readOkres(okresyList.item(i)));
            }
        }
        writer.write("OKRESY: " + okresy.size() + "\n");
        for (OkresDto okres : okresy) {
            writer.write(okres + "\n");
        }
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
                    okres.setVusc(Integer.parseInt(textContent));
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
                    okres.setGeometrie(textContent);
                    break;
                case OkresTags.ELEMENT_NESPRAVNE_UDAJE:
                    okres.setNespravneudaje(textContent);
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
    private void readOrps(Node orpNode) throws IOException {
        List<OrpDto> orps = new ArrayList<>();
        NodeList orpList = orpNode.getChildNodes();
        for (int i = 0; i < orpList.getLength(); i++) {
            if ((orpList.item(i).getNodeName()).equals(ELEMENT_ORP)) {
                orps.add(readOrp(orpList.item(i)));
            }
        }
        writer.write("ORP: " + orps.size() + "\n");
        for (OrpDto orp : orps) {
            writer.write(orp + "\n");
        }
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
                    orp.setSpravniobecKod(Integer.parseInt(textContent));
                    break;
                case OrpTags.ELEMENT_VUSC:
                    orp.setVusc(Integer.parseInt(textContent));
                    break;
                case OrpTags.ELEMENT_OKRES:
                    orp.setOkres(Integer.parseInt(textContent));
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
                    orp.setGeometrie(textContent);
                    break;
                case OrpTags.ELEMENT_NESPRAVNEUDAJE:
                    orp.setNespravneudaje(textContent);
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
    private void readPous(Node pouNode) throws IOException {
        List<PouDto> pous = new ArrayList<>();
        NodeList pouList = pouNode.getChildNodes();
        for (int i = 0; i < pouList.getLength(); i++) {
            if ((pouList.item(i).getNodeName()).equals(ELEMENT_POU)) {
                pous.add(readPou(pouList.item(i)));
            }
        }
        writer.write("POU: " + pous.size() + "\n");
        for (PouDto pou : pous) {
            writer.write(pou + "\n");
        }
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
                    pou.setOrp(Integer.parseInt(textContent));
                    break;
                case PouTags.ELEMENT_PLATIOD:
                    pou.setPlatiod(textContent);
                    break;
                case PouTags.ELEMENT_PLATIDO:
                    pou.setPlatido(textContent);
                    break;
                case PouTags.ELEMENT_IDTRANSAKCE:
                    pou.setIdtransakce(Long.parseLong(textContent));
                    break;
                case PouTags.ELEMENT_GLOBALNIIDNAVHRUZMENY:
                    pou.setGlobalniidnavrhuzmeny(Long.parseLong(textContent));
                    break;
                case PouTags.ELEMENT_GEOMETRIE:
                    pou.setGeometrie(textContent);
                    break;
                case PouTags.ELEMENT_NESPRAVNEUDAJE:
                    pou.setNespravneudaje(textContent);
                    break;
                case PouTags.ELEMENT_DATUMVZNIKU:
                    pou.setDatumvzniku(textContent);
                    break;
                default:
                    break;
            }
        }
        return pou;
    }
    //endregion

    //region OBCE
    private void readObce(Node obceNode) throws IOException {
        List<ObecDto> obce = new ArrayList<>();
        NodeList obceList = obceNode.getChildNodes();
        for (int i = 0; i < obceList.getLength(); i++) {
            if ((obceList.item(i).getNodeName()).equals(ELEMENT_OBEC)) {
                obce.add(readObec(obceList.item(i)));
            }
        }
        writer.write("OBCE: " + obce.size() + "\n");
        for (ObecDto obec : obce) {
            writer.write(obec + "\n");
        }
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
                    obec.setOkres(Integer.parseInt(textContent));
                    break;
                case ObecTags.ELEMENT_POU:
                    obec.setPou(Integer.parseInt(textContent));
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
                    String mk = readMK(dataNode).toString();
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
                    obec.setClenenisrozsahtypkod(Integer.parseInt(textContent));
                    break;
                case ObecTags.ELEMENT_CLENENISMTYKOD:
                    obec.setClenenismtykod(Integer.parseInt(textContent));
                    break;
                case ObecTags.ELEMENT_NUTSLAU:
                    obec.setNutslau(textContent);
                    break;
                case ObecTags.ELEMENT_GEOMETRIE:
                    obec.setGeometrie(textContent);
                    break;
                case ObecTags.ELEMENT_NESPRAVNEUDAJE:
                    obec.setNespravneudaje(textContent);
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
    private void readCastiObce(Node castiObceNode) throws IOException {
        List<CastObceDto> castiObce = new ArrayList<>();
        NodeList castiObceList = castiObceNode.getChildNodes();
        for (int i = 0; i < castiObceList.getLength(); i++) {
            if ((castiObceList.item(i).getNodeName()).equals(ELEMENT_CAST_OBCE)) {
                castiObce.add(readCastObce(castiObceList.item(i)));
            }
        }
        writer.write("CASTI_OBCE: " + castiObce.size() + "\n");
        for (CastObceDto castObec : castiObce) {
            writer.write(castObec + "\n");
        }
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
                    castObec.setObec(Integer.parseInt(textContent));
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
                    castObec.setGlobalniidnavrzmeny(Long.parseLong(textContent));
                    break;
                case CastObceTags.ELEMENT_MLUVNICKECHARAKTERISTIKY:
                    String mk = readMK(dataNode).toString();
                    castObec.setMluvnickecharakteristiky(mk);
                    break;
                case CastObceTags.ELEMENT_GEOMETRIE:
                    castObec.setGeometrie(textContent);
                    break;
                case CastObceTags.ELEMENT_NESPRAVNEUDAJE:
                    castObec.setNespravneudaje(textContent);
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
    private void readMops(Node mopNode) throws IOException {
        List<MopDto> mops = new ArrayList<>();
        NodeList mopList = mopNode.getChildNodes();
        for (int i = 0; i < mopList.getLength(); i++) {
            if ((mopList.item(i).getNodeName()).equals(ELEMENT_MOP)) {
                mops.add(readMop(mopList.item(i)));
            }
        }
        writer.write("MOP: " + mops.size() + "\n");
        for (MopDto mop : mops) {
            writer.write(mop + "\n");
        }
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
                    mop.setObec(Integer.parseInt(textContent));
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
                    mop.setGlobalniidnavrzmeny(Long.parseLong(textContent));
                    break;
                case MopTags.ELEMENT_GEOMETRIE:
                    mop.setGeometrie(textContent);
                    break;
                case MopTags.ELEMENT_NESPRAVNEUDAJE:
                    mop.setNespravneudaje(textContent);
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
    private void readSpravniObvody(Node spravniObvodyNode) throws IOException {
        List<SpravniObvodDto> spravniObvody = new ArrayList<>();
        NodeList spravniObvodyList = spravniObvodyNode.getChildNodes();
        for (int i = 0; i < spravniObvodyList.getLength(); i++) {
            if ((spravniObvodyList.item(i).getNodeName()).equals(ELEMENT_SOV)) {
                spravniObvody.add(readSpravniObvod(spravniObvodyList.item(i)));
            }
        }
        writer.write("SPRAVNI_OBVODY: " + spravniObvody.size() + "\n");
        for (SpravniObvodDto spravniObvod : spravniObvody) {
            writer.write(spravniObvod + "\n");
        }
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
                    spravniObvod.setSpravnimomckod(Integer.parseInt(textContent));
                    break;
                case SpravniObvodTags.ELEMENT_OBEC:
                    spravniObvod.setObec(Integer.parseInt(textContent));
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
                    spravniObvod.setGlobalniidnavrzmeny(Long.parseLong(textContent));
                    break;
                case SpravniObvodTags.ELEMENT_GEOMETRIE:
                    spravniObvod.setGeometrie(textContent);
                    break;
                case SpravniObvodTags.ELEMENT_NESPRAVNEUDAJE:
                    spravniObvod.setNespravneudaje(textContent);
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


    //region Additional methods
    private List<String> readMK(Node mk) {
        List<String> mks = new ArrayList<>();
        NodeList mkList = mk.getChildNodes();
        for (int i = 0; i < mkList.getLength(); i++) {
            mks.add(mkList.item(i).getTextContent());
        }
        return mks;
    }
    //endregion
}