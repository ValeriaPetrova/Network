package ru.nsu.task3.Model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Place {
    private final String name;
    private final String country;
    private final String city;
    private final String street;
    private final String housenumber;
    private final String lat;
    private final String lng;

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
