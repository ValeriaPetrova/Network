package ru.nsu.task3;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import ru.nsu.task3.Model.Description.Description;
import ru.nsu.task3.Model.NearbyPlaces.NearbyPlaces;
import ru.nsu.task3.Model.Place.Place;
import ru.nsu.task3.Model.Weather.Weather;

public class Parser {
    @SneakyThrows
    public static Place parsePlace(String request) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(request, Place.class);
    }

    @SneakyThrows
    public static NearbyPlaces parseNearbyPlace(String request) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(request, NearbyPlaces.class);
    }

    @SneakyThrows
    public static Description parseDescription(String request) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(request, Description.class);
    }

    @SneakyThrows
    public static Weather parseWeather(String request) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(request, Weather.class);
    }
}
