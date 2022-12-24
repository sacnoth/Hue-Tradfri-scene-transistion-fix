package de.sacnoth.hue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.sacnoth.network.JSONCommunication;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@AllArgsConstructor
@Log4j2
public class HueCommunication {

    private final String bridgeIP;
    private final String apiKey;

    public void fixScene(Scene scene, Resources<Light> tradfriLights) throws IOException {
        Resources<Light> lightsToFix = tradfriLights.stream()
                .filter(light -> scene.getLights().contains(light.id()))
                .collect(Resources.toResources());

        log.info("Fixing lights {} in scene {}", lightsToFix, scene);
        JsonNode lightstates = JSONCommunication.get(getUrl("scenes/" + scene.id()))
                .get("lightstates");

        for (Light lightToFix : lightsToFix) {
            log.debug("Fixing light {} in scene {}", lightToFix, scene);
            ObjectNode lightstate = (ObjectNode) lightstates.get(lightToFix.id());
            lightstate.put("transitiontime", 0);

            writeLightstate(scene.id(), lightToFix.id(), lightstate);
        }
    }

    private void writeLightstate(String sceneID, String lightID, JsonNode payload) throws IOException {
        JSONCommunication.put(getUrl("scenes/" + sceneID + "/lightstates/" + lightID), payload);
    }

    private String getUrl(String resource) {
        return "https://" + bridgeIP + "/api/" + apiKey + "/" + resource;
    }
}
