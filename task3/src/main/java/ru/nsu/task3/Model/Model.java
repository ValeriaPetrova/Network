package ru.nsu.task3.Model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
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
import java.util.concurrent.CopyOnWriteArrayList;
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
    private CompletableFuture<CopyOnWriteArrayList<Place>> places;
    private CompletableFuture<CopyOnWriteArrayList<InformationAboutPlace>> listOfNearbyPlaces;
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

    private CompletableFuture<CopyOnWriteArrayList<Place>> receivePlaceAsync() throws UnsupportedEncodingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String placeNameUTF = URLEncoder.encode(placeName, StandardCharsets.UTF_8.toString());
        String request = String.format("%s?q=%s&key=%s", urlGraphHopper, placeNameUTF, keyApiGraphHopper);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        logger.info(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parsePlace);
    }

    private CompletableFuture<CopyOnWriteArrayList<InformationAboutPlace>> receiveListOfNearbyPlacesAsync(Place place) {
        String request = String.format("%sradius?radius=1000&lon=%s&lat=%s&units=metric&apikey=%s", urlOpenTripMap, place.getLng(), place.getLat(), keyApiOpenTripMap);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        logger.info(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parseNearbyPlace);
    }

    private CompletableFuture<Description> receiveDescriptionAsync(InformationAboutPlace informationAboutPlace) {
        String request = String.format("%sxid/%s?apikey=%s", urlOpenTripMap, informationAboutPlace.getXid(), keyApiOpenTripMap);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        logger.info(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parseDescription);
    }

    private CompletableFuture<Weather> receiveWeatherAsync(Place place) {
        String request = String.format("%s?lat=%s&lon=%s&units=metric&appid=%s", urlOpenWeatherMap, place.getLat(), place.getLng(), keyApiOpenWeatherMap);
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
        Place place = places.get().get(idx);
        listOfNearbyPlaces = receiveListOfNearbyPlacesAsync(place);
        weather = receiveWeatherAsync(place);
        if (isError) {
            throw new ModelException("Bad response");
        }
    }

    public void setDescriptionOfPlace(int idx) throws ExecutionException, InterruptedException {
        description = receiveDescriptionAsync(listOfNearbyPlaces.get().get(idx));
    }

    private HttpResponse<String> body(HttpResponse<String> httpResponse) {
        if (httpResponse.statusCode() != 200) {
            isError = true;
        }
        return httpResponse;
    }
}
