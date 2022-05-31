import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;

import ij.ImagePlus;
import ij.IJ;
import ij.io.FileSaver;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

import stitching.CommonFunctions;
import stitching.utils.Log;

import mpicbg.models.TranslationModel2D;
import mpicbg.models.TranslationModel3D;
import mpicbg.stitching.Downsampler;
import mpicbg.stitching.ImageCollectionElement;
import mpicbg.stitching.TextFileAccess;
import mpicbg.stitching.StitchingParameters;
import mpicbg.stitching.fusion.Fusion;
import mpicbg.stitching.ImagePlusTimePoint;
import mpicbg.models.InvertibleBoundable;
import mpicbg.stitching.CollectionStitchingImgLib;

import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

public class FrankenStitch {
	/**
	 * BigStitch calls for Big parameters, this method is a shitshow
	 * @param style refers to style of overlap
	 * @param outputFile 
	 * @param directory
	 * @param fileOutName
	 * @param outDirectory
	 * @param projected whether user wants full z stacks to be max projected
	 * @param isReference for running reference section
	 * @param saveFullStack whether to save full worm z stacks
	 * @param slices number of z stacks to take off either side.
	 */ 
    public void BigStitch(int style, String outputFile, String directory, String fileOutName, String outDirectory, 
	boolean projected, boolean isReference, boolean saveFullStack, int slices) {
		
		Downsampler d = null;
		int numChannels = -1; int numTimePoints = -1;
        boolean is2d = false; boolean is3d = false;
		final long startTime = System.currentTimeMillis();
		StitchingParameters params = new StitchingParameters();
		ArrayList<ImageCollectionElement> elements = getLayoutFromFile(directory, outputFile, d);
		if (style==0) { params = refParams();} 
		if (style==1) { params = floParams();}

		for ( final ImageCollectionElement element : elements )
		{
		
            if ( params.virtual )
                Log.info( "Opening VIRTUAL: " + element.getFile().getAbsolutePath() + " ... " );
            else
                Log.info( "Loading: " + element.getFile().getAbsolutePath() + " ... " );
				
			
			long time = System.currentTimeMillis();
			final ImagePlus imp = element.open( params.virtual );
			
			time = System.currentTimeMillis() - time;
			
			if ( imp == null )
				return;
			
			int lastNumChannels = numChannels;
			int lastNumTimePoints = numTimePoints;
			numChannels = imp.getNChannels();
			numTimePoints = imp.getNFrames();
			
			if ( imp.getNSlices() > 1 )
			{
				is3d = true;					
			}
			else
			{
				is2d = true;
			}
			
			// test validity of images
			if ( is2d && is3d )
			{
				Log.error( "Some images are 2d, some are 3d ... cannot proceed" );
				return;
			}
			
			if ( ( lastNumChannels != numChannels ) && lastNumChannels != -1 )
			{
				Log.error( "Number of channels per image changes ... cannot proceed" );
				return;					
			}

			if ( ( lastNumTimePoints != numTimePoints ) && lastNumTimePoints != -1 )
			{
				Log.error( "Number of timepoints per image changes ... cannot proceed" );
				return;					
			}
		}

        final int dimensionality;
		
		if ( is2d )
			dimensionality = 2;
		else
			dimensionality = 3;
		
		params.dimensionality = dimensionality;

        final ArrayList<ImagePlusTimePoint> optimized = CollectionStitchingImgLib.stitchCollection( elements, params );
    	
    	if ( optimized == null )
    		return;
    	
    	// output the result
		for ( final ImagePlusTimePoint imt : optimized )
			Log.info( imt.getImagePlus().getTitle() + ": " + imt.getModel() );

        if ( params.computeOverlap && outputFile != null )
        {
            if ( outputFile.endsWith( ".txt" ) )
                outputFile = outputFile.substring( 0, outputFile.length() - 4 ) + ".registered.txt";
            else
                outputFile = outputFile + ".registered.txt";
                
            writeRegisteredTileConfiguration( new File( directory, outputFile ), elements );
        }

        if ( params.fusionMethod != CommonFunctions.fusionMethodListGrid.length - 1 )
		{
			long time = System.currentTimeMillis();
			
			if ( params.outputDirectory == null )
				Log.info( "Fuse & Display ..." );
			else
				Log.info( "Fuse & Write to disk (into directory '" + new File( params.outputDirectory, "" ).getAbsolutePath() + "') ..." );
			IJ.showStatus("Fusing stitched image...");
			
			// first prepare the models and get the targettype
			final ArrayList<InvertibleBoundable> models = new ArrayList< InvertibleBoundable >();
			final ArrayList<ImagePlus> images = new ArrayList<ImagePlus>();
			
			boolean is32bit = false;
			boolean is16bit = false;
			boolean is8bit = false;
			
			for ( final ImagePlusTimePoint imt : optimized )
			{
				final ImagePlus imp = imt.getImagePlus();
				
				if ( imp.getType() == ImagePlus.GRAY32 )
					is32bit = true;
				else if ( imp.getType() == ImagePlus.GRAY16 )
					is16bit = true;
				else if ( imp.getType() == ImagePlus.GRAY8 )
					is8bit = true;
				
				images.add( imp );
			}
			
			for ( int f = 1; f <= numTimePoints; ++f )
				for ( final ImagePlusTimePoint imt : optimized )
					models.add( (InvertibleBoundable)imt.getModel() );
	
			ImagePlus imp = null;
			
			// test if there is no overlap between any of the tiles
			// if so fusion can be much faster
			boolean noOverlap = false;
			if ( is32bit )
				imp = Fusion.fuse( new FloatType(), images, models, params.dimensionality, params.subpixelAccuracy, params.fusionMethod, params.outputDirectory, noOverlap, false, params.displayFusion );
			else if ( is16bit )
				imp = Fusion.fuse( new UnsignedShortType(), images, models, params.dimensionality, params.subpixelAccuracy, params.fusionMethod, params.outputDirectory, noOverlap, false, params.displayFusion );
			else if ( is8bit )
				imp = Fusion.fuse( new UnsignedByteType(), images, models, params.dimensionality, params.subpixelAccuracy, params.fusionMethod, params.outputDirectory, noOverlap, false, params.displayFusion );
			else
				Log.error( "Unknown image type for fusion." );
			
			Log.info( "Finished fusion (" + (System.currentTimeMillis() - time) + " ms)");
			Log.info( "Finished ... (" + (System.currentTimeMillis() - startTime) + " ms)");
			if ( imp != null )
			{
				imp.setTitle( "Fused" );
                String path = outDirectory + "/" + fileOutName;
                try{
					if (imp.isHyperStack()) {
						if(projected) {
							if(saveFullStack) {
								FileSaver s = new FileSaver(imp);
								s.saveAsTiffStack(path);
							}
							ImagePlus imp_pro = Projector.Zproject(imp, slices);
							String refRef = path.replace("FLO", "REF");
							ImagePlus ref = new ImagePlus(refRef);
							ImagePlus imp_merge = Projector.SplitAndMerge(imp_pro, ref);
							FileSaver p = new FileSaver(imp_merge);
							p.saveAsTiff(path.replace("FLO", "PRO"));
						} else {
							FileSaver s = new FileSaver(imp);
							s.saveAsTiffStack(path);
						}
					} else if(isReference) {
						FileSaver s = new FileSaver(imp);
						s.saveAsTiff(path);
					} else {
						String refRef = path.replace("FLO", "REF");
						ImagePlus ref = new ImagePlus(refRef);
						ImagePlus imp_merge = Projector.SplitAndMerge(imp, ref);
						FileSaver s = new FileSaver(imp_merge);
						s.saveAsTiff(path.replace("FLO", "PRO")); 
					}
                } 
                catch(Exception e) {
                    Log.info(e.toString());
                }
			}
			imp.close();
    	// close all images
    	for ( final ImageCollectionElement element : elements )
    		element.close();
		}
		
	}

	
	private StitchingParameters refParams() {
		StitchingParameters s = new StitchingParameters();
		s.fusionMethod = 0; s.regThreshold = 0.10; s.relativeThreshold = 2.50; s.absoluteThreshold = 3.50;
        s.computeOverlap = true; s.invertX = false; s.invertY = false; s.ignoreZStage = false; s.subpixelAccuracy = false;
        s.displayFusion = false; s.virtual = true; s.cpuMemChoice = 0; s.outputVariant = 0; s.outputDirectory = null;
        s.channel1 = 0; s.channel2 = 0;
        s.timeSelect = 0; s.checkPeaks = 5;
		return s;
	}
	
	private StitchingParameters floParams() {
		StitchingParameters s = new StitchingParameters();
		s.fusionMethod = 0; s.regThreshold = 0.10; s.relativeThreshold = 2.50; s.absoluteThreshold = 3.50;
        s.computeOverlap = false; s.invertX = false; s.invertY = false; s.ignoreZStage = true; s.subpixelAccuracy = false;
        s.displayFusion = false; s.virtual = true; s.cpuMemChoice = 0; s.outputVariant = 0; s.outputDirectory = null;
        s.channel1 = 0; s.channel2 = 0;
        s.timeSelect = 0; s.checkPeaks = 5;
		return s;
	}

    public ArrayList< ImageCollectionElement > getLayoutFromFile( final String directory, final String layoutFile, final Downsampler ds )
	{
		final ArrayList< ImageCollectionElement > elements = new ArrayList< ImageCollectionElement >();
		int dim = -1;
		int index = 0;
		boolean multiSeries = false;
		// A HashMap using the filename (including the full path) as the key is
		// used to access the individual tiles of a multiSeriesFile. This way
		// it's very easy to check if a file has already been opened. Note that
		// the map doesn't get used in the case of single series files below!
		// TODO: check performance on large datasets! Use an array for the
		// ImagePlus'es otherwise and store the index number in the hash map!
		Map<String, ImagePlus[]> multiSeriesMap = new HashMap<String, ImagePlus[]>();
		String pfx = "Stitching_Grid.getLayoutFromFile: ";
		try {
			final BufferedReader in = TextFileAccess.openFileRead( new File( directory, layoutFile ) );
			if ( in == null ) {
				Log.error(pfx + "Cannot find tileconfiguration file '" + new File( directory, layoutFile ).getAbsolutePath() + "'");
				return null;
			}
			int lineNo = 0;
			pfx += "Line ";
			while ( in.ready() ) {
				String line = in.readLine().trim();
				lineNo++;
				if ( !line.startsWith( "#" ) && line.length() > 3 ) {
					if ( line.startsWith( "dim" ) ) {  // dimensionality parsing
						String entries[] = line.split( "=" );
						if ( entries.length != 2 ) {
							Log.error(pfx + lineNo + " does not look like [ dim = n ]: " + line);
							return null;						
						}
						
						try {
							dim = Integer.parseInt( entries[1].trim() );
						}
						catch ( NumberFormatException e ) {
							Log.error(pfx + lineNo + ": Cannot parse dimensionality: " + entries[1].trim());
							return null;														
						}

					} else if ( line.startsWith( "multiseries" ) )  {
						String entries[] = line.split( "=" );
						if ( entries.length != 2 ) {
							Log.error(pfx + lineNo + " does not look like [ multiseries = (true|false) ]: " + line);
							return null;
						}

						if (entries[1].trim().equals("true")) {
							multiSeries = true;
							Log.info(pfx + lineNo + ": parsing MultiSeries configuration.");
						}

					} else {  // body parsing (tiles + coordinates)
						if ( dim < 0 ) {
							Log.error(pfx + lineNo + ": Header missing, should look like [dim = n], but first line is: " + line);
							return null;							
						}
						
						if ( dim < 2 || dim > 3 ) {
							Log.error(pfx + lineNo + ": only dimensions of 2 and 3 are supported: " + line);
							return null;							
						}
						
						// read image tiles
						String entries[] = line.split(";");
						if (entries.length != 3) {
							Log.error(pfx + lineNo + " does not have 3 entries! [fileName; seriesNr; (x,y,...)]");
							return null;						
						}

						String imageName = entries[0].trim();
						if (imageName.length() == 0) {
							Log.error(pfx + lineNo + ": You have to give a filename [fileName; ; (x,y,...)]: " + line);
							return null;						
						}
						
						int seriesNr = -1;
						if (multiSeries) {
							String imageSeries = entries[1].trim();  // sub-volume (series nr)
							if (imageSeries.length() == 0) {
								Log.info(pfx + lineNo + ": Series index required [fileName; series; (x,y,...)" );
							} else {
								try {
									seriesNr = Integer.parseInt( imageSeries );
									Log.info(pfx + lineNo + ": Series nr (sub-volume): " + seriesNr);
								}
								catch ( NumberFormatException e ) {
									Log.error(pfx + lineNo + ": Cannot parse series nr: " + imageSeries);
									return null;
								}
							}
						}

						String point = entries[2].trim();  // coordinates
						if (!point.startsWith("(") || !point.endsWith(")")) {
							Log.error(pfx + lineNo + ": Wrong format of coordinates: (x,y,...): " + point);
							return null;
						}
						
						point = point.substring(1, point.length() - 1);  // crop enclosing braces
						String points[] = point.split(",");
						if (points.length != dim) {
							Log.error(pfx + lineNo + ": Wrong format of coordinates: (x,y,z,...), dim = " + dim + ": " + point);
							return null;
						}
						final float[] offset = new float[ dim ];
						for ( int i = 0; i < dim; i++ ) {
							try {
								offset[ i ] = Float.parseFloat( points[i].trim() ); 
							}
							catch (NumberFormatException e) {
								Log.error(pfx + lineNo + ": Cannot parse number: " + points[i].trim());
								return null;							
							}
						}
						
						// now we can assemble the ImageCollectionElement:
						ImageCollectionElement element = new ImageCollectionElement(
								new File( directory, imageName ), index++ );
						element.setDimensionality( dim );
						if ( dim == 3 )
							element.setModel( new TranslationModel3D() );
						else
							element.setModel( new TranslationModel2D() );
						element.setOffset( offset );

						if (multiSeries) {
							final String imageNameFull = element.getFile().getAbsolutePath();
							if (multiSeriesMap.get(imageNameFull) == null) {
								Log.info(pfx + lineNo + ": Loading MultiSeries file: " + imageNameFull);
								multiSeriesMap.put(imageNameFull, openBFDefault(imageNameFull));
							}
							element.setImagePlus(multiSeriesMap.get(imageNameFull)[seriesNr]);
						}

						elements.add( element );
					}
				}
			}
		}
		catch ( IOException e ) {
			Log.error( "Stitching_Grid.getLayoutFromFile: " + e );
			return null;
		}
		
		if (ds != null) {
			ImagePlus img = elements.get(0).open(true);
			ds.getInput(img.getWidth(), img.getHeight());
			ds.run(elements);
		}
		return elements;
	}

    private ImagePlus[] openBFDefault( String multiSeriesFileName )
	{
		return openBF(multiSeriesFileName, false, false, false, false, false, true);
	}
	
    private ImagePlus[] openBF( String multiSeriesFileName, boolean splitC,
			boolean splitT, boolean splitZ, boolean autoScale, boolean crop,
			boolean allSeries )
	{
		ImporterOptions options;
		ImagePlus[] imps = null;
		try
		{
			options = new ImporterOptions();
			options.setId( new File( multiSeriesFileName ).getAbsolutePath() );
			options.setSplitChannels( splitC );
			options.setSplitTimepoints( splitT );
			options.setSplitFocalPlanes( splitZ );
			options.setAutoscale( autoScale );
			options.setStackFormat(ImporterOptions.VIEW_HYPERSTACK);
			options.setStackOrder(ImporterOptions.ORDER_XYCZT);
			options.setCrop( crop );
			
			options.setOpenAllSeries( allSeries );
			
			imps = BF.openImagePlus( options );
		}
		catch (Exception e)
		{
			Log.error( "Cannot open multiseries file: " + e , e );
			return null;
		}
		return imps;
	}

	protected void writeTileConfiguration( final File file, final ArrayList< ImageCollectionElement > elements )
	{
    	// write the initial tileconfiguration
		final PrintWriter out = TextFileAccess.openFileWrite( file );
		final int dimensionality = elements.get( 0 ).getDimensionality();
		
		out.println( "# Define the number of dimensions we are working on" );
        out.println( "dim = " + dimensionality );
        out.println( "" );
        out.println( "# Define the image coordinates" );
        
        for ( final ImageCollectionElement element : elements )
        {
    		if ( dimensionality == 3 )
    			out.println( element.getFile().getName() + "; ; (" + element.getOffset( 0 ) + ", " + element.getOffset( 1 ) + ", " + element.getOffset( 2 ) + ")");
    		else
    			out.println( element.getFile().getName() + "; ; (" + element.getOffset( 0 ) + ", " + element.getOffset( 1 ) + ")");        	
        }

    	out.close();		
	}

	public void writeRegisteredTileConfiguration( final File file, final ArrayList< ImageCollectionElement > elements )
	{
		// write the tileconfiguration using the translation model
		final PrintWriter out = TextFileAccess.openFileWrite( file );
		final int dimensionality = elements.get( 0 ).getDimensionality();
		
		Log.info( "Writing registered TileConfiguration: " + file );

		out.println( "# Define the number of dimensions we are working on" );
        out.println( "dim = " + dimensionality );
        out.println( "" );
        out.println( "# Define the image coordinates" );
        
        for ( final ImageCollectionElement element : elements )
        {
    		if ( dimensionality == 3 )
    		{
    			final TranslationModel3D m = (TranslationModel3D)element.getModel();
    			out.println( element.getFile().getName() + "; ; (" + m.getTranslation()[ 0 ] + ", " + m.getTranslation()[ 1 ] + ", " + m.getTranslation()[ 2 ] + ")");
    		}
    		else
    		{
    			final TranslationModel2D m = (TranslationModel2D)element.getModel();
    			final double[] tmp = new double[ 2 ];
    			m.applyInPlace( tmp );
    			
    			out.println( element.getFile().getName() + "; ; (" + tmp[ 0 ] + ", " + tmp[ 1 ] + ")");
    		}
        }
    	out.close();		
	}
}
