package de.sacnoth.hue;

import lombok.experimental.Delegate;

public record Scene(
        String id,
        String groupName,
        @Delegate io.github.zeroone3010.yahueapi.domain.Scene scene
) implements Identified {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Scene scene = (Scene) o;
        return id.equals(scene.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return groupName() + "(" + getName() + ")";
    }
}
