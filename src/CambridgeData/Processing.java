package CambridgeData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.*;

public class Processing {
	
	Mat frame;

	public void setframe(Mat Frame){
		frame=Frame.clone();
	}
	
	private Mat getframe(){
		return frame;
	}
	
	public Mat process(){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat outerBox = new Mat();
		Mat frame_smooth =new Mat();
		Mat fmask = new Mat();
		ArrayList<Rect> array = new ArrayList<Rect>();
		Point tl = new Point();
		Point br = new Point();
		
		Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3));
		Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));
		Mat element3 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));
		
		Size sz = new Size(320, 240);
		
		BackgroundSubtractorMOG2 sub = Video.createBackgroundSubtractorMOG2(500,55,false); 
		
		Imgproc.resize(frame, frame, sz);
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
		
		
		/*array = detection_contours(frame_smooth);
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
		}
		Rect roi = new Rect(tl,br);
		Mat hand = frame_smooth.submat(roi);*/
		Imgproc.resize(frame_smooth, frame_smooth, new Size(320,240));
		return frame_smooth;
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

		}

		v.release();

		return rect_array;

	}

}
