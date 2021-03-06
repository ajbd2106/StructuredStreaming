package com.openspark.sqlstream.sink;

import com.openspark.sqlstream.parser.CreateTableParser;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;

import java.util.Map;

import static org.apache.spark.sql.streaming.Trigger.ProcessingTime;

public class KafkaOutput implements BaseOuput {

    Map<String, Object> propMap = null;

    @Override
    public void checkConfig() {
        String outputMode = null;
        try {
            outputMode = propMap.get("outputmode").toString();
        } catch (Exception e) {
            System.out.println("outputMode is not configured, default is complete");
        }
        if (outputMode == null) {
            propMap.put("outputmode", "complete");
        }

    }

    @Override
    public StreamingQuery process(SparkSession spark, Dataset<Row> dataset, CreateTableParser.SqlParserResult config) {

        StreamingQuery query = null;

        propMap = config.getPropMap();
        checkConfig();

        if (propMap.containsKey("process")) {
            String process = getProcessTime(propMap.get("process").toString());
            query = dataset.writeStream()
                    .outputMode(propMap.get("outputmode").toString())
                    .format("console")
                    .trigger(ProcessingTime(process))
                    .queryName("kafkatest")
                    .start();
        } else {
            query = dataset.writeStream()
                    .outputMode(propMap.get("outputmode").toString())
                    .format("console")
                    .queryName("kafkatest")
                    .start();
        }

        return query;
    }

    public static String getProcessTime(String proTimeStr) {
        //String processTime = "2 seconds";
        String number;
        String time;
        number = proTimeStr.replaceAll("[^(0-9)]", "");
        time = proTimeStr.replaceAll("[^(A-Za-z)]", "");
        switch (time) {
            case "s":
            case "S":
                return number + " seconds";
        }
        throw new RuntimeException("process time is illegal");
    }

    @Override
    public Dataset<Row> prepare(SparkSession spark) {
        return null;
    }
}
