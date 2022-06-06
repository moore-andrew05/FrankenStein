
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class ConfigConverter {
    File dir;
    List<String> refTiles = new ArrayList<>();
    List<String> printList = new ArrayList<>();
    List<Integer> dims = new ArrayList<>();
    List<String> files = new ArrayList<>();

    public ConfigConverter(String dir, List<Integer> dims, List<String> refTiles) {
        this.dims = dims;
        this.dir = new File(dir);
        this.refTiles = refTiles;
        listBuilder();
        looper(files);
    }

    private void listBuilder() {
        for(String s: this.refTiles) {
            if(s==null) continue;
            String tmp = s.replace(".txt", ".registered.txt");
            this.files.add(tmp);
        }
    }

    private List<String> listBuilder2(List<String> in) {
        List<String> out = new ArrayList<>();
        for(String s: in) {
            out.add(s.replace(".txt", "_flo.txt"));
        }
        return out;
    }

    public List<String> getPrintList() {
        return printList;
    }

    public List<String> getFiles() {
        return files;
    }

    private String builder(String filename, int dim) {
        Scanner file = fileOpen(filename);
        String rtn = "";
        while(file.hasNextLine()) {
            String line = file.nextLine();
            if(line.startsWith("dim") && dim == 3) {
                rtn += "dim = 3\n";
            } else if(line.startsWith("image")) {
                if (dim == 3) {
                    String edited = line.substring(0, line.length() - 1) + ", 0.0)\n";
                    rtn += edited.replaceAll("_REF", "");
                } else {
                    rtn += line.replaceAll("_REF", "") + "\n";
                }
            } else if(line.equals("ERROR")) {
                rtn = "ERROR";
            } else {
                rtn += line + "\n";
            }
        }
        return rtn;
    }

    private void looper(List<String> files) {
        for (int i = 0; i < files.size(); i++) {
            printList.add(builder(files.get(i), dims.get(i)));
        }
    }

    private Scanner fileOpen(String filename) {
        try {
            return new Scanner(new File(dir + "/" + filename));
        }catch (FileNotFoundException e) {
            System.out.println(e);
            return new Scanner("ERROR");
            // "\n\n" +
            // "#Hi, I'm a Tile file that shouldn't be here\n" +
            // "#I don't do anything and I don't break anything, so I'm going to stay for now");
        }
    }

    public void dirCleaner() {
        List<String> tiles = refTiles;
        List<String> flo_tiles = listBuilder2(refTiles);
        List<String> reg_tiles = files;
        for(int i = 0; i < tiles.size(); i++) {
            File[] filepaths = {new File(dir + "/" + tiles.get(i)), new File(dir + "/" + flo_tiles.get(i)), new File(dir + "/" + reg_tiles.get(i))};
            for(File f: filepaths) {
                f.delete();
            }
        }
    }
}
