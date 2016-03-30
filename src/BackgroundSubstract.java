import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.*;
import org.opencv.videoio.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.*;



public class BackgroundSubstract {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	static Mat imag;

	public static void main(String[] args) {
		JFrame jframe = new JFrame("Background Substraction Output");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel vidpanel = new JLabel();
		jframe.setContentPane(vidpanel);
		jframe.setSize(640, 480);
		jframe.setVisible(true);
		
		JFrame jframe2 = new JFrame("Motion Difference Output");
		jframe2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel vidpanel2 = new JLabel();
		jframe2.setContentPane(vidpanel2);
		jframe2.setSize(640, 480);
		jframe2.setVisible(true);
		
		JFrame jframe3 = new JFrame("Motion Difference Output Smoothing");
		jframe3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel vidpanel3 = new JLabel();
		jframe3.setContentPane(vidpanel3);
		jframe3.setSize(640, 480);
		jframe3.setVisible(true);

		Mat frame = new Mat();
		Mat outerBox = new Mat();
		Mat frame_smooth =new Mat();
		Mat fmask = new Mat();
		ArrayList<Rect> array = new ArrayList<Rect>();
		Point tl = new Point();
		Point br = new Point();
		
		Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3));
		Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));
		Mat element3 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));
		
		VideoCapture camera = new VideoCapture(0);
		Size sz = new Size(640, 480);
		
		BackgroundSubtractorMOG2 sub = Video.createBackgroundSubtractorMOG2(500,55,false); //TODO : Play with the three parameters to get better results; also search for more parameters for this class
		
		while (true) {
			if (camera.read(frame)) {
				Imgproc.resize(frame, frame, sz);
				imag = frame.clone();
				outerBox = new Mat(frame.size(), CvType.CV_8UC1);
				
				sub.apply(frame, fmask);
				Imgproc.morphologyEx(fmask, fmask, Imgproc.MORPH_OPEN, element3, new Point(-1,-1), 1);
				
				Imgproc.GaussianBlur(frame, frame, new Size(11, 11), 0,0);
				Imgproc.medianBlur(frame, frame, 11);
				Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);
				Core.inRange(frame,new Scalar(0, 55, 90, 255),new Scalar(28, 175, 230, 255),outerBox);
												
				Core.bitwise_and(outerBox, fmask, frame_smooth); 
				
				Imgproc.erode(frame_smooth, frame_smooth, element1);
				Imgproc.dilate(frame_smooth, frame_smooth, element2);
				Imgproc.GaussianBlur(frame_smooth, frame_smooth, new Size(3,3), 0,0);
				
				
				array = detection_contours(frame_smooth);
				if (array.size() > 0) {

					Iterator<Rect> it2 = array.iterator();
					Rect obj = it2.next();
					tl = obj.tl();
					br = obj.br();
					while (it2.hasNext()) {
						obj = it2.next();
						
						if(obj.tl().x < tl.x)
							tl.x = obj.tl().x;
						if(obj.tl().y < tl.y)
							tl.y = obj.tl().y;
						
						if(obj.br().x > br.x)
							br.x = obj.br().x;
						if(obj.br().y > br.y)
							br.y = obj.br().y;
						
					}
					Imgproc.rectangle(imag, tl, br, new Scalar(0, 255, 0), 1);

				}
				
				ImageIcon image = new ImageIcon(Mat2bufferedImage(outerBox));
				ImageIcon image2 = new ImageIcon(Mat2bufferedImage(frame_smooth));
				ImageIcon image3 = new ImageIcon(Mat2bufferedImage(imag));
				
				vidpanel.setIcon(image);
				vidpanel.repaint();
				
				vidpanel2.setIcon(image2);
				vidpanel2.repaint();
				
				vidpanel3.setIcon(image3);
				vidpanel3.repaint();
				
				
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
	
	public static ArrayList<Rect> detection_contours(Mat outmat) {
		Mat v = new Mat();
		Mat vv = outmat.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);

		double maxArea = 200;
		
		Rect r = null;
		ArrayList<Rect> rect_array = new ArrayList<Rect>();

		for (int idx = 0; idx < contours.size(); idx++) {
			Mat contour = contours.get(idx);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > maxArea) {
				
				
				r = Imgproc.boundingRect(contours.get(idx));
				rect_array.add(r);
				
			}
			
			//Imgproc.drawContours(imag, contours, idx, new Scalar(255,255, 255));

		}

		v.release();

		return rect_array;

	}
	
}