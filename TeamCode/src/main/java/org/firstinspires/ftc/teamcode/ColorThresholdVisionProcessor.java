package org.firstinspires.ftc.teamcode;

import android.graphics.Canvas;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.teamcode.geometry.Line2d;
import org.firstinspires.ftc.teamcode.geometry.Point2d;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;

public class ColorThresholdVisionProcessor implements VisionProcessor {
    public Scalar lower = new Scalar(29.0, 77.9, 0.0);
    public Scalar upper = new Scalar(51.0, 255.0, 255.0);

    public int pov_x = 0;
    public int pov_y = 0;

    public float right = 0;

    public float left = 0;

    public final float minDistance = 100f;
    //261.538f
    public ColorSpace colorSpace = ColorSpace.HSV;

    private Mat mat = new Mat();
    private Mat ret = new Mat();

    public int rect_threshold = 5000;

    public double prop = 0.182353;

    private Telemetry telemetry = null;

    public ColorThresholdVisionProcessor(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    @Override
    public void init(int width, int height, CameraCalibration calibration) {
    }

    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        ret.release();
        ret = new Mat();

        try {
            pov_x = frame.width() / 2;
            pov_y = frame.height() - 5;

            // convert RBB to Lab
            Imgproc.cvtColor(frame, mat, colorSpace.cvtCode);

            // convert to b/w by removing all colors not in range
            Mat mask = new Mat(mat.rows(), mat.cols(), mat.type());
            //frame.copyTo(mask);
            Core.inRange(mat, lower, upper, mask);

            // Set background to black
            Core.bitwise_and(frame, frame, ret, mask);

            // Blur to remove noise
            Imgproc.GaussianBlur(mask, mask, new org.opencv.core.Size(3, 3), 0);

            // Find contours
            ArrayList<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_TREE,
                    Imgproc.CHAIN_APPROX_SIMPLE);

            //telemetry.addData("Contours", contours.size());

            // Create a list of each bounding rect
            ArrayList<Rect> rects = new ArrayList<>();
            for (MatOfPoint contour : contours) {
                rects.add(Imgproc.boundingRect(contour));
            }

            // Remove all rects whose area is less than rect_threshold
            for (int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                if (rect.area() < rect_threshold) {
                    rects.remove(i);
                    i--;
                }
            }

            ArrayList<Line2d> closest_points = new ArrayList<>();
            for (int i = 0; i < rects.size(); i++) {
                //Imgproc.rectangle(ret, rects.get(i), new Scalar(0, 255, 0), 2);
                //telemetry.addData(i + ": (" + rects.get(i).x + ", " + rects.get(i).y + "),
                // Area: " + rects.get(i).area(), "");
                //int x = rects.get(i).x + rects.get(i).width;
                //int y = rects.get(i).y + rects.get(i).height;

                int height = rects.get(i).height;
                int width = rects.get(i).width;

                ArrayList<Line2d> bb_points = new ArrayList<>();
                //add all points along the top edge of the bounding box
                for (int x = rects.get(i).x; x < rects.get(i).x + rects.get(i).width; x += 10) {
                    bb_points.add(new Line2d(new Point2d(x, rects.get(i).y),
                            new Point2d(pov_x, pov_y), height, width));
                }

                //add all points along the bottom edge of the bounding box
                for (int x = rects.get(i).x; x < rects.get(i).x + rects.get(i).width; x += 10) {
                    bb_points.add(new Line2d(new Point2d(x,
                            rects.get(i).y + rects.get(i).height), new Point2d(pov_x, pov_y),
                            height, width));
                }

                //add all points along the left edge of the bounding box
                for (int y = rects.get(i).y; y < rects.get(i).y + rects.get(i).height; y += 10) {
                    bb_points.add(new Line2d(new Point2d(rects.get(i).x, y),
                            new Point2d(pov_x, pov_y), height, width));
                }

                //add all points along the right edge of the bounding box
                for (int y = rects.get(i).y; y < rects.get(i).y + rects.get(i).height; y += 10) {
                    bb_points.add(new Line2d(new Point2d(rects.get(i).x + rects.get(i).width
                            , y), new Point2d(pov_x, pov_y), height, width));
                }

                bb_points.sort(Comparator.comparingDouble(Line2d::get_distance));

                closest_points.add(bb_points.get(0));
            }

            for (Line2d point : closest_points) {
                Imgproc.circle(ret, point.get_start_point().toPoint(), 5, new Scalar(0, 0, 255), 5);
            }

            // Draw circle in center of frame
            Imgproc.circle(ret, new Point(pov_x, pov_y), 5, new Scalar(255, 0, 0), 5);

            // Draw line from pov to each point in closest_points
            /*for (DistanceRep point : closest_points) {
                Imgproc.line(ret, point.get_start_point().toPoint(), point.get_end_point()
                .toPoint(), new Scalar(255, 0, 0), 2);
            }*/

            // Sort closest_points by lowest distance first
            closest_points.sort(Comparator.comparingDouble(Line2d::get_distance));

            // Draw the 3 closest lines, closest in red, 2nd closest in green, 3rd closest in blue
            for (int i = 0; i < closest_points.size() && i < 3; i++) {
                Line2d point = closest_points.get(i);
                Scalar color = new Scalar(0, 0, 0);
                if (i == 0) // closest
                    color = new Scalar(255, 0, 0);
                else if (i == 1) // 2nd closest
                    color = new Scalar(0, 255, 0);
                else if (i == 2) // 3rd closest
                    color = new Scalar(0, 0, 255);
                Imgproc.line(ret, point.get_start_point().toPoint(),
                        point.get_end_point().toPoint(), color, 2);
                telemetry.addData(i + ": Distance " + (point.get_distance() * prop) + "in, " +
                        "Height: " + point.get_height() * prop + ", Width" + point.get_width() * prop, "");
            }

            Line2d leftPoint = closestLeft(closest_points);
            Line2d rightPoint = closestRight(closest_points);

            if (leftPoint.get_distance() * prop <= minDistance && leftPoint.get_start_point().get_y() > pov_y/2.0) {
                left = weightedDistance(leftPoint) + weightedYPost(leftPoint);
            }else{
                left = 0;
            }

            if (rightPoint.get_distance() * prop <= minDistance && rightPoint.get_start_point().get_y() > pov_y/2.0) {
                right = weightedDistance(rightPoint) + weightedYPost(rightPoint);
            }else{
                right = 0;
            }

            telemetry.addData("left: ", left);
            telemetry.addData("right: ", right);

            displayBars(left, right, ret);

            ret.copyTo(frame);
        } catch (Exception e) {
            telemetry.addData("Error", e.getMessage());
        }

        telemetry.update();

        return null;
    }

    private float weightedDistance(Line2d point) {
        float distance =
                (float) ((Math.abs(minDistance - (point.get_distance() * prop)) / minDistance) * 0.40f);
        if (point.get_distance() * prop < 50) {
            return 0.4f;

        }
        return distance;
    }

    private float weightedYPost(Line2d point) {
        float weight = (float) (Math.abs(pov_y - point.get_start_point().get_y()) / pov_y) * 0.6f;
        if (point.get_start_point().get_y() >= 800) {
            return 0.6f;
        }
        return weight;
    }

    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight,
                            float scaleBmpPxToCanvasPx, float scaleCanvasDensity,
                            Object userContext) {
    }

    private Line2d closestRight(ArrayList<Line2d> closest_points) {
        int index = -1;
        Line2d closest = new Line2d(new Point2d(0, 0), new Point2d(0, 0), 0, 0);
        for (int i = 0; i < closest_points.size(); i++) {
            if (isRight(closest_points.get(i))) {
                closest = closest_points.get(i);
                index = i;
                break;
            }
        }

        if (index == -1) {
            return closest;
        }

        for (int i = index; i < closest_points.size(); i++) {
            if (isRight(closest_points.get(i))) {
                if (closest_points.get(i).get_distance() < closest.get_distance()) {
                    closest = closest_points.get(i);
                }

            }
        }
        return closest;
    }

    private Line2d closestLeft(ArrayList<Line2d> closest_points) {
        int index = -1;
        Line2d closest = new Line2d(new Point2d(0, 0), new Point2d(0, 0), 0, 0);
        for (int i = 0; i < closest_points.size(); i++) {
            if (isLeft(closest_points.get(i))) {
                closest = closest_points.get(i);
                index = i;
                break;
            }
        }

        if (index == -1) {
            return closest;
        }

        for (int i = index; i < closest_points.size(); i++) {
            if (isLeft(closest_points.get(i))) {
                if (closest_points.get(i).get_distance() < closest.get_distance()) {
                    closest = closest_points.get(i);
                }

            }
        }

        return closest;
    }

    private boolean isLeft(Line2d point) {
        return point.get_start_point().get_x() <= pov_x;
    }

    private boolean isRight(Line2d point) {
        return point.get_start_point().get_x() >= pov_x;
    }

    private void displayBars(float left, float right, Mat frame) {
        // left and right will be a value from [0.0, 1.0],
        // draw 2 filled verticle rectangles on the top right corner of the sreen that represent the values
        // they should be 100px tall and 50px wide

        int bar_width = 50;
        int bar_height = 120;

        //left bar
        int left_bar_x = frame.width() - bar_width * 2;
        int left_bar_y = 0;
        int left_bar_fill_height = (int) (left * bar_height);
        int left_bar_fill_y = bar_height - left_bar_fill_height;
        Imgproc.rectangle(frame, new Point(left_bar_x, left_bar_y), new Point(left_bar_x + bar_width, left_bar_y + bar_height), new Scalar(0, 0, 0), -1);

        //right bar
        int right_bar_x = frame.width() - bar_width;
        int right_bar_y = 0;
        int right_bar_fill_height = (int) (right * bar_height);
        int right_bar_fill_y = bar_height - right_bar_fill_height;
        Imgproc.rectangle(frame, new Point(right_bar_x, right_bar_y), new Point(right_bar_x + bar_width, right_bar_y + bar_height), new Scalar(0, 0, 0), -1);

        // fill left bar
        Imgproc.rectangle(frame, new Point(left_bar_x, left_bar_y + left_bar_fill_y), new Point(left_bar_x + bar_width, left_bar_y + bar_height), new Scalar(229, 15, 87), -1);

        // fill right bar
        Imgproc.rectangle(frame, new Point(right_bar_x, right_bar_y + right_bar_fill_y), new Point(right_bar_x + bar_width, right_bar_y + bar_height), new Scalar(19, 122, 227), -1);
    }
}
