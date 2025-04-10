package cca.ruian_puller.download.geometry;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class GeometryParser {

    // For creating geometries
    private final GeometryFactory geometryFactory = new GeometryFactory();
    // For reading WKT (Potential fetching of geometries from DB)
    private final WKTReader wktReader = new WKTReader(geometryFactory);

    //region DefinicniBod / Point / MultiPoint
    public Geometry readDefinicniBod(XMLStreamReader reader) throws XMLStreamException {

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT) {
                String name = reader.getLocalName();
                if (name.equals(GeometryConsts.MULTIPOINT)) {
                    return readMultiPoint(reader);
                } else if (name.equals(GeometryConsts.POINT) || name.equals(GeometryConsts.ADRESNI_BOD)) {
                    return readPoint(reader);
                }
            }
        }
        return null;
    }

    private MultiPoint readMultiPoint(XMLStreamReader reader) throws XMLStreamException {
        List<Point> pointList = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT) {
                String name = reader.getLocalName();
                if (name.equals(GeometryConsts.POS)) {
                    String[] coordinates = reader.getElementText().split(" ");
                    pointList.add(geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]))));
                }
            } else if (event == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals(GeometryConsts.MULTIPOINT)) {
                break;
            }
        }
        return geometryFactory.createMultiPoint(pointList.toArray(new Point[0]));
    }

    private Point readPoint(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT) {
                String name = reader.getLocalName();
                if (name.equals(GeometryConsts.POS)) {
                    String[] coordinates = reader.getElementText().split(" ");
                    return geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
                }
            } else if (event == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals(GeometryConsts.POINT)) {
                break;
            }
        }
        return null;
    }
    //endregion

    public Geometry readDefinicniCara(XMLStreamReader reader) throws XMLStreamException {

        return null;
    }

    //region GeneralizovaneHranice
    public Geometry readGeneralizovaneHranice(XMLStreamReader reader) throws XMLStreamException {

        return null;
    }
    //endregion

    //region OriginalniHranice
    public Geometry readOriginalniHranice(XMLStreamReader reader) throws XMLStreamException {

        return null;
    }
    //endregion
}