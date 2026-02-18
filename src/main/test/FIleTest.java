import com.viscan.Utils.PathParts;
import com.viscan.Utils.PathUtils;

import java.io.IOException;

public class FIleTest {


    public static void main(String[] args) throws IOException {
//        String a = new String("D:\\Users/\\wshxh\\Desktop\\name");
//
//        Path json = Paths.get("D:\\Users\\wshxh\\Desktop\\test\\properties.json");
//
//        List<FileItem> list = List.of(
//                FileItem.fromWindowsPath("D:\\Users\\wshxh\\Desktop\\test\\aaa.txt"),
//                FileItem.fromWindowsPath("D:\\Users\\wshxh\\Desktop\\test\\bbb.txt"),
//                FileItem.fromWindowsPath("D:\\Users\\wshxh\\Desktop\\test\\ccc.txt"),
//                FileItem.fromWindowsPath("D:\\Users\\wshxh\\Desktop\\test\\ddd")
//                );
//
//
//        try {
//            FileItemStorage.save(list, json);
//        } catch (IOException e) {
//
//        }
//
//
//        List<FileItem> revivedList = FileItemStorage.load(json);
//
//        for (FileItem i : revivedList) {
//            System.out.println(i.getLinuxPath());
//        }

//        String res = PathUtils.linuxToWindowsAuto("/mnt/c/Users/wshxh");

        String a = "/mnt/e/viscanTest/fastp_out/ERR14818012_R1.clean.klassify.bowtie.fastq";
        String b = "/mnt/e/viscanTest/fastp_out/ERR14818012_R2.clean.classify.bowtie.fastq";
        PathParts pathPartsa = PathParts.parse(a);
        PathParts pathPartsb = PathParts.parse(b);


        System.out.println(PathUtils.extractCommonPairedFastqBase(pathPartsa.getNameParts().get(0), pathPartsb.getNameParts().get(0)));



//
//        List<String> lcs = PathUtils.longestCommonSubsequence(alist, blist);
//
//        System.out.println(lcs.size());
//
//        for (String tag: lcs
//             ) {
//            System.out.println(tag);
//        }

    }
}

