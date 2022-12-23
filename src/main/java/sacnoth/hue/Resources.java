package sacnoth.hue;

import lombok.experimental.Delegate;

import java.util.Set;
import java.util.stream.Collector;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toUnmodifiableSet;

public record Resources<T extends Identified>(
        @Delegate Set<T> resources
) implements Iterable<T> {
    public static <T extends Identified> Collector<T, Object, Resources<T>> toResources() {
        return collectingAndThen(toUnmodifiableSet(), Resources::new);
    }

    public Set<String> ids() {
        return resources.stream()
                .map(Identified::id)
                .collect(toUnmodifiableSet());
    }

    public boolean contains(String id) {
        return resources.stream()
                .anyMatch(resource -> resource.id().equals(id));
    }

    public T get(String id) {
        return resources.stream()
                .filter(resource -> resource.id().equals(id))
                .findAny()
                .orElseThrow();
    }

    @Override
    public String toString() {
        return resources.toString();
    }
}
