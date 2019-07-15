package com.uway.domcv.entities;

import lombok.Data;

@Data
public class ColumnInfo {
	//表名称
	private String tableName ;
	//字段名称
	private String columnName;
	//字段类型
	private String columnType;
	/**
	 * 是否允许为空
	 */
	private boolean allowNull;
	/**
	 * 注释
	 */
	private String comment;
	/**
     * 是否为主键
     */
    public boolean uniqueKey;
}
