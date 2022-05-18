
public class Main {
    public static void main(String[] args) throws Exception {
        UserIn in = new UserIn();        

        String dir = in.getInput("Enter log file directory (Tile Configuration Files will be saved here as well)");
        String out_dir = in.getInput("Enter output directory (Stitched images will be saved here)");
        String out_name = in.getInput("Enter configuration file name" + 
        " (Will be incremented based on # of images in folder) \n[Do not add .txt to end]");

        dirParser dp = new dirParser(dir);
        FileReader fr = new FileReader(dir, dp);
        FileWriter fw = new FileWriter(out_dir, out_name);

        dp.bigCleaner(dp.getrawFileList());
        
        fr.looper(dp.getcleanfileList());
        for(int i = 1; i <= fr.getPrintList().size(); i++) {
            //System.out.println(fr.getPrintList().get(i));
            fw.outputBuilder(fr.getPrintList().get(i-1), dp.getFirstImage() - 1 + i);
        }

        FrankenStitch frank = new FrankenStitch();
        System.out.println("Config Files Built! \n" +
                            "Stitching reference images...");

        for(int i = 0; i < fw.getRefTiles().size(); i++) {
            frank.BigStitch(0, fw.getRefTiles().get(i), dir, "Fused" + (dp.getFirstImage() + i) + "_REF.tif", out_dir);
        }

        ConfigConverter cc = new ConfigConverter(dir, dp);
        for(int i = 0; i < cc.getPrintList().size(); i++) {
            fw.outputBuilder2(cc.getPrintList().get(i), dp.getFirstImage() + i);
        }

        System.out.println("Reference Images Stitched and Tile Configurations Registered Succesfully!");
        System.out.println("Would you like to proceed to Stitching full z-stacks?" +
                            "\nWARNING: Only stitch full z-stacks if you allocated additional" +
                            " memory when running the jar." +
                            "\nYou will get a heap space error if you attempt to run without additional memory.");
        String choice = in.getInput("Press enter/return to stitch full stacks, type (e)xit to exit without stitching");

        if (choice.trim().toLowerCase().startsWith("e")) System.exit(-1);

        for(int i = 0; i < fw.getFloTiles().size(); i++) {
            frank.BigStitch(1, fw.getFloTiles().get(i), dir, "Fused" + (dp.getFirstImage() + i) + "_FLO.tif", out_dir);
        }
    }
}
