package model;

import model.datasource.*;
import model.filter.ButterworthFilter;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class DataFileUtils {
    private static final int OPENBCI_COMMENTS_LINE_NUM = 5;
    private File workingFile;
    private Map<String, Integer> sliceRecord;
    private static DataFileUtils instance = new DataFileUtils();

    public static DataFileUtils getInstance() {
        return instance;
    }

    private DataFileUtils() {
        this.sliceRecord = new HashMap<String, Integer>();
    }

    public File getWorkingFile() {
        return this.workingFile;
    }

    public FilteredFiniteDataSource loadOpenBCIRawFile(File rawDataFile) {
        this.workingFile = rawDataFile;
        double[][] rawData =  null;
        try {
            List<String> lines = Files.readAllLines(rawDataFile.toPath());
            rawData = new double[8][lines.size() - OPENBCI_COMMENTS_LINE_NUM];
            int lineCounter = -OPENBCI_COMMENTS_LINE_NUM;
            for(String line: lines) {
                if (lineCounter >= 0) {
                    String[] entries = line.split(",");
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

        FilteredFiniteDataSource bciData = new FilteredFiniteDataSource(new SimpleFiniteLengthDataSource(convert2DArrayToMap(rawData)));
        bciData.addFilter(ButterworthFilter.NOTCH_60HZ);
        bciData.addFilter(ButterworthFilter.BANDPASS_1_50HZ);
        return bciData;
    }

    private static Map<String, FiniteLengthStream> convert2DArrayToMap(double[][] originalRawData) {
        Map<String, FiniteLengthStream> dataMap = new HashMap<>(originalRawData.length);
        for (int i = 1; i <= originalRawData.length; i++) {
            dataMap.put(Integer.toString(i), new SimpleArrayStream(originalRawData[i - 1]));
        }
        return dataMap;
    }

    public FiniteLengthDataSource loadGeneralCSVFile(File csvFile) {
        this.workingFile = csvFile;
        Map<String, FiniteLengthStream> rawData = null;
        try {
            List<String> lines = Files.readAllLines(csvFile.toPath());
            String[] headers = lines.get(0).split(",");

            double[][] tmpData = new double[headers.length][lines.size() - 1];

            for(int lineCounter = 1; lineCounter < lines.size(); lineCounter++) {
                String[] entries = lines.get(lineCounter).split(",");
                for (int i = 0; i < tmpData.length; i++) {
                    tmpData[i][lineCounter - 1] = Double.parseDouble(entries[i]);
                }
            }

            rawData = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                rawData.put(headers[i], new SimpleArrayStream(tmpData[i]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rawData == null) {
            return null;
        }

        return new SimpleFiniteLengthDataSource(rawData);
    }

    public String save(String name, FiniteLengthDataSource data) {
        name = name.trim();
        if (name.isEmpty()) {
            return null;
        }

        return saveDataSource(name, data, 0, data.intLength());
    }


    public String saveSlice(String sliceTag, FiniteLengthDataSource data, int start, int end) {
        sliceTag = sliceTag.trim();
        if (sliceTag.isEmpty()) {
            return null;
        }

        Integer recordNum = this.sliceRecord.get(sliceTag);
        recordNum = recordNum != null? recordNum + 1 : 0;
        this.sliceRecord.put(sliceTag, recordNum);

        String filename = sliceTag + "_" + recordNum + ".csv";
        return saveDataSource(filename, data, start, end);
    }

    private String saveDataSource(String filename, FiniteLengthDataSource data, int start, int end) {

        File newFlie = new File(this.workingFile.getParentFile(), filename);
        try {
            newFlie.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFlie));
            Collection<String> tags = data.getTags();
            String[] dataStrInRow = new String[tags.size()];
            int colCounter = 0;
            for(String tag: tags) {
                dataStrInRow[colCounter++] = tag;
            }

            writer.write(String.join(",", dataStrInRow));
            writer.newLine();

            FiniteLengthStream stream;
            for (long i = start; i <= end; i++) {
                colCounter = 0;
                for(String tag: tags) {
                    stream = data.getFiniteDataOf(tag);
                    if (stream.intLength() <= i) {
                        dataStrInRow[colCounter++] = "";
                    } else {
                        dataStrInRow[colCounter++] = Double.toString(stream.get(i));
                    }
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
