package org.hit.android.haim.texasholdem.web;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for performing Coordinate requests
 * <ul>
 *     <li>{@link #upload(Coordinate) Upload}</li>
 *     <li>{@link #update(Coordinate) Update}</li>
 *     <li>{@link #getCoordinate(Long) Get coordinate}</li>
 *     <li>{@link #getAllCoordinates() Get all coordinate}</li>
 *     <li>{@link #getCoordinatesByDistance(Coordinate, int) Get coordinates by distance}</li>
 * </ul>
 * @author Haim Adrian
 * @since 23-Mar-2020
 */
public class CoordinateService {
    // Hide ctor. Access this class through MapStoriesService.
    CoordinateService() {

    }

//    /**
//     * Use this method for uploading a new coordinate.
//     * @param coordinate The coordinate to upload, without identifier
//     * @return A reference to the created coordinate, with its identifier
//     * @throws IOException In case something went wrong during upload
//     */
//    public Coordinate upload(Coordinate coordinate) throws IOException {
//        return MapStoriesService.getInstance().post("coordinate", true, coordinate, Coordinate.class);
//    }
//
//    /**
//     * Use this method for updating an existing coordinate.
//     * @param coordinate The coordinate to update, must have an identifier
//     * @return A reference to the up-to-date coordinate
//     * @throws IOException In case something went wrong during update
//     */
//    public Coordinate update(Coordinate coordinate) throws IOException {
//        return MapStoriesService.getInstance().post("coordinate/" + coordinate.getCoordinateId(), true, coordinate, Coordinate.class);
//    }
//
//    /**
//     * Get info of a coordinate by identifier.<br/>
//     * We use this method when we have a list of coordinates and we need all of the details of a coordinate, including image.<br/>
//     * Usually, when we get list of coordinates, the server does not return the images, in order to reduce the response size.
//     * @param coordinateId The coordinate identifier to find
//     * @return The coordinate, with all of its fields
//     * @throws IOException In case something went wrong while trying to get coordinate info
//     */
//    public Coordinate getCoordinate(Long coordinateId) throws IOException {
//        return MapStoriesService.getInstance().get("coordinate/" + coordinateId, true, Coordinate.class);
//    }
//
//    /**
//     * Get all coordinates from server.<br/>
//     * The response will not contain images. You'll have to use {@link #getCoordinate(Long)} in order to get the image.
//     * @return The coordinates
//     * @throws IOException In case something went wrong while trying to get coordinates
//     */
//    public List<Coordinate> getAllCoordinates() throws IOException {
//        return MapStoriesService.getInstance().get("coordinate", true, new TypeToken<ArrayList<Coordinate>>(){}.getType());
//    }
//
//    /**
//     * Get all coordinates around a specified coordinate from server. We use 1km as radius<br/>
//     * The response will not contain images. You'll have to use {@link #getCoordinate(Long)} in order to get the image.
//     * @param around Around which coordinate to look
//     * @return The coordinates
//     * @throws IOException In case something went wrong while trying to get coordinates
//     */
//    public List<Coordinate> getCoordinatesByDistance(Coordinate around) throws IOException {
//        return getCoordinatesByDistance(around, 1);
//    }
//
//    /**
//     * Get all coordinates around a specified coordinate from server. Use distance to specify the radius to look around<br/>
//     * The response will not contain images. You'll have to use {@link #getCoordinate(Long)} in order to get the image.
//     * @param around Around which coordinate to look
//     * @param distance What distance to use as radius (in KM)
//     * @return The coordinates
//     * @throws IOException In case something went wrong while trying to get coordinates
//     */
//    public List<Coordinate> getCoordinatesByDistance(Coordinate around, int distance) throws IOException {
//        return MapStoriesService.getInstance().get("coordinate/dist", true, new TypeToken<ArrayList<Coordinate>>(){}.getType(),
//                "lat", around.getLatitude(),
//                "lng", around.getLongitude(),
//                "dist", distance);
//    }
}
