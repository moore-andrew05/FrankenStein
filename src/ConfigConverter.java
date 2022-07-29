
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;

public class ConfigConverter {
    File out_dir;
    final String fileName;

    List<String> refTiles = new ArrayList<>();
    List<String> floTiles = new ArrayList<>();
    List<String> regTiles = new ArrayList<>();
    
    public List<String> printList = new ArrayList<>();
    public List<Integer> finalImgNums = new ArrayList<>();

    List<Integer> dims = new ArrayList<>();
    List<String> files = new ArrayList<>();

    public ConfigConverter(String out_dir, List<Integer> dims, List<String> refTiles, List<Integer> nums, String fileName) {
        this.dims = dims;
        this.out_dir = new File(out_dir);
        this.finalImgNums = nums;
        this.refTiles = refTiles;
        this.fileName = fileName;
        
        regBuilder();
        printBuilder(regTiles);

        floBuilder();
    }

    FilenameFilter stitched = new FilenameFilter() {
        public boolean accept(File f, String name) 
        {
            return name.contains(".registered.txt");
        }
    };

    
    private void regBuilder() {
        for(String s: this.refTiles) {
            if(s==null) continue;
            String tmp = s.replace(".txt", ".registered.txt");
            this.regTiles.add(tmp);
        }
    }

    private void floBuilder() {
        for(String s : this.regTiles) {
            String tmp = s.replace(".registered", "_flo");
            this.floTiles.add(tmp);
        }
    }

    private String builder(String filename, int dim) {
        Scanner file = fileOpen(filename);
        String rtn = "";
        while(file.hasNextLine()) {
            String line = file.nextLine();
            if(line.startsWith("dim") && dim == 3) {
                rtn += "dim = 3\n";
            } else if(line.startsWith(this.fileName)) {
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
        file.close();
        return rtn;
    }

    private void printBuilder(List<String> files) {
        for (int i = 0; i < files.size(); i++) {
            printList.add(builder(files.get(i), dims.get(i)));
        }

        for (int i = 0; i < printList.size(); i++) {
            if(printList.get(i) == "ERROR") {
                printList.remove(i);
                dims.remove(i);
                regTiles.remove(i);
                finalImgNums.remove(i);
            }
        }
    }

    private Scanner fileOpen(String filename) {
        try {
            return new Scanner(new File(out_dir + "/" + filename));
        }catch (FileNotFoundException e) {
            System.out.println(e);
            return new Scanner("ERROR");
        }
    }

    /**
     * Loops through output directory and deletes all tile config files to clean up directory.
     */
    public void dirCleaner() {
        for(String s: refTiles) {
            new File(out_dir + "/" + s).delete();
        }
        for(String s: regTiles) {
            new File(out_dir + "/" + s).delete();
        }
        for(String s: floTiles) {
            new File(out_dir + "/" + s).delete();
        }
    }
}
