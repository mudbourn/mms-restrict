package info.mudbourn.mmsrestrict;

import info.mudbourn.mmsrestrict.command.RestrictCommands;
import info.mudbourn.mmsrestrict.net.RestrictNetworking;
import info.mudbourn.mmsrestrict.obtain.Obtain;
import info.mudbourn.mmsrestrict.sweep.Sweep;
import info.mudbourn.mmsrestrict.use.UseGuard;
import info.mudbourn.mmsrestrict.worldgen.Worldgen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class MmsRestrict implements ModInitializer {

    @Override
    public void onInitialize() {
        RestrictApi.reloadConfig();
        Worldgen.register(RestrictApi.config().rules());
        Obtain.register();
        UseGuard.register();
        Sweep.register();
        RestrictNetworking.register();
        RestrictCommands.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            RestrictApi.refresh();
            StartupReport.log(server.registryAccess());
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RestrictApi.clear());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            RestrictApi.refresh();
            StartupReport.log(server.registryAccess());
            RestrictNetworking.broadcast(server);
        });
    }
}
