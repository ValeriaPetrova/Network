package ru.nsu.task3;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public class Parser {

    @SneakyThrows
    public static <T> T parse(String request, Class<T> tClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(request, tClass);
    }

}
