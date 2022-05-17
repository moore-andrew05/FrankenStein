
public class Main {
    public static void main(String[] args) throws Exception {
        UserIn in = new UserIn();        

        String dir = in.getInput("Enter log file directory");
        String out_dir = in.getInput("Enter output directory");
        String out_name = in.getInput("Enter configuration file name (Will be incremented based on # of images in folder) [Do not add .txt to end]");

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
            frank.BigStitch(0, fw.getRefTiles().get(i), dir, "Fused" + (dp.getFirstImage() + i) + "_REF.tif");
        }

        ConfigConverter cc = new ConfigConverter(dir, dp);
        for(int i = 0; i < cc.getPrintList().size(); i++) {
            fw.outputBuilder2(cc.getPrintList().get(i), dp.getFirstImage() + i);
        }

        System.out.println("Final Config files saved, Stitching full Stacks...");

        for(int i = 0; i < fw.getFloTiles().size(); i++) {
            frank.BigStitch(1, fw.getFloTiles().get(i), dir, "Fused" + (dp.getFirstImage() + i) + "_FLO.tif");
        }
    }
}
