package com.uway.domcv.entities;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端参数实体封装
 * @author liruyin
 * @create 2019-06-23 15:01
 */
@Data
@Builder
public class ParserParm implements java.io.Serializable{
    /**
     * 输入路径
     */
    public String inputPath;
    /**
     * 输出路径
     */
    public String outputPath;
    /**
     * 解析读取开始行
     */
    public int startRowNum;

    //读取结束的行
    public int endRowNum;
    /**
     * 要读取的sheetname
     */
    public String sheetName;
    /**
     * 指定要读取的列
     */
    public int[]  readColumns;
    /**
     * 开始的id值
     */
    public  Integer startID;
    /**
     * 开始的datatype值
     */
    public  Integer startDatatype;
    

}
