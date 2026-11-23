package info.mudbourn.mmsrestrict.mixin.recipes;

import info.mudbourn.mmsrestrict.RestrictApi;
import info.mudbourn.mmsrestrict.obtain.RecipeFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    @Mutable
    @Final
    private RecipeMap recipes;

    @Shadow
    @Mutable
    @Final
    private Collection<RecipeHolder<?>> learnableRecipes;

    @Unique
    private HolderLookup.Provider mmsRestrict$provider;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mmsRestrict$captureProvider(HolderLookup.Provider provider, CallbackInfo ci) {
        this.mmsRestrict$provider = provider;
    }

    @Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
    private void mmsRestrict$dropRestrictedRecipes(FeatureFlagSet flags, CallbackInfo ci) {
        RestrictApi.refresh();
        this.recipes = RecipeFilter.filter(this.recipes, this.mmsRestrict$provider);
        this.learnableRecipes = RecipeFilter.filterLearnable(this.learnableRecipes, this.mmsRestrict$provider);
    }
}
