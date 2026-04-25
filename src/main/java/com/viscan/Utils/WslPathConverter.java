package com.viscan.Utils;

import com.viscan.ConfigManager;

import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public final class WslPathConverter {

    private WslPathConverter() {}

    // -------------------- 常量 --------------------
    private static final String WSL_UNC_PREFIX = "\\\\wsl.localhost\\";
    private static final Pattern WINDOWS_PATH = Pattern.compile("^[a-zA-Z]:\\\\.*");
    private static final Pattern MNT_PATH = Pattern.compile("^/mnt/[a-zA-Z]/.*");

    // -------------------- String -> Path --------------------
    public static Path stringToWindowsPath(String str) {
        if (str == null) return null;
        str = str.replace('/', '\\');
        return Path.of(str);
    }

    public static Path stringToMntPath(String str) {
        if (str == null) return null;
        str = str.replace('\\', '/');
        return Path.of(str);
    }

    public static Path stringToLinuxPath(String str) {
        if (str == null) return null;
        str = str.replace('\\', '/');
        return Path.of(str);
    }

    public static Path stringToUncPath(String str) {
        if (str == null) return null;
        return Path.of(str.replace('/', '\\'));
    }

    // 自动识别
    public static Path stringToPath(String str) {
        if (str == null) return null;
        str = str.trim();

        if (str.startsWith(WSL_UNC_PREFIX)) return stringToUncPath(str);
        if (MNT_PATH.matcher(str.replace('\\', '/')).matches()) return stringToMntPath(str);
        if (str.startsWith("/")) return stringToLinuxPath(str);
        if (WINDOWS_PATH.matcher(str.replace('/', '\\')).matches()) return stringToWindowsPath(str);

        throw new IllegalArgumentException("Unknown path format: " + str);
    }

    // -------------------- Path -> Path 转换 --------------------
    public static Path windowsToMnt(Path winPath) {
        if (winPath == null) return null;
        String p = winPath.toString();
        String drive = p.substring(0, 1).toLowerCase(Locale.ROOT);
        String rest = p.substring(2).replace('\\', '/');
        return Path.of("/mnt", drive, rest.startsWith("/") ? rest.substring(1) : rest);
    }

    public static Path mntToWindows(Path mntPath) {
        if (mntPath == null) return null;
        String p = mntPath.toString().replace('\\', '/');
        if (!p.startsWith("/mnt/") || p.length() < 6)
            throw new IllegalArgumentException("Not a valid /mnt path: " + p);
        String drive = p.substring(5, 6).toUpperCase(Locale.ROOT);
        String rest = p.substring(6).replace('/', '\\');
        return Path.of(drive + ":\\" + (rest.startsWith("\\") ? rest.substring(1) : rest));
    }

    public static Path linuxToUnc(Path linuxPath, String distro) {
        if (linuxPath == null) return null;
        if (distro == null || distro.isBlank())
            distro = ConfigManager.getConfig().getWslDistro();

        String p = linuxPath.toString().replace('/', '\\');
        return Path.of(WSL_UNC_PREFIX + distro + p);
    }

    public static Path linuxToUnc(Path linuxPath) {
        return linuxToUnc(linuxPath, ConfigManager.getConfig().getWslDistro());
    }

    public static Path uncToLinux(Path uncPath, String distro) {
        if (uncPath == null) return null;
        if (distro == null || distro.isBlank())
            distro = ConfigManager.getConfig().getWslDistro();

        String prefix = WSL_UNC_PREFIX + distro;
        String p = uncPath.toString().replace('\\', '/');
        if (!p.startsWith(prefix.replace('\\', '/')))
            throw new IllegalArgumentException("UNC path does not belong to distro " + distro);

        String rest = p.substring(prefix.length());
        return Path.of(rest.startsWith("/") ? rest : "/" + rest);
    }

    public static Path uncToLinux(Path uncPath) {
        return uncToLinux(uncPath, ConfigManager.getConfig().getWslDistro());
    }

    // -------------------- Path 拼接 --------------------
    public static Path linuxJoin(Path base, String... subs) {
        if (base == null) base = Path.of("/");
        Path res = base;
        for (String s : subs)
            if (s != null && !s.isEmpty()) res = res.resolve(s.replace('\\', '/'));
        return res;
    }

    public static Path windowsJoin(Path base, String... subs) {
        if (base == null) base = Path.of("C:\\");
        Path res = base;
        for (String s : subs)
            if (s != null && !s.isEmpty()) res = res.resolve(s.replace('/', '\\'));
        return res;
    }

    // -------------------- Path -> String --------------------
    public static String toLinuxString(Path path) {
        if (path == null) return null;
        return path.toString().replace('\\', '/');
    }

    public static String toWindowsString(Path path) {
        if (path == null) return null;
        return path.toString().replace('/', '\\');
    }

    public static String toUncString(Path path) {
        if (path == null) return null;
        return path.toString().replace('/', '\\');
    }

    public static String windowsToLinuxString(Path winPath) {
        return toLinuxString(windowsToMnt(winPath));
    }

    public static String autoToLinuxString(String str) {
        if (str == null) return null;
        str = str.trim();

        // ---- UNC ----
        if (str.startsWith(WSL_UNC_PREFIX)) {
            Path linux = uncToLinux(stringToUncPath(str));
            return toLinuxString(linux);
        }

        // ---- /mnt/ 驱动器路径 ----
        String unixLike = str.replace('\\', '/');
        if (MNT_PATH.matcher(unixLike).matches()) {
            return unixLike;
        }

        // ---- Linux 本地路径 ----
        if (unixLike.startsWith("/")) {
            return unixLike;
        }

        // ---- Windows 路径 ----
        if (WINDOWS_PATH.matcher(str.replace('/', '\\')).matches()) {
            Path winPath = stringToWindowsPath(str);
            Path mntPath = windowsToMnt(winPath);
            return toLinuxString(mntPath);
        }

        throw new IllegalArgumentException("Unknown path format: " + str);
    }


    public enum WslPathType {
        WINDOWS,
        MNT,
        LINUX,
        UNC,
        UNKNOWN
    }

    public static WslPathType detectType(String str) {
        if (str == null) return WslPathType.UNKNOWN;

        if (isUncPath(str)) return WslPathType.UNC;
        if (isMntPath(str)) return WslPathType.MNT;
        if (isLinuxPath(str)) return WslPathType.LINUX;
        if (isWindowsPath(str)) return WslPathType.WINDOWS;

        return WslPathType.UNKNOWN;
    }
    public static boolean isWindowsPath(String str) {
        if (str == null) return false;
        return WINDOWS_PATH.matcher(str.replace('/', '\\')).matches();
    }

    public static boolean isMntPath(String str) {
        if (str == null) return false;
        return MNT_PATH.matcher(str.replace('\\', '/')).matches();
    }
    public static boolean isLinuxPath(String str) {
        if (str == null) return false;
        String unix = str.replace('\\', '/');

        // /mnt/x/xxx 不算
        if (MNT_PATH.matcher(unix).matches()) return false;

        return unix.startsWith("/");
    }
    public static boolean isUncPath(String str) {
        if (str == null) return false;
        return str.startsWith(WSL_UNC_PREFIX);
    }

    public static WslPathType detectType(Path path) {
        if (path == null) return WslPathType.UNKNOWN;
        return detectType(path.toString());
    }
}
