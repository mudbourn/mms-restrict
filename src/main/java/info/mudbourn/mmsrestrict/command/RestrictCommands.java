package info.mudbourn.mmsrestrict.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import info.mudbourn.mmsrestrict.Layer;
import info.mudbourn.mmsrestrict.RestrictApi;
import info.mudbourn.mmsrestrict.Rule;
import info.mudbourn.mmsrestrict.StartupReport;
import info.mudbourn.mmsrestrict.Stat;
import info.mudbourn.mmsrestrict.net.RestrictNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RestrictCommands {

    private RestrictCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> dispatcher.register(
            Commands.literal("mmsrestrict")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("reload").executes(context -> reload(context.getSource())))
                .then(Commands.literal("audit")
                    .executes(context -> audit(context.getSource(), null))
                    .then(Commands.argument("rule", StringArgumentType.greedyString())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                            RestrictApi.config().rules().stream().map(Rule::name).toList(),
                            builder
                        ))
                        .executes(context -> audit(context.getSource(), StringArgumentType.getString(context, "rule")))))
                .then(Commands.literal("check")
                    .then(Commands.argument("id", StringArgumentType.greedyString())
                        .executes(RestrictCommands::check)))
        ));
    }

    private static int reload(CommandSourceStack source) {
        try {
            RestrictApi.tryReloadConfig();
        } catch (IOException | RuntimeException e) {
            source.sendFailure(Component.literal("Config not reloaded, keeping the current one: " + e.getMessage()));
            return 0;
        }
        RestrictApi.refresh();
        StartupReport.log(source.getServer().registryAccess());
        RestrictNetworking.broadcast(source.getServer());
        source.sendSuccess(
            () -> Component.literal("Reloaded " + RestrictApi.config().rules().size() + " rules. Recipes follow /reload; worldgen rules need a restart."),
            true
        );
        return 1;
    }

    private static int audit(CommandSourceStack source, String filter) {
        List<Rule> rules = RestrictApi.config().rules().stream()
            .filter(rule -> filter == null || rule.name().equals(filter.trim()))
            .toList();
        if (rules.isEmpty()) {
            source.sendFailure(Component.literal("No rule named '" + filter + "'"));
            return 0;
        }
        for (Rule rule : rules) {
            List<String> parts = new ArrayList<>();
            for (Stat stat : Stat.values()) {
                if (rule.count(stat) > 0) {
                    parts.add(stat.label() + " " + rule.count(stat));
                }
            }
            String line = rule.name() + ": " + (parts.isEmpty() ? "NO HITS" : String.join(", ", parts));
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return rules.size();
    }

    private static int check(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String raw = StringArgumentType.getString(context, "id").trim();
        Identifier id = Identifier.tryParse(raw);
        if (id == null) {
            source.sendFailure(Component.literal("Invalid id '" + raw + "'"));
            return 0;
        }
        int found = 0;
        for (Rule rule : RestrictApi.config().rules()) {
            String layers = rule.layers().stream()
                .filter(layer -> layer.isItemLayer() ? RestrictApi.set().rules(layer, id).contains(rule) : rule.matchesId(id))
                .map(Layer::label)
                .sorted()
                .collect(Collectors.joining(", "));
            if (!layers.isEmpty()) {
                found++;
                String line = rule.name() + ": " + layers + " (" + rule.reason() + ")";
                source.sendSuccess(() -> Component.literal(line), false);
            }
        }
        if (found == 0) {
            source.sendSuccess(() -> Component.literal(id + " is not restricted"), false);
        }
        return found;
    }
}
