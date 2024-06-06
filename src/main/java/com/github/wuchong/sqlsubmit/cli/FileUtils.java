package com.github.wuchong.sqlsubmit.cli;


import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);
    
    public static List<String> readLine(String filePath) {
        List<String> lines = new ArrayList<>();
        try {
            File file = new File(filePath);
            InputStreamReader input = new InputStreamReader(Files.newInputStream(file.toPath()));
            BufferedReader bf = new BufferedReader(input);
            String str;
            while ((str = bf.readLine()) != null) {
                lines.add(str);
            }
            bf.close();
            input.close();
        } catch (IOException e) {
            LOG.error("{}", filePath);
        }
        return lines;
    }
    
    public static String read(String filePath) throws IOException {
        try {
            File jsonFile = new File(filePath);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = fileReader.read()) != -1) {
                sb.append((char)ch);
            }
            fileReader.close();
            reader.close();
            return sb.toString();
        } catch (IOException ioException) {
            LOG.error("{}", filePath);
            throw ioException;
        }
    }
    
    public static String readFromS3(SqlSubmitConfig sqlSubmitConfig) throws Exception {
        try {
            MinioClient client = MinioClient.builder().endpoint(sqlSubmitConfig.s3Endpoint).credentials(sqlSubmitConfig.s3AccessKey, sqlSubmitConfig.s3SecretKey).build();
            String bucket = sqlSubmitConfig.sqlFilePath.split("/")[2];
            String object = sqlSubmitConfig.sqlFilePath.substring("s3://".length() + bucket.length());
            GetObjectResponse getObjectResponse = client.getObject(
                    (GetObjectArgs)((GetObjectArgs.Builder)((GetObjectArgs.Builder)GetObjectArgs.builder().bucket(bucket)).object(object)).build());
            List<String> lines = new ArrayList<>();
            InputStreamReader inputStreamReader = new InputStreamReader((InputStream)getObjectResponse, StandardCharsets.UTF_8);
            BufferedReader bf = new BufferedReader(inputStreamReader);
            String str;
            while ((str = bf.readLine()) != null) {
                lines.add(str);
            }
            getObjectResponse.close();
            inputStreamReader.close();
            return StringUtils.join(lines, "\n");
        } catch (Exception e) {
            System.out.println("Error read SQL from S3: " + e);
            throw new Exception(e);
        }
    }
}