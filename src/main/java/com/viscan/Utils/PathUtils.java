package com.viscan.Utils;

import com.viscan.ConfigManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {




    public static boolean isDir(Path path) {
        if (!exists(path)) {
            throw new IllegalArgumentException("invalid path");
        }
        return Files.isDirectory(path);
    }

    public static boolean isFile(Path path) {
        if (!exists(path)) {
            throw new IllegalArgumentException("invalid path");
        }
        return Files.isRegularFile(path);
    }


    public static boolean exists(Path path) {
        return path != null && Files.exists(path);
    }


    public static String getName(Path path) {
        return path.getFileName().toString();
    }



    public static String longestCommonSubstring(String s1, String s2) { //todo check
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        int maxLen = 0;
        int endIdx = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if (dp[i][j] > maxLen) {
                        maxLen = dp[i][j];
                        endIdx = i;
                    }
                } else {
                    dp[i][j] = 0;
                }
            }
        }
        return s1.substring(endIdx - maxLen, endIdx);
    }

    public static String extractCommonPairedFastqBase(String name1, String name2) {
        // 捕获 base 和可选的 paired-end 标识
        Pattern pattern = Pattern.compile( "^(.*?)(?:([._-](?:R)?[12]))?(?:_\\d+)?$");

        Matcher m1 = pattern.matcher(name1);
        Matcher m2 = pattern.matcher(name2);

        if (!m1.matches()) {
            throw new IllegalArgumentException("Invalid FASTQ name: " + name1);
        }
        if (!m2.matches()) {
            throw new IllegalArgumentException("Invalid FASTQ name: " + name2);
        }

        String base1 = m1.group(1);
        String base2 = m2.group(1);

        if (base1.isEmpty() || base2.isEmpty()) {
            throw new IllegalArgumentException("Empty base name after parsing");
        }

        String read1 = m1.group(2);
        String read2 = m2.group(2);

        // 如果 base 完全相同，直接返回
        if (base1.equals(base2)) {
            return base1;
        }

        // 如果 base 不同，但只差在 _1/_2/R1/R2 等末尾数字，尝试去掉末尾数字再比对
        String trimmed1 = base1.replaceAll("([._-]?)(R?)[12]$", "");
        String trimmed2 = base2.replaceAll("([._-]?)(R?)[12]$", "");

        if (trimmed1.equals(trimmed2)) {
            return trimmed1; // 去掉 read 标识后的共同 base
        }

        // 否则真正不同，报错
        throw new IllegalArgumentException(
                "Paired files do not share the same base name: " + name1 + " vs " + name2
        );
    }




    public static List<String> longestCommonSubsequence(List<String> a, List<String> b) {
        int m = a.size();
        int n = b.size();
        int[][] dp = new int[m + 1][n + 1];

        // 构建DP表
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.get(i - 1).equals(b.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // 回溯找到最长公共子序列
        List<String> lcs = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (a.get(i - 1).equals(b.get(j - 1))) {
                lcs.add(0, a.get(i - 1)); // 前插，保证顺序
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        return lcs;
    }






    public static String linuxJoin(String base, String... subs) {
        if (base == null) base = "";
        StringBuilder sb = new StringBuilder(base.endsWith("/") ? base.substring(0, base.length() - 1) : base);

        for (String sub : subs) {
            if (sb == null || sub.isEmpty()) continue;
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
                sb.append('/');
            }

            while (sub.startsWith("/")) sub = sub.substring(1);
            sb.append(sub);
        }

        return sb.toString();
    }



    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void deleteDirectoryWsl(String path) throws IOException, InterruptedException {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        String[] cmd = {ConfigManager.getConfig().getWslExecutable(), "rm", "-rf", path};

        Process process = Runtime.getRuntime().exec(cmd);

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            // read stderr
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                StringBuilder errorMsg = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorMsg.append(line).append(System.lineSeparator());
                }
                throw new IOException("Failed to delete directory in WSL. Exit code: " + exitCode +
                        "\nError output:\n" + errorMsg.toString());
            }

        }
    }







}
