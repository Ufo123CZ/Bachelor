package cca.ruian_puller.download.geometry;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class GeometryParser {

    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final WKTReader wktReader = new WKTReader(geometryFactory);

    public Geometry readGeometry(Node geometryNode) {
        try {
            // Check geometry type
            NodeList geometryTypeNodes = geometryNode.getChildNodes();

            for (int i = 0; i < geometryTypeNodes.getLength(); i++) {
                Node geometryTypeNode = geometryTypeNodes.item(i);
                String geometryType = geometryTypeNode.getNodeName();
                String geometryTypeTrim = geometryType.substring(geometryType.indexOf(":") + 1);
                switch (geometryTypeTrim) {
                    case GeometryConsts.DEF_POINT:
                        return readDefinicniBod(geometryTypeNode);
                    default:
                        log.error("Unknown geometry type: {}", geometryType);
                }
            }
        } catch (Exception e) {
            log.error("Error while parsing geometry: {}", e.getMessage());
        }
        return null;
    }

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
}