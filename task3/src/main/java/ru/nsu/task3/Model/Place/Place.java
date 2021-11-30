package ru.nsu.task3.Model.Place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place {
    private CopyOnWriteArrayList<Hits> hits;
}
