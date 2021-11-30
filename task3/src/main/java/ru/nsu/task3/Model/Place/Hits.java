package ru.nsu.task3.Model.Place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hits {
    private Point point;
    private String name;
    private String country;
    private String city;
    private String street;
    private String housenumber;

    @Override
    public String toString() {
        String output = new String("");
        if (country != null) {
            output += String.format("""
                             Country: %s
                             """, country);
        }
        if (city != null) {
            output += String.format("""
                             City: %s
                             """, city);
        }
        if (street != null) {
            output += String.format("""
                             Street: %s
                             """, street);
        }
        if (housenumber != null) {
            output += String.format("""
                             House number: %s
                             """, housenumber);
        }
        if (name != null) {
            output += String.format("""
                             Place name: %s
                             """, name);
        }
        return output;
    }
}
