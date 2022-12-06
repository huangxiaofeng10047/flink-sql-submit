package com.github.wuchong.sqlsubmit.cli;

public enum EnumCommand {
    CREATE_CATALOG, USE_CATALOG, DROP_CATALOG, CREATE_DATABASE, USE_DATABASE, DROP_DATABASE, CREATE_TABLE, DROP_TABLE, INSERT_INTO, SELECT, EXECUTE_STATEMENT_SET, BEGIN, END, WITH, PROCEDURE_CALL_COMPACT;
    
    public String toString() {
        return super.toString().replace('_', ' ');
    }
}