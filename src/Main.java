public class Main {
    public static void main(String[] args) throws Exception {
        UserIn in = new UserIn();        

        String dir = in.getInput("Enter log file directory");
        String out_dir = in.getInput("Enter output directory");
        String out_name = in.getInput("Enter configuration file name (Will be incremented based on # of images in folder)");

        dirParser dp = new dirParser(dir);
        FileReader fr = new FileReader(dir, dp);
        FileWriter fw = new FileWriter(out_dir, out_name);

        dp.bigCleaner(dp.getrawFileList());
        
        fr.looper(dp.getcleanfileList());
        for(int i = 1; i <= fr.getPrintList().size(); i++) {
            //System.out.println(fr.getPrintList().get(i));
            fw.outputBuilder(fr.getPrintList().get(i-1), dp.getFirstImage() - 1 + i);
        }

        System.out.println("Config Files Built! \n" +
                            "Stitch reference images, when finished, press enter.");

        in.getInput("\nPress Enter When Ready:");

        ConfigConverter cc = new ConfigConverter(dir, dp);
        for(int i = 0; i < cc.getPrintList().size(); i++) {
            fw.outputBuilder2(cc.getPrintList().get(i), dp.getFirstImage() + i);
        }

        System.out.println("Final Config files saved, proceed to stitch full z stacks.");
    }
}
