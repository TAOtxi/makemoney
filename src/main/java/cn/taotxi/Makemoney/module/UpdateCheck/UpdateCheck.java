package cn.taotxi.Makemoney.module.UpdateCheck;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.config.MakemoneyConfig;
import cn.taotxi.Makemoney.gui.UpdateNotifyScreen;
import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.util.VersionUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * 通过 Modrinth 来实现模组更新检查
 */
public class UpdateCheck {
    public static final String MODULE_NAME = "updateCheck";

    private static final String MODRINTH_PROJECT_ID = "L0g6pT9H";
    private static final String MODRINTH_API_URL =
            "https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT_ID + "/version";
    public static final String MODRINTH_PROJECT_URL = "https://modrinth.com/mod/" + MODRINTH_PROJECT_ID;
    public static final String GITHUB_REPO_URL = "https://github.com/TAOtxi/makemoney";
    public static final String GITHUB_RELEASE_URL = GITHUB_REPO_URL + "/releases";

    private static final String NOTIFY_TASK_ID = "updateNotifyWindow";
    private static final String NOTIFY_TIMEOUT_TASK_ID = "removeUpdateNotifyWindow";
    /** 检查主菜单是否就绪的周期 */
    private static final int NOTIFY_CHECK_INTERVAL = 5;
    /** 超过这个时间还没能弹窗（网络太慢 / 一直没进主菜单）就放弃本次通知 */
    private static final int NOTIFY_TIMEOUT = 20 * 120;

    private enum Status {
        PENDING,            // 请求还没结束
        FAILED,             // 请求失败，本次启动不通知
        NO_UPDATE,          // 已经是最新版
        UPDATE_AVAILABLE    // 有新版本
    }

    private static volatile Status status = Status.PENDING;
    private static volatile String latestVersion = "";
    private static volatile String latestVersionUrl = MODRINTH_PROJECT_URL;

    public static void initialize() {
        if (!MakemoneyConfig.getInstance().updateNotifyEnabled.getValue()) {
            return;
        }

        String currentVersion = getCurrentModVersion();
        if (currentVersion.isEmpty()) {
            Makemoney.LOGGER.warn("Can not resolve current mod version, skip update check.");
            return;
        }

        Makemoney.LOGGER.info("Update check enabled, current version: {}", currentVersion);
        startRequestThread(currentVersion);
        scheduleNotifyTask();
    }

    // 记录“不再通知”的版本号。下次启动时若查到的最新版本不比它更新，就不再弹窗。
    public static void ignoreVersion(String version) {
        MakemoneyConfig config = MakemoneyConfig.getInstance();
        config.updateNotifyIgnoredVersion.setValue(version);
        config.saveConfig();
        Makemoney.LOGGER.info("Update notification for version {} has been ignored.", version);
    }

    public static String getCurrentModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(Makemoney.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("");
    }

    private static String getMinecraftVersion() {
        return FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("");
    }

    /******************* 网络请求 *******************/

    private static void startRequestThread(String currentVersion) {
        Thread thread = new Thread(() -> requestLatestVersion(currentVersion), "makemoney-update-check");
        thread.setDaemon(true);
        thread.start();
    }

    private static void requestLatestVersion(String currentVersion) {
        try (HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {

            HttpRequest request = HttpRequest.newBuilder(URI.create(buildRequestUrl()))
                    // Modrinth 要求带上可识别的 User-Agent
                    .header("User-Agent", "TAOtxi/makemoney/" + currentVersion + " (github.com/TAOtxi/makemoney)")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                Makemoney.LOGGER.warn("Update check failed, unexpected status code: {}", response.statusCode());
                status = Status.FAILED;
                return;
            }

            parseResponse(response.body(), currentVersion);
        } catch (Exception e) {
            Makemoney.LOGGER.warn("Update check failed: {}", e.toString());
            status = Status.FAILED;
        }
    }

    private static String buildRequestUrl() {
        StringBuilder url = new StringBuilder(MODRINTH_API_URL)
                .append("?loaders=")
                .append(encode("[\"fabric\"]"));

        String minecraftVersion = getMinecraftVersion();
        if (!minecraftVersion.isEmpty()) {
            url.append("&game_versions=").append(encode("[\"" + minecraftVersion + "\"]"));
        }
        return url.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void parseResponse(String body, String currentVersion) {
        JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonArray()) {
            Makemoney.LOGGER.warn("Update check failed, unexpected response body.");
            status = Status.FAILED;
            return;
        }

        // 正式版用户只提示正式版，预发布版用户也提示预发布版
        boolean allowPreRelease = VersionUtil.isPreRelease(currentVersion);

        String bestVersion = null;
        String bestVersionId = "";

        for (JsonElement element : root.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject version = element.getAsJsonObject();

            String versionNumber = getString(version, "version_number");
            if (versionNumber.isEmpty()) {
                continue;
            }
            if (!allowPreRelease && !"release".equals(getString(version, "version_type"))) {
                continue;
            }
            if (bestVersion != null && VersionUtil.compare(versionNumber, bestVersion) <= 0) {
                continue;
            }

            bestVersion = versionNumber;
            bestVersionId = getString(version, "id");
        }

        if (bestVersion == null || VersionUtil.compare(bestVersion, currentVersion) <= 0) {
            Makemoney.LOGGER.info("Update check finished, already up to date.");
            status = Status.NO_UPDATE;
            return;
        }

        latestVersion = bestVersion;
        latestVersionUrl = bestVersionId.isEmpty()
                ? MODRINTH_PROJECT_URL
                : MODRINTH_PROJECT_URL + "/version/" + bestVersionId;
        status = Status.UPDATE_AVAILABLE;
        Makemoney.LOGGER.info("Update check finished, new version found: {}", bestVersion);
    }

    private static String getString(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return "";
        }
        return element.getAsString();
    }

    /******************* 弹窗 *******************/

    private static void scheduleNotifyTask() {
        TaskUtil.createTimeTask(NOTIFY_TASK_ID, UpdateCheck::tryNotify, NOTIFY_CHECK_INTERVAL);
        TaskUtil.createOnceTimeTask(NOTIFY_TIMEOUT_TASK_ID, () -> {
            if (TaskUtil.hasTimeTask(NOTIFY_TASK_ID)) {
                Makemoney.LOGGER.info("Update notification timed out, skip it this time.");
                TaskUtil.removeTimeTask(NOTIFY_TASK_ID);
            }
        }, NOTIFY_TIMEOUT);
    }

    private static void tryNotify() {
        Minecraft client = Minecraft.getInstance();
        Screen currentScreen = client.gui.screen();

        // 等主菜单出现
        if (!(currentScreen instanceof TitleScreen)) {
            return;
        }

        Status currentStatus = status;
        if (currentStatus == Status.PENDING) {
            return;
        }

        stopNotifyTask();

        if (currentStatus != Status.UPDATE_AVAILABLE) {
            return;
        }

        String newVersion = latestVersion;
        String ignoredVersion = MakemoneyConfig.getInstance().updateNotifyIgnoredVersion.getValue();
        if (!ignoredVersion.isEmpty() && VersionUtil.compare(ignoredVersion, newVersion) >= 0) {
            Makemoney.LOGGER.info("Version {} has been ignored, skip notification.", newVersion);
            return;
        }

        Makemoney.LOGGER.info("Showing update notification for version {}.", newVersion);
        client.gui.setScreen(new UpdateNotifyScreen(
                currentScreen,
                getCurrentModVersion(),
                newVersion,
                latestVersionUrl,
                GITHUB_RELEASE_URL
        ));
    }

    private static void stopNotifyTask() {
        TaskUtil.removeTimeTask(NOTIFY_TASK_ID);
        TaskUtil.removeTimeTask(NOTIFY_TIMEOUT_TASK_ID);
    }
}
