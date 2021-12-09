package ru.nsu.task3.Model.NearbyPlaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Features {
    private PlaceProperties properties;

    @Override
    public String toString() {
        return String.format("Place: %s", properties.getName());
    }
}
