import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;

public class FileWriter {
    private final String dir;
    private final String filename;
    private List<String> refTileList = new ArrayList<>();
    private List<String> floTileList = new ArrayList<>();
    private List<Integer> imgNumbers = new ArrayList<>();
    
    FileWriter(String dir, String filename) {
        this.dir = dir;
        this.filename = filename;
    }

    public List<String> getRefTiles() {
        return refTileList;
    }

    public List<String> getFloTiles() {
        return floTileList;
    }
    
    public List<Integer> getImgNumbers() {
        return imgNumbers;
    }

    public void imgNumbers() {
        for(String s: refTileList) {
            String tmp = s.replace("Tile", "");
            tmp = tmp.replace(".txt", "");
            imgNumbers.add(Integer.parseInt(tmp));
        }
    }
    //Writes formatted to output log files
    public void outputBuilder(String lines, int image_num) {
        try {
            if(lines.equals("ERROR")) return;
            refTileList.add(filename + image_num + ".txt");
            PrintWriter writer = new PrintWriter(new File(dir + "/" + filename + image_num + ".txt"));
            writer.println(lines);
            writer.close();
        } catch(FileNotFoundException e) {
            System.out.println(e);
            System.exit(-1);
        }
    }

    public void outputBuilder2(String lines, int image_num) {
        try {
            if(lines.equals("ERROR")) return;
            floTileList.add(filename + image_num + "_flo.txt");
            PrintWriter writer = new PrintWriter(new File(dir + "/" + filename + image_num + "_flo.txt"));
            writer.println(lines);
            writer.close();
        } catch(FileNotFoundException e) {
            System.out.println(e);
            System.exit(-1);
        }
    }
}
