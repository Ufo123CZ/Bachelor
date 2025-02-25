package cca.ruian_puller.download;

import cca.ruian_puller.download.dto.RSDto;
import cca.ruian_puller.download.dto.StatDto;
import cca.ruian_puller.download.elements.RS_Tags;
import cca.ruian_puller.download.elements.Stat_Tags;
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
        for (RSDto stat : rsDtos) {
            writer.write(stat + "\n");
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
}