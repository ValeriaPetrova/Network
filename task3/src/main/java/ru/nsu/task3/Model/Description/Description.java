package ru.nsu.task3.Model.Description;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Description {
    private Info wikipedia_extracts;

    public String getTitle() {
        if (wikipedia_extracts != null) {
            return wikipedia_extracts.getTitle();
        } else {
            return null;
        }
    }

    public String getText() {
        if (wikipedia_extracts != null) {
            return wikipedia_extracts.getText();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("""
                %s
                %s
                """, this.getTitle().split(":")[1], this.getText());
    }
}

