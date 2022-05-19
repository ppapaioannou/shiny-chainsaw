package app.rescue.backend.service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    public Geometry postLocationToPoint(double latitude, double longitude) {

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(latitude, longitude);

        return geometryFactory.createPoint(coordinate);
    }

    public Geometry userLocationToCircle(double latitude, double longitude, double diameterInMeters) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(32); // adjustable
        shapeFactory.setCentre(new Coordinate(latitude, longitude));
        // Length in meters of 1° of latitude = always 111.32 km
        shapeFactory.setWidth(diameterInMeters / 111320d);
        // Length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
        shapeFactory.setHeight(diameterInMeters / (40075000 * Math.cos(Math.toRadians(latitude)) / 360));

        return shapeFactory.createEllipse();
    }

    public double getDistanceFromPostInMeters(Geometry userLocation, Geometry postLocation) {
        if (userLocation == null || postLocation == null) {
            return -1.0;
        }

        /*
        I found that the unit distance returned when call some of the methods distance
        calculation with this api will be in degree unit. To convert it to kilometer,
        assumes that value returned is d then you need to convert it to radian and
        multiply with earth radius 6371km. The formula would be d / 180 * PI * 6371.
         */
        double distance = userLocation.getCentroid().distance(postLocation);

        //L = π * R * a / 180
        distance = (Math.PI * 6371000 * distance) / 180 ;

        //return String.format("%.2f", distance) + " meters";
        return distance;
        //return String.valueOf(distance);
    }

    public boolean proximityCheck(Geometry userLocation, Geometry postLocation) {
        return userLocation.contains(postLocation);
    }
}
