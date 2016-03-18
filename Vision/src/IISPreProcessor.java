import java.awt.image.BufferedImage;

public final class IISPreProcessor {
	private IISPreProcessor()
	{
		//Cannot be called
		//Need to mimic a top level static class.
	}
	
	public static void linearStretching()
	{
		
	}
	
	public static void PowerLaw()
	{
		
	}
	
	public static void HistorgramEqualisation()
	{
		
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
	
	
	
	public static void Median()
	{
		
	}
	
}