# 省略了 License，特此声明

#FROM adoptopenjdk/openjdk11:jre-11.0.9_11.1-alpine
#
## 安装需要的软件
## snappy 是一个压缩库
## libc6-compat 是 ANSI C 的函数库
#RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories
## RUN apk add --no-cache bash snappy libc6-compat \
#RUN apk add --no-cache bash snappy gnupg   \
# && apk add gettext
## Flink 容器里的环境变量
## Flink 软件的安装目录在 /opt
#ENV FLINK_INSTALL_PATH=/opt
## Flikn 的解压目录在 /opt/flink
#ENV FLINK_HOME $FLINK_INSTALL_PATH/flink
## Flink 的依赖包目录在 /opt/flink/lib
#ENV FLINK_LIB_DIR $FLINK_HOME/lib
## Flink 的插件目录在 /opt/flink/plugins
#ENV FLINK_PLUGINS_DIR $FLINK_HOME/plugins
## 这个不知道是什么目录
#ENV FLINK_OPT_DIR $FLINK_HOME/opt
## 这是用户代码的 Jar 包目录，/opt/flink/artifacts
#ENV FLINK_JOB_ARTIFACTS_DIR $FLINK_INSTALL_PATH/artifacts
## 更新一下 PATH，把 Flink 的二进制文件的目录加上 /opt/flink/bin
#ENV PATH $PATH:$FLINK_HOME/bin
#
## 这些 ARG 可以在构建镜像的时候输入参数，默认值都是 NOT_SET，如果设置了就会去找对应的目录，并且打入镜像里
## Flink 的发行版路径，可以在本地指定任何下载或者自行打包的 Flink 发行版包
#ARG flink_dist=NOT_SET
## 用户写的业务代码路径
#ARG job_artifacts=NOT_SET
## Python 的版本，填2或者3
#ARG python_version=NOT_SET
## Hadoop Jar 包的依赖路径
#ARG hadoop_jar=NOT_SET*
#
## 安装 Python，根据前面填的 python_version 这个环境变量，不填就不装
#RUN \
#  if [ "$python_version" = "2" ]; then \
#    apk add --no-cache python; \
#  elif [ "$python_version" = "3" ]; then \
#    apk add --no-cache python3 && ln -s /usr/bin/python3 /usr/bin/python; \
#  fi
#
## 把 Flink 发行版和 Hadoop jar（不一定有 Hadoop）放在 /opt/flink 目录
#ADD $flink_dist $hadoop_jar $FLINK_INSTALL_PATH/
## 用户代码放在 /opt/artifacts
#ADD $job_artifacts/* $FLINK_JOB_ARTIFACTS_DIR/
#ENV GOSU_VERSION 1.11
#COPY gosu-amd64 /usr/local/bin/gosu
#COPY gosu-amd64.asc /usr/local/bin/gosu.asc
#
#RUN set -ex; \
#  export GNUPGHOME="$(mktemp -d)"; \
#  for server in ha.pool.sks-keyservers.net $(shuf -e \
#                          hkp://p80.pool.sks-keyservers.net:80 \
#                          keyserver.ubuntu.com \
#                          hkp://keyserver.ubuntu.com:80 \
#                          pgp.mit.edu) ; do \
#      gpg --batch --keyserver "$server" --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 && break || : ; \
#  done && \
#  gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu; \
#  gpgconf --kill all; \
#  rm -rf "$GNUPGHOME" /usr/local/bin/gosu.asc; \
#  chmod +x /usr/local/bin/gosu; \
#  gosu nobody true
#RUN set -x && \
#  ln -s $FLINK_INSTALL_PATH/flink-[0-9]* $FLINK_HOME && \
#  for jar in $FLINK_JOB_ARTIFACTS_DIR/*.jar; do [ -f "$jar" ] || continue; ln -s $jar $FLINK_LIB_DIR; done && \
#  if [ -n "$python_version" ]; then ln -s $FLINK_OPT_DIR/flink-python-*-java-binding.jar $FLINK_LIB_DIR; fi && \
#  if [ -f ${FLINK_INSTALL_PATH}/flink-shaded-hadoop* ]; then ln -s ${FLINK_INSTALL_PATH}/flink-shaded-hadoop* $FLINK_LIB_DIR; fi && \
#  # 创建 flink 用户组和 flink 用户，并且更改下面目录的用户权限
#  addgroup -S flink && adduser -D -S -H -G flink -h $FLINK_HOME flink && \
#  chown -R flink:flink ${FLINK_INSTALL_PATH}/flink-* && \
#  chown -R flink:flink ${FLINK_JOB_ARTIFACTS_DIR}/ && \
#  chown -h flink:flink $FLINK_HOME  && \
#  sed -i 's/rest.address: localhost/rest.address: 0.0.0.0/g' $FLINK_HOME/conf/flink-conf.yaml && \
#  sed -i 's/rest.bind-address: localhost/rest.bind-address: 0.0.0.0/g' $FLINK_HOME/conf/flink-conf.yaml && \
#  sed -i 's/jobmanager.bind-host: localhost/jobmanager.bind-host: 0.0.0.0/g' $FLINK_HOME/conf/flink-conf.yaml && \
#  sed -i 's/taskmanager.bind-host: localhost/taskmanager.bind-host: 0.0.0.0/g' $FLINK_HOME/conf/flink-conf.yaml && \
#  sed -i '/taskmanager.host: localhost/d' $FLINK_HOME/conf/flink-conf.yaml
## 把这个脚本拷贝到镜像
#COPY docker/flink/docker-entrypoint.sh /
FROM flink:1.15.4
COPY juicefs-hadoop-1.1-dev.jar /opt/flink/lib/
COPY  flink-s3-fs-hadoop-1.15.4.jar /opt/flink/lib/
COPY  target/flink-sql-submit-1.0.jar /opt/flink/lib/
#install hadoop
RUN set -ex; \
    wget -nv -O /opt/flink/lib/hudi-flink.jar https://nexus.lrting.top/repository/maven-deploy/org/apache/hudi/hudi-flink1.15-bundle/0.12.0/hudi-flink1.15-bundle-0.12.0.jar ; \
     wget -nv -O /opt/hadoop.tar.gz https://mirrors.tuna.tsinghua.edu.cn/apache/hadoop/common/hadoop-3.3.4/hadoop-3.3.4.tar.gz ; \
        tar -xzf /opt/hadoop.tar.gz -C /opt/ ; \
        rm -rf /opt/hadoop.tar.gz ; \
           mv /opt/hadoop-3.3.4 /opt/hadoop ; \
     cp /opt/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-client-core-3.3.4.jar /opt/flink/lib/


ENV HADOOP_HOME=/opt/hadoop