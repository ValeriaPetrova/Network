package ru.nsu.task3.Model;

import lombok.Getter;

public class Weather {
    @Getter
    public String temp;
    @Getter
    public String tempFeelsLike;
    @Getter
    public String speedWind;

    public Weather(String temp, String  tempFeelsLike, String speedWind) {
        this.temp = temp;
        this.tempFeelsLike = tempFeelsLike;
        this.speedWind = speedWind;
    }

    @Override
    public String toString() {
        return String.format("""
                Temperature: %s °C
                Feels like %s °C
                Wind speed: %s m/s
                """, temp, tempFeelsLike, speedWind);
    }
}
