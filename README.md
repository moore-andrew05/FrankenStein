# **FrankenStein (Or Frankenstein's Monster)**

Created by Andrew Moore

### **Built around FIJI Stitching Plugin** 
S. Preibisch, S. Saalfeld, P. Tomancak (2009) “Globally optimal stitching of tiled 3D microscopic image acquisitions”, Bioinformatics, 25(11):1463-1465.


## **How to Use**

### ***What is this for?***

Stitching images of later stage *c. Elegans* when taking images at higher magnification (60x or 100x) can become very tedious if done completely by hand. Because of the worm's shape, taking grid style images for merging is inefficient and wasteful. By using stage position metadata we can stitch entire images at once instead of manually moving pariwise through the image. This program is currently specific to DeltaVision directory structures and log files.

This program will create a stitched reference image and a stitched composite image containing all original z-stacks and channels for fluorescence if desired. These composite images can be very large (10 GB+) so additional post processing such as projecting and merging with reference images can automatically be performed if desired. 

### *Installation and Running*

#### *Running on the Command Line*

Navigate to the newest release and download the jar file contained in the assets. Place the jar wherever you want to run it from on your computer. Then using terminal (macOS) or powershell (Windows), cd into the directory that contains the jar. Run the following line:

> \> java -Xmx14G -jar FrankenStein_{version}.jar commandline

Replace {version} with the downloaded version, your command line argument should match the name of the jar. the "commandline" following the jar is a flag to specify that you want to run on the command line.

*Note: The Xmx command specifies the max heap space available to the Java VM. Depending on the size of the images being worked with, java will very quickly run out of heap space, requiring that we allocate more memory. The example above allocates 14Gb of memory. Some image sets may require up to ~30Gb. Even if you do not have the required amount of physical memory in your system, you may be able to set the Xmx high enough as it utilizes virtual memory. This will significantly slow the performance of the program, but will allow the program to complete. Regardless, if you get a java heap space error at any point, rerun the program with higher memory allocation.*

Follow the prompts in the program and you should have stitched images!

#### *Running Headless*

Users that wish to use scripting or who are more comfortable may wish to use flags to run the program without any further input. To do so...

> \> java -Xmx{virtual_mem(int)}G -jar FrankenStein_{version}.jar {input_directory} {output_directory} {filename} {saveStacks (T/F)} {slices (optional)}

Flags:
    
    input_directory: directory that contains the images to be processed (absolute path)
    
    output_directory: directory processed images will be written to (absolute path)
    
    filename: entire filename up to the first incrementor (See below for help)
    
    saveStacks: whether to save full stacks of each image, not recommended if not 
    needed as will take up a lot of space. Simply enter true or false
    
    slices: specifies how many z-stacks to remove from top and bottom of images when projecting. 
    Mostly for late stage worms where top and bottom of image can vary greatly between tiles. 
    Optional parameter, default is 0.

### *Tips for Naming Images*

DeltaVision images with multiple tiles are incremented using two seperate incrementors following the image name and separated by an underscore. (e.g. "image1_1..."; the two incrementors are 1_1). The first incrementor is for each "image", or set of points; the second tracks the number of points in a single image (Note: the second incrementor will not always start with 1 depending on how you set the points up when you image). The program will ask for your image names at the start. If your files were named like the previous example, image1_1, you will just plug in "image" for the file names when prompted.

You should be able to name your images anything without throwing an error, but there may be errors if your filenames are especially "challenging"; not all cases have been tested. Importantly, the image names not including the incrementor must all be the same in a single directory. If you need different image names, use a separate directory and run the program twice.

### *Known Issues*

-If only reference images are present, the program will throw an error. Images will be stitched properly but Tile configs will not be cleaned up.

-Single Tile 2D images may cause issues when they are z-projected

### *IN DEVELOPMENT*

Currently working on an addition to stitch slidebook exports captured on the confocal we use. It's somewhat working at the moment, but still buggy. To run the confocal setting run with the flags  "command (con)focal".
