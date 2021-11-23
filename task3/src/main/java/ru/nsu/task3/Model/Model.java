package ru.nsu.task3.Model;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.task3.Parser;

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

public class Model {
    private final static String urlGraphHopper = "https://graphhopper.com/api/1/geocode";
    private final static String keyApiGraphHopper = "7148ff36-3a88-42e0-9390-ffe9e256a037";
    private final static String urlOpenTripMap = "https://api.opentripmap.com/0.1/ru/places/";
    private final static String keyApiOpenTripMap = "5ae2e3f221c38a28845f05b6c50acd7ee481d2e30e1bd88d99e42a26";
    private final static String urlOpenWeatherMap = "http://api.openweathermap.org/data/2.5/weather";
    private final static String keyApiOpenWeatherMap = "143494f8d432d2a19f11ceec08b9c945";

    public boolean isError = false;

    @Setter
    public String placeName;
    @Getter
    public CopyOnWriteArrayList<Place> places;
    @Getter
    public CompletableFuture<CopyOnWriteArrayList<InformationAboutPlace>> listOfNearbyPlaces;
    @Getter
    public Mode mode;
    @Getter
    public CompletableFuture<Description> description;
    @Getter
    private CompletableFuture<Weather> weather;

    public String receivePlace() throws UnsupportedEncodingException, ExecutionException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        String placeNameUTF = URLEncoder.encode(placeName, StandardCharsets.UTF_8.toString());
        String request = String.format("%s?q=%s&key=%s", urlGraphHopper, placeNameUTF, keyApiGraphHopper);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        System.out.println(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).get();
    }

    public CompletableFuture<CopyOnWriteArrayList<InformationAboutPlace>> receiveListOfNearbyPlaces(Place place) {
        HttpClient httpClient = HttpClient.newHttpClient();
        String request = String.format("%sradius?radius=1000&lon=%s&lat=%s&units=metric&apikey=%s", urlOpenTripMap, place.getLng(), place.getLat(), keyApiOpenTripMap);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        System.out.println(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parseNearbyPlace);
    }

    public CompletableFuture<Description> receiveDescription(InformationAboutPlace informationAboutPlace) {
        HttpClient httpClient = HttpClient.newHttpClient();
        String request = String.format("%sxid/%s?apikey=%s", urlOpenTripMap, informationAboutPlace.getXid(), keyApiOpenTripMap);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        System.out.println(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parseDescription);
    }

    public CompletableFuture<Weather> receiveWeather(Place place) {
        HttpClient httpClient = HttpClient.newHttpClient();
        String request = String.format("%s?lat=%s&lon=%s&units=metric&appid=%s", urlOpenWeatherMap, place.getLat(), place.getLng(), keyApiOpenWeatherMap);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request)).header("accept", "application/json").build();
        System.out.println(request);
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(this::body).thenApply(HttpResponse::body).thenApply(Parser::parseWeather);
    }

    public void searchPlaces() throws ModelException, UnsupportedEncodingException, ExecutionException, InterruptedException {
        mode = Mode.SEARCH;
        isError = false;
        places = new CopyOnWriteArrayList<>();
        String request = receivePlace();
        if (isError) {
            throw new ModelException("Error in request");
        }
        this.places = Parser.parsePlace(request);
    }

    public void choosePlace(int idx) throws ModelException {
        isError = false;
        mode = Mode.RECEIVE_INFORMATION;
        Place place = places.get(idx);
        listOfNearbyPlaces = receiveListOfNearbyPlaces(place);
        weather = receiveWeather(place);
        if (isError) {
            throw new ModelException("Bad response");
        }
    }

    public void setDescriptionOfPlace(int idx) throws ExecutionException, InterruptedException {
        description = receiveDescription(listOfNearbyPlaces.get().get(idx));
    }

    public HttpResponse<String> body(HttpResponse<String> httpResponse) {
        if (httpResponse.statusCode() != 200) {
            isError = true;
        }
        return httpResponse;
    }
}
