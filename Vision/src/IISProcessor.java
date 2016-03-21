import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
	
	public int calculateMagnitudeOfDifference(int testArea, int testPerimeter, int area, int perimeter)
	{
		
		int magnitude=0;
		int differenceArea = testArea - area;
		int differencePerimeter = testPerimeter - perimeter;
		int sumOfDifferences = (int) ((Math.pow(differenceArea, 2) + (Math.pow(differencePerimeter,2))));
		magnitude = (int) Math.sqrt(sumOfDifferences);
		
		return magnitude;
		
	}

	//This isnt finished
	//Need to send in the image, but accesed through main?
//	public void nearestNeighbourCalc(BufferedImage source, int [] areaArray, int [] perimeterArray, BufferedImage [] imgs) throws IOException
//	{
//		//array containing area values for each other binary image
//		int [] v1d = new int [perimeterArray.length];
//		int [] v2d = new int [areaArray.length];
//		//get area of image passed in to calculate nearest neighbours
//		int v1t = calculatePerimeter(source);
//		int v2t = area(source);
//		
//		for(int i=0;i<areaArray.length;i++)
//		{	
//			v1d[i] = v1t-perimeterArray[i];
//			v2d[i] = v2t-areaArray[i];
//		}
//		
//		
//		
//		int [] vd = new int [areaArray.length];
//		
//		for(int i=0;i<vd.length;i++)
//		{
//			vd[i]=calculateMagnitudeOfDifference(v2t, v1t, v2d[i], v1d[i]);
//		}
//		
//		//select three closest images in terms of area
//		
//		
//		Arrays.sort(vd);
//		
//		int first = 0;
//		int second = 0;
//		int third = 0;
//		
//		if(vd.length>=3)
//		{
//		//three smallest
//		first = vd[0];
//		second = vd[1];
//		third = vd[2];
//		}
//		
//		System.out.println("First"+first);
//		System.out.println("Second"+second);
//		System.out.println("Third"+third);
//		
//		BufferedImage [] nnImages = new BufferedImage [3];
//		int imgCounter=0;
		
//		for(int i=0;i<imgs.length;i++)
//		{
//			if(calculateMagnitudeOfDifference(area(source), calculatePerimeter(source), area(imgs[i]),calculatePerimeter(imgs[i]))==first||
//					calculateMagnitudeOfDifference(area(source), calculatePerimeter(source), area(imgs[i]),calculatePerimeter(imgs[i]))==second||
//							calculateMagnitudeOfDifference(area(source), calculatePerimeter(source), area(imgs[i]),calculatePerimeter(imgs[i]))==third)
//			{
//				nnImages[imgCounter] = imgs[i];
//				imgCounter++;
//			}
//		}
//		
//		JVision j = new JVision();
//		j.setBounds(0, 0, 1500, 1000);
//		
//		int x =0;
//		int y = 0;	
//		
//		for(int i=0;i<nnImages.length;i++)
//		{
//			displayAnImage(nnImages[i], j, x, y, "");
//			x+=250;
//		}
//		
//		displayAnImage(source,j,0,500,"original");	
//	}

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

	public int[] calculatePerimeter(ArrayList<BufferedImage> images) throws IOException
	{
		//A pixel is part of the perimeter if it is nonzero and it is connected to at least one zero-valued pixel.
		int[] perArr = new int[images.size()];
		for(int k=0;k<images.size();k++)
		{
			
			int width = images.get(k).getWidth();
			int height = images.get(k).getHeight();
			int [][] perimeterDraw = new int [width][height]; 
			Raster r = images.get(k).getRaster();
			width= r.getHeight();
			height= r.getWidth();
			int perimeter=0;
			BufferedWriter out = new BufferedWriter(new FileWriter("file.txt"));



			for(int i=1;i<width;i++)
			{
				for(int j=1;j<height;j++)
				{
					if(r.getSample(j, i, 0)==1)
					{
						//breaks because check is wrong
						if(i>0&&j>0&&i<width&&j<height)				
						{
							if(r.getSample(j-1, i, 0)==0||r.getSample(j+1, i, 0)==0||r.getSample(j, i-1, 0)==0||
									r.getSample(j, i+1, 0)==0||r.getSample(j-1, i+1, 0)==0||r.getSample(j-1, i-1, 0)==0||
									r.getSample(j+1, i+1, 0)==0||r.getSample(j+1, i-1, 0)==0)
							{										
								perimeter++;
								perimeterDraw[i][j]=1;
							}
						}

					}
					else
					{
						perimeterDraw[i][j]=0;
					}

				}
			}
			
			perArr[k] = perimeter;
			

			for(int i=0;i<width;i++)
			{
				for(int j=0;j<height;j++)
				{

					out.write(""+perimeterDraw[i][j]);

				}
				out.newLine();
			}

			out.close();
			
		}
		
		return perArr;
	
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
	public static void displayAnImage(BufferedImage img, JVision display, int x, int y, String title)
	{
		display.imdisp(img,title,x,y);
	}
}