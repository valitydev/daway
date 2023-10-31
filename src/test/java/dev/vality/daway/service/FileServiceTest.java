package dev.vality.daway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FileService.class})
class FileServiceTest {

    @Autowired
    FileService fileService;

    @Test
    void testFailGetClientRack() {
        String path = "/fail/path";

        String clientRack = fileService.getClientRack(path);

        assertNull(clientRack);

    }

    @Test
    void testGetClientRack() {
        String path = "src/test/resources/rack.txt";
        String clientRack = fileService.getClientRack(path);

        assertNotNull(clientRack);
        assertEquals("euc1-az1", clientRack);

    }
}