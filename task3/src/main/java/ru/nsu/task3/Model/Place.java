package ru.nsu.task3.Model;

import lombok.Getter;

public class Place {
    @Getter
    public final String name;
    @Getter
    public final String country;
    @Getter
    public final String city;
    @Getter
    public final String street;
    @Getter
    public final String num;
    @Getter
    public final String lat;
    @Getter
    public final String lng;

    public Place(String name, String country, String city, String street, String num, String lat, String lng) {
        this.name = name;
        this.country = country;
        this.city = city;
        this.street = street;
        this.num = num;
        this.lat = lat;
        this.lng = lng;
    }

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
        if (num != null) {
            output += String.format("""
                             House number: %s
                             """, num);
        }
        if (name != null) {
            output += String.format("""
                             Place name: %s
                             """, name);
        }
        return output;
    }
}
