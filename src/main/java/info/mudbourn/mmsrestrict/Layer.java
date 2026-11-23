package info.mudbourn.mmsrestrict;

import java.util.Locale;

public enum Layer {
    OBTAIN,
    USE,
    SWEEP,
    HIDE,
    WORLDGEN;

    public static Layer parse(String name) {
        for (Layer layer : values()) {
            if (layer.label().equals(name.toLowerCase(Locale.ROOT))) {
                return layer;
            }
        }
        return null;
    }

    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isItemLayer() {
        return this != WORLDGEN;
    }
}
