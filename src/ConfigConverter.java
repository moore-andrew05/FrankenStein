
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class ConfigConverter {
    File dir;
    List<String> refTiles = new ArrayList<>();
    List<String> printList = new ArrayList<>();
    int dim;
    List<String> files = new ArrayList<>();

    public ConfigConverter(String dir, int dim, List<String> refTiles) {
        this.dim = dim;
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

    public List<String> getPrintList() {
        return printList;
    }

    public List<String> getFiles() {
        return files;
    }

    private String builder(String filename) {
        Scanner file = fileOpen(filename);
        String rtn = "";
        while(file.hasNextLine()) {
            String line = file.nextLine();
            if(line.startsWith("dim") && dim == 3) {
                rtn += "dim = 3\n";
            } else if(line.startsWith("image")) {
                if (this.dim == 3) {
                    String edited = line.substring(0, line.length() - 1) + ", 0.0)\n";
                    rtn += edited.replaceAll("_REF", "");
                } else {
                    rtn += line.replaceAll("_REF", "") + "\n";
                }
            } else {
                rtn += line + "\n";
            }
        }
        return rtn;
    }

    private void looper(List<String> files) {
        for (String s : files) {
            printList.add(builder(s));
        }
    }

    private Scanner fileOpen(String filename) {
        try {
            return new Scanner(new File(dir + "/" + filename));
        }catch (FileNotFoundException e) {
            System.out.println(e);
            return new Scanner("Hi, I'm a Tile file that shouldn't be here\n" +
            "I don't do anything and I don't break anything, so I'm going to stay for now");
        }
    }
}
