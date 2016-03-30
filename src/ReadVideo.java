import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.*;
import org.opencv.imgproc.Imgproc;

public class ReadVideo {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main (String args[]){
		VideoCapture cam = new VideoCapture("G:\\Documents\\Gesture Recognition Technology\\Hand Gesture Videos Set1\\0000 closed fingers hand turning left\\1.avi");
		JFrame jframe = new JFrame("Reading Video");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel vidpanel = new JLabel();
		jframe.setContentPane(vidpanel);
		jframe.setSize(640, 480);
		jframe.setVisible(true);
		
		Mat frame = new Mat();
		
		while (true){
			if (cam.read(frame)){
				Imgproc.resize(frame, frame, new Size(640,480));
				
				ImageIcon image = new ImageIcon(Mat2bufferedImage(frame));
				vidpanel.setIcon(image);
				vidpanel.repaint();
			}
		}
	}
	
	public static BufferedImage Mat2bufferedImage(Mat image) {
		MatOfByte bytemat = new MatOfByte();
		Imgcodecs.imencode(".jpg", image, bytemat);
		byte[] bytes = bytemat.toArray();
		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage img = null;
		try {
			img = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}

}
