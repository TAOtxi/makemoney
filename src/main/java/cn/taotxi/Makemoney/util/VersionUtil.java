package cn.taotxi.Makemoney.util;

/**
 * 语义化版本号工具。
 *
 * <p>本模组的版本号形如 {@code 2.7.6+26.2}，而发布到 Modrinth / GitHub 上的
 * 版本号来自 git tag，形如 {@code v2.7.6+26.2}，预发布版形如 {@code v2.6.0-beta.1+1.21.11}。
 * 因此比较之前需要先去掉 {@code v} 前缀和 {@code +} 之后的构建元数据（游戏版本）。
 */
public final class VersionUtil {
    private VersionUtil() {
    }

    // 归一化版本号
    public static String normalize(String version) {
        if (version == null) {
            return "";
        }
        String result = version.trim();
        if (result.startsWith("v") || result.startsWith("V")) {
            result = result.substring(1);
        }
        int plusIndex = result.indexOf('+');
        if (plusIndex >= 0) {
            result = result.substring(0, plusIndex);
        }
        return result.trim();
    }

    // 是否为预发布版本（归一化后带有 {@code -beta.1} 这样的后缀）
    public static boolean isPreRelease(String version) {
        return normalize(version).indexOf('-') >= 0;
    }

    // 比较两个版本号，{@code a} 更新返回正数，{@code b} 更新返回负数，相同返回 0
    public static int compare(String a, String b) {
        String normalizedA = normalize(a);
        String normalizedB = normalize(b);

        int result = compareCore(core(normalizedA), core(normalizedB));
        if (result != 0) {
            return result;
        }

        return comparePreRelease(preRelease(normalizedA), preRelease(normalizedB));
    }

    private static String core(String normalizedVersion) {
        int index = normalizedVersion.indexOf('-');
        return index < 0 ? normalizedVersion : normalizedVersion.substring(0, index);
    }

    private static String preRelease(String normalizedVersion) {
        int index = normalizedVersion.indexOf('-');
        return index < 0 ? "" : normalizedVersion.substring(index + 1);
    }

    private static int compareCore(String coreA, String coreB) {
        String[] partsA = coreA.isEmpty() ? new String[0] : coreA.split("\\.");
        String[] partsB = coreB.isEmpty() ? new String[0] : coreB.split("\\.");
        int length = Math.max(partsA.length, partsB.length);

        for (int i = 0; i < length; i++) {
            long numberA = i < partsA.length ? parseLeadingNumber(partsA[i]) : 0;
            long numberB = i < partsB.length ? parseLeadingNumber(partsB[i]) : 0;
            int result = Long.compare(numberA, numberB);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    // 预发布标识比较：没有预发布标识的版本更新（{@code 1.0.0 > 1.0.0-beta.1}）
    private static int comparePreRelease(String preReleaseA, String preReleaseB) {
        boolean emptyA = preReleaseA.isEmpty();
        boolean emptyB = preReleaseB.isEmpty();
        if (emptyA && emptyB) {
            return 0;
        }
        if (emptyA) {
            return 1;
        }
        if (emptyB) {
            return -1;
        }

        String[] partsA = preReleaseA.split("\\.");
        String[] partsB = preReleaseB.split("\\.");
        int length = Math.min(partsA.length, partsB.length);

        for (int i = 0; i < length; i++) {
            int result = comparePreReleaseIdentifier(partsA[i], partsB[i]);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(partsA.length, partsB.length);
    }

    private static int comparePreReleaseIdentifier(String identifierA, String identifierB) {
        boolean numericA = isNumeric(identifierA);
        boolean numericB = isNumeric(identifierB);

        if (numericA && numericB) {
            return Long.compare(Long.parseLong(identifierA), Long.parseLong(identifierB));
        }
        // 纯数字标识的优先级低于含字母的标识
        if (numericA) {
            return -1;
        }
        if (numericB) {
            return 1;
        }
        return identifierA.compareTo(identifierB);
    }

    private static boolean isNumeric(String value) {
        if (value.isEmpty() || value.length() > 18) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // 取字符串开头的连续数字，遇到非数字即停止，无数字则返回 0
    private static long parseLeadingNumber(String value) {
        int end = 0;
        while (end < value.length() && Character.isDigit(value.charAt(end)) && end < 18) {
            end++;
        }
        return end == 0 ? 0 : Long.parseLong(value.substring(0, end));
    }
}
