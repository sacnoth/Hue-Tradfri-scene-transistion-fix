package sacnoth.hue;

import io.github.zeroone3010.yahueapi.domain.LightDto;
import lombok.experimental.Delegate;

public record Light(
        String id,
        @Delegate LightDto light
) implements Identified {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Light light = (Light) o;
        return id.equals(light.id);
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
