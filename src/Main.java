import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {

        if(args[0].trim().toLowerCase().startsWith("command")) {
            commandLineTool();
            System.exit(-1);
        }
    
        String dir;
        String out_dir;
        String filename;
        
        boolean saveStack = false;
        boolean projected = true;
        int slices = 0;


        dir = args[0];
        out_dir = args[1];
        filename = args[2];
        if(args[3].trim().toLowerCase().startsWith("t")) {
            saveStack = true;
        }

        if(args.length > 4) {
            try{
                slices = Integer.parseInt(args[4]);
            } catch(NumberFormatException n) {
                n.printStackTrace();
            }
        }

        orgHandler oH = new orgHandler(dir, out_dir, filename);
        FrankenStitch frank = new FrankenStitch();

        for(int i = 0; i < oH.refTileList.size(); i++) {
            frank.BigStitch(0, oH.refTileList.get(i), dir, filename + (oH.tiledImgNums.get(i)) + "_REF.tif", out_dir, 
            false, true, false, 0);
        }

        ConfigConverter cc = new ConfigConverter(out_dir, oH.dims, oH.refTileList, oH.tiledImgNums, filename);

        for(int i = 0; i < cc.printList.size(); i++) {
            oH.outputBuilder(cc.printList.get(i), oH.floTileList, cc.finalImgNums.get(i), "_flo");
        }

        for(int i = 0; i < cc.floTiles.size(); i++) {

            String name = filename + cc.finalImgNums.get(i) + "_FLO.tif";

            if(!Arrays.asList(oH.existingFused).contains(name)) {
                frank.BigStitch(1, cc.floTiles.get(i), dir, name, out_dir, 
                projected, false, saveStack, slices);
            }
        }

        for (int i = 0; i < oH.singleRefs.size(); i++) {

            String name = filename + oH.singleImgNums.get(i) + "_FLO.tif";

            System.out.println(oH.singleRefs);
            System.out.println(oH.singleFlos);

            String refPath = dir + "/" + oH.singleRefs.get(i);
            String floPath = dir + "/" + oH.singleFlos.get(i);

            String out_path = out_dir + "/" + name;

            Projector.singleTileProject(refPath , floPath, out_path, slices, saveStack);
        }

        cc.dirCleaner();
    }

    private static void commandLineTool() {
        startScreen();
        
        UserIn in = new UserIn();        

        String dir = in.getInput("Enter Image Directory");
        String out_dir = in.getInput("Enter output directory (Stitched images will be saved here)");
        System.out.println("\nIf you are unclear on what to input for image names, see the README on github.\n");
        String fileName = in.getInput("Enter the name of the images (do not include incrementors)").trim();
        
        long start = System.currentTimeMillis();
        
        orgHandler oH = new orgHandler(dir, out_dir, fileName);
        FrankenStitch frank = new FrankenStitch();

        System.out.println("Config Files Built! \n" +
                            "Stitching reference images...");


        for(int i = 0; i < oH.refTileList.size(); i++) {
            frank.BigStitch(0, oH.refTileList.get(i), dir, fileName + (oH.tiledImgNums.get(i)) + "_REF.tif", out_dir, 
            false, true, false, 0);
        }


        ConfigConverter cc = new ConfigConverter(out_dir, oH.dims, oH.refTileList, oH.tiledImgNums, fileName);

        for(int i = 0; i < cc.printList.size(); i++) {
            oH.outputBuilder(cc.printList.get(i), oH.floTileList, cc.finalImgNums.get(i), "_flo");
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
        
        
        for(int i = 0; i < cc.floTiles.size(); i++) {

            String name = fileName + cc.finalImgNums.get(i) + "_FLO.tif";

            if(!Arrays.asList(oH.existingFused).contains(name)) {
                frank.BigStitch(1, cc.floTiles.get(i), dir, name, out_dir, 
                projected, false, saveStack, slices);
            }
        }

        for (int i = 0; i < oH.singleRefs.size(); i++) {

            String name = fileName + oH.singleImgNums.get(i) + "_FLO.tif";

            System.out.println(oH.singleRefs);
            System.out.println(oH.singleFlos);

            String refPath = dir + "/" + oH.singleRefs.get(i);
            String floPath = dir + "/" + oH.singleFlos.get(i);

            String out_path = out_dir + "/" + name;

            Projector.singleTileProject(refPath , floPath, out_path, slices, saveStack);
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
        "                          / ____| |     (_)    v0.1.1 \n" +
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
