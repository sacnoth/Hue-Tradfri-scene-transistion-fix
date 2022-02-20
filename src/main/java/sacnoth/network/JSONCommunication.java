package sacnoth.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JSONCommunication {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode get(String url) throws IOException {
        String answer = HTTPCommunication.get(url);

        return objectMapper.readTree(answer);
    }

    public static void put(String url, JsonNode payload) throws IOException {
        String serializedPayload = objectMapper.writeValueAsString(payload);

        String response = HTTPCommunication.put(url, serializedPayload);

        JsonNode parsedResponse = objectMapper.readTree(response);
        for (JsonNode arrayEntry : parsedResponse) {
            assert arrayEntry.has("success");
        }
    }
}
