import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class FileReader {
    private final String indicator = "Stage coordinates";
    private final String indicator2 = "Pixel Size:";
    private final String directory;
    private final int dim = 2;
    private List<String> printList = new ArrayList<String>();
    private double factor = 0;
    private dirParser parsed; 

    FileReader(String directory, dirParser parsed){
        this.directory=directory;
        this.parsed = parsed;
    }
    
    public List<String> printList() {
        return printList;
    }

    public List<String> getPrintList() {
        return printList;
    }
    
    //Reads in the REF log file and returns only the line with the Stage coordinates
    private String lineReader(String filename) {
        try{
            Scanner in = new Scanner(new File(filename));
            while(in.hasNextLine()) {
                String line = in.nextLine();
                if(line.contains(indicator2)) {
                    String nums = line.substring(line.indexOf(":") + 1, line.length() - 1).trim();
                    String[] nums2 = nums.split(" ");
                    double num = Double.parseDouble(nums2[0]);
                    this.factor = 1 / num;
                }
                if(line.contains(indicator)) {
                    return line;
                }
            }

        }
        catch(FileNotFoundException e) {
            System.out.println("File not found");
        }
        return "";
    }


    private String[] cleaner(String filename) {
        //Takes the line read from the reader and returns an array with the x and y coordinates
        String line = lineReader(filename);
        String line1 = line.substring(line.indexOf("(") + 1, line.length() - 1);
        String[] arr = line1.split(",");
        for (int i=0;i < arr.length; i++) {
            if(arr[i].substring(0, 1).equals("-")) {
                continue;
            }
            arr[i] = arr[i].substring(1);
        }
        String[] arr1 = Arrays.copyOf(arr, 2);
        return arr1;  
    }

    //Loops through specified array of filenames and builds correct format
    //for TileConfiguration. Returns this formatted string to later write.
    private void builder(List<String> dir) {
        String tileFormat = "dim=" + this.dim + "\n\n";

        for(int i = 0; i < dir.size(); i++) {
            String[] coords = cleaner(directory + "/" + dir.get(i));
            tileFormat += (dir.get(i).substring(0, dir.get(i).lastIndexOf(".")) + "; ; " 
            + String.format("(%.2f, %.2f)\n", (Double.parseDouble(coords[0]) * factor * -1.0), 
            (Double.parseDouble(coords[1]) * factor)));   
        }
        if(tileFormat.length() < 10) {
            tileFormat = "ERROR";
        }
        printList.add(tileFormat);
    }

    public void looper(List<List<String>> big_list) {
        for (int i = parsed.getFirstImage() - 1; i < big_list.size(); i++) {
            System.out.println(big_list.get(i));
            builder(big_list.get(i));
        }
    }
}
