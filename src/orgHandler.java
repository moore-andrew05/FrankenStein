import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.PrintWriter;

public class orgHandler {

    File dir;
    File out_dir;
    final String fileName;

    String[] rawFileList;
    String[] existingFused;

    List<List<String>> cleanFileList = new ArrayList<List<String>>();
    public List<String> singleTiles = new ArrayList<String>();
    public List<String> singleFloTiles = new ArrayList<String>();

    List<Integer> dims = new ArrayList<Integer>();
    List<Integer> tiledImgNums = new ArrayList<Integer>();
    List<Integer> singleImgNums = new ArrayList<Integer>();

    public List<String> refTileList = new ArrayList<>();
    public List<String> floTileList = new ArrayList<>();

    int lastImg;
    boolean hasLeadingZeroes = false;



    private final String STAGE_INDICATOR = "Stage coordinates";
    private final String PIXEL_INDICATOR = "Pixel Size:";
    private final String TILE_NAME = "Tile";
    
    private final int REFERANCE_DIMS = 2;
    private double FACTOR = 0;

    public List<String> printList = new ArrayList<String>();




    FilenameFilter refLogFilter = new FilenameFilter() {
        public boolean accept(File f, String name)
            {
                return name.contains("REF.dv.log");
            }
    };

    FilenameFilter existingFilter = new FilenameFilter() {
        public boolean accept(File f, String name)
                {
                    return name.contains("Fused") && (name.contains("FLO") || name.contains("PRO"));
                }
    };




    orgHandler(String dir, String out_dir, String fileName) {
        this.dir = new File(dir);
        this.out_dir = new File(out_dir);
        this.fileName = fileName;

        rawFileList = this.dir.list(refLogFilter);
        existingFused = this.dir.list(existingFilter);

        this.lastImg = findLastImage();
        leadingZeroes();
        bigCleaner(rawFileList);


        List<String> tmp = new ArrayList<>();
        for(List<String> tiles: cleanFileList) {
            tmp.add(tiles.get(0));
        }

        tiledImgNums = getImgNums(tmp);
        singleImgNums = getImgNums(singleTiles);

        findDims(tmp);

        for (int i = 0; i < cleanFileList.size(); i++) {
            builder(cleanFileList.get(i));
        }

        for (int i = 0; i < printList.size(); i++) {
            outputBuilder(printList.get(i), refTileList, tiledImgNums.get(i), "");
        }
    }




    private List<Integer> getImgNums(List<String> tiles) {
        List<Integer> rtn = new ArrayList<Integer>();

        for(String s: tiles) {
            String tmp = s.substring(fileName.length(), s.indexOf("_", fileName.length()));
            rtn.add(Integer.parseInt(tmp));
        }

        return rtn;
    }

    private void findDims(List<String> tiles) {
        for(int i = 0; i < cleanFileList.size(); i++) {
            
            String file = tiles.get(i).replace("_REF", "");

            Scanner in = openFile(dir + "/" + file);

            try {
                while(in.hasNextLine()) {
                    String line = in.nextLine().toLowerCase();
                    if (line.trim().startsWith("zwt")) {
                        if(Integer.parseInt(line.substring(line.indexOf(":") + 1, 
                        line.indexOf("x", line.indexOf(":"))).trim()) == 1) {
                            dims.add(2);
                        } else {
                            dims.add(3);
                        }
                    } 
                }
            } catch (NumberFormatException e) {
                    e.printStackTrace();
            }
        } 
    }
    
    private Scanner openFile(String path) {
        try {
            File tmp = new File(path);
            Scanner rtn = new Scanner(tmp);
            return rtn;
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return new Scanner("");
    }

    private void leadingZeroes() {
        String s = rawFileList[0].substring(fileName.length());
        if (s.startsWith("0")) hasLeadingZeroes = true;
    }





    private int findLastImage() {
        int max = 0;
        for(int i = 0; i < rawFileList.length; i++) {
            int num = numFromString(rawFileList[i]);   
            if (num > max) max = num;
        }
        return max;
    }

    /**
     * Helper method for finding min and max image numbers, cleans the string using
     * the file name to find the incrementor.
     * @return number of the image from the file name
     */
    private int numFromString(String in) {
        String sub = in.substring(this.fileName.length());
        try (Scanner s = new Scanner(sub).useDelimiter("\\D+")) {
            int rtn = s.nextInt();
            s.close();
            return rtn;
        }
    }

    /**
     * Takes rawImageList generated in constructor and sorts into a 2D array,
     * with rows sorted by image #. Checker is specific to our delta vision
     * file incrementors, but can be easily altered.
     * @param files raw file list.
     */
    public void bigCleaner(String[] files) {
        
        for(int i = 0; i < lastImg; i++) {
            String checker = fileName + (i+1) + "_";
            if(hasLeadingZeroes && i < 9) checker = fileName + "0" + (i+1) + "_";
           
            List<String> tmp = new ArrayList<String>();

            for(int k = 0; k < files.length; k++) {
                if (files[k].startsWith(checker)) {
                    tmp.add(files[k]);
                }
            }
            if(tmp.size() > 1) {
                cleanFileList.add(tmp);
            } else if(tmp.size() == 1) {
                singleTiles.add(tmp.get(0));
            }
        }
        if (cleanFileList.isEmpty()) {
            System.out.println("Files Not Found, Check that your filename is correct!");
            System.exit(-1);
        }
    }

    private String lineReader(String filename) {
        try{
            Scanner in = new Scanner(new File(filename));
            while(in.hasNextLine()) {
                String line = in.nextLine();
                if(line.contains(PIXEL_INDICATOR)) {
                    String nums = line.substring(line.indexOf(":") + 1, line.length() - 1).trim();
                    String[] nums2 = nums.split(" ");
                    double num = Double.parseDouble(nums2[0]);
                    this.FACTOR = 1 / num;
                }
                if(line.contains(STAGE_INDICATOR)) {
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

    private void builder(List<String> tiles) {
        String tileFormat = "dim=" + this.REFERANCE_DIMS + "\n\n";

        for(int i = 0; i < tiles.size(); i++) {
            String[] coords = cleaner(this.dir + "/" + tiles.get(i));
            tileFormat += (tiles.get(i).substring(0, tiles.get(i).lastIndexOf(".")) + "; ; " 
            + String.format("(%.2f, %.2f)\n", (Double.parseDouble(coords[0]) * FACTOR * -1.0), 
            (Double.parseDouble(coords[1]) * FACTOR)));   
        }
        if(tileFormat.length() < 10) {
            tileFormat = "ERROR";
        }
        printList.add(tileFormat);
    }

    //Writes formatted to output log files
    public void outputBuilder(String lines, List<String> tileList, int image_num, String extraName) {
        try {
            if(lines.equals("ERROR")) return;
            tileList.add(TILE_NAME + image_num + extraName + ".txt");
            PrintWriter writer = new PrintWriter(new File(out_dir + "/" + TILE_NAME + image_num + extraName + ".txt"));
            writer.println(lines);
            writer.close();
        } catch(FileNotFoundException e) {
            System.out.println(e);
            System.exit(-1);
        }
    }

    







    public static void main(String[] args) {
        orgHandler oH = new orgHandler("Y:/robert/220711_ELT-2_Regulation_Rep2/ELT2gfp/ELT2rnai", 
        "E:/local_files/Stitching/Outputs/220728_tests", 
        "220711_ELT2gfp_ELT2rnai_worm");

        System.out.println(oH.cleanFileList);
        System.out.println(oH.singleTiles);

        System.out.println(oH.tiledImgNums);
        System.out.println(oH.singleImgNums);

        System.out.println(oH.printList);
    }

    





}