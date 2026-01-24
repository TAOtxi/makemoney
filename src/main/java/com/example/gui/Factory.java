package com.example.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.example.module.AutoRepair.AutoRepair;
import com.example.util.T;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class Factory {
    public static Option<Boolean> addToggleOption(
        Component name, 
        Component desc, 
        Boolean defaultValue,
        Supplier<Boolean> getter,
        Consumer<Boolean> setter
    ) {
        return Option.<Boolean>createBuilder()
                .name(name)
                .description(OptionDescription.of(desc))
                .binding(
                    defaultValue,
                    getter,
                    setter
                )
                .controller(opt -> BooleanControllerBuilder.create(opt)
                    .formatValue( val -> val? 
                        T.tl("message.value.on").withStyle(ChatFormatting.GREEN) : 
                        T.tl("message.value.off").withStyle(ChatFormatting.RED))
                    )
                .build();
    }
}
