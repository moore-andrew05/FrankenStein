import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import java.io.PrintWriter;

public class Slidebook_Converter {
    File dir;
    File out_dir;
    final String filename;
    String currfile;
    
    String[] rawFileList;
    
    List<String> currNames = new ArrayList<String>();
    List<double[]> currCoords = new ArrayList<double[]>();


    List<String> tiles = new ArrayList<String>();
    Set<Integer> imgNums = new HashSet<Integer>();
    List<String[]> bigList = new ArrayList<String[]>();

    
    final double FACTOR = 1 / 0.160494;
    
    FilenameFilter logFilter = new FilenameFilter() {
        public boolean accept(File f, String name)
        {
            return name.contains(".log") && name.contains(filename);
        }
    };

    FilenameFilter imgFilter = new FilenameFilter() {
        public boolean accept(File f, String name)
        {
            return name.contains(".log") && name.contains(currfile);
        }
    };
    
    Slidebook_Converter(String dir, String out_dir, String filename) {
        this.filename = filename;
        this.dir = new File(dir);
        this.out_dir = new File(out_dir);
        this.rawFileList = this.dir.list(logFilter);
        this.imgNums = getImgNums();
        
        for (int num: imgNums) {
            this.currfile = filename + num + " ";
            String[] img = this.dir.list(imgFilter);

            this.currCoords.clear();
            this.currNames.clear();
            
            for (String f: img) {
                lineReader(dir + "/" + f);
            }
            outputBuilder(builder(), num);
        }
    }
    
    private Set<Integer> getImgNums() {
        Set<Integer> rtn = new HashSet<Integer>();

        for (String s: rawFileList) {
            int imgNum = new Scanner(s).useDelimiter("\\D+").nextInt();
            rtn.add(imgNum);
        }
        return rtn;

    }

    private void lineReader(String filename) {

        int line_ind = 0;
        double[] coords = new double[2];
        try{
            Scanner in = new Scanner(new File(filename));
            while(in.hasNextLine()) {
                String line = in.nextLine();

                if(line_ind > 0) {
                    String[] data = line.split("\\t");
                    coords[0] = Double.parseDouble(data[1]); 
                    coords[1] = Double.parseDouble(data[2]);
                    currNames.add(data[data.length - 1]);
                    line_ind -= 1;
                }
                if(line.trim().startsWith("IFD")) {
                    line_ind = 1;
                }

            }
            currCoords.add(coords);
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found");
        }
    }

    private String builder() {
        String tileFormat = "dim=" + 2 + "\n\n";

        for(int i = 0; i < currNames.size(); i++) {
            double[] coords = currCoords.get(i);
            tileFormat += (currNames.get(i)) + "; ; " 
            + String.format("(%.2f, %.2f)\n", (coords[0] * FACTOR * -1.0), 
            (coords[1] * FACTOR));   
        }
        if(tileFormat.length() < 10) {
            tileFormat = "ERROR";
        }
        return tileFormat;
    }

    public void outputBuilder(String lines, int img_num) {
        try {
            if(lines.equals("ERROR")) return;
            PrintWriter writer = new PrintWriter(new File(out_dir + "/" + this.filename + img_num +  ".txt"));
            writer.println(lines);
            writer.close();
            tiles.add(this.filename + img_num + ".txt");
        } catch(FileNotFoundException e) {
            System.out.println(e);
            System.exit(-1);
        }
    }
}
