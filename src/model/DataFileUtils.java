package model;

import model.datasource.*;
import model.filter.ButterworthFilter;
import net.razorvine.pyro.PyroProxy;
import oracle.PyOracle;
import view.component.trainingview.TrainingDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class DataFileUtils {
    private static final int OPENBCI_COMMENTS_LINE_NUM = 5;
    private static final String IMAGE_TYPE = "png";
    private final PyroProxy jsonFileOracle;
    private File workingFile;
    private File workingDirectory;
    private Map<String, Integer> sliceRecord;
    private static DataFileUtils instance = new DataFileUtils();

    public static DataFileUtils getInstance() {
        return instance;
    }

    private DataFileUtils() {
        this.sliceRecord = new HashMap<String, Integer>();

        this.jsonFileOracle = PyOracle.getInstance().getOracle("json-file");
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
        fileChooser.setCurrentDirectory(this.workingDirectory);
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

    public void goOutDirectory() {
        this.workingDirectory = this.workingDirectory.getParentFile();
    }

    private void setWorkingFile(File file) {
        this.workingFile = file;
        this.workingDirectory = this.workingFile.getParentFile();
    }

    public String saveFragmentDataSources(String dirName, Collection<FragmentDataSource> data) {
        this.goIntoDirectory(dirName);

        File rootDir = this.workingDirectory;
        String rootDirPath = null;
        List<String[]> fragmentHeaders = new LinkedList<>();
        Map<String, Integer> ids = new HashMap<>();
        String tag;
        String[] header;
        int id;

        for (FragmentDataSource fragment: data) {
            header = new String[3];
            tag = header[0] = fragment.getFragmentTag();
            id = ids.getOrDefault(header[0], 0);
            header[1] = String.valueOf(id);
            ids.put(tag, ++id);
            header[2] = String.valueOf(fragment.getStartingPosition());
            fragmentHeaders.add(header);

            makeSureInSpecSubDir(dirName, tag);
            this.saveDataSource(tag + "_" + header[1] + ".csv", fragment);
        }

        this.workingDirectory = rootDir;

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
            rootDirPath = rootDir.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.goOutDirectory();
        return rootDirPath;
    }

    private void makeSureInSpecSubDir(String parentDirName, String subDirName) {
        if (!this.workingDirectory.getName().equals(subDirName)) { // may in parent
            if (!this.workingDirectory.getName().equals(parentDirName)) { // in another sub of parent
                this.goOutDirectory();
            }
            this.goIntoDirectory(subDirName);
        }
    }

    public Map<String, Collection<FragmentDataSource>> loadFragmentDataSources(File dirFile) {
        Map<String, Collection<FragmentDataSource>> result = new HashMap<>();
        this.workingDirectory = dirFile;
        String dirName = dirFile.getName();
        File headerFile = new File(this.workingDirectory, "fragment_headers.csv");
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
                this.makeSureInSpecSubDir(dirName, tag);
                fragmentFile = new File(this.workingDirectory, tag + "_" + entries[1] + ".csv");
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

        bciData.addFilters(ButterworthFilter.NOTCH_60HZ, ButterworthFilter.BANDPASS_1_50HZ);
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
            tirmSpaces(headers);
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

    private static void tirmSpaces(String[] texts) {
        for (int i = 0; i < texts.length; i++) {
            texts[i] = texts[i].trim();
        }
    }

    public String saveDataSource(String name, FiniteLengthDataSource data) {
        name = name.trim();
        if (name.isEmpty()) {
            return null;
        }

        return saveDataSource(name, data, 0, data.intLength() - 1);
    }


    public String saveSlice(FragmentDataSource data) {

        String sliceTag = data.getFragmentTag();
        Integer recordNum = this.sliceRecord.get(sliceTag);
        recordNum = recordNum != null? recordNum + 1 : 0;
        this.sliceRecord.put(sliceTag, recordNum);

        String filename = sliceTag + "_" + recordNum + ".csv";
        return this.saveDataSource(filename, data);
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

    public String saveImage(BufferedImage imageBuffer, String imageName) {
        this.goIntoDirectory("images");
        String path = null;
        File imageFile = new File(this.workingDirectory, imageName + "." + IMAGE_TYPE);
        try {
            ImageIO.write(imageBuffer, IMAGE_TYPE, imageFile);
            path = imageFile.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.goOutDirectory();

        return path;
    }

    public String saveStructureAsJson(Map<String, Object> structure, String organizingFolderName, String fileName) {
        boolean hasOrganizingFolder = organizingFolderName != null;
        if (hasOrganizingFolder) {
            this.goIntoDirectory(organizingFolderName);
        }
        String path = null;
        try {
            path = new File(this.workingDirectory, fileName + ".json").getCanonicalPath();
            jsonFileOracle.call_oneway("save", structure, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (hasOrganizingFolder) {
            this.goOutDirectory();
        }

        return path;
    }

    public Map<String, Object> loadJsonAsStructure(File jsonFile) {
        try {
            return (Map<String, Object>) jsonFileOracle.call("load", jsonFile.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showSavedDialog(Component parentComponent, String filePath) {
        JOptionPane.showConfirmDialog(parentComponent, "Saved as \"" + filePath + "\"", "Info", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }
}
