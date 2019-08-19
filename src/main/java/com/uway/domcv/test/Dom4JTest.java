package com.uway.domcv.test;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.Iterator;

/**
 * @author liruyin
 * @create 2019-06-21 23:58
 */
public class Dom4JTest {
    public static void main(String[] args) {
        // 调用dom4j生成xml方法
//        createDom4j(new File("E:\\dom4j.xml"));
    	int a = 031;
    	int b = 0x32;
    	int c = 0b111;
    	System.out.println("a  :"+a);
    	System.out.println("b  :"+b);
    	System.out.println("c  :"+c);
    }
    public static void createDom4j(File file){
        try{
            // 创建Document
            Document document = DocumentHelper.createDocument();

            // 添加根节点
            Element root = document.addElement("root");

            // 在根节点下添加第一个子节点
            Element oneChildElement= root.addElement("person").addAttribute("attr", "root noe");

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
                    .addText("person two child two");

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

            // 递归打印xml文档信息
            parseElement(document.getRootElement());

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
}


