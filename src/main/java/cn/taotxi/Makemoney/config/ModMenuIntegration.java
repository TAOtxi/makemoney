package cn.taotxi.Makemoney.config;

import com.terraformersmc.modmenu.api.ModMenuApi;

import cn.taotxi.Makemoney.gui.ConfigScreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::getConfigScreen;
    }
}
