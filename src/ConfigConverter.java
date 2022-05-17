import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class ConfigConverter {
    File dir;
    String[] fileList;
    dirParser parsed;
    List<String> printList = new ArrayList<>();

    FilenameFilter registeredFilter = new FilenameFilter() {
        public boolean accept(File f, String name)
                {
                    return name.contains(".registered");
                }
    };

    public ConfigConverter(String dir, dirParser parsed) {
        this.dir = new File(dir);
        fileList = this.dir.list(registeredFilter);
        looper(fileList);
        this.parsed = parsed;
    }

    public List<String> getPrintList() {
        return printList;
    }

    private String builder(String filename) {
        Scanner file = fileOpen(filename);
        String rtn = "";
        while(file.hasNextLine()) {
            String line = file.nextLine();
            if(line.startsWith("dim")) {
                rtn += "dim=3\n";
            } else if(line.startsWith("image")) {
                String edited = line.substring(0, line.length() - 1) + ", 0.0)\n";
                rtn += edited.replaceAll("_REF", "");
            } else {
                rtn += "\n";
            }
        }
        return rtn;
    }

    private void looper(String[] fileList) {
        for (String s : fileList) {
            printList.add(builder(s));
        }
    }

    private Scanner fileOpen(String filename) {
        try {
            return new Scanner(new File(dir + "/" + filename));
        }catch (FileNotFoundException e) {
            System.out.println(e);
            return null;
        }
    }
}
