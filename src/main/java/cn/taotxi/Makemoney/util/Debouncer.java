package cn.taotxi.Makemoney.util;

import java.util.Timer;
import java.util.TimerTask;

public class Debouncer {
    private final long delay;
    private Timer timer;

    public Debouncer(long delay) {
        this.delay = delay;
    }

    public void debounce(Runnable action) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        }, delay);
    }
}
