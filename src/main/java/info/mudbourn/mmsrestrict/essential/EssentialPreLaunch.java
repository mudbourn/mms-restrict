package info.mudbourn.mmsrestrict.essential;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixins;

// Registers the Essential mixin config once Essential's own loader has put its classes on the classpath.
public class EssentialPreLaunch implements PreLaunchEntrypoint {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");

    @Override
    public void onPreLaunch() {
        if (!FabricLoader.getInstance().isModLoaded(EssentialRestrict.MOD_ID)) {
            return;
        }
        if (!EssentialRestrict.isActive()) {
            LOG.info("Essential is installed but essential.enabled is off, leaving it untouched");
            return;
        }
        Mixins.addConfiguration(EssentialRestrict.MIXIN_CONFIG);
        LOG.info("Essential restrictions on, allowing {}", EssentialRestrict.section().allow());
    }
}
