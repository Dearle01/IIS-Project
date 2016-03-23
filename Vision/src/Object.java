import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Object {
	public ArrayList<BufferedImage> trainingImages;
	public ArrayList<BufferedImage> preprocessedImages;
	public ArrayList<BufferedImage> postprocessedImages;
	public ArrayList<BufferedImage> thresholdedImages;

	public int area, perimeter;

	public String name;
	public int [] areaArr;
	public int [] perimArr;

	public Object(ArrayList<BufferedImage> images, String className)
	{
		trainingImages = images;
		areaArr = new int [trainingImages.size()];
		perimArr = new int [trainingImages.size()];//twice?
		name = className;
		area = 0;
		perimeter = 0;
	}

	public void displayAllTraining()
	{
		IISProcessor.displayAll(trainingImages, "Training for " + name);
	}
	public void displayAllPreP()
	{
		IISProcessor.displayAll(preprocessedImages, "PreProcessed " + name);
	}
	public void displayAllPostP()
	{
		IISProcessor.displayAll(postprocessedImages, "PostProcessed " + name);
	}
	public void displayAllThreshold()
	{
		IISProcessor.displayAll(thresholdedImages, "Thresholded "  + name);
	}

	//Get the area and perimeter for all of the images
	public void Classify()
	{
		perimArr = IISProcessor.getPerimeter(thresholdedImages);
	}
	public void displayAllHistogram()
	{
		try
		{
			IISProcessor.displayAllHistogram(trainingImages, "Histograms for original "  + name);
		}
		catch(HistogramException e)
		{
			System.out.print("Error printing histogram");
		}

	}
	public void Preprocess()
	{
		preprocessedImages = IISProcessor.PowerLaw(trainingImages,1.5f);
		//preprocessedImages = IISProcessor.EnhanceBrightness(preprocessedImages, 20);
	}

	public void Threshold()
	{
		try
		{
			thresholdedImages = IISProcessor.ThresholdImages(trainingImages);
		}
		catch(HistogramException e)
		{
			System.out.print("Uh-Oh!");
		}
	}

	public void PostProcess() throws IOException, HistogramException
	{
		postprocessedImages = IISProcessor.PostProcessImages(thresholdedImages);
		IISProcessor.printAreas(postprocessedImages);
		TestNewImage();
	}

	public void TestNewImage() throws IOException, HistogramException
	{
		JVision jvis = new JVision();

		jvis.setBounds(0, 0, 1500, 1000);
		jvis.setTitle("Testing Phase");

		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));

		chooser.showOpenDialog(jvis);
		File file = chooser.getSelectedFile();

		BufferedImage testImage = IISProcessor.readInImage(file.getName());
		ArrayList<BufferedImage> sourceImage = new ArrayList<BufferedImage>();
		Collections.addAll(sourceImage, testImage);
		
		sourceImage= IISProcessor.PowerLaw(sourceImage,1.5f);
		sourceImage = IISProcessor.EnhanceBrightness(sourceImage, 20);
		sourceImage = IISProcessor.ThresholdImages(sourceImage);
		sourceImage = IISProcessor.PostProcessImages(sourceImage);	
		
		IISProcessor.nearestNeighbourCalc(sourceImage.get(0), postprocessedImages);

	}


}


