package ru.nsu.task3.Model.Weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Temperature {
    private String temp;
    private @JsonProperty("feels_like") String feelsLike;
}
