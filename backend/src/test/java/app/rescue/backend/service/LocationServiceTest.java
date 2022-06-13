package app.rescue.backend.service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LocationServiceTest {

    private LocationService underTest;

    private Random random;

    @BeforeEach
    void setUp() {
        underTest = new LocationService();
        random = new Random();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canTurnPostLocationToPoint() {
        double expectedLatitude = random.nextDouble();
        double expectedLongitude = random.nextDouble();

        Geometry point = underTest.turnPostLocationToPoint(expectedLatitude, expectedLongitude);

        double actualLatitude = point.getCentroid().getX();
        double actualLongitude = point.getCentroid().getY();

        assertEquals(expectedLatitude, actualLatitude);
        assertEquals(expectedLongitude, actualLongitude);
    }

    @Test
    void canTurnUserLocationToCircle() {
        double expectedLatitude = random.nextDouble();
        double expectedLongitude = random.nextDouble();
        double expectedDiameterInMeters = random.nextDouble();

        Geometry circle = underTest.turnUserLocationToCircle(expectedLatitude, expectedLongitude, expectedDiameterInMeters);

        double actualLatitude = circle.getCentroid().getX();
        double actualLongitude = circle.getCentroid().getY();

        double actualDiameterInMeters = Double.MIN_NORMAL;
        double distance;
        Coordinate[] coordinates = circle.getCoordinates();
        for (int i = 0; i <coordinates.length; i++) {
            Coordinate a = coordinates[i];
            for (int j = i + 1; j <coordinates.length - 1; j++) {
                Coordinate b = coordinates[j];
                distance = convert(a.distance(b));
                if (distance > actualDiameterInMeters) {
                    actualDiameterInMeters = distance;
                }
            }
        }

        // The turnUserLocationToCircle does not create an accurate circle
        // an approximation is good enough
        assertEquals(expectedLatitude, actualLatitude, 0.00001);
        assertEquals(expectedLongitude, actualLongitude, 0.00001);
        assertEquals(expectedDiameterInMeters, actualDiameterInMeters, 0.01);
    }

    @Test
    void canGetDistanceFromPostInMeters() {
        double expectedLatitude = random.nextDouble();
        double expectedLongitude = random.nextDouble();

        Geometry postLocation = underTest.turnPostLocationToPoint(expectedLatitude, expectedLongitude);

        expectedLatitude = random.nextDouble();
        expectedLongitude = random.nextDouble();
        double expectedDiameterInMeters = random.nextDouble();

        Geometry userLocation = underTest.turnUserLocationToCircle(expectedLatitude, expectedLongitude, expectedDiameterInMeters);

        double expected = convert(userLocation.distance(postLocation));

        double actual = underTest.getDistanceFromPostInMeters(userLocation, postLocation);

        // an approximation is good enough
        assertEquals(expected, actual, 1);
    }

    @Test
    void getDistanceFromPostInMetersWillReturnMinus1WhenUserLocationIsNull() {
        double expectedLatitude = random.nextDouble();
        double expectedLongitude = random.nextDouble();

        Geometry postLocation = underTest.turnPostLocationToPoint(expectedLatitude, expectedLongitude);

        double actual = underTest.getDistanceFromPostInMeters(null, postLocation);

        assertThat(actual).isEqualTo(-1d);
    }

    @Test
    void getDistanceFromPostInMetersWillReturnMinus1WhenPostLocationIsNull() {
        double expectedLatitude = random.nextDouble();
        double expectedLongitude = random.nextDouble();
        double expectedDiameterInMeters = random.nextDouble();

        Geometry userLocation = underTest.turnUserLocationToCircle(expectedLatitude, expectedLongitude, expectedDiameterInMeters);


        double actual = underTest.getDistanceFromPostInMeters(userLocation, null);

        assertThat(actual).isEqualTo(-1d);
    }

    @Test
    void proximityCheckWillReturnTrue() {
        //given the same location
        double expectedLatitude = random.nextDouble();
        double expectedLongitude = random.nextDouble();

        Geometry postLocation = underTest.turnPostLocationToPoint(expectedLatitude, expectedLongitude);

        double expectedDiameterInMeters = random.nextDouble();

        Geometry userLocation = underTest.turnUserLocationToCircle(expectedLatitude, expectedLongitude, expectedDiameterInMeters);

        boolean actual = underTest.proximityCheck(userLocation, postLocation);

        assertThat(actual).isTrue();
    }

    @Test
    void proximityCheckWillReturnFalse() {
        //given two location very far away
        double expectedLatitude = 38.103923997383816d;
        double expectedLongitude = 23.73929106861493d;

        Geometry postLocation = underTest.turnPostLocationToPoint(expectedLatitude, expectedLongitude);

        expectedLatitude = -21.084242578486997;
        expectedLongitude = 55.50596710281108;
        double expectedDiameterInMeters = 1000d;

        Geometry userLocation = underTest.turnUserLocationToCircle(expectedLatitude, expectedLongitude, expectedDiameterInMeters);

        boolean actual = underTest.proximityCheck(userLocation, postLocation);

        assertThat(actual).isFalse();
    }

    private double convert(double distance) {
        // convert radians to meters
        return (Math.PI * 6371000 * distance) / 180 ;
    }
}