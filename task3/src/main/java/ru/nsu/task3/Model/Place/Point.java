package ru.nsu.task3.Model.Place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Point {
    private String lat;
    private String lng;
}
