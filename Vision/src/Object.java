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
	
	public Object(ArrayList<BufferedImage> trainingImages, String className)
	{
		trainingImages = new ArrayList<BufferedImage>();
		areaArr = new int [trainingImages.size()];
		perimArr = new int [trainingImages.size()];//twice?
		name = className;
		area = 0;
		perimeter = 0;
	}
	
	public void displayAll()
	{
		IISProcessor.displayAll(trainingImages);
	}
	public void Preprocess()
	{
		try
		{
			preprocessedImages = IISProcessor.LinearStretching(trainingImages);
		}
		catch(HistogramException e)
		{
			System.out.print("Uh-Oh!");
		}
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


