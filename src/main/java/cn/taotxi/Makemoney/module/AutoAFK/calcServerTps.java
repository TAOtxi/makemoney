package cn.taotxi.Makemoney.module.AutoAFK;

import net.minecraft.client.Minecraft;

public class calcServerTps {
    private static final Minecraft client = Minecraft.getInstance();
    private static long lastTick = -1;
    private static long lastTime = -1;
    private static float msTps = 50f;

    public static void onSetTime(long currentTick) {
        if (lastTick == -1) {
            lastTick = currentTick;
            lastTime = System.nanoTime();
            return;
        }

        // if (msTps == Float.POSITIVE_INFINITY || msTps == Float.NEGATIVE_INFINITY) {
        //     msTps = 50f;
        //     return;
        // }

        long currentTime = System.nanoTime();
        long deltaTick = currentTick - lastTick;

        if (deltaTick == 0) {
            return;
        }

        long deltaTime = currentTime - lastTime;
        lastTime = currentTime;
        lastTick = currentTick;

        float instanceMsTps = (float) deltaTime / (float) deltaTick / 1e6f;
        msTps = 0.8f * msTps + 0.2f * instanceMsTps;
    }

    public static void reset() {
        lastTick = -1;
        lastTime = -1;
        msTps = 50f;
    }

    public static float getTps() {
        float msTps = getMsTps();

        if (msTps <= 50f) {
            return 20.0f;
        }
        return round2(1000 / msTps);
    }

    public static float getMsTps() {
        if (client.isSingleplayer()) {
            return client.getSingleplayerServer().getAverageTickTimeNanos() / 1e6f;
        }

        if (msTps <= 50.0f) {
            return 50.0f;
        }
        return round2(msTps);
    }

    private static float round2(float value) {
        return Math.round(value * 10) / 10f;
    }
}
