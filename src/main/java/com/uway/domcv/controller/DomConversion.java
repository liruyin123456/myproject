package com.uway.domcv.controller;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uway.domcv.entities.ColumnInfo;
import com.uway.domcv.entities.ParserParm;


/**
 * 文件转换控制类
 * @author liruyin
 * @create 2019-06-23 14:42
 */
@Controller
public class DomConversion {
    @RequestMapping("/docParser")
    public  String indexView(String path){
        return "index";
    }
    /**
     * 用来保存所有的表名和字段信息             表名			字段信息实体
     */
    private static LinkedHashMap<String,ArrayList<ColumnInfo>> allTableInfo=new LinkedHashMap<String, ArrayList<ColumnInfo>>();
    /**
     * 保存该表的其它信息
     */
    private static HashMap<String, ArrayList<String>> ColumnTypeInfo = new HashMap<String,ArrayList<String>>();

    public static void main(String[] args) {
        ParserParm parserParm =ParserParm.builder().build();
        //parserParm.setInputPath("F:/LTE中兴参数采集需求_V2.0_20190605.xlsx");
//        parserParm.setInputPath("C:/Users/liruyin/Desktop/NR爱立信性能采集需求_V1.1_20190731.xls");
        parserParm.setInputPath("D:/SVNwarehouse/svnDoc/2.1 数据支撑/2.1.3 CDMA网/2.1.3.1 性能数据/华为/设计文档/2 采集设计/"
        		+ "华为厂家性能采集需求表2019-8-1.xlsx");
        //不包头
        parserParm.setStartRowNum(7385);
        //包尾
        parserParm.setEndRowNum(7741);
        //parserParm.setStartRowNum(9415);
      	//parserParm.setEndRowNum(10357);
        parserParm.setSheetName("性能采集需求表_文件型");
        //下标0开始					表名、字段名 、类型、主键、是否允许为空、注释、厂家源字段
        parserParm.setReadColumns(new int[]{11,12,13,7,8,10,4});
        if(null == parserParm.getTablespaceName()){
        	parserParm.setTablespaceName("IGP");
        }
        readExcel(parserParm);
        System.out.println(allTableInfo);
        try {
            //outDataAlterTable();
            outDataCreateTable(parserParm);
            System.out.println(allTableInfo.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //readExcel(parserParm);
        //parserXmlDom(parserParm);
        //parserXmlDomForParse(parserParm);
        //parseDom4j(new File("c:/Users/liruyin/Desktop/江苏电信-增加 GRID_ID_20，GRID_ID_100 两个字段/lte_mreo_xml_export.xml"));
        //parseDom4j(new File("C:/Users/liruyin/Desktop/dianxin_cdma_hw_pm_xml_export.xml"));
    }

    private static void outDataAlterTable() throws IOException {
        OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream("E:/outAlter.sql") );
        //根据表名和字段生成sql
        for (Map.Entry<String, ArrayList<ColumnInfo>> tableInfo : allTableInfo.entrySet()) {
            ArrayList<ColumnInfo> tableclumns = tableInfo.getValue();
            StringBuffer sql =new StringBuffer("alter table  "+tableInfo.getKey()+" add (\n");
            StringBuffer commentSql =new StringBuffer("");
            for (ColumnInfo columnInfo : tableclumns) {
                sql.append(columnInfo.getColumnName()+"\t")
                        .append(columnInfo.getColumnType())
                        .append(columnInfo.isAllowNull()?" ":"  NOT NULL")
                        .append(",\n");
               // System.out.println("表信息----"+ columnInfo);
                if(!StringUtils.isEmpty(columnInfo.getComment())){
                    commentSql.append("COMMENT ON COLUMN ")
                            .append(columnInfo.getTableName()+"."+columnInfo.getColumnName())
                            .append(" IS \'").append(columnInfo.getComment())
                            .append("\';").append("\t\n");
                }

            }
            sql.replace(sql.length()-2,sql.length()," ");
            sql.append(" );\n");
            System.out.println(sql.toString());
            System.out.println(commentSql.toString());
            if(tableclumns == null||tableclumns.size() == 0 || StringUtils.isEmpty(tableclumns.get(0).getColumnName())){
            	continue;
            }
            osw.write(sql.toString());
            osw.write(commentSql.toString());
        }
        osw.flush();
        osw.close();
        //往表里面添加字段
    }

    
    private static void outDataCreateTable(ParserParm parserParm) throws IOException {
        OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream("E:/outAdd.sql") );
        //根据表名和字段生成sql
        for (Map.Entry<String, ArrayList<ColumnInfo>> tableInfo : allTableInfo.entrySet()) {
            ArrayList<ColumnInfo> tableclumns = tableInfo.getValue();
            //建表sql
            StringBuffer sql =new StringBuffer("create table   "+tableInfo.getKey()+"(\n");
            //注释sql
            StringBuffer commentSql =new StringBuffer("");
            //索引sql
            String unName ="";
            if(tableInfo.getKey().length()>24){
            	unName =tableInfo.getKey().substring(0, 24);
            }else{
            	unName =tableInfo.getKey();
            }
            StringBuffer unSql=new StringBuffer("create unique index UN_"+unName+"_N1 on "+tableInfo.getKey()+" (");
            for (ColumnInfo columnInfo : tableclumns) {
            	if(StringUtils.isEmpty(columnInfo.getColumnName())){
            		continue;
            	}
                sql.append(columnInfo.getColumnName()+"\t")
                        .append(columnInfo.getColumnType())
                        .append(columnInfo.isAllowNull()?" ":" NOT NULL")
                        .append(",\n");
                
               // 如果注释内容不为空才添加注释
                if(!StringUtils.isEmpty(columnInfo.getComment())){
                	commentSql.append("COMMENT ON column ")
                		.append(columnInfo.getTableName())
                		.append(".").append(
                				columnInfo.getColumnName())
                		.append(" IS \'").append(columnInfo.getComment())
                		.append("\';\t\n");
                }
                //添加索引
                if(columnInfo.isUniqueKey()){
                	unSql.append(columnInfo.getColumnName() +" ,");
                }
                
                
            }
            sql.replace(sql.length()-2,sql.length()," ");
            sql.append(" )\n")
            //添加分区
            .append("partition by range (STAMPTIME)"+
            "("+
  "partition PART_2022012123 values less than (TO_DATE(' 2022-01-21 23:00:00', 'SYYYY-MM-DD HH24:MI:SS', 'NLS_CALENDAR=GREGORIAN'))\n"+
    "tablespace  " + parserParm.getTablespaceName()+"\n"+
    "pctfree 10 \n"+
    "initrans 1 \n"+
    "maxtrans 255 \n"+
    "storage \n"+
    "("+
      "initial 8M \n"+
      "next 1M \n"+
      "minextents 1 \n"+
      "maxextents unlimited \n"+
    ")"+
");\n");
       //unsql后处理
       unSql.replace(unSql.length()-1,unSql.length()," ");
       
       unSql.append(")").append("tablespace  "+parserParm.getTablespaceName()+" \n").append("pctfree 10 \n")
       .append("initrans 2 \t").append("maxtrans 255  storage  \n")
       .append("(  initial 80K \n").append("next 1M \n").append("minextents 1 \n").
       append("maxextents unlimited );\n");
            System.out.println(sql.toString());
            System.out.println(commentSql.toString());
            
            if(tableclumns == null||tableclumns.size() == 0 || StringUtils.isEmpty(tableclumns.get(0).getColumnName())){
            	continue;
            }
            osw.write(sql.toString());
            osw.write(unSql.toString());
            //osw.flush();
            osw.write(commentSql.toString());
        }
        osw.flush();
        osw.close();
        //往表里面添加字段
    }
    /**
     * /excel/export
     * @return
     */
    @ResponseBody
    @RequestMapping("/excel/export")
    public static String parserXmlDom(ParserParm parserParm ){
        System.out.println("_____________________-"+parserParm);
        createDom4j(new File("E:/tempexport.xml"));

        return  "success";
    }
    
    /**
     * /excel/export
     * @return
     */
    @ResponseBody
    @RequestMapping("/excel/export")
    public static String parserXmlDomForParse(ParserParm parserParm ){
        System.out.println("_____________________-"+parserParm);
        //createDom4j(new File("E:/tempParser.xml"));
        File file =new File("E:/tempParser.xml");
        try{
            // 创建Document
            Document document = DocumentHelper.createDocument();

            // 添加根节点
            Element root = document.addElement("templets");

            if(allTableInfo.isEmpty()){
                return "无法生成文件";
            };
            int id=1;
            int dataType=4001;
            int idOfferSet=0;
            Iterator<String> keyIterator = allTableInfo.keySet().iterator();
            while (keyIterator.hasNext()){

                String tableName= keyIterator.next();

                Element exportElement= root.addElement("templet").
                        addAttribute("id",String.valueOf(id+idOfferSet))
                        .addAttribute("elementType", tableName).
                                addAttribute("dataType",String.valueOf(dataType+idOfferSet));
                //xportElement.addElement("table").addAttribute("value",tableName);

                Element columnsElement=exportElement.addElement("fields");

                ArrayList<ColumnInfo> tableColumns = allTableInfo.get(tableName);
                Iterator<ColumnInfo> columnIterator = tableColumns.iterator();
                while (columnIterator.hasNext()){
                	ColumnInfo next = columnIterator.next();
                    String columnName = next.getColumnName();
                    String sourceColumn = next.getSourceField();
                    columnsElement.addElement("field").
                            addAttribute("name",sourceColumn).
                            addAttribute("index",columnName.toUpperCase());
                }
                idOfferSet++;

            }
            // 在根节点下添加第一个子节点
          /*  Element oneChildElement= root.addElement("person").
                    addAttribute("attr", "root noe")
                    .addAttribute("","");

            oneChildElement.addElement("people")
                    .addAttribute("attr", "child one")
                    .addText("person one child one");
            oneChildElement.addElement("people")
                    .addAttribute("attr", "child two")
                    .addText("person one child two");

            // 在根节点下添加第一个子节点
            Element twoChildElement= root.addElement("person").addAttribute("attr", "root two");

            twoChildElement.addElement("people")
                    .addAttribute("attr", "child one")
                    .addText("person two child one");
            twoChildElement.addElement("people")
                    .addAttribute("attr", "child two")
                    .addText("person two child two");*/

            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter( new FileOutputStream(file), format);
            writer.write(document);

            System.out.println("dom4j CreateDom4j success!");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  "success";
    }

    public static void readExcel(ParserParm parserParm) {

        InputStream is = null;
        File excelFile = null;
        try {
            excelFile = new File(parserParm.getInputPath());
            //输入流
            is = new FileInputStream(excelFile);
            /*使用import org.apache.poi.ss.usermodel.*包，同
              时支持两种格式的读取*/
            //使用Workbook可以读取2003/2007/2010的excel文件
            Workbook workbook = WorkbookFactory.create(is);
            // Sheet workbookSheet = workbook.getSheet("Sheet1");

            Iterator<Sheet> sheets = workbook.sheetIterator();
            Sheet sheet = workbook.getSheet(parserParm.getSheetName());
            //迭代遍历sheet
           // while (sheets.hasNext()) {
                //Sheet sheet = sheets.next();

                Iterator<Row> rows = sheet.rowIterator();
                //迭代遍历每行
                String tableName =null;
                List<ColumnInfo> comlumns =new ArrayList<ColumnInfo>();

                while (rows.hasNext()) {
                    ArrayList<ColumnInfo> newComlumns =null;
                    Row row = rows.next();
                    //大于开始行小于结束行   不在范围内下一个
                    if(row.getRowNum()<parserParm.getStartRowNum() ||row.getRowNum()>parserParm.getEndRowNum()){
                        continue;
                    }
                    //当前的表名
                    String currentTableName="";
                    boolean isAdd =false;
                    int[] readColumns=parserParm.getReadColumns();
                    //记录没行的字段信息
                    ColumnInfo columnInfo=new ColumnInfo();
                    for (int i = 0; i <readColumns.length ; i++) {

                        Cell cell = row.getCell(readColumns[i]);

                        if(cell==null){
                            continue;
                        }
                        Object cellValue = null;
                        //获取单元格内容的枚举类型，分别进行处理
                        CellType cellType =    cell.getCellTypeEnum();
                        switch (cellType) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                
                                if("".equals(cellValue)) {
                                	continue;
                                }
                                
                                break;
                            case NUMERIC:
                                cellValue = cell.getNumericCellValue();
                                break;
                            case BLANK:
                            	//空白单元格
                                cellValue = "";
                                break;
                            case BOOLEAN:
                                cellValue = cell.getBooleanCellValue();
                                break;
                            case ERROR:
                            	//这是错误
                                cellValue = "";
                                break;
                            case FORMULA:
                            	//这是公式
                                cellValue = "";
                                break;
                            default:
                            	//未知错误
                                cellValue = "";
                                break;
                        }
                        System.out.print(cellValue + "\t");
                        if(i  == 0){
                        	//记录表名
                            currentTableName=(String)cellValue;
                            columnInfo.setTableName(currentTableName);

                        }else if(i == 1){
                        	//记录字段名
                            columnInfo.setColumnName(cellValue.toString());

                        }else if(i==2){
                        	//类型
                            columnInfo.setColumnType(cellValue.toString());
                        }else if(i==3){
                        	//是否允许为空
                        	String cellStr=cellValue.toString();
                            columnInfo.setAllowNull("Y".equals(cellStr)||"是".equals(cellStr)?true:false);
                        }else if(i==4){
                        	//是否为主键
                        	String cellStr=cellValue.toString();
                            columnInfo.setUniqueKey("Y".equals(cellStr)||"是".equals(cellStr)?true:false);
                        }else if(i==5){
                        	//注释
                            columnInfo.setComment(cellValue.toString());
                        }else if(i==6){
                        	//厂家源字段
                        	columnInfo.setSourceField(cellValue.toString());
                        }



                    }
                    //初次记录表名
                    if(tableName==null){
                        tableName= currentTableName;
                    }
                    //将字段加入集合
                    if(!currentTableName.equals(tableName)){

                        newComlumns= (ArrayList<ColumnInfo>) ((ArrayList<ColumnInfo>) comlumns).clone();
                        allTableInfo.put(tableName,newComlumns);
                        tableName= currentTableName;
                        isAdd=true;
                        comlumns.clear();
                    }
                    comlumns.add(columnInfo);
                    if(isAdd) {
                        allTableInfo.put(tableName,(ArrayList<ColumnInfo>) newComlumns);
                    }
                    //Iterator<Cell> cells = row.cellIterator();

                    //迭代遍历每个单元格

                    System.out.println();
                }
                if(null != tableName){
                allTableInfo.put(tableName,(ArrayList<ColumnInfo>) comlumns);
                }
            //}

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println(allTableInfo.toString());
            if(is!=null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void createDom4j(File file){
        try{
            // 创建Document
            Document document = DocumentHelper.createDocument();

            // 添加根节点
            Element root = document.addElement("templets");

            if(allTableInfo.isEmpty()){
                return;
            };
            int id=1;
            int dataType=1;
            int idOfferSet=0;
            Iterator<String> keyIterator = allTableInfo.keySet().iterator();
            while (keyIterator.hasNext()){

                String tableName= keyIterator.next();

                Element exportElement= root.addElement("export").
                        addAttribute("id",String.valueOf(id+idOfferSet))
                        .addAttribute("type", "105").
                                addAttribute("dataType",String.valueOf(dataType+idOfferSet));
                exportElement.addElement("table").addAttribute("value",tableName);

                Element columnsElement=exportElement.addElement("columns");

                ArrayList<ColumnInfo> tableColumns = allTableInfo.get(tableName);
                Iterator<ColumnInfo> columnIterator = tableColumns.iterator();
                while (columnIterator.hasNext()){
                    String columnName = columnIterator.next().getColumnName();
                    columnsElement.addElement("column").
                            addAttribute("name",columnName.toUpperCase()).
                            addAttribute("property",columnName.toUpperCase());
                }
                idOfferSet++;

            }
            // 在根节点下添加第一个子节点
          /*  Element oneChildElement= root.addElement("person").
                    addAttribute("attr", "root noe")
                    .addAttribute("","");

            oneChildElement.addElement("people")
                    .addAttribute("attr", "child one")
                    .addText("person one child one");
            oneChildElement.addElement("people")
                    .addAttribute("attr", "child two")
                    .addText("person one child two");

            // 在根节点下添加第一个子节点
            Element twoChildElement= root.addElement("person").addAttribute("attr", "root two");

            twoChildElement.addElement("people")
                    .addAttribute("attr", "child one")
                    .addText("person two child one");
            twoChildElement.addElement("people")
                    .addAttribute("attr", "child two")
                    .addText("person two child two");*/

            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter( new FileOutputStream(file), format);
            writer.write(document);

            System.out.println("dom4j CreateDom4j success!");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void parseDom4j(File file){
        try {
            // 创建dom4j解析器
            SAXReader reader = new SAXReader();

            // 获取Document节点
            Document document = reader.read( file );
            System.out.println("Root element :" + document.getRootElement().getName());
            String tableName="NEMANAGEDELEMENT_L";
            //document.getRootElement().
            List<Node> nodeList = document.getRootElement().selectNodes("export/table[@value='NEMANAGEDELEMENT_L']");
            System.out.println("nodelist"+nodeList);
            Node singleNode = document.getRootElement().selectSingleNode("export/table[@value='" + tableName + "']");
            System.out.println("singleNode"+singleNode);
            // 递归打印xml文档信息
           // parseElement(document.getRootElement());
           // Iterator<Element> elementIterator = document.getRootElement().elementIterator();
           // while (elementIterator.hasNext()){
            Element nemanagedElement = isExistsTableElement(document.getRootElement(), "NEMANAGEDELEMENT_L");
            System.out.println("nemanagedelement_l"+nemanagedElement);

           
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public static void parseElement(Element rootElement){
        Element element;
        for (Iterator<?> iterator = rootElement.elementIterator(); iterator.hasNext();) {

            element = (Element)iterator.next();
            System.out.println("Current Element Name :" + element.getName() +" , Text :"+ element.getTextTrim());
            if (element.getNodeType() == Node.ELEMENT_NODE) {
                if(element.hasContent()){
                    parseElement(element);
                }
            }
        }
    }

    /**
     * 是否存在对应表节点
     * @param rootElement
     * @return
     */
    public static   Element isExistsTableElement(Element rootElement,String tableName){
        Iterator<Element> elementIterator = rootElement.elementIterator();
        while (elementIterator.hasNext()){
            Element nextElement = elementIterator.next();
            for (Iterator<?> iterator = nextElement.elementIterator(); iterator.hasNext();) {

                Element element = (Element)iterator.next();
                //如果是表名标签
                String qualifiedName = element.getQualifiedName();
                if(qualifiedName.equals("table")){

                    Attribute tablenAttr = element.attribute("value");
                    System.out.println("table name :"+tablenAttr.getValue());
                    if(tablenAttr.getValue().equals(tableName)) {
                        return nextElement;
                    }

                }
        }

        //Element element = null;

            //System.out.println("Current Element Name :" + element.getName() +" , Text :"+ element.getTextTrim());
            /*if (element.getNodeType() == Node.ELEMENT_NODE) {
                if(element.hasContent()){
                    parseElement(element);
                }
            }*/
        }
        return null;

    }
    /**
     * 给xml文件中的表在oracle添加分区
     */
    @Test
    public  void addPartitionByXML(){
    	File file=null;
    	File outFile=null;
    	try {
    		if(file==null){
    			file=new File("D:\\SVNwarehouse\\igp\\trunk\\igp_v3\\app_runner\\template\\export\\lte\\lte_eric52_pm_xml_export.xml");
    		}
    		BufferedOutputStream bos =new BufferedOutputStream(new FileOutputStream(new File("C:\\Users\\liruyin\\Desktop\\myoutpartion.txt")) );
    		
				
            // 创建dom4j解析器
            SAXReader reader = new SAXReader();

            // 获取Document节点
            Document document = reader.read( file );
            String tableName="NEMANAGEDELEMENT_L";
            //document.getRootElement().
            List<Node> nodeList = document.getRootElement().selectNodes("export/table");
            //System.out.println("nodelist"+nodeList);
            //Node singleNode = document.getRootElement().selectSingleNode("export/table[@value='" + tableName + "']");
            //System.out.println("singleNode"+singleNode);
            // 递归打印xml文档信息
           // parseElement(document.getRootElement());
           // Iterator<Element> elementIterator = document.getRootElement().elementIterator();
           // while (elementIterator.hasNext()){
            //Element nemanagedelement_l = isExistsTableElement(document.getRootElement(), "NEMANAGEDELEMENT_L");
           // System.out.println("nemanagedelement_l"+nemanagedelement_l);
            
            Iterator<Node> elementIterator = nodeList.iterator();
            StringBuffer sb=new StringBuffer("");
            SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 1);
            Date date= c.getTime();
           
           
            while (elementIterator.hasNext()){
                Node node= (Element) elementIterator.next();
                Element elem =node.getParent();
                for (Iterator<?> iterator = elem.elementIterator(); iterator.hasNext();) {

                    Element element = (Element)iterator.next();
                    //如果是表名标签
                    String qualifiedName = element.getQualifiedName();
                    if(qualifiedName.equals("table")){
                        Attribute tablenAttr = element.attribute("value");
                        System.out.println("table name :"+tablenAttr.getValue());
                        /*alter table test add  partition PART_2019006123 
                         * values less than 
							(TO_DATE(' 2099-01-21 23:00:00', 'SYYYY-MM-DD HH24:MI:SS', 'NLS_CALENDAR=GREGORIAN'));
                         * */
                        sb.append("alter table  "+ tablenAttr.getValue() +
                        		" add  partition PART_2019006123 values less than")
                        		.append("(TO_DATE('").append(simpleDateFormat.format(date))
                        		.append("', 'SYYYY-MM-DD HH24:MI:SS', 'NLS_CALENDAR=GREGORIAN'));")
                        		.append("\n");
                        break;
                    }
                }  
            }
            
            bos.write(sb.toString().getBytes());
            bos.flush();
            bos.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
		}
    	
    }
    
}
