package com.ahqlab.hodooopencv.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;
import static org.opencv.core.Core.NORM_HAMMING;
import static org.opencv.core.Core.NORM_L2;

public class HodooUtil {
    private static final String TAG = HodooUtil.class.getSimpleName();
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    public static Bitmap createShadowBitmap(Bitmap originalBitmap) {
        Canvas c = new Canvas(originalBitmap);
        Paint mShadow = new Paint();
// radius=10, y-offset=2, color=black
        mShadow.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
// in onDraw(Canvas)
        c.drawBitmap(originalBitmap, 0.0f, 0.0f, mShadow);
        return originalBitmap;
    }
    public static int compareFeature(String filename1) {
        String target = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "HodooOpenCV" + File.separator + "target.jpg";

        int retVal = 0;
        long startTime = System.currentTimeMillis();

        Mat img1 = Imgcodecs.imread(target, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Mat img2 = Imgcodecs.imread(filename1, Imgcodecs.CV_LOAD_IMAGE_COLOR);

        Mat gray1 = new Mat();
        Mat gray2 = new Mat();
        Imgproc.cvtColor(img1, gray1, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(img2, gray2, Imgproc.COLOR_RGB2GRAY);

        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

        /*********Be careful in here ORB or FeatureDetector****************/
        FastFeatureDetector featureDetector = FastFeatureDetector.create();
        //ORB detector = ORB.create();
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        detector.detect(gray1, keypoints1);
        detector.detect(gray2, keypoints2);

        extractor.compute(gray1, keypoints1, descriptors1);
        extractor.compute(gray2, keypoints2, descriptors2);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        MatOfDMatch matches = new MatOfDMatch();
        //Log.e(TAG, String.format("DescriptorExtractori : ++++++++++++++++++++++++++++++++++%d ", descriptors2.cols()));
        if (descriptors2.cols() == descriptors1.cols()) {
            matcher.match(descriptors1, descriptors2 ,matches);

            DMatch[] match = matches.toArray();
            double max_dist = 0; double min_dist = 100;

            for (int i = 0; i < descriptors1.rows(); i++) {
                double dist = match[i].distance;
                if( dist < min_dist ) min_dist = dist;
                if( dist > max_dist ) max_dist = dist;
            }

            for (int i = 0; i < descriptors1.rows(); i++) {
                if (match[i].distance <= 50) {
                    retVal++;
                }
            }
            if ( DEBUG ) Log.e(TAG, String.format("retVal : ++++++++++++++++++++++++++++++++++%d ", retVal));
        }

        long estimatedTime = System.currentTimeMillis() - startTime;
        MatOfByte drawnMatches = new MatOfByte();

        Mat imgMatches = new Mat();
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matches, imgMatches, new Scalar(0, 255, 0), new Scalar(255, 0, 0), drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
        Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES) + File.separator + "HodooOpenCV" + File.separator + "result.jpg", imgMatches);

        return retVal;
    }

}
