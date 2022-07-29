import ij.plugin.ZProjector;

import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.plugin.RGBStackMerge;
import ij.plugin.ChannelSplitter;
import loci.formats.FormatException;
import loci.plugins.BF;


public class Projector {
    public static ImagePlus Zproject(ImagePlus imp, int slices) {
        return ZProjector.run(imp, "max", slices + 1, (imp.getStackSize() / 3) - slices);
    }
    
    public static ImagePlus SplitAndMerge(ImagePlus flo, ImagePlus ref) {
        ImagePlus[] imp_split = ChannelSplitter.split(flo);

        int n = imp_split.length;
        ImagePlus[] full_stack = new ImagePlus[n + 1];

        for(int i = 0; i < n; i++) {
            full_stack[i] = imp_split[i];
        }

        full_stack[n] = ref;
        IJ.run(full_stack[n], "Grays", "");

        if(full_stack.length < 3) {
            IJ.run(full_stack[0], "Green", "");
        }

        return RGBStackMerge.mergeChannels(full_stack, false);
    }
    
    public static ImagePlus Merge(ImagePlus flo, ImagePlus ref) {
        ImagePlus[] imps = {flo, ref};
        return RGBStackMerge.mergeChannels(imps, false);
    }

    public static void singleTileProject(String refPath, String floPath, String out_path, int slices, boolean saveStack) {
        try {
            ImagePlus[] imps = BF.openImagePlus(floPath);            
            ImagePlus[] imps_ref = BF.openImagePlus(refPath);
        
            ImagePlus imp = imps[0];
            ImagePlus imp_ref = imps_ref[0];
    
            if(saveStack) {
                FileSaver z = new FileSaver(imp);
                z.saveAsTiffStack(out_path);
            }

            ImagePlus imp_proj = Zproject(imp, slices);

            FileSaver r = new FileSaver(imp_ref);
            r.saveAsTiff(out_path.replace("FLO", "REF"));

            ImagePlus imp_merged = SplitAndMerge(imp_proj, imp_ref);
            FileSaver p = new FileSaver(imp_merged);
            p.saveAsTiff(out_path.replace("FLO", "PRO"));

        } catch (FormatException | IOException e) {
            e.printStackTrace();
        }
    }
}
