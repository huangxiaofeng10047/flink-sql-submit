package com.github.wuchong.sqlsubmit.cli;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SqlParser {
    public static Map<String, String>[] parseSqlFile(String sqlFilePath) throws Exception {
        String[] sqlCommands = FileUtils.read(sqlFilePath).trim().split(";");
        HashMap[] arrayOfHashMap = new HashMap[100];
        int count = 0;
        for (String sql : sqlCommands) {
            arrayOfHashMap[count] = (HashMap)call(sql);
            count++;
        }
        return parseExecuteStatSet((Map<String, String>[])arrayOfHashMap);
    }
    
    public static Map<String, String>[] parseSqlFileFromS3(SqlSubmitConfig sqlSubmitConfig) throws Exception {
        String[] sqlCommands = FileUtils.readFromS3(sqlSubmitConfig).trim().split(";");
        HashMap[] arrayOfHashMap = new HashMap[100];
        int count = 0;
        for (String sql : sqlCommands) {
            arrayOfHashMap[count] = (HashMap)call(sql);
            count++;
        }
        return parseExecuteStatSet((Map<String, String>[])arrayOfHashMap);
    }
    
    public static Map<String, String> call(String sql) throws Exception {
        String fSql = sql.trim().toUpperCase();
        Map<String, String> sqlMap = new HashMap<>();
        for (EnumCommand sqlType : EnumCommand.values()) {
            if (deleteSqlComment(fSql).startsWith(sqlType.toString())) {
                sqlMap.put(sqlType.name(), sql);
            }
        }
        if (sqlMap.isEmpty()) {
            throw new Exception("no support sql"+ sql);
        }
        return sqlMap;
    }
    
    public static String deleteSqlComment(String originalSql) {
        String[] sqlLines = originalSql.split("\n");
        StringBuilder targetSql = new StringBuilder();
        for (String sqlLine : sqlLines) {
            if (!sqlLine.startsWith("--")) {
                targetSql.append(sqlLine);
            }
        }
        return targetSql.toString();
    }
    
    public static Map<String, String>[] parseExecuteStatSet(Map<String, String>[] originalSqlMaps) {
        int count = 0;
        int beginPos = -1;
        int endPos = -1;
        StringBuilder executeStatSetString = new StringBuilder();
        Map<String, String> executeStatSetMap = new HashMap<>(1);
        for (Map<String, String> originalSqlMap : originalSqlMaps) {
            if (originalSqlMap == null) {
                break;
            }
            if (EnumCommand.EXECUTE_STATEMENT_SET.name().equals(originalSqlMap.keySet().iterator().next())) {
                beginPos = count;
            } else if (EnumCommand.END.name().equals(originalSqlMap.keySet().iterator().next())) {
                endPos = count;
            }
            count++;
        }
        if (beginPos > -1 && endPos > -1) {
            for (int i = beginPos; i < endPos + 1; i++) {
                executeStatSetString.append(originalSqlMaps[i].values().iterator().next());
                executeStatSetString.append(";");
            }
            executeStatSetMap.put(EnumCommand.EXECUTE_STATEMENT_SET.name(), executeStatSetString
                    .substring(0, executeStatSetString.length() - 1));
            int pos = 0;
            HashMap[] arrayOfHashMap = new HashMap[originalSqlMaps.length];
            for (Map<String, String> sqlMap : (Map[])Arrays.<Map>copyOfRange((Map[])originalSqlMaps, 0, beginPos)) {
                arrayOfHashMap[pos] = (HashMap)sqlMap;
                pos++;
            }
            arrayOfHashMap[pos] = (HashMap)executeStatSetMap;
            pos++;
            for (Map<String, String> sqlMap : (Map[])Arrays.<Map>copyOfRange((Map[])originalSqlMaps, endPos + 1, originalSqlMaps.length - 1)) {
                arrayOfHashMap[pos] = (HashMap)sqlMap;
                pos++;
            }
            return (Map<String, String>[])arrayOfHashMap;
        }
        return originalSqlMaps;
    }
}

