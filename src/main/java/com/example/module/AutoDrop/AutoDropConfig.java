package com.example.module.AutoDrop;

import java.util.ArrayList;
import java.util.List;

import com.example.config.BaseConfig;

public class AutoDropConfig extends BaseConfig {
    public AutoDropConfig(String moduleName) {
        super(moduleName);
    }

    public boolean enabled = getDefaultEnabled();
    public boolean newItemMode = getDefaultNewItemMode();
    public List<Integer> ingnoreSlots = getDefaultIngnoreSlot();
    public String throwDirection = getDefaultThrowDirection();
    public int checkInterval = getDefaultCheckInterval();

    public static boolean getDefaultEnabled() {
        return false;
    }

    public static boolean getDefaultNewItemMode() {
        return true;
    }

    public static List<Integer> getDefaultIngnoreSlot() {
        return new ArrayList<>();
    }

    public static String getDefaultThrowDirection() {
        return "down";
    }

    public static boolean getDefaultItemEnabled() {
        return true;
    }

    public static int getDefaultCheckInterval() {
        return 100;
    }

    public static String getDefaultName() {
        return "*";
    }

    public static String getDefaultID() {
        return "*";
    }

    public static String getDefaultTag() {
        return "*";
    }

    public class Item {
        public boolean enabled = getDefaultItemEnabled();
        public String name = getDefaultName();
        public String id = getDefaultID();
        public List<String> tags = new ArrayList<>();
        public List<Integer> applyEnchantments = new ArrayList<>();

    }
}
