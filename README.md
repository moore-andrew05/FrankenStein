# **FrankenStein (Or Frankenstein's Monster)**

### **Built around FIJI Stitching Plugin** 
S. Preibisch, S. Saalfeld, P. Tomancak (2009) “Globally optimal stitching of tiled 3D microscopic image acquisitions”, Bioinformatics, 25(11):1463-1465.


## **How to Use**

### ***What is this for?***

Stitching images of later stage *c. Elegans* when taking images at higher magnification (60x or 100x) can become very tedious if done completely by hand. This program is currently specific to DeltaVision directory structures like we have. By using stage position metadata we can stitch entire images at once instead of manually moving pariwise through the image.

This program will create a stitched reference image and a stitched composite image containing all original z-stacks and channels for fluorescence if desired. These composite images can be very large (10 GB+) so additional post processing such as projecting and merging with reference images can automatically be performed if desired. 

### *Installation and Running*

Navigate to the newest release and download the jar file contained in the assets. Place the jar wherever you want to run it from on your computer. Then using terminal (macOS) or powershell (Windows), cd into the directory that contains the jar. Run the following line:

> \> java -Xmx14G -jar FrankenStein_<version>.jar

Replace <version> with the downloaded version, your command line argument should match the name of the jar.

*Note: The Xmx command specifies the max heap space available to the Java VM, depending on the size of the images being worked with, java will very quickly run out of heap space, requiring that we allocate more memory. The example above allocates 14Gb of memory. Some image sets may require up to ~30Gb. Even if you do not have the required amount of physical memory in your system, you may be able to set the Xmx high enough as it utilizes virtual memory. This will significantly slow the performance of the program, but will allow the program to complete. Regardless, if you get a java heap space error at any point, rerun the program with higher memory allocation.*

Follow the prompts in the program and you should have stitched images!

### *Known Bugs*

If the program is run multiple times on the same directory and different tile names are used, there will be some errors thrown by java. These errors do not affect the actual stitching, but if you have to run multiple times on the same directory use the same Tile name.
