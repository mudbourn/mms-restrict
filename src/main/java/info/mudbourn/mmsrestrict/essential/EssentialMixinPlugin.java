package info.mudbourn.mmsrestrict.essential;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

// Applies the Essential mixins only when the restriction is active and gates the Leawind ones on its mod ids.
public class EssentialMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");
    private static final String LEAWIND_PREFIX = "info.mudbourn.mmsrestrict.mixin.essential.leawind.";
    // Leawind's Third Person compat is switched off for now; set true to bring the emote camera handoff back.
    public static final boolean LEAWIND_COMPAT = false;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!EssentialRestrict.isActive()) {
            return false;
        }
        if (mixinClassName.startsWith(LEAWIND_PREFIX)) {
            FabricLoader loader = FabricLoader.getInstance();
            return LEAWIND_COMPAT && loader.isModLoaded("leawind_third_person") && loader.isModLoaded("perspective_api");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        LOG.info("Essential restriction applied: {} -> {}", mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1), targetClassName);
    }
}
