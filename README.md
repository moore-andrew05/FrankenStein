# **FrankenStein (Or Frankenstein's Monster)**

### **Based on FIJI Stitching Plugin** 
S. Preibisch, S. Saalfeld, P. Tomancak (2009) “Globally optimal stitching of tiled 3D microscopic image acquisitions”, Bioinformatics, 25(11):1463-1465.


## **How to Use**

### ***What is this for?***

Stitching later stage *c. Elegans* when taking images at higher magnification (60x or 100x) can become very tedious if done completely by hand. This program is currently specific to DeltaVision directory structures like we have. By using stage position metadata we can stitch entire image at once instead of manually moving pariwise through the image.

This program will create a stitched reference image and a stitched composite image containing all original z-stacks and channels for fluorescence. These composite images can be very large (10 GB+) so additional post processing such as projecting will likely be required to be performed manually if desired (Additional functionality coming soon).

### *Installation and Running*

> \> java -Xmx14G -jar FrankenStein.jar

