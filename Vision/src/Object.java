import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Object {
	public ArrayList<BufferedImage> trainingImages;
	public ArrayList<BufferedImage> preprocessedImages;
	public ArrayList<BufferedImage> postprocessedImages;
	public ArrayList<BufferedImage> thresholdedImages;
	
	public int area, perimeter;
	
	public String name;
	int [] areaArr;
	int [] perimArr;
	
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
		preprocessedImages = IISProcessor.EnhanceBrightness(preprocessedImages, 20);
	}
	
	public void Threshold()
	{
		try
		{
			thresholdedImages = IISProcessor.ThresholdImages(preprocessedImages);
		}
		catch(HistogramException e)
		{
			System.out.print("Uh-Oh!");
		}
	}
	
	public void PostProcess()
	{
			postprocessedImages = IISProcessor.PostProcessImages(thresholdedImages);
	}
	
	
}


