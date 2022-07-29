# **FrankenStein (Or Frankenstein's Monster)**

Created by Andrew Moore

### **Built around FIJI Stitching Plugin** 
S. Preibisch, S. Saalfeld, P. Tomancak (2009) “Globally optimal stitching of tiled 3D microscopic image acquisitions”, Bioinformatics, 25(11):1463-1465.


## **How to Use**

### ***What is this for?***

Stitching images of later stage *c. Elegans* when taking images at higher magnification (60x or 100x) can become very tedious if done completely by hand. Because of the worm's shape, taking grid style images for merging is inefficient and wasteful. By using stage position metadata we can stitch entire images at once instead of manually moving pariwise through the image. This program is currently specific to DeltaVision directory structures and log files.

This program will create a stitched reference image and a stitched composite image containing all original z-stacks and channels for fluorescence if desired. These composite images can be very large (10 GB+) so additional post processing such as projecting and merging with reference images can automatically be performed if desired. 

### *Installation and Running*

Navigate to the newest release and download the jar file contained in the assets. Place the jar wherever you want to run it from on your computer. Then using terminal (macOS) or powershell (Windows), cd into the directory that contains the jar. Run the following line:

> \> java -Xmx14G -jar FrankenStein_{version}.jar

Replace {version} with the downloaded version, your command line argument should match the name of the jar.

*Note: The Xmx command specifies the max heap space available to the Java VM. Depending on the size of the images being worked with, java will very quickly run out of heap space, requiring that we allocate more memory. The example above allocates 14Gb of memory. Some image sets may require up to ~30Gb. Even if you do not have the required amount of physical memory in your system, you may be able to set the Xmx high enough as it utilizes virtual memory. This will significantly slow the performance of the program, but will allow the program to complete. Regardless, if you get a java heap space error at any point, rerun the program with higher memory allocation.*

Follow the prompts in the program and you should have stitched images!

### *Tips for Naming Images*

DeltaVision images with multiple tiles are incremented using two seperate incrementors following the image name and separated by an underscore. (e.g. "image1_1..."; the two incrementors are 1_1). The first incrementor is for each "image", or set of points; the second tracks the number of points in a single image (Note: the second incrementor will not always start with 1 depending on how you set the points up when you image). The program will ask for your image names at the start. If your files were named like the previous example, image1_1, you will just plug in "image" for the file names when prompted.

You should be able to name your images anything without throwing an error, but there may be errors if your filenames are especially "challenging"; not all cases have been tested. Importantly, the image names not including the incrementor must all be the same in a single directory. If you need different image names, use a separate directory and run the program twice.

### *Known Issues*

-Directories with only one image may not work.

-If only reference images are present, the program will throw an error. Images will be stitched properly but Tile configs will not be cleaned up.

