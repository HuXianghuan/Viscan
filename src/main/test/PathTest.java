
import com.viscan.ConfigManager;
import com.viscan.Main;
import com.viscan.Utils.WslPathConverter;
import com.viscan.validate.ConfigValidator;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTest {
    public static void main(String[] args) throws URISyntaxException {

        Path jarDir = Paths.get(PathTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();



        System.out.println(jarDir);



    }


}
