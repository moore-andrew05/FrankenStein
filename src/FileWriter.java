import java.io.PrintWriter;
//import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class FileWriter {
    //private final Scanner in = new Scanner(System.in);
    private final String dir;
    private final String filename;
    //private final int num_images;
    

    FileWriter(String dir, String filename) {
        this.dir = dir;
        this.filename = filename;
        //this.num_images = num_images;
    }
    //Writes formatted to output log files
    public void outputBuilder(String lines, int image_num) {
        try {
            PrintWriter writer = new PrintWriter(new File(dir + "/" + filename + image_num + ".txt"));
            writer.println(lines);
            writer.close();
        } catch(FileNotFoundException e) {
            System.out.println(e);
            System.exit(-1);
        }
    }

    public void outputBuilder2(String lines, int image_num) {
        try {
            PrintWriter writer = new PrintWriter(new File(dir + "/" + filename + image_num + "_flo.txt"));
            writer.println(lines);
            writer.close();
        } catch(FileNotFoundException e) {
            System.out.println(e);
            System.exit(-1);
        }
    }
}