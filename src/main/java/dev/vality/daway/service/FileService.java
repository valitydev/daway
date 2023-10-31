package dev.vality.daway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
@Service
public class FileService {

    public static final String DELIMITER = "=";

    public String getClientRack(String path) {
        if (Objects.isNull(path)) {
            return null;
        }
        String rack = getProperty(path);
        log.debug("Get kafka rack: {} for consumer group", rack);
        return rack;
    }

    private String getProperty(String stringFilePath) {
        try {
            var path = Paths.get(stringFilePath);
            String content = Files.readString(path);
            return content.split(DELIMITER)[1];
        } catch (IOException e) {
            log.debug("Can't parse property from path: {}", stringFilePath, e);
            return null;
        }
    }


}
