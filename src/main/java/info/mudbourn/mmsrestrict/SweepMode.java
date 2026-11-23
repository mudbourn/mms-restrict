package info.mudbourn.mmsrestrict;

import java.util.Locale;

public enum SweepMode {
    DELETE,
    LOG_ONLY;

    public static SweepMode parse(String name) {
        for (SweepMode mode : values()) {
            if (mode.name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) {
                return mode;
            }
        }
        return null;
    }
}
