import ij.plugin.ZProjector;
import ij.ImagePlus;
import ij.plugin.RGBStackMerge;
import ij.plugin.ChannelSplitter;

public class Projector {
    public static ImagePlus Zproject(ImagePlus imp, int slices) {
        if(!imp.isHyperStack()) return null;

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
        return RGBStackMerge.mergeChannels(full_stack, false);
    }

    public static ImagePlus Merge(ImagePlus flo, ImagePlus ref) {
        ImagePlus[] imps = {flo, ref};
        return RGBStackMerge.mergeChannels(imps, false);
    }
}
