package com.github.wuchong.sqlsubmit.cli;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class SqlSubmitConfig {
    
    @Parameter(names = {"--help", "-h"}, help = true, required = false)
    public Boolean help;
    
    @Parameter(names = {"--sqlFilePath", "-f"}, description = "SQL", required = true)
    public String sqlFilePath;
    
    @Parameter(names = {"--mode", "-m"}, description = "Flink", required = false)
    public String mode;
    
    @Parameter(names = {"--s3Endpoint", "-e"}, description = "加入SOL文件路径为S3,则需要指定本配置", required = false)
    public String s3Endpoint;
    
    @Parameter(names = {"--s3AccessKey", "-a"}, description = "加入SOL文件路径为S3,则需要指定本配置", required = false)
    public String s3AccessKey;
    
    @Parameter(names = {"--s3SecretKey", "-s"}, description = "加入SOL文件路径为S3,则需要指定本配置", required = false)
    public String s3SecretKey;
    
    public static SqlSubmitConfig getConfig(String[] args) {
        SqlSubmitConfig cfg = new SqlSubmitConfig();
        try {
            JCommander cmd = new JCommander(cfg, null, args);
            if (cfg.help != null || args.length == 0) {
                cmd.usage();
                System.exit(1);
            }
            return cfg;
        } catch (ParameterException e) {
            String[] argv = {"--help"};
            JCommander cmd = new JCommander(cfg, null, argv);
            System.out.println("请指定正确参数");
            cmd.usage();
            throw new ParameterException(e);
        }
    }
}