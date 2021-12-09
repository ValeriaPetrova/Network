package ru.nsu.task3.Model.Weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {
    private Temperature main;
    private Wind wind;

    public String getTemp() {
        if (main != null) {
            return main.getTemp();
        } else {
            return null;
        }
    }

    public String getFeelsLike() {
        if (main != null) {
            return main.getFeelsLike();
        } else {
            return null;
        }
    }

    public String getWind() {
        if (wind != null) {
            return wind.getSpeed();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("""
                Temperature: %s °C
                Feels like: %s °C
                Wind speed: %s m/s
                """, this.getTemp(), this.getFeelsLike(), this.getWind());
    }
}
