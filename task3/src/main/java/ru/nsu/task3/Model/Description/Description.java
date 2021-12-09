package ru.nsu.task3.Model.Description;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Description {
    private @JsonProperty("wikipedia_extracts") Info wikipediaExtracts;

    public String getTitle() {
        if (wikipediaExtracts != null) {
            return wikipediaExtracts.getTitle();
        } else {
            return null;
        }
    }

    public String getText() {
        if (wikipediaExtracts != null) {
            return wikipediaExtracts.getText();
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

