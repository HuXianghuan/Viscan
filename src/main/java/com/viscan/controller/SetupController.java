package com.viscan.controller;

import com.kodedu.terminalfx.Terminal;
import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.kodedu.terminalfx.config.TerminalConfig;
import com.viscan.ConfigManager;
import com.viscan.EnviromentManager;
import com.viscan.StatusIndicator;
import com.viscan.Utils.PathUtils;
import com.viscan.Utils.WslPathConverter;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.util.List;


public class SetupController {

    @FXML
    private HBox wslStatusContainer;

    @FXML
    private HBox fastpStatusContainer;
    @FXML
    private HBox bowtie2StatusContainer;
    @FXML
    private HBox kraken2StatusContainer3;
    @FXML
    private HBox recentrifugeStatusContainer4;

    @FXML
    private Tab thisTab;



    @FXML
    public void initialize() {

        refreshTools();

    }


    public static String checkWslStatus() {
        try {
            Process process = new ProcessBuilder("wsl.exe", "-e", "true")
                    .redirectErrorStream(true)
                    .start();

            int code = process.waitFor();

            if (code == 0) return "ok";
            else return "warning";

        } catch (Exception e) {
            return "error";
        }
    }

    public void checkWslAsync(HBox container) {
        StatusIndicator indicator = new StatusIndicator();
        Label message = new Label("Checking...");
        container.getChildren().setAll(indicator.getCircle(), message);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return checkWslStatus();
            }
        };

        task.setOnSucceeded(e -> {
            String status = task.getValue();
            switch (status) {
                case "ok" -> {
                    indicator.setStatus("ok");
                    message.setText("WSL available");
                }
                case "warning" -> {
                    indicator.setStatus("warning");
                    message.setText("WSL installed but not running");
                }
                case "error" -> {
                    indicator.setStatus("error");
                    message.setText("WSL not installed");
                }
            }
        });

        task.setOnFailed(e -> {
            indicator.setStatus("fail");
            message.setText("WSL check failed");
        });

        new Thread(task, "wsl-check").start();
    }


    public void checkToolAsync(String toolName, String toolDir, List<String> commands, HBox container) {
        StatusIndicator indicator = new StatusIndicator();
        Label message = new Label("Checking...");
        container.getChildren().setAll(indicator.getCircle(), message);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return checkToolStatus(toolDir, commands);
            }
        };

        task.setOnSucceeded(e -> {
            String status = task.getValue();
            switch (status) {
                case "ok" -> {
                    indicator.setStatus("ok");
                    message.setText(toolName + " available");
                }
                case "warning" -> {
                    indicator.setStatus("warning");
                    message.setText(toolName + " partially working");
                }
                case "error" -> {
                    indicator.setStatus("error");
                    message.setText(toolName + " not found or WSL unavailable");
                }
            }
        });

        task.setOnFailed(e -> {
            indicator.setStatus("error");
            message.setText(toolName + " check failed");
        });

        new Thread(task, toolName + "-check").start();
    }


    public static String checkToolStatus(String toolDir, List<String> commands) {

        if ("error".equals(checkWslStatus())) {
            return "error";
        }

        boolean anySuccess = false;
        boolean anyFailure = false;

        for (String cmd : commands) {
            if (runInWsl(toolDir, cmd)) {
                anySuccess = true;
            } else {
                anyFailure = true;
            }
        }

        if (anySuccess && anyFailure) return "warning";
        if (anySuccess) return "ok";
        return "error";
    }

    private static boolean runInWsl(String toolDir, String command) {
        try {
            String fullCmd = PathUtils.linuxJoin(toolDir, command);

            Process process = new ProcessBuilder(
                    "wsl.exe", "-e", "bash", "-lc", fullCmd
            ).redirectErrorStream(true).start();

            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }


    @FXML
    private void refreshTools() {
        checkWslAsync(wslStatusContainer);

        checkToolAsync("fastp", ConfigManager.getConfig().getFastpExecutableDir(),
                List.of("fastp --version"), fastpStatusContainer);

        checkToolAsync("bowtie2", ConfigManager.getConfig().getBowtie2ExecutableDir(),
                List.of("bowtie2 --version"), bowtie2StatusContainer);

        checkToolAsync("kraken2", ConfigManager.getConfig().getKraken2ExcutableDir(),
                List.of("k2 --version"), kraken2StatusContainer3);

        checkToolAsync("recentrifuge", ConfigManager.getConfig().getRecentrifugeExecutableDir(),
                List.of("rcf --version"), recentrifugeStatusContainer4);
    }


    @FXML
    private void installTools() {

        Path externalPath = EnviromentManager.getMntWorkDir().resolve("external");

        String installScript = WslPathConverter.toLinuxString(externalPath.resolve("install.sh"));
        String mamba = WslPathConverter.toLinuxString(externalPath.resolve("micromamba"));

        String execuatableEnv = WslPathConverter.toLinuxString(externalPath.resolve("tools_env").resolve("bin"));








        TabPane tabPane = thisTab.getTabPane();


        Path workDir = Path.of(ConfigManager.getConfig().getWorkDir());
        TerminalConfig config = new TerminalConfig();

        TerminalBuilder builder = new TerminalBuilder(config);
        builder.setTerminalPath(workDir);

        config.setWindowsTerminalStarter(ConfigManager.getConfig().getWslExecutable());

        TerminalTab terminalTab = builder.newTerminal();

        terminalTab.setText("micromamba");

        tabPane.getTabs().add(terminalTab);

        terminalTab.onTerminalFxReady(() -> {
            Terminal terminal = terminalTab.getTerminal();
            terminal.command(
                    "chmod +x \"" + installScript + "\" \\\r"
                    + "&& \\\r"
                    + "chmod +x \"" + mamba + "\" \\\r"
                    + "&& \\\r"
                    + "\"" + installScript + "\"\r"

            );


        });

        ConfigManager.getConfig().setFastpExecutableDir(execuatableEnv);
        ConfigManager.getConfig().setBowtie2ExecutableDir(execuatableEnv);
        ConfigManager.getConfig().setKraken2ExcutableDir(execuatableEnv);
        ConfigManager.getConfig().setRecentrifugeExecutableDir(execuatableEnv);
        ConfigManager.saveConfig();




    }

}
