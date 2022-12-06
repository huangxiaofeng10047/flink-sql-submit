package com.github.wuchong.sqlsubmit.cli;
public class SqlSubmitException extends Exception {
    private static final long serialVersionUID = 4235225697094262603L;
    
    private final String msg;
    
    public SqlSubmitException(String msg) {
        this.msg = msg;
    }
    
    public String getMessage() {
        return this.msg;
    }
}

