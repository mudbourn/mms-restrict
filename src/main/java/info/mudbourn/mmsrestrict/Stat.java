package info.mudbourn.mmsrestrict;

public enum Stat {
    RECIPES("recipes removed"),
    LOOT("loot stacks filtered"),
    TRADES("trades removed"),
    MOB_GEAR("mob gear blocked"),
    FEATURES("features denied"),
    SPAWNS("spawns denied"),
    STRUCTURES("placement attempts denied"),
    SWEPT("items swept"),
    REFUNDS("refunds given"),
    USES("uses blocked");

    private final String label;

    Stat(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
