import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.catalog.hive.HiveCatalog
import org.apache.flink.types.Row
import org.apache.flink.table.api.scala._
import org.apache.flink.api.scala._

/**
 *
 * @author huangxf168482
 * @version 1.0
 * @date 2020/05/29 11:39
 *
 */
object Main {
  def main(args: Array[String]): Unit = {
    val streamEnv = StreamExecutionEnvironment.getExecutionEnvironment
    streamEnv.setParallelism(5)
    streamEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val tableEnvSettings = EnvironmentSettings.newInstance()
      .useBlinkPlanner()
      .inStreamingMode()
      .build()
    val tableEnv = StreamTableEnvironment.create(streamEnv, tableEnvSettings)

    val catalog = new HiveCatalog(
      "rtdw", // catalog name
      "default", // default database
      "/Users/huangxf/dev/install/apache-hive-1.2.2-bin/conf", // Hive config (hive-site.xml) directory
      "1.1.0" // Hive version
    )
    tableEnv.registerCatalog("rtdw", catalog)
    tableEnv.useCatalog("rtdw")

    val createDbSql = "CREATE DATABASE IF NOT EXISTS rtdw.ods"
    tableEnv.sqlUpdate(createDbSql)
    val createTableSql =
      """
        |
        |CREATE TABLE rtdw.ods.streaming_user_active_log (
        |  eventType STRING COMMENT '...',
        |  userId STRING,
        |  shareUserId STRING,
        |  platform STRING,
        |  columnType STRING,
        |  merchandiseId STRING,
        |  fromType STRING,
        |  siteId STRING,
        |  categoryId STRING,
        |  ts BIGINT,
        |  procTime AS PROCTIME(), -- 处理时间
        |  eventTime AS TO_TIMESTAMP(FROM_UNIXTIME(ts / 1000, 'yyyy-MM-dd HH:mm:ss')), -- 事件时间
        |  WATERMARK FOR eventTime AS eventTime - INTERVAL '10' SECOND -- 水印
        |) WITH (
        |  'connector.type' = 'kafka',
        |  'connector.version' = '0.11',
        |  'connector.topic' = 'ng_log_par_extracted',
        |  'connector.startup-mode' = 'latest-offset', -- 指定起始offset位置
        |  'connector.properties.zookeeper.connect' = 'localhost:2181',
        |  'connector.properties.bootstrap.servers' = 'localhost:9092',
        |  'connector.properties.group.id' = 'rtdw_group_test_1',
        |  'format.type' = 'json',
        |  'format.derive-schema' = 'true', -- 由表schema自动推导解析JSON
        |  'update-mode' = 'append'
        |)
      """.stripMargin
//    tableEnv.sqlUpdate(createTableSql)
    val queryActiveSql =
      """
        |SELECT eventType,
        |TUMBLE_START(eventTime, INTERVAL '30' SECOND) AS windowStart,
        |TUMBLE_END(eventTime, INTERVAL '30' SECOND) AS windowEnd,
        |COUNT(userId) AS pv,
        |COUNT(DISTINCT userId) AS uv
        |FROM rtdw.ods.streaming_user_active_log
        |WHERE platform = 'xyz'
        |GROUP BY eventType, TUMBLE(eventTime, INTERVAL '30' SECOND)
      """.stripMargin
    val result = tableEnv.sqlQuery(queryActiveSql)

    result
      .toAppendStream[Row]
      .print()
      .setParallelism(1)


  }
}
