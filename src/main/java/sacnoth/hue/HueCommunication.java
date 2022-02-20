package sacnoth.hue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import sacnoth.network.JSONCommunication;

import java.io.IOException;
import java.util.Collection;

@AllArgsConstructor
@Log4j2
public class HueCommunication {

    private final String bridgeIP;
    private final String apiKey;

    public void fixScene(String sceneID, Collection<String> lightsToFix) throws IOException {
        log.info("Fixing lights {} in scene {}", lightsToFix, sceneID);
        JsonNode lightstates = JSONCommunication.get(getUrl("scenes/" + sceneID))
                .get("lightstates");

        for (String lightToFix : lightsToFix) {
            ObjectNode lightstate = (ObjectNode) lightstates.get(lightToFix);
            lightstate.put("transitiontime", 0);

            writeLightstate(sceneID, lightToFix, lightstate);
        }
    }

    private void writeLightstate(String sceneID, String lightID, JsonNode payload) throws IOException {
        JSONCommunication.put(getUrl("scenes/" + sceneID + "/lightstates/" + lightID), payload);
    }

    private String getUrl(String resource) {
        return "https://" + bridgeIP + "/api/" + apiKey + "/" + resource;
    }
}
