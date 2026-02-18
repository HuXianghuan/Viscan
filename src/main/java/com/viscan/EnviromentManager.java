package com.viscan;

import com.viscan.Utils.WslPathConverter;

import java.nio.file.Path;

public class EnviromentManager {


    public static Path getMntWorkDir() {
        Path workDir = Path.of(System.getProperty("user.dir"));
        Path mntAppDir = WslPathConverter.windowsToMnt(workDir);

        return mntAppDir;
    }

    public static Path getWinWorkDir() {
        Path workDir = Path.of(System.getProperty("user.dir"));
        return workDir;
    }

    public static String getInstallScript() {
        Path scriptPath = getMntWorkDir().resolve("external").resolve("install.sh");

        return WslPathConverter.toLinuxString(scriptPath);
    }





    public static String getExternalToolsBin() {
        try {
            // 1. 使用“程序启动目录”，不是 classpath
            Path appDir = Path.of(System.getProperty("user.dir"));

            // 2. 外部工具必须放在 jar 外
            Path binDir = appDir
                    .resolve("viscan_env")
                    .resolve("bin");

            if (!java.nio.file.Files.isDirectory(binDir)) {
                throw new IllegalStateException(
                        "External tools directory not found: " + binDir
                );
            }

            // 3. 转为 WSL 路径
            Path wslBinDir = WslPathConverter.windowsToMnt(binDir);
            return WslPathConverter.toLinuxString(wslBinDir);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to resolve external tools bin directory", e
            );
        }
    }
}
