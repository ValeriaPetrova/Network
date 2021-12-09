package ru.nsu.task3.Model.NearbyPlaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NearbyPlaces {
    private CopyOnWriteArrayList<Features> features;
}
