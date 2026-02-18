import com.viscan.ConfigManager;
import com.viscan.tools.BaseTool;
import com.viscan.tools.option.IntOption;
import com.viscan.tools.option.PositionOption;
import com.viscan.tools.option.ValueOption;

public class ToolTest {
    public static void main(String[] args) {
        BaseTool fastpTool = new BaseTool();
        fastpTool
                .addOption(new ValueOption("--hello", "you"))
                .addOption(new PositionOption(1, "lo"))
                .addOption(new IntOption("--day", 28))
                .addOption(new PositionOption(0, "hel"))
                .addOption(new PositionOption(99, "insertP"));

        System.out.println(fastpTool.buildCommand(ConfigManager.getConfig().getFastpExecutableDir()));
    }
}
