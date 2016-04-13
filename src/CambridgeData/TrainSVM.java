package CambridgeData;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.*;
import org.opencv.ml.Ml;
import org.opencv.ml.SVM;

import java.io.*;

public class TrainSVM {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main (String args[]){
		File f = new File("G:\\Documents\\Gesture Recognition Technology\\Hand Gesture Videos Set1");
		File type[] = f.listFiles();
		
		Mat frame = new Mat();
		Processing ob = new Processing();
		Mat hand;
		
		int num_files = 25;
		int area = 100*75;
		Mat trainingMat = new Mat(num_files,area,CvType.CV_32FC1);
		Mat labels= new Mat(num_files,1,CvType.CV_32SC1);
		
		int i,j,k,r,c,num_video=0;
		double fno;
		
		for (i=0;i<type.length;i++){
			File videos[] = type[i].listFiles();
			System.out.println("Video Type"+(i+1));
			
			for(j =0;j<videos.length-2;j++){
				System.out.println("Video no:"+(j+1));
				
				VideoCapture cam = new VideoCapture(videos[j].getAbsolutePath());
				double count = cam.get(Videoio.CAP_PROP_FRAME_COUNT);
				int l=0;
				for(k=1;k<=3;k++){
					switch(k){
					case 1:
						fno = 0;
						break;
					case 2:
						fno = (int)count/2;
						break;
					case 3:
						fno = count-1;
						break;
					default:
						System.out.println("Array exceeded.");
						fno=0;
						
					}
					cam.set(Videoio.CAP_PROP_POS_FRAMES, fno);
					if (cam.read(frame)){
						
						ob.setframe(frame);
						hand = ob.process().clone();
						
						
						for(r=0;r<hand.rows();r++){
							for(c=0;c<hand.cols();c++){
								double temp[]= hand.get(r, c);
								trainingMat.put(num_video, l++, temp);
							}
						}	
					}
				}
				
				System.out.println("Matrix :" + (num_video+1) + ", No of rows: " + l);
				
				labels.put(num_video, 0,(i+1) );
				
				num_video++;
				cam.release();
			}
			
		}
		
		System.out.println(labels.dump());
		//System.out.println(trainingMat.dump());
		
				
		SVM svm = SVM.create();
		svm.setType(SVM.C_SVC);
		svm.setKernel(SVM.POLY);
        svm.setDegree(6.0);
        svm.setGamma(100);
        svm.setC(10);

        svm.train(trainingMat, Ml.ROW_SAMPLE, labels);
        
        
        
        File p_f = new File("G:\\Documents\\Gesture Recognition Technology\\Hand Gesture Videos Set1");
        File p_type[] = p_f.listFiles();
        
        for (i=0;i<p_type.length;i++){
			File p_videos[] = p_type[i].listFiles();
			
			for(j =5;j<p_videos.length;j++){
				Mat testMat = new Mat(1,area,CvType.CV_32FC1);
				//if (i==1&&j==6||i==2&&j==5||i==3&&j==5||i==3&&j==6)
					//continue;
				VideoCapture p_cam = new VideoCapture(p_videos[j].getAbsolutePath());
				double p_count = p_cam.get(Videoio.CAP_PROP_FRAME_COUNT);
				int p_l=0;
				for(k=1;k<=3;k++){
					switch(k){
					case 1:
						fno = 0;
						break;
					case 2:
						fno = (int)p_count/2;
						break;
					case 3:
						fno = p_count-1;
						break;
					default:
						System.out.println("Array exceeded.");
						fno=0;
						
					}
					p_cam.set(Videoio.CAP_PROP_POS_FRAMES, fno);
					if (p_cam.read(frame)){
						
						ob.setframe(frame);
						hand = ob.process().clone();
						
						
						for(r=0;r<hand.rows();r++){
							for(c=0;c<hand.cols();c++){
								double p_temp[]= hand.get(r, c);
								testMat.put(0, p_l++, p_temp);
							}
						}	
					}
				}
				
				int res =(int)svm.predict(testMat);
				System.out.println("Correct Label: "+(i+1)+" Predicted Label: "+res);
				p_cam.release();
			}
			
		}
	}
	
}
