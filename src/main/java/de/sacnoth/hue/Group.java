package de.sacnoth.hue;

import lombok.experimental.Delegate;

public record Group(
        String id,
        @Delegate io.github.zeroone3010.yahueapi.domain.Group group
) implements Identified {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Group group = (Group) o;
        return id.equals(group.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
