package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.generated.maidartificearms.CompatConfig;
import cn.blockforge.generated.maidartificearms.MaidArtificeArms;

import java.util.HashMap;
import java.util.Map;

public final class DebugLog {

    private static final Map<String, Long> LAST = new HashMap<>();

    private DebugLog() {}

    public static boolean on() {
        return CompatConfig.DEBUG_LOG.get();
    }

    public static void info(String key, int intervalTicks, String fmt, Object... args) {
        if (!on()) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = LAST.get(key);
        if (last != null && now - last < (long) intervalTicks * 50L) {
            return;
        }
        LAST.put(key, now);
        if (LAST.size() > 512) {
            LAST.clear();
        }
        MaidArtificeArms.LOGGER.info("[IAA-Maid] " + fmt, args);
    }
}
