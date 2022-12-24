package de.sacnoth.hue;

import lombok.experimental.Delegate;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collector;

import static java.util.Collections.disjoint;
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
        return ids().contains(id);
    }

    public boolean containsAny(Collection<String> searchIDs) {
        return !disjoint(ids(), searchIDs);
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
