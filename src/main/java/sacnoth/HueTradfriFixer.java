package sacnoth;

import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.HueBridge;
import io.github.zeroone3010.yahueapi.discovery.HueBridgeDiscoveryService;
import lombok.extern.log4j.Log4j2;
import sacnoth.hue.Group;
import sacnoth.hue.HueCommunication;
import sacnoth.hue.Light;
import sacnoth.hue.Resources;
import sacnoth.hue.Scene;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static java.util.Collections.disjoint;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toUnmodifiableList;

@Log4j2
public class HueTradfriFixer {
    private static final String HARDCODED_API_KEY = null;
    private static final String HARDCODED_IP = null;

    public static void main(String[] args) throws IOException {

        final String apiKey;
        if (HARDCODED_API_KEY != null) {
            apiKey = HARDCODED_API_KEY;
        } else if (args.length > 0 && args[0] != null) {
            apiKey = args[0];
        } else {
            throw new IllegalStateException("Requires an API key via the first command line parameter or hardcoded.");
        }

        final String bridgeIp;
        if (HARDCODED_IP != null) {
            bridgeIp = HARDCODED_IP;
        } else if (args.length > 1 && args[1] != null) {
            bridgeIp = args[1];
        } else {
            bridgeIp = null;
        }

        Future<List<HueBridge>> bridgesFuture = new HueBridgeDiscoveryService()
                .discoverBridges(bridge -> log.info("Bridge found: " + bridge));
        List<HueBridge> autoDetectedBridges;
        try {
            autoDetectedBridges = bridgesFuture.get();
        } catch (Exception ex) {
            log.warn("Unable to auto detect Hue bridges!", ex);
            autoDetectedBridges = emptyList();
        }

        final List<HueBridge> bridges;
        if (bridgeIp == null) {
            bridges = autoDetectedBridges;
        } else {
            log.info("Adding hardcoded IP {} to the list of Hue brdiges", bridgeIp);
            bridges = Stream.concat(
                    Stream.of(new HueBridge(bridgeIp)),
                    autoDetectedBridges.stream().filter(bridge -> !bridge.getIp().equals(bridgeIp))
            ).toList();
        }

        if (bridges.isEmpty()) {
            log.error("Discovered no bridges");
            System.exit(1);
        }

        for (HueBridge bridge : bridges) {
            Hue hue = new Hue(bridge.getIp(), apiKey);
            log.info("Connected to bridge {}", bridge.getIp());

            Resources<Light> tradfriLights = hue.getRaw()
                    .getLights()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().getManufacturername().contains("IKEA"))
                    .map(light -> new Light(light.getKey(), light.getValue()))
                    .collect(collectingAndThen(toUnmodifiableList(), Resources::new));

            log.info("Discovered TRÅDFRI lights: {}", tradfriLights);

            Resources<Group> tradfriGroups = hue.getRaw()
                    .getGroups()
                    .entrySet()
                    .stream()
                    .filter(entry -> !disjoint(entry.getValue().getLights(), tradfriLights))
                    .map(group -> new Group(group.getKey(), group.getValue()))
                    .collect(collectingAndThen(toUnmodifiableList(), Resources::new));

            log.info("Groups containing TRÅDFRI lights: {}", tradfriGroups);

            Resources<Scene> tradfriScenes = hue.getRaw()
                    .getScenes()
                    .entrySet()
                    .stream()
                    .filter(entry -> tradfriGroups.containsID(entry.getValue().getGroup()))
                    .map(scene -> new Scene(scene.getKey(), tradfriGroups.get(scene.getValue().getGroup()).getName(), scene.getValue()))
                    .collect(collectingAndThen(toUnmodifiableList(), Resources::new));

            log.info("Scenes containing TRÅDFRI lights: {}", tradfriScenes);

            HueCommunication hueCommunication = new HueCommunication(bridge.getIp(), apiKey);

            for (Scene tradfriScene : tradfriScenes) {
                hueCommunication.fixScene(tradfriScene, tradfriLights);
            }
        }
    }
}
