package ru.nsu.task3.Model.Weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Temperature {
    private String temp;
    private String feels_like;
}
