package ru.nsu.task3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import ru.nsu.task3.Model.*;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
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
                    currentElement.get("num") == null ? null : currentElement.get("num").asText(),
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
    public static Description parseDescription(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        String name = jsonNode.get("wikipedia_extracts") == null ? null : jsonNode.get("wikipedia_extracts").get("title").asText().split(":")[1];
        String description = jsonNode.get("wikipedia_extracts") == null ? null : jsonNode.get("wikipedia_extracts").get("text").asText();
        return new Description(name, description);
    }

    @SneakyThrows
    public static Weather parseWeather(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        return new Weather(jsonNode.get("main").get("temp").asText(),
                jsonNode.get("main").get("feels_like").asText(),
                jsonNode.get("wind").get("speed").asText()
        );
    }
}
