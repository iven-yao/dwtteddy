import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage bufImg, bufImgs[];
	int width = 512; // default image width and height
	int height = 512;
	long fps = 1000/5; // default

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

	public RGB avg(RGB rgb1, RGB rgb2) {
		int r = (rgb1.r + rgb2.r)/2;
		int g = (rgb1.g + rgb2.g)/2;
		int b = (rgb1.b + rgb2.b)/2;
		return new RGB(r,g,b);
	}
	
	RGB[][][] levelRGB = new RGB[10][height][width];


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
					levelRGB[9][y][x] = rgb;
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
        for(int y = 0; y<height; y++) {
            for(int x = 0; x < width; x++) {
                if(x < currLevelWidth) {
					// calculate the dwt new rgb from upper level:
					RGB rgb1 = levelRGB[level+1][y][x*2];
					RGB rgb2 = levelRGB[level+1][y][x*2+1];
					levelRGB[level][y][x] = avg(rgb1,rgb2);
				} 
				else {
				 	levelRGB[level][y][x] = new RGB(0,0,0);
				}
            }
        }

        // col by col
		for(int y = 0; y<height; y++) {
            for(int x = 0; x < width; x++) {
                if(y < currLevelWidth) {
					// calculate the dwt new rgb from curr level:
					RGB rgb1 = levelRGB[level][y*2][x];
					RGB rgb2 = levelRGB[level][y*2+1][x];
					levelRGB[level][y][x] = avg(rgb1,rgb2);	
				} 
				else {
					levelRGB[level][y][x] = new RGB(0, 0, 0);
				}
            }
        }
    }

	private void idwt(int level) {
		int currLevelWidth = (int)Math.pow(2, level);
		int step = width/currLevelWidth;
		for(int y = height-1; y >=0; y--) {
			for(int x = width-1; x>=0; x--) {
				levelRGB[level][y][x] = levelRGB[level][y/step][x/step];
			}
		}
		

	}

	private void setImgRGB(RGB[][] outputRGB, BufferedImage img) {
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{

				int r = outputRGB[y][x].r;
				int g = outputRGB[y][x].g;
				int b = outputRGB[y][x].b;
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);		
				img.setRGB(x,y,pix);
			}
		}
	}

	public void showIms(String[] args) throws InterruptedException{

		// Read a parameter from command line
		// String param1 = args[1];
		// System.out.println("The second parameter was: " + param1);

		// Read in the specified image
		bufImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(args[0], bufImg);
		int lvl = Integer.valueOf(args[1]);
		if(lvl == -1) {
			bufImgs = new BufferedImage[10];
			bufImgs[9] = bufImg;
			//dwt
			for(int level = 8; level >= 0; level--) {
				bufImgs[level] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				dwt(level);
			}

			for(int level = 8; level >= 0; level--) {
				idwt(level);
				setImgRGB(levelRGB[level], bufImgs[level]);
			}

		} else {
			for(int level = 8; level >= lvl; level--){
				dwt(level);
			}
			idwt(lvl);
			setImgRGB(levelRGB[lvl], bufImg);
		}

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(bufImg));

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

		if(lvl == -1) {
			for(int i = 0; i < 10; i++) {
				lbIm1.setIcon(new ImageIcon(bufImgs[i]));
				Thread.sleep(fps);
			}
		}
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		try {
			ren.showIms(args);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
