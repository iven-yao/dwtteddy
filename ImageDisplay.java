import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage buffered[];
	int width = 512; // default image width and height
	int height = 512;

	class YUV{
		double y,u,v;
		public YUV(double y, double u, double v) {
			this.y = y;
			this.u = u;
			this.v = v;
		}

		public String toString(){
			return "("+y+", "+u+" ,"+ v +")";
		}
	}

	class RGB{
		int r,g,b;
		public RGB(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public String toString(){
			return "("+r+", "+g+" ,"+ b +")";
		}
	}
	
	RGB[][] inputRGB = new RGB[height][width];
	YUV[][] convertedYUV = new YUV[height][width];
	RGB[][] outputRGB = new RGB[height][width];

	private YUV RGBtoYUV(RGB rgb){
		double y = 0.299*rgb.r + 0.587*rgb.g + 0.114*rgb.b;
		double u = 0.596*rgb.r - 0.274*rgb.g - 0.322*rgb.b;
		double v = 0.211*rgb.r - 0.523*rgb.g + 0.312*rgb.b;

		return new YUV(y,u,v);
	}

	private RGB YUVtoRGB(YUV yuv){
		int r = (int)(1*yuv.y + 0.956*yuv.u + 0.621*yuv.v);
		int g = (int)(1*yuv.y - 0.272*yuv.u - 0.647*yuv.v);
		int b = (int)(1*yuv.y - 1.106*yuv.u + 1.703*yuv.v);

		r = r<0?0:r>255?255:r;
		g = g<0?0:g>255?255:g;
		b = b<0?0:b>255?255:b;

		return new RGB(r, g, b);
	}

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;

			// read input, covert to yuv space
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					int r = Byte.toUnsignedInt(bytes[ind]);
					int g = Byte.toUnsignedInt(bytes[ind+height*width]);
					int b = Byte.toUnsignedInt(bytes[ind+height*width*2]); 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					
					img.setRGB(x,y,pix);
					ind++;

					RGB rgb = new RGB(r,g,b);
					// System.out.println(tmp);

					inputRGB[y][x] = rgb;
					convertedYUV[y][x] = RGBtoYUV(rgb);
				}
			}

			raf.close();
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

    private void dwt(int level) {
        int currLevelWidth = (int)Math.pow(2, level);
        // row by row
        for(int y = 0; y<currLevelWidth*2; y++) {
            for(int x = 0; x < currLevelWidth; x++) {
                
            }
        }

        // col by col

    }

	public void showIms(String[] args){

		// Read a parameter from command line
		// String param1 = args[1];
		// System.out.println("The second parameter was: " + param1);

		// Read in the specified image
		buffered = new BufferedImage[10];
		readImageRGB(args[0], buffered[0]);
        for(int level = 9; level > Integer.valueOf(args[1]); level--){

        }

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(buffered[0]));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
