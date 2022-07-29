import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mdbtools.libmdb.file;







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

    int lastImg;

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
     * file names, but can be easily altered.
     * @param files raw file list.
     */
    public void bigCleaner(String[] files) {
        
        for(int i = 0; i < lastImg; i++) {
            String checker = fileName + (i+1) + "_";
            List<String> tmp = new ArrayList<String>();
            for(int k = 0; k < files.length; k++) {
                if (files[k].toLowerCase().startsWith(checker)) {
                    tmp.add(files[k]);
                }
            }
            if(tmp.size() > 1) {
                cleanFileList.add(tmp);
            }
        }
    }

    





}