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

public class SkinFiltering {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	static Mat imag = null;

	public static void main(String[] args) {
		JFrame jframe = new JFrame("HUMAN MOTION DETECTOR FPS");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel vidpanel = new JLabel();
		jframe.setContentPane(vidpanel);
		jframe.setSize(640, 480);
		jframe.setVisible(true);

		Mat frame = new Mat();
		Mat outerBox_gray = new Mat();
		Mat outerBox = new Mat();
		Mat diff_frame = null;
		Mat tempon_frame = null;
		ArrayList<Rect> array = new ArrayList<Rect>();
		VideoCapture camera = new VideoCapture(0);
		Size sz = new Size(640, 480);
		int i = 0;

		while (true) {
			if (camera.read(frame)) {
				Imgproc.resize(frame, frame, sz);
				imag = frame.clone();
				outerBox = new Mat(frame.size(), CvType.CV_8UC1);
				outerBox_gray = new Mat(frame.size(), CvType.CV_8UC1);
				
				Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2HSV);
				Imgproc.GaussianBlur(outerBox, outerBox, new Size(7, 7), 1,1);
				
				double temp[] = {0,0,0};
				for(int r=0; r< outerBox.rows() ;r++)
					for(int c=0; c<outerBox.cols(); c++)
						if( (outerBox.get(r,c)[0]>5) && (outerBox.get(r,c)[0]<17) && (outerBox.get(r,c)[1]>38) && (outerBox.get(r,c)[1]<250) && (outerBox.get(r,c)[2]<51) && (outerBox.get(r,c)[0]<242));
						else
								outerBox.put(r, c, temp);
				
				Imgproc.cvtColor(outerBox, outerBox, Imgproc.COLOR_HSV2BGR);
				Imgproc.cvtColor(outerBox, outerBox_gray, Imgproc.COLOR_BGR2GRAY);
				
				//Imgproc.threshold(outerBox_gray, outerBox_gray, 60, 255, Imgproc.THRESH_BINARY);
				Imgproc.adaptiveThreshold(outerBox_gray, outerBox_gray, 255,
					    Imgproc.ADAPTIVE_THRESH_MEAN_C, 
						Imgproc.THRESH_BINARY_INV,5,2);
				
				Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
				Mat element3 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,7));
				/*Imgproc.morphologyEx(outerBox_gray, outerBox_gray, Imgproc.MORPH_ERODE, element2, new Point(-1,-1), 3);
				Imgproc.morphologyEx(outerBox_gray, outerBox_gray, Imgproc.MORPH_OPEN, element3, new Point(-1,-1), 1);
				Imgproc.morphologyEx(outerBox_gray, outerBox_gray, Imgproc.MORPH_CLOSE, element3, new Point(-1,-1), 1);
				
				Imgproc.medianBlur(outerBox_gray, outerBox_gray, 15);*/
				
				Imgproc.GaussianBlur(outerBox, outerBox, new Size(5, 5), 3);

				/*if (i == 0) {
					jframe.setSize(frame.width(), frame.height());
					diff_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
					tempon_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
					diff_frame = outerBox.clone();
				}

				if (i == 1) {
					Core.subtract(outerBox, tempon_frame, diff_frame);
					Imgproc.adaptiveThreshold(diff_frame, diff_frame, 255,
						    Imgproc.ADAPTIVE_THRESH_MEAN_C, 
							Imgproc.THRESH_BINARY_INV,11,2);
					
					Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15,15));
					Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,4));
					
					Imgproc.erode(diff_frame, diff_frame, element1);
					Imgproc.dilate(diff_frame,diff_frame,element);
					Imgproc.GaussianBlur(diff_frame, diff_frame, new Size(9, 9), 3);
				
				}

				i = 1;*/

				ImageIcon image = new ImageIcon(Mat2bufferedImage(outerBox_gray));
				vidpanel.setIcon(image);
				vidpanel.repaint();
				tempon_frame = outerBox.clone();

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
