package com.viscan.controller;

import com.kodedu.terminalfx.Terminal;
import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.kodedu.terminalfx.config.TerminalConfig;
import com.viscan.ConfigManager;
import com.viscan.EnviromentManager;
import com.viscan.Utils.PathUtils;
import com.viscan.Utils.WslPathConverter;
import com.viscan.alert.AppAlert;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Path;

public class InstallController {

    @FXML
    private TextField scriptLocationField;
    @FXML
    private TextField envLocationField;
    @FXML
    private TextField pythonVerField;
    @FXML
    private TextField packagesField;
    @FXML
    private TextField channelsField;
    @FXML
    private RadioButton rewriteShebangRadio;
    @FXML
    private Tab thisTab;

    @FXML
    private void initialize() {
        Path workDirPath = EnviromentManager.getMntWorkDir();
        Path externalPath = workDirPath.resolve("external");
        Path installScriptPath = externalPath.resolve("install.sh");
        Path installEnvPath = externalPath.resolve("tools_env");

        String installScriptLinux = WslPathConverter.toLinuxString(installScriptPath);
        String installEnvLinux = WslPathConverter.toLinuxString(installEnvPath);

        scriptLocationField.setText(installScriptLinux);
        envLocationField.setText(installEnvLinux);

        pythonVerField.setText("3.12");

        packagesField.setText("fastp=1.0.1,bowtie2=2.5.4,kraken2=2.17.1,recentrifuge=2.0.0");

        channelsField.setText("conda-forge,bioconda");


    }

    @FXML
    private void installTools() {
        String cmd = String.join(" ",
                quote(scriptLocationField.getText()),
                quote(envLocationField.getText()),
                quote(pythonVerField.getText()),
                quote(packagesField.getText()),
                quote(channelsField.getText()),
                quote((rewriteShebangRadio.isSelected()) ? "yes" : "no")
        );


        System.out.println(cmd);

        Path workDirPath = EnviromentManager.getMntWorkDir();
        Path externalPath = workDirPath.resolve("external");

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
                    cmd+"\r"
            );

            tabPane.getSelectionModel().select(terminalTab);

        });

        ConfigManager.getConfig().setFastpExecutableDir(execuatableEnv);
        ConfigManager.getConfig().setBowtie2ExecutableDir(execuatableEnv);
        ConfigManager.getConfig().setKraken2ExcutableDir(execuatableEnv);
        ConfigManager.getConfig().setRecentrifugeExecutableDir(execuatableEnv);
        ConfigManager.saveConfig();



    }
    @FXML
    private void deleteEnv() throws IOException {
        // 1. Build absolute Windows path to the environment directory
        Path envPath = EnviromentManager.getWinWorkDir()
                .resolve("external")
                .resolve("tools_env")
                .toAbsolutePath()
                .normalize();

        // 2. Ensure the path is indeed a Windows-style path
        if (WslPathConverter.detectType(envPath) != WslPathConverter.WslPathType.WINDOWS) {
            throw new IllegalStateException(
                    "Expected a Windows path, but got: " + envPath
            );
        }

        // 3. Convert Windows path → WSL /mnt path
        Path mntPath = WslPathConverter.windowsToMnt(envPath);
        String mntEnv = WslPathConverter.toLinuxString(mntPath);

        // 4. Ask for confirmation, showing BOTH Windows and WSL paths
        boolean confirm = AppAlert.confirm(
                "Remove Environment",
                "You are about to permanently delete the tool environment.\n\n" +
                        "Windows path:\n" +
                        "  " + envPath + "\n\n" +
                        "WSL path (/mnt):\n" +
                        "  " + mntEnv + "\n\n" +
                        "This action is irreversible.\n" +
                        "Are you absolutely sure?"
        );

        if (!confirm) return;

        // 5. Execute deletion via WSL
        try {
            PathUtils.deleteDirectoryWsl(mntEnv);
        } catch (InterruptedException e) {
            AppAlert.error("Failed to delete the environment.\n\n", e.getMessage());
            throw new RuntimeException(e);
        }

        // 6. Success notification
        AppAlert.info(
                "Environment Removed",
                "The tool environment has been successfully deleted:\n\n" +
                        envPath + "\n\n" +
                        "(WSL path: " + mntEnv + ")"
        );
    }



    protected static String quote(String text) {
        return "\"" + text + "\"";
    }
}
