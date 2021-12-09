package ru.nsu.task3.Model.NearbyPlaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceProperties {
    private String name;
    private String xid;
}
