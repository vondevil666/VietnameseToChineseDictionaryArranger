
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by dongz on 2017/6/15.
 */
public class WriteIntoFile {

    public WriteIntoFile(ArrayList<String> stringList) {
        outputToTxt(stringList);
    }

//    public WriteIntoFile(ArrayList<String[]> arrayList) {
//        outputToExcel(arrayList);
//    }

    public void outputToExcel(ArrayList<String[]> arrayList){

        //准备工作
        Workbook wb=new XSSFWorkbook();
        Sheet sheet=wb.createSheet("Sheet");
        Row row;
        FileOutputStream fileOut;

        sheet.setColumnWidth(0,8000); //设定第1列的列宽
        sheet.setColumnWidth(1,8000);
        sheet.setColumnWidth(2,8000);
        row=sheet.createRow(0);
        row.createCell(0).setCellValue("越南语");
        row.createCell(1).setCellValue("词性/例句/短语");
        row.createCell(2).setCellValue("汉语");

        for(int i=0;i<arrayList.size();i++) {
            row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(arrayList.get(i)[0]);
            row.createCell(1).setCellValue(arrayList.get(i)[1]);
            row.createCell(2).setCellValue(arrayList.get(i)[2]);
        }

        try{
            File outputFile=new File("测试输出excel_正文_1.xlsx");
            fileOut=new FileOutputStream(outputFile);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void outputToTxt(ArrayList<String> stringList) {
        try {
            File file = new File("需手工处理列表_1.txt");
            StringBuilder sb = new StringBuilder();
            for (String s : stringList) {
                sb.append(s).append('\n');
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(sb.toString());
            bw.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
