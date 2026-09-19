package cn.taotxi.Makemoney.util.help;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

/**
 * 可翻页的指令帮助菜单。
 *
 * <p>翻页依赖聊天文本组件的 run_command 点击事件：点击后客户端会走
 * {@code sendUnattendedCommand}，该调用被 Fabric 的客户端指令 API 拦截，
 * 从而重新执行 {@code /<模块> help <页码>} 并输出对应页内容。
 *
 * <p>用法：
 * <pre>{@code
 * private static final HelpMenu HELP = HelpMenu.of("autodrop", "autodrop.help")
 *     .alias("ad")
 *     .entry("on", "autodrop.help.on")
 *     .build();
 * }</pre>
 */
public class HelpMenu {
    /**
     * 每页条目上限。首页还要占用页头、简介、别名与页脚共 4 行，
     * 取 6 可使首页正好落在默认聊天栏的 10 行内。
     */
    private static final int ENTRIES_PER_PAGE = 6;
    /** 页脚最多直接列出的页码数，超出则只显示当前页附近的页码 */
    private static final int MAX_PAGE_BUTTONS = 7;
    /**
     * 页头分割线。默认聊天栏可视宽度为 320px（{@code chatWidth=1.0} 时
     * {@code floor(1.0 * 280 + 40)}），而 ━ 属于 Unicode 字体页、单字约 9px，
     * 取 8 个可为标题与页码留出足够横向空间，避免页头折行。
     */
    private static final String DIVIDER = "━".repeat(8);

    private final String rootCommand;
    private final String helpKeyPrefix;
    private final List<String> aliases;
    private final List<HelpEntry> entries;

    private HelpMenu(Builder builder) {
        this.rootCommand = builder.rootCommand;
        this.helpKeyPrefix = builder.helpKeyPrefix;
        this.aliases = List.copyOf(builder.aliases);
        this.entries = List.copyOf(builder.entries);
    }

    public static Builder of(String rootCommand, String helpKeyPrefix) {
        return new Builder(rootCommand, helpKeyPrefix);
    }

    public static class Builder {
        private final String rootCommand;
        private final String helpKeyPrefix;
        private final List<String> aliases = new ArrayList<>();
        private final List<HelpEntry> entries = new ArrayList<>();

        private Builder(String rootCommand, String helpKeyPrefix) {
            this.rootCommand = rootCommand;
            this.helpKeyPrefix = helpKeyPrefix;
        }

        /** 登记该模块的指令别名，仅用于在帮助中展示 */
        public Builder alias(String... alias) {
            for (String a : alias) {
                this.aliases.add(a);
            }
            return this;
        }

        /** 点击填入输入框，适用于带参数或有副作用的指令 */
        public Builder entry(String usage, String descKey) {
            this.entries.add(HelpEntry.of(usage, descKey));
            return this;
        }

        /** 点击立即执行，仅用于打开界面、查看信息一类的安全指令 */
        public Builder runEntry(String usage, String descKey) {
            this.entries.add(HelpEntry.run(usage, descKey));
            return this;
        }

        /** 不可点击的说明行 */
        public Builder textEntry(String usage, String descKey) {
            this.entries.add(HelpEntry.text(usage, descKey));
            return this;
        }

        public HelpMenu build() {
            return new HelpMenu(this);
        }
    }

    public int getTotalPages() {
        if (entries.isEmpty()) return 1;
        return (entries.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;
    }

    /**
     * 第 page 页（从 1 开始）的起始条目下标。
     *
     * <p>条目在各页间均摊，避免出现「前几页塞满、末页只剩一条」的情况。
     * 例如 8 条分 2 页时为 4 + 4，而非 6 + 2。
     */
    private int pageStart(int page, int totalPages) {
        return (page - 1) * entries.size() / totalPages;
    }

    /** 把条目的 usage 展开为可执行的完整指令 */
    private String resolveCommand(HelpEntry entry) {
        if (entry.isAbsolute()) return entry.usage();
        if (entry.usage().isEmpty()) return "/" + rootCommand;
        return "/" + rootCommand + " " + entry.usage();
    }

    /**
     * 构建指定页的帮助文本。页码从 1 开始，越界时自动收敛到有效范围。
     */
    public Component build(int page) {
        int totalPages = getTotalPages();
        int currentPage = Mth.clamp(page, 1, totalPages);

        MutableComponent result = Component.empty();
        result.append(buildHeader(currentPage, totalPages));

        int from = pageStart(currentPage, totalPages);
        int to = pageStart(currentPage + 1, totalPages);
        for (int i = from; i < to; i++) {
            result.append(Component.literal("\n")).append(buildEntryLine(entries.get(i)));
        }

        if (totalPages > 1) {
            result.append(Component.literal("\n")).append(buildFooter(currentPage, totalPages));
        }
        return result;
    }

    /** 页头：分割线 + 模块名 + 页码，随后是模块功能简介与别名 */
    private Component buildHeader(int currentPage, int totalPages) {
        MutableComponent header = Component.empty();

        header.append(Component.literal(DIVIDER).withStyle(ChatFormatting.DARK_GRAY));
        header.append(Component.literal(" "));
        header.append(T.tl(helpKeyPrefix + ".title")
            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        if (totalPages > 1) {
            header.append(Component.literal(" "));
            header.append(Component.literal(currentPage + "/" + totalPages)
                .withStyle(ChatFormatting.DARK_GRAY));
        }
        header.append(Component.literal(" "));
        header.append(Component.literal(DIVIDER).withStyle(ChatFormatting.DARK_GRAY));

        // 功能简介只在第一页显示，避免翻页时重复占用行数
        if (currentPage == 1) {
            header.append(Component.literal("\n"));
            header.append(T.tl(helpKeyPrefix + ".desc").withStyle(ChatFormatting.GRAY));

            if (!aliases.isEmpty()) {
                header.append(Component.literal("\n"));
                header.append(T.tl("help.ui.alias").withStyle(ChatFormatting.DARK_GRAY));
                header.append(Component.literal(" "));
                for (int i = 0; i < aliases.size(); i++) {
                    if (i > 0) {
                        header.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
                    }
                    header.append(Component.literal("/" + aliases.get(i))
                        .withStyle(ChatFormatting.AQUA));
                }
            }
        }
        return header;
    }

    /** 单个条目：指令用法 + 描述，整行可点击 */
    private Component buildEntryLine(HelpEntry entry) {
        String command = resolveCommand(entry);

        MutableComponent line = Component.empty();
        line.append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY));
        line.append(Component.literal(command).withStyle(ChatFormatting.YELLOW));
        line.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY));
        line.append(T.tl(entry.descKey()).withStyle(ChatFormatting.GRAY));

        Style style = Style.EMPTY;
        switch (entry.clickMode()) {
            case SUGGEST -> style = style
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(
                    T.tl("help.ui.hover.suggest", Component.literal(command)
                        .withStyle(ChatFormatting.YELLOW))
                        .withStyle(ChatFormatting.GRAY)));
            case RUN -> style = style
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(
                    T.tl("help.ui.hover.run", Component.literal(command)
                        .withStyle(ChatFormatting.YELLOW))
                        .withStyle(ChatFormatting.GRAY)));
            case NONE -> { }
        }
        return line.withStyle(style);
    }

    /** 页脚：上一页 / 页码 / 下一页 */
    private Component buildFooter(int currentPage, int totalPages) {
        MutableComponent footer = Component.empty();

        footer.append(buildNavButton("help.ui.prev", currentPage - 1, currentPage > 1));
        footer.append(Component.literal(" "));

        int from = 1;
        int to = totalPages;
        if (totalPages > MAX_PAGE_BUTTONS) {
            int half = MAX_PAGE_BUTTONS / 2;
            from = Mth.clamp(currentPage - half, 1, totalPages - MAX_PAGE_BUTTONS + 1);
            to = from + MAX_PAGE_BUTTONS - 1;
        }
        for (int i = from; i <= to; i++) {
            if (i > from) {
                footer.append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY));
            }
            footer.append(buildPageButton(i, i == currentPage));
        }

        footer.append(Component.literal(" "));
        footer.append(buildNavButton("help.ui.next", currentPage + 1, currentPage < totalPages));
        return footer;
    }

    /** 上一页 / 下一页按钮，不可用时显示为暗灰且不可点击 */
    private Component buildNavButton(String labelKey, int targetPage, boolean enabled) {
        MutableComponent button = T.tl(labelKey);
        if (!enabled) {
            return button.withStyle(ChatFormatting.DARK_GRAY);
        }
        return button.withStyle(Style.EMPTY
            .withColor(ChatFormatting.GREEN)
            .withClickEvent(new ClickEvent.RunCommand(helpCommand(targetPage)))
            .withHoverEvent(new HoverEvent.ShowText(
                T.tl("help.ui.hover.page", targetPage).withStyle(ChatFormatting.GRAY))));
    }

    /** 单个页码按钮，当前页高亮且不可点击 */
    private Component buildPageButton(int page, boolean isCurrent) {
        if (isCurrent) {
            return Component.literal(String.valueOf(page))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        return Component.literal(String.valueOf(page)).withStyle(Style.EMPTY
            .withColor(ChatFormatting.GRAY)
            .withUnderlined(true)
            .withClickEvent(new ClickEvent.RunCommand(helpCommand(page)))
            .withHoverEvent(new HoverEvent.ShowText(
                T.tl("help.ui.hover.page", page).withStyle(ChatFormatting.GRAY))));
    }

    private String helpCommand(int page) {
        return "/" + rootCommand + " help " + page;
    }

    /** 直接输出指定页帮助 */
    public void show(FabricClientCommandSource source, int page) {
        source.sendFeedback(build(page));
    }

    /**
     * 构建 {@code help [页码]} 子指令节点，供模块挂到自己的指令树上。
     */
    public LiteralArgumentBuilder<FabricClientCommandSource> helpCommand() {
        return ClientCommands.literal("help")
            .executes(this::executeFirstPage)
            .then(ClientCommands.argument("page", IntegerArgumentType.integer(1))
                .executes(context -> {
                    show(context.getSource(), context.getArgument("page", Integer.class));
                    return 1;
                }));
    }

    /** 供 {@code .executes()} 直接引用，输出第一页 */
    public int executeFirstPage(CommandContext<FabricClientCommandSource> context) {
        show(context.getSource(), 1);
        return 1;
    }
}
