import java.awt.image.BufferedImage;


public class Testing {


	
	public static void main(String[] args)

	{

	new Testing();

	}

	//constructor

	public Testing()

	{

	try

	{

		 
			
		

	}

	catch(Exception e)

	{ }

	}
	
	public static void displayAnImage(BufferedImage img, JVision display, int x, int y, String title)

	{

		display.imdisp(img,title,x,y);

		
	}
	
	
	public static BufferedImage readInImage(String filename)

	{

		BufferedImage img;

		img = ImageOp.readInImage(filename);

		return img;

	}

	
}
