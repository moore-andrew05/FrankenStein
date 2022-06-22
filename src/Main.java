
public class Main {
    public static void main(String[] args) throws Exception {
        startScreen();
        
        UserIn in = new UserIn();        

        String dir = in.getInput("Enter Image Directory");
        String out_dir = in.getInput("Enter output directory (Stitched images will be saved here)");
        System.out.println("\nIf you are unclear on what to input for image names, see the README on github.\n");
        String fileName = in.getInput("Enter the name of the images (do not include incrementors)").trim();
        
        final String OUT_NAME = "Tile";
        long start = System.currentTimeMillis();
        
        dirParser dp = new dirParser(dir, out_dir, fileName);
        FileReader fr = new FileReader(dir, dp);
        FileWriter fw = new FileWriter(out_dir, OUT_NAME);
        
        fr.looper(dp.getcleanfileList());
        for(int i = 0; i < fr.getPrintList().size(); i++) {
            fw.outputBuilder(fr.getPrintList().get(i), dp.getFirstImage() + i);
        }

        FrankenStitch frank = new FrankenStitch();
        System.out.println("Config Files Built! \n" +
                            "Stitching reference images...");

        fw.imgNumbers();
        for(int i = 0; i < fw.getRefTiles().size(); i++) {
            frank.BigStitch(0, fw.getRefTiles().get(i), dir, "Fused" + (fw.getImgNumbers().get(i)) + "_REF.tif", out_dir, 
            false, true, false, 0);
        }

        ConfigConverter cc = new ConfigConverter(out_dir, dp.getDims(), fw.getRefTiles());
        
        for(int i = 0; i < cc.getPrintList().size(); i++) {
            fw.outputBuilder2(cc.getPrintList().get(i), fw.getImgNumbers().get(i));
        }
        
        long t1 = System.currentTimeMillis();
        System.out.println("\n\n\n--------------------------------------------------");
        System.out.println("Reference Images Stitched and Tile Configurations Registered Succesfully!");
        System.out.println("Would you like to proceed to Stitching full z-stacks?" +
                            "\nWARNING: Only stitch full z-stacks if you allocated additional" +
                            " memory when running the jar." +
                            "\nYou will get a heap space error if you attempt to run without additional memory.");
        String choice = in.getInput("Press enter/return to stitch full stacks, type (e)xit to exit without stitching");
        
        if (choice.trim().toLowerCase().startsWith("e")) { 
            cc.dirCleaner();
            endScreen();
            System.exit(-1);
        }      
        String choice2 = in.getInput("If stitching full z-stacks, type (y)es to MAX project and save");
        int slices = 0;
        boolean projected = false;
        boolean saveStack = false;

        if (choice2.trim().toLowerCase().startsWith("y")) {
            projected = true;
            String s_slices = in.getInput("How many z-stacks would you like to take off top and bottom of image?"  + 
            "\n(Press enter to keep default of 0)");
            try {
                slices = Integer.parseInt(s_slices.trim());
            } catch (NumberFormatException n) {
                System.out.println("\nDefaults Kept\n");
            }
            String stack_saved = in.getInput("Would you like to also save the full stitched image with z-stacks?" +
            "\n[Not recommended if not needed as file sizes are >5GB in many cases]" +
            "\nType (y)es to save, press enter to continue without saving");
            if (stack_saved.trim().toLowerCase().startsWith("y")) saveStack = true;
        }
        long t2 = System.currentTimeMillis();
        for(int i = 0; i < fw.getFloTiles().size(); i++) {
            String name = "Fused" + fw.getImgNumbers().get(i) + "_FLO.tif";
            if(!dp.getExistingFused().contains(name)) {
                frank.BigStitch(1, fw.getFloTiles().get(i), dir, name, out_dir, 
                projected, false, saveStack, slices);
            }
        }
        cc.dirCleaner();

        long stop = System.currentTimeMillis();
        long sub = t2 - t1;
        timeTaken(start, stop, sub);

        endScreen();
    }

    private static void endScreen() {
        System.out.println("       +==========================================================+\n" +
        String.format("       |%34s%25s\n", "Finished!!", "|") +
        "       +==========================================================+\n");
    }

    private static void startScreen() {
        System.out.println(
        "       +==========================================================+\n" +
        "                  ______               _              \n" +
        "                 |  ____|             | |             \n" +
        "                 | |__ _ __ __ _ _ __ | | _____ _ __  \n" +
        "                 |  __| '__/ _` | '_ \\| |/ / _ \\ '_ \\ \n" +
        "                 | |  | | | (_| | | | |   <  __/ | | |\n" +
        "                 |_|  |_|  \\__,_|_| |_|_|\\_\\___|_| |_|\n" +
        "                           _____ _       _            \n" +
        "                          / ____| |     (_)    v1.1.4 \n" +
        "                         | (___ | |_ ___ _ _ __       \n" +
        "                          \\___ \\| __/ _ \\ | '_ \\      \n" +
        "                          ____) | ||  __/ | | | |    \n" +
        "                         |_____/ \\__\\___|_|_| |_| \n" +
        "       +==========================================================+\n\n");
    }

    private static void timeTaken(long start, long stop, long sub) {
        long dif = stop - start - sub;
        long tsecs = dif / 1000;
        int mins = (int) tsecs / 60;
        int secs = (int) tsecs % 60;
        
        System.out.printf("\nTime Elapsed: %d mins, %d secs\n", mins, secs);
    }
}
