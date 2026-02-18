import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.viscan.alert.AppAlert;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class TerminalTest extends Application {
    @Override
    public void start(Stage stage) {
        // 1. 创建 TerminalBuilder
        TerminalBuilder terminalBuilder = new TerminalBuilder();

        // 2. 创建一个终端 Tab
        TerminalTab terminalTab = terminalBuilder.newTerminal();

        // 3. 放进 TabPane
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(terminalTab);

        // 4. Scene & Stage
        Scene scene = new Scene(tabPane, 900, 600);
        stage.setTitle("TerminalFX Test");
        stage.setScene(scene);
        stage.show();

        boolean flag = AppAlert.confirm("test", "Hello");
        AppAlert.info("flag", String.valueOf(flag));

        // 5. 终端就绪后执行命令（关键）
        terminalTab.onTerminalFxReady(() -> {
            terminalTab.getTerminal().command("java -version\r");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
