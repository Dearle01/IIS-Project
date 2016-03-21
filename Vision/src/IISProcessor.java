import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Arrays;

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
	
	public static ArrayList<BufferedImage> PowerLaw(ArrayList<BufferedImage> images)
	{
		ArrayList<BufferedImage> PLImages = new ArrayList<BufferedImage>();
		float gamma = 1f;
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
			Histogram h = new Histogram(img);
			
			float threshold = (float) (mean(img) + 0.01  * standardDev(img));		
		
			short [] arr = new short[256];

			for(int i=0;i<arr.length;i++)
			{
				if(i <= threshold)
				{
					arr[i] = 255;
				}
				else
				{
					arr[i] = 0;
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
	
	//This isnt finished
	//Need to send in the image, but accesed through main?
	public void nearestNeighbourCalc(BufferedImage source, int [] areaArray, BufferedImage [] imgs)
	{
		//array containing area values for each other binary image
		int [] vd = new int [areaArray.length];
		//get area of image passed in to calculate nearest neighbours
		int vt = area(source);
		for(int i=0;i<areaArray.length;i++)
		{		
			vd[i]=vt-areaArray[i];
		}
		
		//select three closest images in terms of area
		// needs to be updated to use perimeter also
		
		Arrays.sort(vd);
		
		int first = 0;
		int second = 0;
		int third = 0;
		
		if(vd.length>=3)
		{
		//three smallest
		first = vd[0];
		second = vd[1];
		third = vd[2];
		}
		
		BufferedImage [] nnImages = new BufferedImage [3];
		int imgCounter=0;
		
		for(int i=0;i<imgs.length;i++)
		{
			if(area(source)-area(imgs[i])==first||area(source)-area(imgs[i])==second||area(source)-area(imgs[i])==third)
			{
				nnImages[imgCounter] = imgs[i];
				imgCounter++;
			}
		}
		
		JVision j = new JVision();
		j.setBounds(0, 0, 1500, 1000);
		
		int x =0;
		int y = 0;	
		
		for(int i=0;i<nnImages.length;i++)
		{
			displayAnImage(nnImages[i], j, x, y, "");
			x+=250;
		}
		
		displayAnImage(source,j,0,500,"original");	
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
	
	public static ArrayList<BufferedImage> PostProcessImages(ArrayList<BufferedImage> images)
	{
		ArrayList<BufferedImage> postPImages = new ArrayList<BufferedImage>();
		for(BufferedImage img : images)
		{
			BufferedImage i = ImageOp.close(img, 2);
			i = ImageOp.open(img, 1);
			postPImages.add(i);
		}
		return postPImages;
	}
	
	public static int area(BufferedImage source)
	{
		
		int width = source.getWidth();
		int height = source.getHeight();
		Raster r = source.getRaster();
		width= r.getHeight();
		height= r.getWidth();
		int area=0;
		
		for(int i=0;i<width;i++)
		{
			for(int j=0;j<height;j++)
			{
				if(r.getSample(j, i, 0)==1)
				{
					area++;
				}
			}
		}
		
		return area;	
	}
	
	public static BufferedImage readInImage(String filename)

	{
		BufferedImage img;
		img = ImageOp.readInImage(filename);
		return img;
	}

	public static void displayAll(ArrayList<BufferedImage> images) 
	{
		JVision jvis = new JVision();
		jvis.setBounds(0, 0, 1200, 900);
		for(BufferedImage a : images)
		{
			int x = 0;
			int y = 0;
			//createAndDisplayHistogram(a.postprocessedImages,jvisClass,x+300,y,"");
			displayAnImage(a,jvis,x,y,"");
			x+=250;
			if(x>=1250)
			{
				x=0;
				y+=250;		
			}
		}
	}
	public static void displayAnImage(BufferedImage img, JVision display, int x, int y, String title)
	{
		display.imdisp(img,title,x,y);
	}
}