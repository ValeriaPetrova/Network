package ru.nsu.task3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import ru.nsu.task3.Model.*;
import ru.nsu.task3.Model.Description.Description;
import ru.nsu.task3.Model.NearbyPlaces.InformationAboutPlace;
import ru.nsu.task3.Model.Weather.Weather;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class Parser {
    @SneakyThrows
    public static CopyOnWriteArrayList<Place> parsePlace(String request) {
        CopyOnWriteArrayList<Place> places = new CopyOnWriteArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(String.valueOf(request));
        Iterator<JsonNode> iterator = jsonNode.get("hits").elements();
        if(!iterator.hasNext()) {
            throw new ModelException("Can't find places");
        }
        while (iterator.hasNext()) {
            JsonNode currentElement = iterator.next();
            Place currentPlace = new Place(currentElement.get("name") == null ? null : currentElement.get("name").asText(),
                    currentElement.get("city") == null ? null : currentElement.get("city").asText(),
                    currentElement.get("country") == null ? null : currentElement.get("country").asText(),
                    currentElement.get("street") == null ? null : currentElement.get("street").asText(),
                    currentElement.get("housenumber") == null ? null : currentElement.get("housenumber").asText(),
                    currentElement.get("point").get("lat") == null ? null : currentElement.get("point").get("lat").asText(),
                    currentElement.get("point").get("lng") == null ? null : currentElement.get("point").get("lng").asText()
            );
            places.add(currentPlace);
        }
        return places;
    }

    @SneakyThrows
    public static CopyOnWriteArrayList<InformationAboutPlace> parseNearbyPlace(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        CopyOnWriteArrayList<InformationAboutPlace> list = new CopyOnWriteArrayList<>();
        JsonNode jsonNode = objectMapper.readTree(response);
        Iterator<JsonNode> iterator = jsonNode.get("features").elements();
        while (iterator.hasNext()) {
            JsonNode currentElement = iterator.next();
            if (!currentElement.get("properties").get("name").asText().isEmpty()) {
                list.add(new InformationAboutPlace(currentElement.get("properties").get("name").asText(), currentElement.get("properties").get("xid").asText()));
            }
        }
        return list;
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
