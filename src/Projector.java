import ij.plugin.ZProjector;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.RGBStackMerge;
import ij.plugin.ChannelSplitter;


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
        if(full_stack.length < 3) {
            IJ.run(full_stack[0], "Green", "");
        }
        return RGBStackMerge.mergeChannels(full_stack, false);
    }

    public static ImagePlus Merge(ImagePlus flo, ImagePlus ref) {
        ImagePlus[] imps = {flo, ref};
        return RGBStackMerge.mergeChannels(imps, false);
    }
}
