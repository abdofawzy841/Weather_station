package com.weather;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.avro.reflect.ReflectData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter.Mode;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class BWriter {
    private static final int LIMIT = 10;
    private List<Status> buffer;
    private long id;
    private long dayTime = 1000 * 60 * 60 * 24;
    private DateFormat formatter;
    public BWriter(long id){
        this.id = id;
        buffer = new LinkedList<Status>();
        formatter = new SimpleDateFormat("dd-MM-yyyy");
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
        ParquetWriter<Status> writer = null;
        try {
                for(Status status: buffer){
                    String day = formatter.format(new Date(status.getStatus_timestamp()));
                    if(!startDay.equals(day)){
                        if(writer != null)
                            writer.close();
                        startDay = day;
                        output = id + "/" + startDay + "-" 
                            + (status.getStatus_timestamp()%dayTime) + ".parquet";
                        writer = getWriterFor(output);
                    }
                    writer.write(status);
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        buffer.clear();
    }
    private ParquetWriter<Status> getWriterFor(String filePath) {
        try {
            return AvroParquetWriter
                    .<Status>builder(new Path(filePath))
                    .withSchema(ReflectData.AllowNull.get().getSchema(Status.class))
                .withDataModel(ReflectData.get())
                .withConf(new Configuration())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriteMode(Mode.OVERWRITE)
                .build();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}