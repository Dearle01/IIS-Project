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
		IISProcessor.displayAll(trainingImages, "Training");
	}
	public void displayAllPreP()
	{
		IISProcessor.displayAll(preprocessedImages, "PreProcessed");
	}
	public void displayAllPostP()
	{
		IISProcessor.displayAll(postprocessedImages, "PostProcessed");
	}
	public void displayAllThreshold()
	{
		IISProcessor.displayAll(thresholdedImages, "Thresholded");
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


