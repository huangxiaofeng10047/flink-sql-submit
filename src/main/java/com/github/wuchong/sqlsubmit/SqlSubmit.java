package com.github.wuchong.sqlsubmit;
import java.util.Map;

import com.github.wuchong.sqlsubmit.cli.SqlSubmitConfig;
import com.github.wuchong.sqlsubmit.cli.SqlSubmitException;
import com.github.wuchong.sqlsubmit.cli.EnumRunningMode;
import com.github.wuchong.sqlsubmit.cli.SqlParser;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlSubmit {
    private static final Logger LOG = LoggerFactory.getLogger(SqlSubmit.class);
    
    private static void checkS3Config(SqlSubmitConfig sqlSubmitConfig) throws SqlSubmitException {
        if (sqlSubmitConfig.s3Endpoint == null || sqlSubmitConfig.s3AccessKey == null || sqlSubmitConfig.s3SecretKey == null){
            throw new SqlSubmitException("Please check s3 config, all of endpoint, accessKey and secretKey should not be null");
    
        }
        if (!sqlSubmitConfig.sqlFilePath.toUpperCase().startsWith("S3://") || sqlSubmitConfig.sqlFilePath
                .split("/")[2] == null) {
            throw new SqlSubmitException("Please check s3 sqlFilePath path, it should be like \"s3://flink/sql-tasks/basic.sql\"");
        }
    }
    
    public static void main(String[] args) throws Exception {

        EnvironmentSettings settings;
        SqlSubmitConfig sqlSubmitConfig = SqlSubmitConfig.getConfig(args);
        if (sqlSubmitConfig.sqlFilePath.toUpperCase().startsWith("S3")) {
            checkS3Config(sqlSubmitConfig);
        }
        String runningMode = (sqlSubmitConfig.mode == null) ? "streaming" : sqlSubmitConfig.mode;
        LOG.info("Flink" + runningMode);
        LOG.info("Flink" + sqlSubmitConfig.sqlFilePath);
        if (EnumRunningMode.BATCH.name().equals(runningMode.toUpperCase())) {
            settings = EnvironmentSettings.newInstance().inBatchMode().build();
        } else if (EnumRunningMode.STREAMING.name().equals(runningMode.toUpperCase())) {
            settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        } else {
            throw new Exception("no flink run mode"+ runningMode);
        }
        TableEnvironment tableEnvironment = TableEnvironment.create(settings);
        for (Map<String, String> sql : SqlParser.parseSqlFileFromS3(sqlSubmitConfig)) {
            if (sql != null && sql.size() != 0) {
                LOG.info(sql.values().iterator().next());
                tableEnvironment.executeSql(sql.values().iterator().next());
            }
        }
    }
}

