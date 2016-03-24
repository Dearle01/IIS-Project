import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;

public final class IISProcessor {
	private IISProcessor()
	{
		//Cannot be called
		//Need to mimic a top level static class.
	}

	public static ArrayList<BufferedImage> LinearStretching(ArrayList<BufferedImage> trainingImages) throws HistogramException
	{
		ArrayList<BufferedImage> LSImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : trainingImages)
		{		
			Histogram h = new Histogram(img);

			//calculating m and c values for linear stretching 
			float hmax = h.getMaxValue();
			float hmin = h.getMinValue();
			float m = 255/(hmax - hmin);
			float c = (-1*m)*hmin;			

			short [] arr = new short[256];
			for(int i=0;i<arr.length;i++)
			{
				if(i < (-c/m))
				{
					arr[i] = 0;
				}
				else if(i > ((255-c)/m))
				{
					arr[i] = 255;
				}
				else
				{
					arr[i] = (short) ((m*i) + c);
				}
			}
			LSImages.add(ImageOp.pixelop(img ,arr));	
		}
		return LSImages;
	}

	public static ArrayList<BufferedImage> PowerLaw(ArrayList<BufferedImage> images, float gamma)
	{
		ArrayList<BufferedImage> PLImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : images)
		{					
			short [] arr = new short[256];
			for(int i=0;i<arr.length;i++)
			{
				arr[i] = (short) (Math.pow(i,gamma)/Math.pow(255,gamma-1));
			}

			PLImages.add(ImageOp.pixelop(img,arr));	
		}
		return PLImages;
	}

	public static ArrayList<BufferedImage> HistogramEqualisation(ArrayList<BufferedImage> images) throws HistogramException
	{
		ArrayList<BufferedImage> HistImages = new ArrayList<BufferedImage>();
		float gamma = 1f;
		for(BufferedImage img : images)
		{			
			Histogram hist = new Histogram(img);
			short [] arr = new short[256];
			for(int i=0;i<arr.length;i++)
			{
				arr[i] = (short) Math.max(0, (short)((256 * hist.getCumulativeFrequency(i)) / (hist.getNumSamples()-1)));
			}

			HistImages.add(ImageOp.pixelop(img,arr));	
		}
		return HistImages;
	}

	public static ArrayList<BufferedImage> EdgeExtraction(ArrayList<BufferedImage> images) throws HistogramException
	{
		ArrayList<BufferedImage> edgeImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : images)
		{			
			final float[] HIGHPASS1X2 = {-10.f,10.f,

					0.f,0.f};

			final float[] HIGHPASS2X1 = {-10.f,0.f,

					10.f,0.f};

			BufferedImage enhancedImage = ImageOp.convolver(img,HIGHPASS1X2);
			BufferedImage enhancedImageSecond = ImageOp.convolver(img,HIGHPASS2X1);
			BufferedImage edge = ImageOp.imagrad(enhancedImage, enhancedImageSecond);
			edgeImages.add(edge);
		}
		return edgeImages;
	}

	public static BufferedImage LowPassFilter(BufferedImage source, int maskDimensions)
	{
		int dimensions = maskDimensions*maskDimensions;
		float[] LOWPASS= new float[dimensions];	
		for(int i=0;i<LOWPASS.length;i++)
		{
			LOWPASS[i] = (float)1/dimensions;
		}

		BufferedImage enhancedImage = ImageOp.convolver(source,LOWPASS);
		return enhancedImage;
	}


	public static ArrayList<BufferedImage> Median(ArrayList<BufferedImage> images, int m)
	{
		ArrayList<BufferedImage> medianImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : images)
		{			
			medianImages.add(ImageOp.median(img, m));
		}
		return medianImages;
	}	

	public static ArrayList<BufferedImage> performNoiseReduction(ArrayList<BufferedImage> images, int maskDimensions)
	{
		ArrayList<BufferedImage> noiseRedImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : images)
		{
			int dimensions = maskDimensions*maskDimensions;
			float[] LOWPASS= new float[dimensions];

			for(int i=0;i<LOWPASS.length;i++)
			{
				LOWPASS[i] = (float)1/dimensions;
			}

			noiseRedImages.add(ImageOp.convolver(img,LOWPASS));
		}
		return noiseRedImages;
	}

	public static ArrayList<BufferedImage> EnhanceBrightness(ArrayList<BufferedImage> images, int c)
	{
		ArrayList<BufferedImage> brightnessImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : images)
		{
			short [] arr = new short[256];
			for(int i=0;i<arr.length;i++)
			{
				if(i < -c)
				{
					arr[i] = 0;
				}
				else if(i > (255-c))
				{
					arr[i] = 255;
				}
				else
				{
					arr[i] = (short) (i + c);
				}
			}

			brightnessImages.add(ImageOp.pixelop(img,arr));
		}
		return brightnessImages;
	}

	//automatic thresholding assumes a bimodal histogram, will look into alternative solution
	public static ArrayList<BufferedImage> ThresholdImages(ArrayList<BufferedImage> images)throws HistogramException
	{
		ArrayList<BufferedImage> LSImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : images)
		{		
			float threshold = (float) (mean(img) + 0.7  * standardDev(img));		

			short [] arr = new short[256];
			
			for(int i=0;i<arr.length;i++)
			{
				if(i <= threshold)
				{
					arr[i] = 0;
				}
				else
				{
					arr[i] = 255;
				}
			}
			LSImages.add(ImageOp.pixelop(img ,arr));
		}
		return LSImages;
	}

	private static int mean(BufferedImage source)
	{
		int width = source.getWidth();
		int height = source.getHeight();
		Raster rast = source.getRaster();
		int mean = 0;
		for(int i=0;i<width;i++)
		{
			for(int j=0;j<height;j++)
			{
				int g = rast.getSample(j,i,0);
				mean +=g;
			}
		}

		mean/= (width*height);

		return mean;
	}

	public static int calculateMagnitudeOfDifference(int testArea, int testPerimeter, int [] areas, int[] perimeters)
	{

		int[] differenceAreas = new int [areas.length];
		int[] differencePerimeters = new int [perimeters.length];
		double[] sumOfDifferences = new double [perimeters.length];
		int lowestMagnitude = 100000;
		int currentMagnitude = 0;
		for(int i=0;i<areas.length;i++)
		{
			differenceAreas[i] = testArea - areas[i];
			differencePerimeters[i] = testPerimeter-perimeters[i];
			sumOfDifferences[i] = ((Math.pow(differenceAreas[i], 2) + (Math.pow(differencePerimeters[i],2))));
			currentMagnitude = (int) Math.sqrt(sumOfDifferences[i]);
			if(currentMagnitude < lowestMagnitude)
			{
				lowestMagnitude = currentMagnitude;
			}
		}

		return lowestMagnitude;

	}

	//This isnt finished
	//Need to send in the image, but accesed through main?
	public static Object nearestNeighbourCalc(BufferedImage source, Vector<Object> classes) throws IOException
	{
		//get area of image passed in to calculate nearest neighbours
				ArrayList<BufferedImage> sourceImage = new ArrayList<BufferedImage>();
				Collections.addAll(sourceImage, source);
				int[] v1t = getPerimeter(sourceImage);
				int[] v2t = area(sourceImage);
				
				int[] MagDif = new int[classes.size()];
				int lowestMagIndex = 0;
				int lowestMagnitude = Integer.MAX_VALUE;
				
				//do this for each group
				for(int i = 0; i < classes.size(); i++)
				{		
					int [] perimeters = getPerimeter(classes.elementAt(i).thresholdedImages);
					int [] areas = area(classes.elementAt(i).thresholdedImages);
					MagDif[i] = calculateMagnitudeOfDifference(v2t[0], v1t[0], areas, perimeters);
					if(MagDif[i] < lowestMagnitude)
					{
						//keep the index so that we can return that class
						lowestMagIndex = i;
						//record the max so we can keep comparing with newest value.
						lowestMagnitude = MagDif[i];
						System.out.println("Lowest Magnitude is now " + classes.elementAt(i).name + " at " + MagDif[i] + ". At Index of :" + i);
					}
				}
				return classes.elementAt(lowestMagIndex);
	}

	public static int standardDev(BufferedImage source)
	{
		int width = source.getWidth();
		int height = source.getHeight();
		Raster rast = source.getRaster();
		int mean = 0;
		for(int i=0;i<width;i++)
		{
			for(int j=0;j<height;j++)
			{
				int g = rast.getSample(j,i,0);
				mean +=g;
			}
		}

		mean/= (width*height);

		int sqdif =0;

		for(int i=0;i<width;i++)
		{
			for(int j=0;j<height;j++)
			{

				int g = rast.getSample(j,i,0);
				int minus = g-mean;
				minus= (int) Math.pow(minus,2);
				sqdif+=minus;
			}
		}

		sqdif/=(width*height);

		return (int) Math.sqrt(sqdif);
	}
	
	public static Object TestNewImage(Vector<Object> classes) throws IOException, HistogramException
	{
		JVision jvis = new JVision();

		jvis.setBounds(0, 0, 1500, 1000);
		jvis.setTitle("Testing Phase");

		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));

		chooser.showOpenDialog(jvis);
		File file = chooser.getSelectedFile();

		BufferedImage testImage = IISProcessor.readInImage(file.getName());
		ArrayList<BufferedImage> image = new ArrayList<BufferedImage>();
		image.add(testImage);
		Object testObject = new Object(image, "");
		testObject.Preprocess();
		testObject.Threshold();
		testObject.PostProcess();
		
		IISProcessor.displayAnImage(testObject.postprocessedImages.get(0), jvis, 0, 0, "");
		return IISProcessor.nearestNeighbourCalc(testObject.postprocessedImages.get(0), classes);
	}

	public static ArrayList<BufferedImage> PostProcessImages(ArrayList<BufferedImage> images)
	{
		ArrayList<BufferedImage> postPImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : images)
		{
			BufferedImage i = ImageOp.open(img, 2);
			BufferedImage j = ImageOp.close(i, 2);
			postPImages.add(j);
		}
		return postPImages;
	}

	public static int[] area(ArrayList<BufferedImage> source)
	{
		int [] areaArr = new int[source.size()];
		for(int k=0;k<source.size();k++)
		{
			int width = source.get(k).getWidth();
			int height = source.get(k).getHeight();
			Raster r = source.get(k).getRaster();
			width= r.getHeight();
			height= r.getWidth();
			int area=0;

			for(int i=0;i<width;i++)
			{
				for(int j=0;j<height;j++)
				{
					if(r.getSample(i, j, 0)>0)
					{
						area++;
					}
				}
			}			
			areaArr[k]=(width*height)-area;
		}
		return areaArr;	
	}
	
	

	public static int[] getPerimeter(ArrayList<BufferedImage> images)
	{
		ArrayList<BufferedImage> reducedImages = new ArrayList<BufferedImage>();
		
		
		for(BufferedImage img : images)
		{
			BufferedImage i = ImageOp.dilate(img, 2);//1 doesnt work, so 2? // dont't even ask
			reducedImages.add(i);
			
		}
		
		
		int imageCount = images.size();
		int[] perArr = new int[imageCount];
		
		int[] areaReduced = area(reducedImages);
		int[] areaNormal = area(images);
		
		//Go through each image and get the difference between the image and its closed self.
		for(int i=0; i < imageCount; i++)
		{
			perArr[i] = areaNormal[i] - areaReduced[i];
		}
		return perArr;
	}
	
	public static float[] getCompactness(ArrayList<BufferedImage> images)
	{	
		float [] compactnessValues = new float[images.size()];
		int [] perimeters = new int [images.size()];
		perimeters=getPerimeter(images);
		int [] areas = new int [images.size()];
		areas=area(images);
		for(int i=0;i<compactnessValues.length;i++)
		{
			compactnessValues[i]=(float) (Math.pow(perimeters[i],2)/areas[i]);
		}
		
		return compactnessValues;
	}

	public static BufferedImage readInImage(String filename)

	{
		BufferedImage img;
		img = ImageOp.readInImage(filename);
		return img;
	}

	public static void displayAll(ArrayList<BufferedImage> images, String displayName) 
	{
		JVision jvis = new JVision();
		jvis.setBounds(0, 0, 1200, 900);
		int x = 0;
		int y = 0;
		for(BufferedImage a : images)
		{

			//createAndDisplayHistogram(a.postprocessedImages,jvisClass,x+300,y,"");
			displayAnImage(a,jvis,x,y,displayName);
			x+=250;
			if(x>=1250)
			{
				x=0;
				y+=250;		
			}
		}
	}
	
	public static void displayAllHistogram(ArrayList<BufferedImage> images, String displayName) throws HistogramException
	{
		JVision jvis = new JVision();
		jvis.setBounds(0, 0, 1200, 900);
		int x = 0;
		int y = 0;
		for(BufferedImage a : images)
		{
			Histogram hist = new Histogram(a);
			GraphPlot gp = new GraphPlot(hist);
			//createAndDisplayHistogram(a.postprocessedImages,jvisClass,x+300,y,"");
			displayAnImage(gp,jvis,x,y,displayName);
			x+=290;
			if(x>=1250)
			{
				x=0;
				y+=290;		
			}
		}
	}
	public static void displayAnImage(BufferedImage img, JVision display, int x, int y, String title)
	{
		display.imdisp(img,title,x,y);
	}
	
	public static void printAreas(ArrayList<BufferedImage> imgs) throws IOException
	{
		for(int i:area(imgs))
		{
			System.out.println("This is the area "+i);
		}
		
		for(int i:getPerimeter(imgs))
		{
			System.out.println("This is Dearle's perimeter"+i);
		}
		
		for(float f:getCompactness(imgs))
		{
			System.out.println("this is the compactness"+f);
		}
		
		
	}
	
	
	
}