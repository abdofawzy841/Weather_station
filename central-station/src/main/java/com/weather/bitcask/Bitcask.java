package com.weather.bitcask;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Bitcask{
    Map<Long, ValueLocation> keyDir;
    String currentFile = "current.data";
    String currentFilePath;
    String directoryName;
    HashMap<Long, ValueLocation> mergedHints;
    int maxSize = 10000;

    public Bitcask(String directoryName, long interval) {
        this.directoryName = directoryName;
        this.currentFilePath = directoryName+File.separator+currentFile;
        this.keyDir = new HashMap<>();
        this.mergedHints = new HashMap<>();
        try {
            if(!Files.exists(Paths.get(directoryName))){
                Files.createDirectory(Paths.get(directoryName));
            }
            if(!Files.exists(Paths.get(currentFilePath))){
                Files.createFile(Paths.get(currentFilePath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        rebuild();
        scheduleCompression(interval);
    }
    public void scheduleCompression(long interval){
        Executors.newScheduledThreadPool(1)
            .scheduleAtFixedRate(()->{
                compression();
            },interval,interval, TimeUnit.SECONDS);
    }

    public void compression(){
        Map<Long, ValueLocation> copyHints = new HashMap<>(mergedHints);

        File[] oldFiles = Stream.of(new File(directoryName).listFiles())
            .filter(file -> !file.getName().contains("current"))
            .sorted((o1, o2) -> {
                long first = Long.parseLong(o1.getName().split("\\.")[0]);
                long second = Long.parseLong(o2.getName().split("\\.")[0]);
                return (int) (second-first);
            })
            .toArray((size) -> new File[size]);
        
        if(oldFiles.length<=2){
            return;
        }
        
        long min = Long.parseLong(oldFiles[oldFiles.length-1].getName().split("\\.")[0]);
        String compressName = (min-1)+".data";
        String tempName = "compressed.data";
        String compressedPath = directoryName+File.separator+tempName;
        try {
            Files.createFile(Paths.get(compressedPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File compressed = new File(compressedPath);
        Map<Long, ValueLocation> compressedHint = new HashMap<>();
        for(Long key: copyHints.keySet()){
            String val = readRecord(key,false);
            Record record = new Record(key,val);
            ValueLocation newFilePosition = new ValueLocation(
                compressName,(int)(compressed.length()),record.getLength());
            compressedHint.put(key,newFilePosition);
            record.writeRecord(compressedPath);
        }

        createHintFile(compressName, compressedHint);
        compressed.renameTo(new File(directoryName+File.separator+compressName));
        for(Long key: compressedHint.keySet()){
            synchronized(mergedHints){
                if(mergedHints.get(key).equals(copyHints.get(key))){
                    mergedHints.put(key, compressedHint.get(key));
                }
            }
        }

        for(File f: oldFiles){
            f.delete();
        }
    }

    public void addRecord(Long key, String message){
        Record record = new Record(key,message);
        int fileSize = 0;
        try {
            fileSize = (int) Files.size(Paths.get(currentFilePath));
            if(fileSize+record.getRecord().length>maxSize){
                generateDataAndHint();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        record.writeRecord(currentFilePath);
        keyDir.put(key,new ValueLocation(currentFile,
                fileSize,record.getRecord().length));
    }

    private void generateDataAndHint(){
        File oldFile = new File(currentFilePath);
        String name = System.currentTimeMillis()+".data";
        File newFile = new File(directoryName+File.separator+name);
        oldFile.renameTo(newFile);
        try {
          Files.createFile(Paths.get(currentFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized(mergedHints){
            for(Long key: keyDir.keySet()){
                keyDir.get(key).fileName = name;
                mergedHints.put(key, keyDir.get(key));
            }
        }
        createHintFile(name, keyDir);
        keyDir.clear();
    }
    public String get(Long key){
        String value = readRecord(key, true);
        if(value == null)
            value = readRecord(key, false);
        return value;
    }

    private String readRecord(Long key, boolean memT){
        Map<Long, ValueLocation> used = memT? keyDir: mergedHints;
        if(used.containsKey(key)){
            ValueLocation location = used.get(key);
            try {
                RandomAccessFile toRead = new RandomAccessFile(
                    directoryName+File.separator+location.fileName,"r");
                toRead.seek(location.offset);
                byte[] readIn = new byte[location.size];
                toRead.read(readIn,0,location.size);
                toRead.close();
                int keysize = ByteBuffer.wrap(readIn,0,4).getInt();
                return new String(Arrays.copyOfRange(readIn,8+keysize,readIn.length));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void processHintFile(File file){
        byte[] readIn = new byte[(int)file.length()];
        try {
            FileInputStream toRead = new FileInputStream(file);
            toRead.read(readIn);
            toRead.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0; i<readIn.length; i++){
            int keySize = ByteBuffer.wrap(readIn,i,4).getInt();
            int offset = ByteBuffer.wrap(readIn,i+4,4).getInt();
            int recordSize = ByteBuffer.wrap(readIn,i+8,4).getInt();
            long key = ByteBuffer.wrap(readIn,i+12,keySize).getLong();
            if(!mergedHints.containsKey(key)){
                mergedHints.put(key, new ValueLocation(
                    file.getName().replace("hint","data"), offset, recordSize));
            }
            i = i+12+keySize-1;
        }
    }

    private void createHintFile(String compressName, Map<Long, ValueLocation> hints){
        compressName = compressName.replace("data","hint");
        try(FileOutputStream writer = new FileOutputStream(directoryName+File.separator+compressName)){
            for(Long key: hints.keySet()){
                byte[] keyBytes = ByteBuffer.allocate(8).putLong(key).array();
                byte[] hintRecord = new byte[12+keyBytes.length];
                ValueLocation get = hints.get(key);
                System.arraycopy(ByteBuffer.allocate(4).putInt(keyBytes.length).array(),0,hintRecord,0,4);
                System.arraycopy(ByteBuffer.allocate(4).putInt(get.offset).array(),0,hintRecord,4,4);
                System.arraycopy(ByteBuffer.allocate(4).putInt(get.size).array(),0,hintRecord,8,4);
                System.arraycopy(keyBytes,0,hintRecord,12,keyBytes.length);
                writer.write(hintRecord);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void rebuild(){
        mergedHints = new HashMap<>();

        Stream.of(new File(directoryName).listFiles())
            .filter(file -> file.getName().contains("hint"))
            .sorted((o1, o2) -> {
                long first = Long.parseLong(o1.getName().split("\\.")[0]);
                long second = Long.parseLong(o2.getName().split("\\.")[0]);
                return (int) (second-first);
            })
            .forEach(file -> processHintFile(file));

        rebuildKeyDir();
    }

    private void rebuildKeyDir(){
        File curr = new File(currentFilePath);
        byte[] readIn = new byte[(int)curr.length()];
        try {
            FileInputStream toRead = new FileInputStream(curr);
            toRead.read(readIn);
            toRead.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0; i<readIn.length; i++){
            int keySize = ByteBuffer.wrap(readIn,i,4).getInt();
            int valSize = ByteBuffer.wrap(readIn,i+4,4).getInt();
            long key = ByteBuffer.wrap(readIn,i+8,keySize).getLong();
            keyDir.put(key,new ValueLocation(currentFile,i,8+keySize+valSize));
            i = i+8+keySize+valSize-1;
        }
    }
}
