

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

public class SkinFilteringHSV {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	static Mat imag = null;

	public static void main(String[] args) {
		JFrame jframe = new JFrame("HSV Skin Output");
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
		Mat frame_smooth =null;
		Mat diff_frame = null;
		Mat tempon_frame = null;
		
		
		VideoCapture camera = new VideoCapture(0);
		Size sz = new Size(640, 480);
		int i = 0;

		while (true) {
			if (camera.read(frame)) {
				Imgproc.resize(frame, frame, sz);
				imag = new Mat(frame.size(),CvType.CV_8UC3, new Scalar(0,0,0));
				outerBox = new Mat(frame.size(), CvType.CV_8UC1);
								
				Imgproc.GaussianBlur(frame, frame, new Size(11, 11), 0,0);
				Imgproc.medianBlur(frame, frame, 11);
				
				Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);
				
				Core.inRange(frame,new Scalar(0, 55, 90, 255),new Scalar(28, 175, 230, 255),outerBox);
				
				Mat element4 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,7),new Point(4,4));
				Imgproc.erode(outerBox, outerBox, element4);
				//Imgproc.morphologyEx(frame, frame, Imgproc.MORPH_OPEN, element4, new Point(-1,-1), 1);
				
				
				
				if (i == 0) {
					jframe.setSize(frame.width(), frame.height());
					jframe2.setSize(frame.width(), frame.height());
					jframe3.setSize(frame.width(), frame.height());
					diff_frame = new Mat(frame.size(), CvType.CV_8UC1);
					tempon_frame = new Mat(frame.size(), CvType.CV_8UC1);
					frame_smooth = new Mat(frame.size(), CvType.CV_8UC1);
					
					diff_frame = outerBox.clone();
				}

				if (i == 1) {
					Core.subtract(outerBox, tempon_frame, diff_frame);
					Imgproc.adaptiveThreshold(diff_frame, diff_frame, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,3,0);
					
					//Imgproc.medianBlur(diff_frame, frame_smooth, 11);
					
					Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(11,11));
					//Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(11,11));
					
					Imgproc.erode(diff_frame, frame_smooth, element1);
					//Imgproc.dilate(frame_smooth, frame_smooth, element2);
					
					//Imgproc.GaussianBlur(frame_smooth, frame_smooth, new Size(3, 3), 0,0);
					
					detection_contours(frame_smooth);
					
				}

				i = 1;
				tempon_frame = outerBox.clone();
				
				ImageIcon image = new ImageIcon(Mat2bufferedImage(outerBox));
				ImageIcon image2 = new ImageIcon(Mat2bufferedImage(diff_frame));
				ImageIcon image3 = new ImageIcon(Mat2bufferedImage(frame_smooth));
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

	public static void detection_contours(Mat outmat) {
		Mat v = new Mat();
		Mat vv = outmat.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(vv, contours, v, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);

		double maxArea = 100;
		int maxAreaIdx = -1;
		

		for (int idx = 0; idx < contours.size(); idx++) {
			/*Mat contour = contours.get(idx);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > maxArea) {
				maxArea = contourarea;
				maxAreaIdx = idx;
				
				Imgproc.drawContours(imag, contours, maxAreaIdx, new Scalar(255,255, 255));
			}*/
			Imgproc.drawContours(imag, contours, idx, new Scalar(255,255, 255));

		}

		v.release();

	}

}
