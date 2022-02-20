package sacnoth;

import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.HueBridge;
import io.github.zeroone3010.yahueapi.discovery.HueBridgeDiscoveryService;
import lombok.extern.log4j.Log4j2;
import sacnoth.hue.HueCommunication;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Collections.disjoint;

@Log4j2
public class HueTradfriFixer {
    private static final String HARDCODED_API_KEY = null;

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        String apiKey;
        if (HARDCODED_API_KEY != null) {
            apiKey = HARDCODED_API_KEY;
        } else if (args.length > 0 && args[0] != null) {
            apiKey = args[0];
        } else {
            throw new IllegalStateException("Requires an API key via the first command line parameter or hardcoded.");
        }

        Future<List<HueBridge>> bridgesFuture = new HueBridgeDiscoveryService()
                .discoverBridges(bridge -> log.info("Bridge found: " + bridge));
        List<HueBridge> bridges = bridgesFuture.get();

        if (bridges.isEmpty()) {
            log.error("Discovered no bridges");
            System.exit(1);
        }

        for (HueBridge bridge : bridges) {
            Hue hue = new Hue(bridge.getIp(), apiKey);
            log.info("Connected to bridge {}", bridge.getIp());

            List<String> tradfriLights = hue.getRaw()
                    .getLights()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().getManufacturername().contains("IKEA"))
                    .map(Entry::getKey)
                    .toList();

            log.info("Discovered TRADFRI lights: {}", tradfriLights);

            List<String> tradfriGroups = hue.getRaw()
                    .getGroups()
                    .entrySet()
                    .stream()
                    .filter(entry -> !disjoint(entry.getValue().getLights(), tradfriLights))
                    .map(Entry::getKey)
                    .toList();

            log.info("Groups containing TRADFRI lights: {}", tradfriGroups);

            List<String> tradfriScenes = hue.getRaw()
                    .getScenes()
                    .entrySet()
                    .stream()
                    .filter(entry -> tradfriGroups.contains(entry.getValue().getGroup()))
                    .map(Entry::getKey)
                    .toList();

            log.info("Scenes containing TRADFRI lights: {}", tradfriScenes);

            HueCommunication hueCommunication = new HueCommunication(bridge.getIp(), apiKey);

            for (String tradfriScene : tradfriScenes) {
                hueCommunication.fixScene(tradfriScene, tradfriLights);
            }
        }
    }
}
