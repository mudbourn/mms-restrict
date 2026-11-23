package info.mudbourn.mmsrestrict.client.jei;

import info.mudbourn.mmsrestrict.client.ClientRestricted;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RestrictJeiPlugin implements IModPlugin {

    private static volatile IIngredientManager ingredients;
    private static volatile List<ItemStack> removed = List.of();

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("mms_restrict", "hide");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        ingredients = runtime.getIngredientManager();
        removed = List.of();
        refresh();
    }

    @Override
    public void onRuntimeUnavailable() {
        ingredients = null;
        removed = List.of();
    }

    // Restores the previously hidden stacks, then hides the current set.
    public static void refresh() {
        if (ingredients == null) {
            return;
        }
        if (!removed.isEmpty()) {
            ingredients.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, removed);
        }
        List<ItemStack> next = new ArrayList<>();
        for (ItemStack stack : ingredients.getAllItemStacks()) {
            if (ClientRestricted.isHidden(stack)) {
                next.add(stack);
            }
        }
        removed = next;
        if (!next.isEmpty()) {
            ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, next);
        }
    }
}
