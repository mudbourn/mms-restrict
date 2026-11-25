package info.mudbourn.mmsrestrict.client.essential;

import info.mudbourn.mmsrestrict.essential.EssentialRestrict;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

// Minecraft-side checks shared by the Essential mixins.
public final class EssentialGuard {

    private static final long OPENING_WINDOW_MS = 30_000L;

    private static volatile long openingUntil;

    private EssentialGuard() {
    }

    // Essential overlays (modals, dropdowns) are only wanted on allowed screens or while one is opening.
    public static boolean allowsOverlay() {
        if (System.currentTimeMillis() < openingUntil) {
            return true;
        }
        Screen screen = Minecraft.getInstance().gui.screen();
        return screen != null && EssentialRestrict.isAllowedEssentialScreen(screen.getClass().getName());
    }

    // Opens the overlay window for the modal flow Essential runs before an allowed screen.
    public static void openingAllowedScreen() {
        openingUntil = System.currentTimeMillis() + OPENING_WINDOW_MS;
    }

    public static void allowedScreenOpened() {
        openingUntil = 0L;
    }
}
