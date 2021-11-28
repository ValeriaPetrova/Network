package ru.nsu.task3.Model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Weather {
    private final String temp;
    private final String tempFeelsLike;
    private final String speedWind;

    @Override
    public String toString() {
        return String.format("""
                Temperature: %s °C
                Feels like %s °C
                Wind speed: %s m/s
                """, temp, tempFeelsLike, speedWind);
    }
}
