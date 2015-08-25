package model;

import model.datasource.*;
import model.filter.ButterworthFilter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class DataFileUtils {
    private static final int OPENBCI_COMMENTS_LINE_NUM = 5;
    private File workingFile;
    private File workingDirectory;
    private Map<String, Integer> sliceRecord;
    private static DataFileUtils instance = new DataFileUtils();

    public static DataFileUtils getInstance() {
        return instance;
    }

    private DataFileUtils() {
        this.sliceRecord = new HashMap<String, Integer>();
    }

    public File loadFileDialog(Component frame, String fileExtension) {

        JFileChooser fileChooser = new JFileChooser();
        if (fileExtension.equals("dir")) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("data Files", fileExtension);
            fileChooser.setFileFilter(filter);
        }
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(workingDirectory);
        if (fileChooser.showDialog(frame, "Load") == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }



    public File getWorkingFile() {
        return this.workingFile;
    }

    public void goIntoDirectory(String dirName) {
        workingDirectory = new File(this.workingDirectory, dirName);
        workingDirectory.mkdir();

    }

    private void setWorkingFile(File file) {
        this.workingFile = file;
        this.workingDirectory = this.workingFile.getParentFile();
    }

    public void saveFragmentDataSources(String dirName, Collection<FragmentDataSource> data) {
        goIntoDirectory(dirName);
        List<String[]> fragmentHeaders = new LinkedList<>();
        String tag;
        String[] header;
        int id;

        id = 0;
        for (FragmentDataSource fragment: data) {
            header = new String[3];
            header[0] = fragment.getFragmentTag();
            header[1] = String.valueOf(id++);
            header[2] = String.valueOf(fragment.getStartingPosition());
            fragmentHeaders.add(header);
            this.save(header[0] + "_" + header[1] + ".csv", fragment);
        }


        File newFlie = new File(this.workingDirectory, "fragment_headers.csv");
        try {
            newFlie.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFlie));

            writer.write(String.join(",", new String[]{"tag", "id", "timestamp"}));
            writer.newLine();

            for (String[] row: fragmentHeaders) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.workingDirectory = this.workingDirectory.getParentFile();
    }

    public Map<String, Collection<FragmentDataSource>> loadFragmentDataSource(File dirFile) {
        Map<String, Collection<FragmentDataSource>> result = new HashMap<>();

        File headerFile = new File(dirFile, "fragment_headers.csv");
        try {
            List<String> lines = Files.readAllLines(headerFile.toPath());
            lines = lines.subList(1, lines.size());
            File fragmentFile;
            String tag;
            for (String line: lines) {
                String[] entries = line.split(",");
                tag = entries[0];
                if (!result.containsKey(tag)) {
                    result.put(tag, new LinkedList<>());
                }
                fragmentFile = new File(dirFile, tag + "_" + entries[1] + ".csv");
                result.get(tag).add(new ReconstructedFragmentDataSource(tag, Long.parseLong(entries[2]), loadGeneralCSVFile(fragmentFile)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        this.workingDirectory = dirFile.getParentFile();
        return result;
    }

    public FilteredFiniteDataSource loadOpenBCIRawFile(File rawDataFile) {
        this.setWorkingFile(rawDataFile);

        double[][] rawData =  null;
        try {
            List<String> lines = Files.readAllLines(rawDataFile.toPath());
            lines = lines.subList(OPENBCI_COMMENTS_LINE_NUM, lines.size());
            rawData = new double[8][lines.size()];
            int lineCounter = 0;
            for(String line: lines) {
                String[] entries = line.split(",");
                for (int i = 0; i < 8; i++) {
                    rawData[i][lineCounter] = Double.parseDouble(entries[i + 1]);
                }
                lineCounter++;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rawData == null) {
            return null;
        }

        FilteredFiniteDataSource bciData = new FilteredFiniteDataSource(new SimpleFiniteDataSource(convert2DArrayToMap(rawData)));

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
        this.setWorkingFile(csvFile);
        Map<String, FiniteLengthStream> rawData = null;
        try {
            List<String> lines = Files.readAllLines(csvFile.toPath());
            String[] headers = lines.get(0).split(",");
            lines = lines.subList(1, lines.size());

            double[][] tmpData = new double[headers.length][lines.size()];

            int lineCounter = 0;
            for(String line: lines) {
                String[] entries = line.split(",");
                for (int i = 0; i < tmpData.length; i++) {
                    if (entries[i].toUpperCase().equals("NAN")) {
                        entries[i] = "NaN";
                    }
                    tmpData[i][lineCounter] = Double.parseDouble(entries[i]);
                }
                lineCounter++;
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

        return new SimpleFiniteDataSource(rawData);
    }

    public String save(String name, FiniteLengthDataSource data) {
        name = name.trim();
        if (name.isEmpty()) {
            return null;
        }

        return saveDataSource(name, data, 0, data.intLength() - 1);
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

        File newFlie = new File(this.workingDirectory, filename);
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
