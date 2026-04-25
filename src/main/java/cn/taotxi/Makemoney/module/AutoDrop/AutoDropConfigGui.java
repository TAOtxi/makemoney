package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.List;
import java.util.stream.Collectors;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.util.InventoryUtil;
import cn.taotxi.Makemoney.util.StringUtil;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

public class AutoDropConfigGui {
    public static ConfigCategory.Builder createAutoDropCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autodrop.name"))
                .tooltip(T.tl("autodrop.desc"));


        // TODO: 重新打开后，延迟计时器重新计时
        category.option(Factory.addToggleOption(
            T.tl("module.enabled"),
            T.tl("module.enabled.desc"),
            AutoDropConfig.getDefaultEnabled(),
            () -> AutoDrop.config.enabled,
            val -> AutoDrop.config.enabled = val
        ));

        category.option(Option.<Integer>createBuilder()
                .name(T.tl("autodrop.launchDelay"))
                .description(OptionDescription.of(T.tl("autodrop.launchDelay.desc")))
                .binding(
                    AutoDropConfig.getDefaultLaunchDelay(),
                    () -> AutoDrop.config.launchDelay,
                    val -> AutoDrop.config.launchDelay = Math.clamp(val, 0, 100000)
                )
                .controller(IntegerFieldControllerBuilder::create)
                .build()
        );

        category.option(Factory.addToggleOption(
            T.tl("autodrop.showAttentionMsg"),
            T.tl("autodrop.showAttentionMsg.desc"),
            AutoDropConfig.getDefaultShowAttentionMsg(),
            () -> AutoDrop.config.showAttentionMsg,
            val -> AutoDrop.config.showAttentionMsg = val
        ));

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.ignoreCurrentSlot"))
                .description(OptionDescription.of(T.tl("autodrop.ignoreCurrentSlot.desc")))
                .action((yaclScreen, button) -> {   
                    /**
                     * TODO: 待修Bug：重置ignoreSlots之后，需要点两次此按钮才能生效。
                     * 原因：
                     *  重置ignoreSlots时，ignoreSlots被置空的任务添加到待做任务队列中，当
                     *  点击该按钮时，首先会将一些槽位添加进配置对象中，随后reload时，会调用保存函数，
                     *  也就是将执行待做任务队列中的任务，但此时的待做任务是将ignoreSlots置空的任务，
                     *  因此ignoreSlots被覆盖掉了，也就是置空，唉。没想到有啥优雅的解决办法。
                     **/
                    if (Minecraft.getInstance().player == null) return;
                    
                    List<Integer> slots = InventoryUtil.getInventoryNotEmptySlots();
                    AutoDrop.config.ingnoreSlots.addAll(slots);

                    // 去重
                    AutoDrop.config.ingnoreSlots = 
                        AutoDrop.config.ingnoreSlots.stream()
                           .distinct()
                           .collect(Collectors.toList());

                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent);
                })
                .build()
        );

        category.option(Option.<String>createBuilder()
                .name(T.tl("autodrop.ignoreSlots"))
                .description(OptionDescription.createBuilder()
                    .text(T.tl("autodrop.ignoreSlots.desc"))
                    // TODO: 图片大小待优化或找其它替代方式
                    .webpImage(Identifier.fromNamespaceAndPath(Makemoney.MOD_ID, "images/slot_example.webp"))
                    .build())
                .binding(
                    AutoDropConfig.getDefaultIngnoreSlot(),
                    () -> StringUtil.listToStr(AutoDrop.config.ingnoreSlots),
                    val -> AutoDrop.config.ingnoreSlots = StringUtil.strToIntList(val)
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        // TODO: 玩家自定义更精确的丢弃朝向
        category.option(Option.<String>createBuilder()
                .name(T.tl("autodrop.throwDirection"))
                .description(OptionDescription.of(T.tl("autodrop.throwDirection.desc")))
                .binding(
                    AutoDropConfig.getDefaultThrowDirection(),
                    () -> AutoDrop.config.throwDirection,
                    val -> AutoDrop.config.throwDirection = val
                )
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .values(AutoDropConfig.getAllThrowDirections().stream().map(Enum::name).collect(Collectors.toList())))
                .build()
        );

        category.option(Option.<Integer>createBuilder()
                .name(T.tl("autodrop.checkInterval"))
                .description(OptionDescription.of(T.tl("autodrop.checkInterval.desc")))
                .binding(
                    AutoDropConfig.getDefaultCheckInterval(),
                    () -> AutoDrop.config.checkInterval,
                    val -> AutoDrop.config.checkInterval = Math.clamp(val, 1, 10000)
                )
                .controller(IntegerFieldControllerBuilder::create)
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addBlock").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.addItems();
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent);
                })
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addPresetBlock").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addPresetBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.addPresetItems();
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent);
                })
                .build()
        );

        for (int i = 0; i < AutoDrop.config.items.size(); i++) {
            AutoDropConfig.Item item = AutoDrop.config.items.get(i);
            OptionGroup.Builder whiteListGroup = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.block.name", i+1))
                    .description(OptionDescription.of(T.tl("autodrop.block.name.desc", i+1)));
            

            whiteListGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.block.remove").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autodrop.block.remove.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.removeItem(item);
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent);
                })
                .build()
            );

             whiteListGroup.option(Factory.addToggleOption(
                T.tl("autodrop.block.enabled"),
                T.tl("autodrop.block.enabled.desc"),
                AutoDropConfig.Item.getDefaultItemEnabled(),
                () -> item.enabled,
                val -> item.enabled = val
            ));

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.block.itemName"))
                    .description(OptionDescription.of(T.tl("autodrop.block.itemName.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultName(),
                        () -> item.name,
                        val -> item.name = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.block.itemID"))
                    .description(OptionDescription.of(T.tl("autodrop.block.itemID.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultID(),
                        () -> item.id,
                        val -> item.id = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.block.itemTags"))
                    .description(OptionDescription.of(T.tl("autodrop.block.itemTags.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultTag(),
                        () -> StringUtil.listToStr(item.tags),
                        val -> item.saveTags(val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

             whiteListGroup.option(Option.<Integer>createBuilder()
                    .name(T.tl("autodrop.block.minEnchantRequir"))
                    .description(OptionDescription.of(T.tl("autodrop.block.minEnchantRequir.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultMinEnchantRequir(),
                        () -> item.minEnchantRequir,
                        val -> item.minEnchantRequir = val
                    )
                    .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .min(0)
                        .max(100))
                    .build()
            );

            category.group(whiteListGroup.build());
            category.group(ListOption.<String>createBuilder()
                    .name(T.tl("autodrop.block.enchantment.name"))
                    .description(OptionDescription.of(T.tl("autodrop.block.enchantment.name.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultEnchantments(),
                        () -> item.getEnchantList(),
                        val -> item.saveEnchantList(val)
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }
        return category;
    }

}
