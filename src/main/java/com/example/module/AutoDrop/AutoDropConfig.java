package com.example.module.AutoDrop;

import com.example.config.BaseConfig;

public class AutoDropConfig extends BaseConfig {
    public AutoDropConfig(String moduleName) {
        super(moduleName);
    }

    public boolean enabled = false;

    public static boolean getDefaultEnabled() {
        return false;
    }

    public class Item {
        public boolean enabled = false;
    }
}
