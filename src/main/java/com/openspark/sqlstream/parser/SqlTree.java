package com.openspark.sqlstream.parser;


import java.util.*;

import static com.openspark.sqlstream.util.DtStringUtil.newArrayList;
import static com.openspark.sqlstream.util.DtStringUtil.newHashSet;

/**
 * Object structure obtained by parsing SQL
 */

public class SqlTree {

    private Set<CreateFuncParser.SqlParserResult> functionList = newHashSet();

    private Map<String, CreateTableParser.SqlParserResult> preDealTableMap = new HashMap<>();

    private Map<String, CreateTableParser.SqlParserResult> preDealSinkMap = new HashMap<>();

    private Map<String, Object> preDealSparkEnvMap = new HashMap<>();

    private Map<String, TableInfo> tableInfoMap = new LinkedHashMap();

    private Set<InsertSqlParser.SqlParseResult> execSqlList = newHashSet();

    private InsertSqlParser.SqlParseResult execSql;

    private String appInfo;

    public Set<CreateFuncParser.SqlParserResult> getFunctionList() {
        return functionList;
    }

    public String getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(String appInfo) {
        this.appInfo = appInfo;
    }

    public Map<String, CreateTableParser.SqlParserResult> getPreDealTableMap() {
        return preDealTableMap;
    }

    public Map<String, Object> getPreDealSparkEnvMap(){
        return preDealSparkEnvMap;
    }

    public Map<String, CreateTableParser.SqlParserResult> getPreDealSinkMap() {
        return preDealSinkMap;
    }

    public Set<InsertSqlParser.SqlParseResult> getExecSqlList() {
        return execSqlList;
    }

    public void addFunc(CreateFuncParser.SqlParserResult func) {
        functionList.add(func);
    }

    public void addPreDealTableInfo(String tableName, CreateTableParser.SqlParserResult table) {
        preDealTableMap.put(tableName, table);
    }

    public void addPreDealSinkInfo(String tableName, CreateTableParser.SqlParserResult table) {
        preDealSinkMap.put(tableName, table);
    }

    public void addPreDealSparkEnvInfo(Map<String, Object> sparkEnv) {
        sparkEnv.forEach((key,value)->{
            preDealSparkEnvMap.put(key,value);
        });
    }

    public void addExecSql(InsertSqlParser.SqlParseResult execSql) {
        execSqlList.add(execSql);
    }

    public Map<String, TableInfo> getTableInfoMap() {
        return tableInfoMap;
    }

    public InsertSqlParser.SqlParseResult getExecSql() {
        return execSql;
    }

    public void setExecSql(InsertSqlParser.SqlParseResult execSql) {
        this.execSql = execSql;
    }

    public void addTableInfo(String tableName, TableInfo tableInfo) {
        tableInfoMap.put(tableName, tableInfo);
    }

    public void clear() {
        functionList.clear();
        preDealTableMap.clear();
        preDealSinkMap.clear();
        tableInfoMap.clear();
        execSqlList.clear();
        execSql = null;
    }
}
