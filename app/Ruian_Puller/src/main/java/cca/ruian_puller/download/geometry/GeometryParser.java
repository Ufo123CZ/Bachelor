package cca.ruian_puller.download.geometry;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import lombok.extern.log4j.Log4j2;

import javax.sound.sampled.Line;
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
                switch (geometryTypeTrim) {
                    case GeometryConsts.DEF_POINT:
                        geometries[0] = readDefinicniBod(geometryTypeNode);
                        break;
                    case GeometryConsts.GEN_HRANICE:
                        geometries[1] = readGenHranice(geometryTypeNode);
                        break;
                    case GeometryConsts.ORI_HRANICE:
                        geometries[2] = readOriHranice(geometryTypeNode);
                        break;
                    default:
                        log.error("Unknown geometry type: {}", geometryType);
                }
            }
            return geometries;
//            // unite geometries that are not null
//            Geometry result = null;
//
//            if (geometryDefBod != null) {
//                result = geometryDefBod;
//            }
//            if (geometryGenHranice != null) {
//                result = (result == null) ? geometryGenHranice : result.union(geometryGenHranice);
//            }
//            if (geometryOriHranice != null) {
//                result = (result == null) ? geometryOriHranice : result.union(geometryOriHranice);
//            }
//
//            if (result == null) {
//                log.warn("No geometry found");
//            }
//            if (geometryDefBod != null && geometryGenHranice != null && geometryOriHranice != null) {
//                return geometryDefBod.union(geometryGenHranice).union(geometryOriHranice);
//            } else if (geometryDefBod != null && geometryGenHranice != null) {
//                return geometryDefBod.union(geometryGenHranice);
//            } else if (geometryDefBod != null && geometryOriHranice != null) {
//                return geometryDefBod.union(geometryOriHranice);
//            } else if (geometryGenHranice != null && geometryOriHranice != null) {
//                return geometryGenHranice.union(geometryOriHranice);
//            } else if (geometryDefBod != null) {
//                return geometryDefBod;
//            } else if (geometryGenHranice != null) {
//                return geometryGenHranice;
//            } else if (geometryOriHranice != null) {
//                return geometryOriHranice;
//            } else {
//                log.error("No geometry found");
//                return null;
//            }
        } catch (Exception e) {
            log.error("Error while parsing geometry: {}", e.getMessage());
        }
        return null;
    }

    //region DefinicniBod / Point / MultiPoint
    private Geometry readDefinicniBod(Node defBod) {
        NodeList defBodNodes = defBod.getChildNodes();
        for (int j = 0; j < defBodNodes.getLength(); j++) {
            Node pointNode = defBodNodes.item(j);
            String pointNodeName = pointNode.getNodeName();
            if (pointNodeName.equals(GeometryConsts.MULTIPOINT)) {
                return readMultiPoint(pointNode);
            } else if (pointNodeName.equals(GeometryConsts.POINT)) {
                return readPoint(pointNode);
            }
        }
        return null;
    }

    private MultiPoint readMultiPoint(Node multiPointNode) {
        List<Point> pointList = new ArrayList<>();

        NodeList multiPointNodes = multiPointNode.getChildNodes();
        for (int l = 0; l < multiPointNodes.getLength(); l++) {
            Node pointMembers = multiPointNodes.item(l);
            String members = pointMembers.getNodeName();
            if (members.equals(GeometryConsts.MULTIPOINT_MEMBERS)) {
                NodeList multiPointMembers = pointMembers.getChildNodes();
                for (int m = 0; m < multiPointMembers.getLength(); m++) {
                    Node multiPointMember = multiPointMembers.item(m);
                    String multiPointMemberName = multiPointMember.getNodeName();
                    if (multiPointMemberName.equals(GeometryConsts.POINT)) {
                        pointList.add(readPoint(multiPointMember));
                    }
                }
            }
        }
        return geometryFactory.createMultiPoint(pointList.toArray(new Point[0]));
    }

    private Point readPoint(Node point) {
        NodeList pointNodes = point.getChildNodes();

        for (int n = 0; n < pointNodes.getLength(); n++) {
            Node pointNode = pointNodes.item(n);
            String pointName = pointNode.getNodeName();
            if (pointName.equals(GeometryConsts.POS)) {
                String[] coordinates = pointNode.getTextContent().split(" ");
                return geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
            }
        }
        return null;
    }
    //endregion

    //region GeneralizovaneHranice
    private Geometry readGenHranice(Node genHranice) {
        // MultiSurface -> surfaceMember -> Polygon -> exterior -> LinearRing -> posList
        // MultiSurface -> surfaceMember -> Polygon -> interior -> LinearRing -> posList
        NodeList genHraniceNodes = genHranice.getChildNodes();
        for (int i = 0; i < genHraniceNodes.getLength(); i++) {
            Node multiSurface = genHraniceNodes.item(i);
            String multiSurfaceName = multiSurface.getNodeName();
            if (multiSurfaceName.equals(GeometryConsts.MULTI_SURFACE)) {
                return readMultiSurface(multiSurface, false);
            }
        }
        return null;
    }
    //endregion

    //region OriginalniHranice
    private Geometry readOriHranice(Node oriHranice) {
        NodeList oriHraniceNodes = oriHranice.getChildNodes();
        for (int i = 0; i < oriHraniceNodes.getLength(); i++) {
            Node multiSurface = oriHraniceNodes.item(i);
            String multiSurfaceName = multiSurface.getNodeName();
            if (multiSurfaceName.equals(GeometryConsts.MULTI_SURFACE)) {
                return readMultiSurface(multiSurface, true);
            }
        }
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