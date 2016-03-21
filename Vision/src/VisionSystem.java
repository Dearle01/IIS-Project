
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

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
			Scanner console = new Scanner(System.in);
			JVision jvis = new JVision();
			//int intInp = console.nextInt();
			
			Scanner br = new Scanner(System.in);
	        System.out.println("How many classes do you need to train?");
	        int classCount = 0;
	        try
	        {
	        	classCount = br.nextInt();
	        }
	        catch(Exception e) 
	        {
	        	System.out.println("Please Enter an integer");
	        }
	        br.close();

			jvis.setBounds(0, 0, 1500, 1000);
			jvis.setTitle("Original");
			Vector<Object> classes = new Vector<Object>();
			
			//Get all files in class for training
			//Loop through this based on user input
			JFileChooser chooser = new JFileChooser("C:\\Users\\Adam\\Downloads\\Datasets\\test");
			for(int i = 0; i < classCount; i++)
			{			
				//open dialog for selecting multiple images
				//change the path to the directory where your images are stored etc
				
				chooser.setMultiSelectionEnabled(true);
				chooser.showOpenDialog(jvis);
				File[] files = chooser.getSelectedFiles();

				//loading in the training images
				ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
				
				for (int j = 0; j < files.length; j++) {
					
					BufferedImage buffImg = readInImage(files[j].getName()); 
					images.add(buffImg);
				}
				classes.add(new Object(images, "ss"));	
			}
			
			//Will maybe incorporate this to the below loop
			for(Object a : classes)
			{
			 a.Preprocess();
			 a.Threshold();
			 a.PostProcess();
			
			 a.displayAllTraining();
			 a.displayAllPreP();
			 a.displayAllPostP();
			 a.displayAllThreshold();
			}
	
			//System.out.println(calculatePerimeter(postprocessedImages[i]));			
		}

		catch(Exception e)
		{ 
			System.out.println(e);		
		}
	}

	public int centroid(BufferedImage source)
	{	
		return 0;		
	}
	
	//needs work
	public int calculatePerimeter(BufferedImage source)
	{
		//A pixel is part of the perimeter if it is nonzero and it is connected to at least one zero-valued pixel.
		
		int width = source.getWidth();
		int height = source.getHeight();
		Raster r = source.getRaster();
		width= r.getHeight();
		height= r.getWidth();
		int perimeter=0;
		
		for(int i=1;i<width;i++)
		{
			for(int j=1;j<height;j++)
			{
				if(r.getSample(j, i, 0)==1)
				{
					//breaks because check is wrong
					if(i>0&&j>0)				
					{
						if(r.getSample(j-1, i, 0)==0||r.getSample(j+1, i, 0)==0||r.getSample(j, i-1, 0)==0||
							r.getSample(j, i+1, 0)==0||r.getSample(j-1, i+1, 0)==0||r.getSample(j-1, i-1, 0)==0||
							r.getSample(j+1, i+1, 0)==0||r.getSample(j+1, i-1, 0)==0)
						{										
							perimeter++;
						}
					}
				}
			}
		}
		
		return perimeter;		
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
}