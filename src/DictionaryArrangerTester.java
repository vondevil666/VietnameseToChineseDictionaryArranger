import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dongz on 2017/6/13.
 */
public class DictionaryArrangerTester {
    private static String testFilePath = "arrangedtext1.txt";

    public static void main(String[] args) {
        DictionaryArranger da=new DictionaryArranger();
        DictionaryArrangerTester dat=new DictionaryArrangerTester();

        String testText=dat.getTestText(testFilePath);
        ArrayList<String[]> finalList=da.arrangeFullText(testText);
        ArrayList<String> needManualProcessList=da.needManualProcessList;
        ArrayList<String> sentenceWithEqualitySignList=da.sentenceWithEqualitySignList;

//        打印或输出可正确处理部分的内容，打印到控制台，输出到excel文件
//        dat.showFinalList(finalList);
//        new WriteIntoFile(finalList);

//        打印或输出待手动处理的部分，输出到txt文件
//        dat.showNeedManualProcessList(needManualProcessList);
        new WriteIntoFile(needManualProcessList);

//        打印或输出含等号的部分，输出到txt文件
//        dat.showSentenceWithEqualitySignList(sentenceWithEqualitySignList);

        System.out.println("Process Finished.");
    }

    private String getTestText(String testFilePath) {
        StringBuilder sb=new StringBuilder();
        try {
            File file = new File(testFilePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String tmp = "";
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp).append('\n');
            }
        } catch (IOException e) {
            System.out.println("Exception:未找到输入文件。");
        }
        return sb.toString();
    }

    private void showFinalList(ArrayList<String[]> finalList) {
        System.out.println("已录入数据数量为： "+finalList.size());
        for (String[] s : finalList) {
            System.out.println("");
            System.out.println(s[0]);
            System.out.println(s[1]);
            System.out.println(s[2]);
        }
    }

    private void showNeedManualProcessList(ArrayList<String> list) {
        System.out.println("需进一步手工处理的词条数为： "+list.size());
        for (String s : list) {
            System.out.println(s);
        }
    }

    private void showSentenceWithEqualitySignList(ArrayList<String> list) {
        System.out.println("含等号的词条数为： "+list.size());
        for (String s : list) {
            System.out.println(s);
        }
    }
}
