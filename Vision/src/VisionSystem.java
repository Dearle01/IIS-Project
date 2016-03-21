
import java.awt.image.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;


public class VisionSystem
{
	public static void main(String[] args)
	{
		new VisionSystem();
	}

	public VisionSystem()
	{
		try
		{
		
			JVision jvis = new JVision();
			
			jvis.setBounds(0, 0, 1500, 1000);
			jvis.setTitle("Original");
			
			//open dialog for selecting multiple images
			//change the path to the directory where your images are stored etc
			JFileChooser chooser = new JFileChooser("C:\\Users\\40083276\\Desktop\\Datasets\\training");
			chooser.setMultiSelectionEnabled(true);
			chooser.showOpenDialog(jvis);
			File[] files = chooser.getSelectedFiles();

			//loading in the training images
			ArrayList<BufferedImage> trainingImages = new ArrayList<BufferedImage>();
			
			for (int i = 0; i < files.length; i++) {
				
				BufferedImage buffImg = readInImage(files[i].getName()); 
				trainingImages.add(buffImg);
			}

			//coordinates for displaying images on screen
			int x=0;
			int y=0;

			//display images on jvis, increment x and y coordinates appropriately
			for (int i = 0; i < trainingImages.size(); i++) {

				createAndDisplayHistogram(trainingImages.get(i),jvis,x+300,y,"");
				displayAnImage(trainingImages.get(i),jvis,x,y,"");
				x+=250;
				if(x>=1250)
				{
					x=0;
					y+=250;		
				}
			}

//			preprocessed images created using approp method, stored in array
			BufferedImage[] preprocessedImages = new BufferedImage[trainingImages.size()];

			for(int i=0;i<trainingImages.size();i++)
			{
				
				Histogram h = new Histogram(trainingImages.get(i));
								
				//calculating m and c values for linear stretching 
				float hmax = h.getMaxValue();
				float hmin = h.getMinValue();
				float m = 255/(hmax - hmin);
				float c = (-1*m)*hmin;
				System.out.println(m);
				System.out.println(c);
				
				preprocessedImages[i] = enhanceContrast(trainingImages.get(i),m,c);
				//preprocessedImages[i] = enhanceContrastH(trainingImages.get(i));
				//preprocessedImages[i] = enhanceContrastPower(trainingImages.get(i));
				//preprocessedImages[i] = performNoiseReduction(preprocessedImages[i], 1);
				
			}
			
			

			JVision second = new JVision();
			second.setBounds(0, 0, 1500, 1000);

			x=0;
			y=0;

			// and again display for whatever reason
			for (int i = 0; i < preprocessedImages.length; i++) {

				displayAnImage(preprocessedImages[i],second,x,y,"");
				
				createAndDisplayHistogram(preprocessedImages[i],second,x+250,y,"");
				x+=250;
				if(x>=1250)
				{
					x=0;
					y+=250;
				}
			}

//			// same again but with thresholding the preprocessed images
			BufferedImage[] thresholdedImages = new BufferedImage[preprocessedImages.length];

			for(int i=0;i<preprocessedImages.length;i++)
			{
				thresholdedImages[i]=preprocessedImages[i];
				
				//automatic thresholding assumes a bimodal histogram, will look into alternative solutions
				float threshold = (float) (mean(thresholdedImages[i]) + 0.01  * standardDev(thresholdedImages[i]));
				System.out.println(threshold);
				thresholdedImages[i] = thresholdAnImage(preprocessedImages[i], threshold);

			}

			JVision third = new JVision();
			x=0;
			y=0;

			for (int i = 0; i < thresholdedImages.length; i++) {

				displayAnImage(thresholdedImages[i],third,x,y,"");
				x+=250;
				
				if(x>=750)
				{
					x=0;
					y+=250;
				}
			}


			// and finally post process and display
			BufferedImage[] postprocessedImages = new BufferedImage[thresholdedImages.length];
			
			for(int i=0;i<preprocessedImages.length;i++)
			{
				postprocessedImages[i] = postprocessAnImage(thresholdedImages[i]);
				
			}

			JVision fourth = new JVision();

			x=0;
			y=0;
			
			int [] areaArr = new int [postprocessedImages.length];
			int [] perimArr= new int [postprocessedImages.length];

			for (int i = 0; i < postprocessedImages.length; i++) {

				displayAnImage(postprocessedImages[i],fourth,x,y,"");
				x+=250;
				if(x>=750)
				{
					x=0;
					y+=250;
				}
							
				for(int j=0;j<areaArr.length;j++){
					areaArr[j]=area(postprocessedImages[j]);
					perimArr[j]= calculatePerimeter(postprocessedImages[i]);
				}
				
				System.out.println("This is the perimeter" +calculatePerimeter(postprocessedImages[i]));

			}
			
			
			nearestNeighbourCalc(postprocessedImages[0],areaArr,perimArr,postprocessedImages);
			
		}

		catch(Exception e)
		{ 
			System.out.println(e);
			e.printStackTrace();
		}
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
	
	public void nearestNeighbourCalc(BufferedImage source, int [] areaArray, int [] perimeterArray, BufferedImage [] imgs) throws IOException
	{
		//array containing area values for each other binary image
		int [] v1d = new int [perimeterArray.length];
		int [] v2d = new int [areaArray.length];
		//get area of image passed in to calculate nearest neighbours
		int v1t = calculatePerimeter(source);
		int v2t = area(source);
		
		for(int i=0;i<areaArray.length;i++)
		{	
			v1d[i] = v1t-perimeterArray[i];
			v2d[i] = v2t-areaArray[i];
		}
		
		
		
		int [] vd = new int [areaArray.length];
		
		for(int i=0;i<vd.length;i++)
		{
			vd[i]=calculateMagnitudeOfDifference(v2t, v1t, v2d[i], v1d[i]);
		}
		
		//select three closest images in terms of area
		
		
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
		
		System.out.println("First"+first);
		System.out.println("Second"+second);
		System.out.println("Third"+third);
		
		BufferedImage [] nnImages = new BufferedImage [3];
		int imgCounter=0;
		
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
	}

	public BufferedImage postprocessAnImage(BufferedImage source)
	{
		BufferedImage i = ImageOp.close(source, 5);
		//i = ImageOp.open(source, 3);
		return i;
	}

	public int mean(BufferedImage source)
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
	
	public int centroid(BufferedImage source)
	{	
		return 0;		
	}
	
	//needs work
	public int calculatePerimeter(BufferedImage source) throws IOException
	{
		//A pixel is part of the perimeter if it is nonzero and it is connected to at least one zero-valued pixel.
		
		int width = source.getWidth();
		int height = source.getHeight();
		int [][] perimeterDraw = new int [width][height]; 
		Raster r = source.getRaster();
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
		
		for(int i=0;i<width;i++)
		{
			for(int j=0;j<height;j++)
			{
				
				out.write(""+perimeterDraw[i][j]);
	
			}
			out.newLine();
		}
		
		out.close();
		
		return perimeter;		
	}
	
	
	
	public int area(BufferedImage source)
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

	public int standardDev(BufferedImage source)
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



	public BufferedImage performAutomaticThresholding(BufferedImage source)
	{
		return source;
	}


	public short[] thresholdLut(float t)
	{
		short [] arr = new short[256];

		for(int i=0;i<arr.length;i++)
		{
			if(i <= t)
			{
				arr[i] = 255;
			}
			else
			{
				arr[i] = 0;
			}
		}

		return arr;
	}

	public BufferedImage preprocessAnImage(BufferedImage source, int threshold)
	{
		return thresholdAnImage(source,threshold);
	}

	public BufferedImage thresholdAnImage(BufferedImage source, float threshold)
	{
		short [] arr = thresholdLut(threshold);

		BufferedImage enhancedImage = ImageOp.pixelop(source,arr);
		return enhancedImage;
	}

	public BufferedImage readInImage(String filename)

	{
		BufferedImage img;
		img = ImageOp.readInImage(filename);
		return img;
	}

	public void displayAnImage(BufferedImage img, JVision display, int x, int y, String title)
	{
		display.imdisp(img,title,x,y);
	}

	public void createAndDisplayHistogram(BufferedImage img,JVision display,int x,int y,String title) throws Exception
	{
		Histogram hist = new Histogram(img);
		GraphPlot gp = new GraphPlot(hist);
		display.imdisp(gp,title,x,y);
	}

	public short[] brightnessLut(int c)
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
		return arr;
	}

	public short[] linearStretchLut(float m,float c)
	{
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
		return arr;
	}

	public BufferedImage enhanceContrast(BufferedImage source, float m, float c)
	{
		short [] arr = linearStretchLut(m,c);
		BufferedImage enhancedImage = ImageOp.pixelop(source,arr);
		return enhancedImage;
	}


	public BufferedImage enhanceBrightness(BufferedImage source)
	{
		short [] arr = brightnessLut(25);
		BufferedImage enhancedImage = ImageOp.pixelop(source,arr);
		return enhancedImage;
	}

	public short[] powerLawLut(float gamma)
	{
		short [] arr = new short[256];
		for(int i=0;i<arr.length;i++)
		{
			arr[i] = (short) (Math.pow(i,gamma)/Math.pow(255,gamma-1));
		}
		return arr;
	}


	//only use this if histogram occupies upper or lower range 
	public BufferedImage enhanceContrastPower(BufferedImage source)
	{
		short [] arr = powerLawLut(1f);
		BufferedImage enhancedImage = ImageOp.pixelop(source,arr);
		return enhancedImage;
	}


	public short[] histogramEqualisationLut (Histogram hist) throws HistogramException
	{
		short [] arr = new short[256];

		for(int i=0;i<arr.length;i++)
		{
			arr[i] = (short) Math.max(0, (short)((256 * hist.getCumulativeFrequency(i)) / (hist.getNumSamples()-1)));
		}
		return arr;
	}
	
	public BufferedImage enhanceContrastH(BufferedImage source) throws HistogramException
	{	
		Histogram hist = new Histogram(source);
		short [] arr = histogramEqualisationLut(hist);
		BufferedImage enhancedImage = ImageOp.pixelop(source,arr);

		return enhancedImage;
	}

	public BufferedImage performNoiseReduction(BufferedImage source, int maskDimensions)
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

	public BufferedImage median(BufferedImage source,int m)
	{
		BufferedImage enhancedImage = ImageOp.median(source, m);
		return enhancedImage;
	}

	public BufferedImage performEdgeExtraction(BufferedImage source)
	{
		final float[] HIGHPASS1X2 = {-10.f,10.f,

				0.f,0.f};

		final float[] HIGHPASS2X1 = {-10.f,0.f,

				10.f,0.f};

		BufferedImage enhancedImage = ImageOp.convolver(source,HIGHPASS1X2);
		BufferedImage enhancedImageSecond = ImageOp.convolver(source,HIGHPASS2X1);
		BufferedImage edge = ImageOp.imagrad(enhancedImage, enhancedImageSecond);
		return edge;
	}
}