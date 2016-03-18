import java.awt.image.BufferedImage;

public class Vs

{

public static void main(String[] args)

{

new VisionSystem();

}

//constructor

public Vs()

{

try

{
	JVision jvis = new JVision();
	BufferedImage imageOne;

	imageOne = readInImage("/Vision/src/vehicle3.jpg");

	displayAnImage(imageOne, jvis, 1, 1, "");

}

catch(Exception e)

{ }

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


}