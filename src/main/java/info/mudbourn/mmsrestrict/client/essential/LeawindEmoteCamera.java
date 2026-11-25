package info.mudbourn.mmsrestrict.client.essential;

import io.github.leawind.perspectiveapi.api.PerspectiveAPI;
import io.github.leawind.perspectiveapi.api.PerspectiveOverrideRegistration;
import io.github.leawind.thirdperson.ThirdPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

// Hands the emote camera to Leawind's Third Person through Perspective API instead of changing the vanilla camera type.
public final class LeawindEmoteCamera {

    private static final Logger LOG = LoggerFactory.getLogger("mms_restrict");
    private static final int PRIORITY = 500;
    private static final long MAX_NANOS = TimeUnit.MINUTES.toNanos(10);
    private static final int FIRST_PERSON = 0;

    private static PerspectiveOverrideRegistration registration;
    private static long expiresAt;
    private static boolean failed;

    private LeawindEmoteCamera() {
    }

    // True when Leawind now owns the emote camera; false leaves Essential to set it as usual.
    public static synchronized boolean begin(int cameraType) {
        end();
        if (cameraType == FIRST_PERSON || failed) {
            return false;
        }
        try {
            registration = PerspectiveAPI.getOverrideChain().register(PRIORITY, () -> ThirdPerson.PERSPECTIVE_ID);
            expiresAt = System.nanoTime() + MAX_NANOS;
            return true;
        } catch (Throwable error) {
            failed = true;
            LOG.warn("Leawind camera override unavailable, emotes use Essential's own camera", error);
            return false;
        }
    }

    // True when an override was active and is now released.
    public static synchronized boolean end() {
        if (registration == null) {
            return false;
        }
        try {
            registration.unregister();
        } catch (Throwable error) {
            LOG.warn("Could not release the Leawind camera override", error);
        }
        registration = null;
        return true;
    }

    public static synchronized boolean active() {
        return registration != null;
    }

    public static synchronized void tick(boolean inWorld) {
        if (registration != null && (!inWorld || System.nanoTime() > expiresAt)) {
            end();
        }
    }
}
