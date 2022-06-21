import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.Arrays;

public class dirParser {
    File dir; 
    File out_dir;
    List<String> existingFused = new ArrayList<>();
    String[] rawfileList; //List generated by parsing directory.
    List<List<String>> cleanfileList = new ArrayList<List<String>>(); //Sorted File list.
    List<Integer> dims = new ArrayList<>();
    int first_img;
    int last_img;
    final String fileName;
    boolean hasLeadingZeroes = false;

    FilenameFilter refLogFilter = new FilenameFilter() {
        public boolean accept(File f, String name)
                {
                    return name.contains("REF.dv.log");
                }
    };

    FilenameFilter logFilter = new FilenameFilter() {
        public boolean accept(File f, String name) 
                {
                    return name.contains(".dv.log") && !name.contains("REF");
                }
    };

    FilenameFilter existingFilter = new FilenameFilter() {
        public boolean accept(File f, String name)
                {
                    return name.contains("Fused") && (name.contains("FLO") || name.contains("PRO"));
                }
    };

    dirParser(String dir, String out_dir, String fileName) {
        this.dir = new File(dir);
        this.fileName = fileName.toLowerCase();
        this.out_dir = new File(out_dir);
        rawfileList = this.dir.list(refLogFilter);
        existingFused = Arrays.asList(this.out_dir.list(existingFilter));
        leadingZeroes();
        this.first_img = findFirstImage();
        this.last_img = findLastImage();
        bigCleaner(rawfileList);
        findDims();
    }

    /**
     * checks if given images have incrementors with leading zeroes.
     */
    private void leadingZeroes() {
        String s = rawfileList[0].substring(fileName.length());
        if (s.startsWith("0")) hasLeadingZeroes = true;
    }
    
    /**
     * Finds the dimensions of each image in the set and places them in array.
     * Needed in case there are 2D and 3D images in the directory.
     */
    private void findDims() {
        String last_img = "";
        for(int i = 0; i < getcleanfileList().size(); i++) {
            String file = getcleanfileList().get(i).get(0).replace("_REF", "");
            String start = file.substring(0, file.indexOf("_"));
            if(start.equals(last_img)) continue;
            last_img = start;
            File tmp = new File(dir + "/" + file);
            try {
                try (Scanner in = new Scanner(tmp)) {
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    } 

    public List<String> getExistingFused() {
        return existingFused;
    }

    public List<Integer> getDims() {
        return dims;
    }

    public int getLastImage() {
        return last_img;
    }

    public int getFirstImage() {
        return first_img;
    }

    public String[] getrawFileList() {
        return rawfileList;
    }

    public List<List<String>> getcleanfileList() {
        return cleanfileList;
    }

    /**
     * Returns number of last image in set. Could easily be 
     * rewritten and improved. Currently very specific to our
     * generated delta vision file names.
     * @return the max value of image incrementor
     */

    private int findLastImage() {
        int max = 0;
        for(int i = 0; i < rawfileList.length; i++) {
            int num = numFromString(rawfileList[i]);   
            if (num > max) max = num;
        }
        return max;
    }

    private int findFirstImage() {
        int min = 1000;
        for(int i = 0; i < rawfileList.length; i++) {
            int num = numFromString(rawfileList[i]);
            if (num < min) min = num;
        }
        return min;
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
     * file names, but can be easily altered.
     * @param files raw file list.
     */
    public void bigCleaner(String[] files) {
        int last = getLastImage();
        for(int i = 0; i < last; i++) {
            String checker = fileName + (i+1) + "_";
            if(hasLeadingZeroes && i < 9) checker = fileName + "0" + (i+1) + "_";
            System.out.println(checker);
            List<String> tmp = new ArrayList<String>();
            for(int k = 0; k < files.length; k++) {
                if (files[k].toLowerCase().startsWith(checker)) {
                    tmp.add(files[k]);
                }
            }
            if(tmp.size() > 1) {
                cleanfileList.add(tmp);
            }
        }
        if (cleanfileList.isEmpty()) {
            System.out.println("Files Not Found, Check that your filename is correct!");
            System.exit(-1);
        }
    }
}
