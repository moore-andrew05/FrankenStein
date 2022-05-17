import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class FileReader {
    private final String indicator = "Stage coordinates";
    private final String directory;// = "Z:/Jessica/Live_Bacterial_Imaging/20220329_CL2122worms";
    private final int dim = 2;
    //private final String name = "image";
    private List<String> printList = new ArrayList<String>();
    private final double factor = 9.330099;//9.27550915;
    private dirParser parsed; 

    FileReader(String directory, dirParser parsed){
        this.directory=directory;
        this.parsed = parsed;
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
                //System.out.println(line);
                if(line.contains(indicator)) {
                    //System.out.println(line);
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
        printList.add(tileFormat);
    }

    public void looper(List<List<String>> big_list) {
        for (int i = parsed.getFirstImage() - 1; i < big_list.size(); i++) {
            System.out.println(big_list.get(i));
            builder(big_list.get(i));
        }
    }



    public static void main(String[] args) {
        //FileReader test = new FileReader();
        //dirParser test1 = new dirParser("Z:/Jessica/Live_Bacterial_Imaging/20220328_CL2122worms");
        // System.out.println(Arrays.toString(test1.getrawFileList()));
        // test.looper(test1.getrawFileList());
        // test1.bigCleaner(test1.getrawFileList());
        // for (int i = 0; i < test1.getcleanfileList().size(); i++) {
        //     System.out.println(test.looper(test1.getcleanfileList().get(i)));
        // }


        //System.out.println(test.cleaner("image1_1_R3D_REF.dv.log")[1]);
        /*
        Scanner userIn = new Scanner(System.in);
        System.out.println("Please enter image number: ");
        int image_n = userIn.nextInt();
        System.out.println("Enter first point: ");
        int f = userIn.nextInt();
        System.out.println("Enter last point");
        int x = userIn.nextInt();
        userIn.close();
        */
        /*
        for (int i = f; i <= x; i++) {
            String filename = "image" + image_n + "_" + i + "_R3D_REF.dv.log";
            System.out.println(Arrays.toString(test.cleaner(filename)));
        }
        */
        
    }

}
