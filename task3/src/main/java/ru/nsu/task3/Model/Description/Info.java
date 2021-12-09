package ru.nsu.task3.Model.Description;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Info {
    private String title;
    private String text;
}
