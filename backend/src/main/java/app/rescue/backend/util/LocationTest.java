package app.rescue.backend.util;


import app.rescue.backend.service.LocationService;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class LocationTest {

    public static void main(String[] args) {

        LocationService locationHelper = new LocationService();
        double latitude = 39.6649567814473;
        double longitude = 20.85393331551958;
        double diameterInMeters = 7000;

        Geometry circle1 = locationHelper.userLocationToCircle(latitude, longitude, diameterInMeters);


        latitude = 39.63671633124024;
        longitude = 20.864948776812938;

        //Geometry circle2 = locationHelper.userLocationToCircle(latitude, longitude, diameterInMeters);


        Geometry point1 = locationHelper.postLocationToPoint(latitude, longitude);


        latitude = 35.63671633124024;
        longitude = 21.864948776812938;
        Geometry point2 = locationHelper.postLocationToPoint(latitude, longitude);

        System.out.println(point2);

        System.out.println(circle1.getCentroid().distance(point1));
        System.out.println(circle1.getCentroid().distance(point2));


    }
}