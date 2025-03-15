package cca.ruian_puller.download.geometry;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

    public Geometry[] readGeometry(Node geometryNode) {
        try {
            Geometry[] geometries = new Geometry[3];

            // Check geometry type
            NodeList geometryTypeNodes = geometryNode.getChildNodes();

            for (int i = 0; i < geometryTypeNodes.getLength(); i++) {
                Node geometryTypeNode = geometryTypeNodes.item(i);
                String geometryType = geometryTypeNode.getNodeName();
                String geometryTypeTrim = geometryType.substring(geometryType.indexOf(":") + 1);

                // Skip empty nodes
//                if (geometryTypeTrim.equals(VdpParserConst.EMPTY_NODE)) continue;

                switch (geometryTypeTrim) {
                    case GeometryConsts.DEF_POINT:
//                        geometries[0] = readDefinicniBod(geometryTypeNode);
                        break;
                    case GeometryConsts.GEN_HRANICE:
//                        geometries[1] = readGenHranice(geometryTypeNode);
                        break;
                    case GeometryConsts.ORI_HRANICE:
//                        geometries[2] = readOriHranice(geometryTypeNode);
                        break;
                    default:
                        log.error("Unknown geometry type: {}", geometryType);
                }
            }
            return geometries;
        } catch (Exception e) {
            log.error("Error while parsing geometry: {}", e.getMessage());
        }
        return null;
    }

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

    //region GeneralizovaneHranice
    public Geometry readGeneralizovaneHranice(XMLStreamReader reader) throws XMLStreamException {
//        // MultiSurface -> surfaceMember -> Polygon -> exterior -> LinearRing -> posList
//        // MultiSurface -> surfaceMember -> Polygon -> interior -> LinearRing -> posList
//        NodeList genHraniceNodes = genHranice.getChildNodes();
//        for (int i = 0; i < genHraniceNodes.getLength(); i++) {
//            Node multiSurface = genHraniceNodes.item(i);
//            String multiSurfaceName = multiSurface.getNodeName();
//            if (multiSurfaceName.equals(GeometryConsts.MULTI_SURFACE)) {
//                return readMultiSurface(multiSurface, false);
//            }
//        }
        return null;
    }
    //endregion

    //region OriginalniHranice
    public Geometry readOriginalniHranice(XMLStreamReader reader) throws XMLStreamException {
//        NodeList oriHraniceNodes = oriHranice.getChildNodes();
//        for (int i = 0; i < oriHraniceNodes.getLength(); i++) {
//            Node multiSurface = oriHraniceNodes.item(i);
//            String multiSurfaceName = multiSurface.getNodeName();
//            if (multiSurfaceName.equals(GeometryConsts.MULTI_SURFACE)) {
//                return readMultiSurface(multiSurface, true);
//            }
//        }
        return null;
    }
    //endregion

    //region Polygon / LineString / Curve
    private Geometry readMultiSurface(Node multiSurface, boolean isOri) {
        List<Polygon> polygonList = new ArrayList<>();

        NodeList multiSurfaceNodes = multiSurface.getChildNodes();
        for (int j = 0; j < multiSurfaceNodes.getLength(); j++) {
            Node surfaceMember = multiSurfaceNodes.item(j);
            String surfaceMemberName = surfaceMember.getNodeName();
            if (surfaceMemberName.equals(GeometryConsts.SURFACE_MEMBER)) {
                NodeList surfaceMemberNodes = surfaceMember.getChildNodes();
                for (int k = 0; k < surfaceMemberNodes.getLength(); k++) {
                    Node polygon = surfaceMemberNodes.item(k);
                    String polygonName = polygon.getNodeName();
                    if (polygonName.equals(GeometryConsts.POLYGON)) {
                        if (isOri) polygonList.add(readPolygon(polygon));
                        else polygonList.add(readPolygonOri(polygon));
                    }
                }
            }
        }
        return geometryFactory.createMultiPolygon(polygonList.toArray(new Polygon[0]));
    }

    private Polygon readPolygon(Node polygon) {
        LinearRing exterior = null;
        List<LinearRing> interiorList = new ArrayList<>();

        NodeList polygonNodes = polygon.getChildNodes();
        for (int l = 0; l < polygonNodes.getLength(); l++) {
            Node polygonNode = polygonNodes.item(l);
            String polygonNodeName = polygonNode.getNodeName();
            if (polygonNodeName.equals(GeometryConsts.EXTERIOR)) {
                exterior = readLinearRing(polygonNode);
            } else if (polygonNodeName.equals(GeometryConsts.INTERIOR)) {
                interiorList.add(readLinearRing(polygonNode));
            }
        }
        return geometryFactory.createPolygon(exterior, interiorList.toArray(new LinearRing[0]));
    }

    private LinearRing readLinearRing(Node linearRing) {
        NodeList linearRingNodes = linearRing.getChildNodes();
        for (int m = 0; m < linearRingNodes.getLength(); m++) {
            Node posList = linearRingNodes.item(m);
            String posListName = posList.getNodeName();
            if (posListName.equals(GeometryConsts.POS_LIST)) {
                String[] coordinates = posList.getTextContent().split(" ");
                Coordinate[] coords = new Coordinate[coordinates.length / 2];
                for (int n = 0; n < coordinates.length; n += 2) {
                    coords[n / 2] = new Coordinate(Double.parseDouble(coordinates[n]), Double.parseDouble(coordinates[n + 1]));
                }
                return geometryFactory.createLinearRing(coords);
            }
        }
        return null;
    }

    private Polygon readPolygonOri(Node polygonNode) {
        LinearRing exteriorLR = null;
        List<LinearRing> interiorListLR = new ArrayList<>();

        LineString exteriorLS = null;
        List<LineString> interiorListLS = new ArrayList<>();

        NodeList polygonNodes = polygonNode.getChildNodes();

        for(int i = 0; i < polygonNodes.getLength(); i++) {
            Node polygonChild = polygonNodes.item(i);
            String polygonChildName = polygonChild.getNodeName();

            if (polygonChildName.equals(GeometryConsts.EXTERIOR)) {
                NodeList exteriorNodes = polygonChild.getChildNodes();

                for (int j = 0; j < exteriorNodes.getLength(); j++) {
                    Node exteriorChild = exteriorNodes.item(j);
                    String exteriorChildName = exteriorChild.getNodeName();

                    if (exteriorChildName.equals(GeometryConsts.LINEAR_RING)) {
                        exteriorLR = readLinearRing(exteriorChild);
                    } else if (exteriorChildName.equals(GeometryConsts.RING)) {
                        exteriorLS = readRing(exteriorChild);
                    }
                }
            } else if (polygonChildName.equals(GeometryConsts.INTERIOR)) {
                NodeList interiorNodes = polygonChild.getChildNodes();

                for (int j = 0; j < interiorNodes.getLength(); j++) {
                    Node interiorChild = interiorNodes.item(j);
                    String interiorChildName = interiorChild.getNodeName();

                    if (interiorChildName.equals(GeometryConsts.LINEAR_RING)) {
                        interiorListLR.add(readLinearRing(interiorChild));
                    } else if (interiorChildName.equals(GeometryConsts.RING)) {
                        interiorListLS.add(readRing(interiorChild));
                    }
                }
            }
        }

        // Used Geom
        if (exteriorLR != null && !interiorListLR.isEmpty()) {
            return geometryFactory.createPolygon(exteriorLR, interiorListLR.toArray(new LinearRing[0]));
        } else if (exteriorLS != null && !interiorListLS.isEmpty()) {
            return geometryFactory.createPolygon((LinearRing) exteriorLS, (LinearRing[]) interiorListLS.toArray(new LineString[0]));
        } else if (exteriorLR != null && !interiorListLS.isEmpty()) {
            return geometryFactory.createPolygon(exteriorLR, (LinearRing[]) interiorListLS.toArray(new LineString[0]));
        } else if (exteriorLS != null && !interiorListLR.isEmpty()) {
            return geometryFactory.createPolygon((LinearRing) exteriorLS, interiorListLR.toArray(new LinearRing[0]));
        } else {
            log.error("No exterior or interior found");
            return null;
        }
    }

    private LineString readRing(Node ringNode) {
        NodeList ringNodes = ringNode.getChildNodes();
        List<LineString> LCStrings = new ArrayList<>();
        for(int i = 0; i < ringNodes.getLength(); i++) {
            Node ringChild = ringNodes.item(i);
            String ringChildName = ringChild.getNodeName();

            if (ringChildName.equals(GeometryConsts.CURVE_MEMBER)) {
                NodeList curveMemberNodes = ringChild.getChildNodes();

                for(int j = 0; j < curveMemberNodes.getLength(); j++) {
                    Node curveMemberNode = curveMemberNodes.item(j);
                    String curveMemberName = curveMemberNode.getNodeName();

                    if (curveMemberName.equals(GeometryConsts.LINE_STRING)) {
                        LCStrings.add(readLineString(curveMemberNode));
                    } else if (curveMemberName.equals(GeometryConsts.CURVE)) {
                        LCStrings.add(readCurve(curveMemberNode));
                    }
                }
            }
        }

        // Unite LineStrings
        LineString result = null;
        for (LineString ls : LCStrings) {
            if (result == null) {
                result = ls;
            } else {
                result = (LineString) result.union(ls);
            }
        }
        return result;
    }

    private LineString readLineString(Node lineStringNode) {
        NodeList lineStringNodes = lineStringNode.getChildNodes();
        for(int i = 0; i < lineStringNodes.getLength(); i++) {
            Node lineStringChild = lineStringNodes.item(i);
            String lineStringChildName = lineStringChild.getNodeName();

            if (lineStringChildName.equals(GeometryConsts.POS_LIST)) {
                String[] coordinates = lineStringChild.getTextContent().split(" ");
                Coordinate[] coords = new Coordinate[coordinates.length / 2];
                for (int j = 0; j < coordinates.length; j += 2) {
                    coords[j / 2] = new Coordinate(Double.parseDouble(coordinates[j]), Double.parseDouble(coordinates[j + 1]));
                }
                return geometryFactory.createLineString(coords);
            }
        }
        return null;
    }

    private LineString readCurve(Node curveNode) {
        NodeList curveNodes = curveNode.getChildNodes();

        for(int i = 0; i < curveNodes.getLength(); i++) {
            Node curveChild = curveNodes.item(i);
            String curveChildName = curveChild.getNodeName();

            if (curveChildName.equals(GeometryConsts.SEGMENTS)) {
                NodeList segmentsNodes = curveChild.getChildNodes();

                for(int j = 0; j < segmentsNodes.getLength(); j++) {
                    Node segmentsNode = segmentsNodes.item(j);
                    String segmentsName = segmentsNode.getNodeName();

                    if (segmentsName.equals(GeometryConsts.ARC_STRING)) {
                        NodeList arcStringNodes = segmentsNode.getChildNodes();

                        for(int k = 0; k < arcStringNodes.getLength(); k++) {
                            Node arcStringNode = arcStringNodes.item(k);
                            String arcStringName = arcStringNode.getNodeName();

                            if (arcStringName.equals(GeometryConsts.POS_LIST)) {
                                String[] coordinates = arcStringNode.getTextContent().split(" ");
                                Coordinate[] coords = new Coordinate[coordinates.length / 2];
                                for (int l = 0; l < coordinates.length; l += 2) {
                                    coords[l / 2] = new Coordinate(Double.parseDouble(coordinates[l]), Double.parseDouble(coordinates[l + 1]));
                                }
                                return geometryFactory.createLineString(coords);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    //endregion
}