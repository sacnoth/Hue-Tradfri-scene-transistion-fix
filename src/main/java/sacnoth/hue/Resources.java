package sacnoth.hue;

import lombok.experimental.Delegate;

import java.util.Collection;
import java.util.List;

public record Resources<T extends Identified>(
        @Delegate(excludes = CollectionExclusions.class)
        List<T> resources
) implements Collection<T> {
    @Override
    public boolean contains(Object obj) {
        if (Identified.class.isAssignableFrom(obj.getClass())) {
            Identified identifiedObj = (Identified) obj;

            return containsID(identifiedObj.id());
        } else if (String.class.isAssignableFrom(obj.getClass())) {
            String id = (String) obj;

            return containsID(id);
        } else {
            return false;
        }
    }

    public boolean containsID(String id) {
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

    private interface CollectionExclusions {
        boolean contains(Object obj);
    }
}
