package dev.vality.daway.util;

import dev.vality.geck.serializer.kit.json.JsonHandler;
import dev.vality.geck.serializer.kit.tbase.TBaseProcessor;
import org.apache.thrift.TBase;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Objects;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new JsonMapper();

    public static String thriftBaseToJsonString(TBase thriftBase) {
        try {
            return new TBaseProcessor().process(thriftBase, new JsonHandler()).toString();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't convert to json string: " + thriftBase, e);
        }
    }

    public static JsonNode thriftBaseToJsonNode(TBase thriftBase) {
        try {
            var legacyJsonNode = new TBaseProcessor().process(thriftBase, new JsonHandler());
            return objectMapper.readTree(legacyJsonNode.toString());
        } catch (IOException | JacksonException e) {
            throw new RuntimeException("Couldn't convert to json node: " + thriftBase, e);
        }
    }

    public static String objectToJsonString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JacksonException e) {
            throw new RuntimeException("Couldn't convert object to json string: " + o, e);
        }
    }

    public static <T> T stringToObject(byte[] stringObject, Class<T> type) {
        try {
            return objectMapper.readValue(stringObject, type);
        } catch (JacksonException e) {
            throw new IllegalStateException("Couldn't convert json string to object: ", e);
        }
    }

    public static <T> byte[] toBytes(T object) {
        try {
            if (Objects.isNull(object)) {
                return new byte[0];
            }
            return objectMapper.writeValueAsBytes(object);
        } catch (JacksonException e) {
            throw new IllegalStateException("Couldn't convert object to byte array: ", e);
        }
    }


}
