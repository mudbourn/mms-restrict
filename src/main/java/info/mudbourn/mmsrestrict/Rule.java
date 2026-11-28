package info.mudbourn.mmsrestrict;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

public final class Rule {
    private final String name;
    private final List<Match> matches;
    private final List<java.util.regex.Pattern> recipes;
    private final Set<Layer> layers;
    private final Set<Identifier> dimensions;
    private final String reason;
    private final SweepMode sweepMode;
    private final Map<Identifier, Refund> refunds;
    private final LongAdder[] counts = new LongAdder[Stat.values().length];

    public Rule(String name, List<Match> matches, List<java.util.regex.Pattern> recipes, Set<Layer> layers, Set<Identifier> dimensions, String reason, SweepMode sweepMode, Map<Identifier, Refund> refunds) {
        this.name = name;
        this.matches = List.copyOf(matches);
        this.recipes = List.copyOf(recipes);
        this.layers = Set.copyOf(layers);
        this.dimensions = Set.copyOf(dimensions);
        this.reason = reason;
        this.sweepMode = sweepMode;
        this.refunds = Map.copyOf(refunds);
        for (int i = 0; i < counts.length; i++) {
            counts[i] = new LongAdder();
        }
    }

    public String name() {
        return name;
    }

    public List<Match> matches() {
        return matches;
    }

    public Set<Layer> layers() {
        return layers;
    }

    public Set<Identifier> dimensions() {
        return dimensions;
    }

    public String reason() {
        return reason;
    }

    public SweepMode sweepMode() {
        return sweepMode;
    }

    public Map<Identifier, Refund> refunds() {
        return refunds;
    }

    public boolean has(Layer layer) {
        return layers.contains(layer);
    }

    public boolean hasItemLayer() {
        return layers.stream().anyMatch(Layer::isItemLayer);
    }

    public boolean matchesId(Identifier id) {
        return matches.stream().anyMatch(match -> match.matchesId(id));
    }

    public boolean hasRecipes() {
        return !recipes.isEmpty();
    }

    // Whether a recipe id is one this rule removes by id, whatever it makes.
    public boolean matchesRecipe(Identifier id) {
        String text = id.toString();
        return recipes.stream().anyMatch(pattern -> pattern.matcher(text).matches());
    }

    public boolean inDimension(Identifier dimension) {
        return dimensions.isEmpty() || dimensions.contains(dimension);
    }

    public void add(Stat stat, long amount) {
        counts[stat.ordinal()].add(amount);
    }

    public long count(Stat stat) {
        return counts[stat.ordinal()].sum();
    }

    public long total() {
        long sum = 0;
        for (LongAdder adder : counts) {
            sum += adder.sum();
        }
        return sum;
    }
}
