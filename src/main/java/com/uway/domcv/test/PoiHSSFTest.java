package com.uway.domcv.test;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * @author liruyin
 * @create 2019-06-22 0:08
 */
public class PoiHSSFTest {
    public static void main(String[] args) {
//        readTExcdl("");
        readExcel("E:/测试excel文档.xlsx");

    }
    public static  void  readTExcdl(String path){
        POIFSFileSystem fs= null;
        HSSFWorkbook wb = null;
        try {
            fs = new POIFSFileSystem(new FileInputStream("E:/测试excel文档.xls"));
            wb = new HSSFWorkbook(fs);
            // wb = WorkbookFactory.create(fs);
            //得到Excel工作簿对象

        } catch (IOException e) {
            e.printStackTrace();
        }

        //得到Excel工作表对象
        HSSFSheet sheet = wb.getSheetAt(0);
        System.out.println(sheet.getSheetName());
        //得到Excel工作表的行
        HSSFRow row = sheet.getRow(1);
        //得到Excel工作表指定行的单元格
        HSSFCell cell = row.getCell((short) 0);
        System.out.println(cell.toString());
        HSSFCellStyle cellStyle = cell.getCellStyle();
        System.out.println(cellStyle.toString() +"\t"+cellStyle.getDataFormatString());
        //得到单元格样式
    }
    public static void readExcel(String inputPath) {
        InputStream is = null;
        File excelFile = null;
        try {
            excelFile = new File(inputPath);
            //输入流
            is = new FileInputStream(excelFile);
            /*使用import org.apache.poi.ss.usermodel.*包，同
              时支持两种格式的读取*/
            //使用Workbook可以读取2003/2007/2010的excel文件
            Workbook workbook = WorkbookFactory.create(is);
            Iterator<Sheet> sheets = workbook.sheetIterator();
            //迭代遍历sheet
            while (sheets.hasNext()) {
                Sheet sheet = sheets.next();
                Iterator<Row> rows = sheet.rowIterator();
                //迭代遍历每行
                while (rows.hasNext()) {
                    Row row = rows.next();
                    Iterator<Cell> cells = row.cellIterator();
                    //迭代遍历每个单元格
                    while (cells.hasNext()) {
                        //用object接收每个单元格的值再打印出来
                        Object cellValue = null;
                        Cell cell = cells.next();
                        //获取单元格内容的枚举类型，分别进行处理
                        CellType cellType =    cell.getCellTypeEnum();
                        switch (cellType) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                cellValue = cell.getNumericCellValue();
                                break;
                            case BLANK:
                                cellValue = "空白单元格";
                                break;
                            case BOOLEAN:
                                cellValue = cell.getBooleanCellValue();
                                break;
                            case ERROR:
                                cellValue = "这是错误";
                                break;
                            case FORMULA:
                                cellValue = "这是公式";
                                break;
                            default:
                                cellValue = "未知错误";
                                break;
                        }
                        System.out.print(cellValue + "\t");
                    }
                    System.out.println();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(is!=null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
