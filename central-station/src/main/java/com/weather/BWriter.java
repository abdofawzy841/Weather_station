package com.weather;
import java.io.Closeable;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;


public class BWriter  implements Closeable {
    private static final int LIMIT = 10;
    private List<Status> buffer;
    private long id;
    private long dayTime = 1000 * 60 * 60 * 24;
    private DateFormat formatter;
    private Schema schema;
    public BWriter(long id){
        this.id = id;
        buffer = new LinkedList<Status>();
        formatter = new SimpleDateFormat("dd-MM-yyyy");
        this.schema = generateAvroSchema();
    }
    public void write(Status json){
        buffer.add(json);
        if(buffer.size() >= LIMIT){
            System.out.println("Flushing the buffer");
            flush();
        }
    }
    public void flush(){
        if(buffer.isEmpty())
            return;
        String startDay = "";
        String output = "";
        ParquetWriter<GenericRecord> writer = null;
        try {
                for(Status status: buffer){
                    String day = formatter.format(new Date(status.getStatus_timestamp()));
                    if(!startDay.equals(day)){
                        if(writer != null)
                            writer.close();
                        startDay = day;
                        output = "../data/station_" + id + "/" + startDay + "-" 
                            + (status.getStatus_timestamp()%dayTime) + ".parquet";
                        writer = getWriterFor(output);
                    }
                    GenericRecord statusRecord = statusToRecord(status);
                    writer.write(statusRecord);
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        buffer.clear();
    }
    private ParquetWriter<GenericRecord> getWriterFor(String filePath) {
        try {
            return AvroParquetWriter
                    .<GenericRecord>builder(new Path(filePath))
                    .withSchema(schema)
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .withConf(new Configuration())
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private GenericRecord statusToRecord (Status status) {
        GenericRecord record = new GenericData.Record(schema);
        record.put("station_id", status.getStation_id());
        record.put("s_no", status.getS_no());
        record.put("battery_status", status.getBattery_status());
        record.put("status_timestamp", status.getStatus_timestamp());
        record.put("weather_humidity", status.getWeather().getHumidity());
        record.put("weather_temperature", status.getWeather().getTemperature());
        record.put("weather_wind_speed", status.getWeather().getWind_speed());

        return record;
    }

    private static Schema generateAvroSchema() {
        String statusSchemaJson =
            "{\"type\":\"record\",\n" +
            " \"name\":\"Status\",\n" +
            " \"fields\":[\n" +
            "   {\"name\":\"station_id\", \"type\":\"long\"},\n" +
            "   {\"name\":\"s_no\", \"type\":\"long\"},\n" +
            "   {\"name\":\"battery_status\", \"type\":\"string\"},\n" +
            "   {\"name\":\"status_timestamp\", \"type\":\"long\"},\n" +
            "   {\"name\":\"weather_humidity\", \"type\":\"int\"},\n" +
            "   {\"name\":\"weather_temperature\", \"type\":\"int\"},\n" +
            "   {\"name\":\"weather_wind_speed\", \"type\":\"int\"}\n" +
            " ]}";
        Schema.Parser parser = new Schema.Parser();
        return parser.parse(statusSchemaJson);
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}
