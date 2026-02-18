import com.viscan.Utils.PathParts;
import com.viscan.Utils.PathUtils;
import com.viscan.Utils.WslPathConverter;

import java.nio.file.Path;

public class PathPartsTest {
    public static void main(String[] args) {
        Path p = Path.of("\\\\wsl.localhost\\Ubuntu\\boot");





        boolean flag = WslPathConverter.detectType(p) == WslPathConverter.WslPathType.UNC;
        System.out.println(flag);

    }
}
