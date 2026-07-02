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

        long deltaTick = currentTick - lastTick;
        long deltaTime = System.nanoTime() - lastTime;

        float instanceMsTps = (float) deltaTime / (float) deltaTick / 1e6f;
        msTps = 0.99f * msTps + 0.01f * instanceMsTps;
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
