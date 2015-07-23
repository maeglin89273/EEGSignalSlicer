package model;

import model.datasource.EEGChannels;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class RawDataFileUtils {
    private static final int COMMENTS_LINE_NUM = 5;
    private File saveDir;
    private Map<String, Integer> sliceRecord;
    private static RawDataFileUtils instance = new RawDataFileUtils();

    public static RawDataFileUtils getInstance() {
        return instance;
    }

    private RawDataFileUtils() {
        this.sliceRecord = new HashMap<String, Integer>();
    }

    public EEGChannels load(File rawDataFile) {
        saveDir = rawDataFile.getParentFile();
        double[][] rawData =  null;
        try {
            List<String> lines = Files.readAllLines(rawDataFile.toPath());
            rawData = new double[8][lines.size() - COMMENTS_LINE_NUM];
            int lineCounter = -COMMENTS_LINE_NUM;
            for(String line: lines) {

                if (lineCounter >= 0) {
                    String[] entries = line.split(", ");
                    for (int i = 0; i < 8; i++) {
                        rawData[i][lineCounter] = Double.parseDouble(entries[i + 1]);
                    }
                }
                lineCounter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rawData == null) {
            return null;
        }

        return new EEGChannels(rawData);
    }

    public String saveSlice(String tag, EEGChannels data, int start, int end) {
        tag = tag.trim();
        if (tag.isEmpty()) {
            return null;
        }

        Integer recordNum = this.sliceRecord.get(tag);
        recordNum = recordNum != null? recordNum + 1 : 0;
        this.sliceRecord.put(tag, recordNum);

        String filename = tag + "_" + recordNum + ".csv";
        File newFlie = new File(this.saveDir, filename);
        try {
            newFlie.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFlie));
            double[][] originalRawData = data.getOriginalRawDataInArray();
            String[] dataStrInRow = new String[originalRawData.length];
            for (int i = start; i <= end; i++) {
                for (int j = 0; j < originalRawData.length; j++) {
                        dataStrInRow[j] = Double.toString(originalRawData[j][i]);
                }
                writer.write(String.join(",", dataStrInRow));
                writer.newLine();
            }

            writer.close();

            return filename;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
