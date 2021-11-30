package ru.nsu.task3.Model;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.task3.Model.Description.Description;
import ru.nsu.task3.Model.NearbyPlaces.NearbyPlaces;
import ru.nsu.task3.Model.NearbyPlaces.PlaceProperties;
import ru.nsu.task3.Model.Place.Place;
import ru.nsu.task3.Model.Place.Point;
import ru.nsu.task3.Model.Weather.Weather;
import ru.nsu.task3.Parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

@Getter
public class Model {
    private  static final Logger logger = LoggerFactory.getLogger("APPLICATION");
    private HttpClient httpClient;

    private final static String urlGraphHopper = "https://graphhopper.com/api/1/geocode";
    private static String keyApiGraphHopper = "<INSERT_YOUR_KEY>";
    private final static String urlOpenTripMap = "https://api.opentripmap.com/0.1/ru/places/";
    private static String keyApiOpenTripMap = "<INSERT_YOUR_KEY>";
    private final static String urlOpenWeatherMap = "http://api.openweathermap.org/data/2.5/weather";
    private static String keyApiOpenWeatherMap = "<INSERT_YOUR_KEY>";

    private boolean isError = false;

    @Setter
    private String placeName;
    private CompletableFuture<Place> places;
    private CompletableFuture<NearbyPlaces> listOfNearbyPlaces;
    private Mode mode;
    private CompletableFuture<Description> description;
    private CompletableFuture<Weather> weather;

    public Model() throws FileNotFoundException {
        FileInputStream fileInputStream;
        Properties property = new Properties();
        try{
            fileInputStream = new FileInputStream("src/main/resources/ru/nsu/task3/keysApi.properties");
            property.load(fileInputStream);
            keyApiGraphHopper = property.getProperty("keyApiGraphHopper");
            keyApiOpenTripMap = property.getProperty("keyApiOpenTripMap");
            keyApiOpenWeatherMap = property.getProperty("keyApiOpenWeatherMap");
            httpClient = HttpClient.newHttpClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<Place> receivePlaceAsync() throws UnsupportedEncodingException {
        String placeNameUTF = URLEncoder.encode(placeName, StandardCharsets.UTF_8.toString());
        String request = String.format("%s?q=%s&key=%s", urlGraphHopper, placeNameUTF, keyApiGraphHopper);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        logger.info(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parsePlace);
    }

    private CompletableFuture<NearbyPlaces> receiveListOfNearbyPlacesAsync(Point point) {
        String request = String.format("%sradius?radius=1000&lon=%s&lat=%s&units=metric&apikey=%s", urlOpenTripMap, point.getLng(), point.getLat(), keyApiOpenTripMap);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        logger.info(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parseNearbyPlace);
    }

    private CompletableFuture<Description> receiveDescriptionAsync(PlaceProperties properties) {
        String request = String.format("%sxid/%s?apikey=%s", urlOpenTripMap, properties.getXid(), keyApiOpenTripMap);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        logger.info(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parseDescription);
    }

    private CompletableFuture<Weather> receiveWeatherAsync(Point point) {
        String request = String.format("%s?lat=%s&lon=%s&units=metric&appid=%s", urlOpenWeatherMap, point.getLat(), point.getLng(), keyApiOpenWeatherMap);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        logger.info(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parseWeather);
    }

    public void searchPlaces() throws ModelException, UnsupportedEncodingException {
        mode = Mode.SEARCH;
        isError = false;
        places = receivePlaceAsync();
        if (isError) {
            throw new ModelException("Error in request");
        }
    }

    public void choosePlace(int idx) throws ModelException, ExecutionException, InterruptedException {
        isError = false;
        mode = Mode.RECEIVE_INFORMATION;
        Point point = places.get().getHits().get(idx).getPoint();
        listOfNearbyPlaces = receiveListOfNearbyPlacesAsync(point);
        weather = receiveWeatherAsync(point);
        if (isError) {
            throw new ModelException("Bad response");
        }
    }

    public void setDescriptionOfPlace(int idx) throws ExecutionException, InterruptedException {
        description = receiveDescriptionAsync(listOfNearbyPlaces.get().getFeatures().get(idx).getProperties());
    }

    private HttpResponse<String> body(HttpResponse<String> httpResponse) {
        if (httpResponse.statusCode() != 200) {
            isError = true;
        }
        return httpResponse;
    }
}