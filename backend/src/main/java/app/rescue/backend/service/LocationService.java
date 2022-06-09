package app.rescue.backend.service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    public Geometry turnPostLocationToPoint(double latitude, double longitude) {

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(latitude, longitude);

        return geometryFactory.createPoint(coordinate);
    }

    public Geometry turnUserLocationToCircle(double latitude, double longitude, double diameterInMeters) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(32); // adjustable
        shapeFactory.setCentre(new Coordinate(latitude, longitude));
        // Length in meters of 1° of latitude = always 111.32 km
        shapeFactory.setWidth(diameterInMeters / 111320d);
        // Length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
        shapeFactory.setHeight(diameterInMeters / (40075000 * Math.cos(Math.toRadians(latitude)) / 360));

        return shapeFactory.createCircle();
    }

    public double getDistanceFromPostInMeters(Geometry userLocation, Geometry postLocation) {
        if (userLocation == null || postLocation == null) {
            return -1.0;
        }

        /*
        The method distance of this api is in degree unit.
        To turn it to meters this formula must be used: L = π * R * a / 180,
        where 'R' is the earth radius 6371km
        and 'a' is the initial measurement
         */
        double distance = userLocation.getCentroid().distance(postLocation);

        distance = (Math.PI * 6371000 * distance) / 180 ;

        return distance;
    }

    public boolean proximityCheck(Geometry userLocation, Geometry postLocation) {
        return userLocation.contains(postLocation);
    }
}
