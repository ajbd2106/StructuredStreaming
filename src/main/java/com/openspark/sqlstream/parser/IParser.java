package com.openspark.sqlstream.parser;


public interface IParser {

    /**
     * Whether the parse type is satisfied
     *
     * @param sql
     * @return
     */
    boolean verify(String sql);

    /***
     * Parse sql
     * @param sql
     * @param sqlTree
     */
    void parseSql(String sql, SqlTree sqlTree);
}
