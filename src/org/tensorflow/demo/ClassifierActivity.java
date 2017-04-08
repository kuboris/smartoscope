/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.demo;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // These are the settings for the original v1 Inception model. If you want to
  // use a model that's been produced from the TensorFlow for Poets codelab,
  // you'll need to set IMAGE_SIZE = 299, IMAGE_MEAN = 128, IMAGE_STD = 128,
  // INPUT_NAME = "Mul", and OUTPUT_NAME = "final_result".
  // You'll also need to update the MODEL_FILE and LABEL_FILE paths to point to
  // the ones you produced.
  //
  // To use v3 Inception model, strip the DecodeJpeg Op from your retrained
  // model first:
  //
  // python strip_unused.py \
  // --input_graph=<retrained-pb-file> \
  // --output_graph=<your-stripped-pb-file> \
  // --input_node_names="Mul" \
  // --output_node_names="final_result" \
  // --input_binary=true
//  private static final int INPUT_SIZE = 224;
//  private static final int IMAGE_MEAN = 117;
//  private static final float IMAGE_STD = 1;
    private static final int INPUT_SIZE = 299;
  private static final int IMAGE_MEAN = 128;
  private static final float IMAGE_STD = 128;
  private static final String INPUT_NAME = "Mul";
  private static final String OUTPUT_NAME = "final_result";

  private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
  private static final String LABEL_FILE =
      "file:///android_asset/imagenet_comp_graph_label_strings.txt";

  private static final boolean SAVE_PREVIEW_BITMAP = false;

  private static final boolean MAINTAIN_ASPECT = true;

  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

  private String mPreviousClass = "";
  private int mToastLength =  Toast.LENGTH_SHORT;

  private List<Integer> detectionFrameHistory; // Add to class definition of ClassifierActivity.java
  private Classifier classifier;

  private Integer sensorOrientation;

  private int previewWidth = 0;
  private int previewHeight = 0;
  private byte[][] yuvBytes;
  private int[] rgbBytes = null;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;

  private Bitmap cropCopyBitmap;

  private boolean computing = false;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private TextView mStateTV;
  private ImageView mStateIV;


  private DetectionStateView detectionStateView;

  private BorderedText borderedText;

  private long lastProcessingTimeMs;

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  private static final float TEXT_SIZE_DIP = 10;

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
//    ActionBar actionBar = getActionBar();
//    actionBar.setLogo(R.drawable.new_logo_small);
//    actionBar.setDisplayUseLogoEnabled(true);
//    actionBar.setDisplayShowHomeEnabled(true);
//    actionBar.setTitle(R.string.app_name);
//    actionBar.setDisplayHomeAsUpEnabled(true);


    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);
    classifier =
        TensorFlowImageClassifier.create(
            getAssets(),
            MODEL_FILE,
            LABEL_FILE,
            INPUT_SIZE,
            IMAGE_MEAN,
            IMAGE_STD,
            INPUT_NAME,
            OUTPUT_NAME);

//    resultsView = (ResultsView) findViewById(R.id.results);
    mStateIV = (ImageView) findViewById(R.id.doctorImage);
    mStateTV = (TextView) findViewById(R.id.label);
    detectionFrameHistory = new ArrayList<>(2);
    detectionFrameHistory.add(0);
    detectionFrameHistory.add(0);
//    detectionFrameHistory.add(0);

    detectionStateView = (DetectionStateView) findViewById(R.id.detectionState);
    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    final Display display = getWindowManager().getDefaultDisplay();
    final int screenOrientation = display.getRotation();

    LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);

    sensorOrientation = rotation + screenOrientation;

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbBytes = new int[previewWidth * previewHeight];
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            INPUT_SIZE, INPUT_SIZE,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    yuvBytes = new byte[3][];

    addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            renderDebug(canvas);
          }
        });
  }

  @Override
  public void onImageAvailable(final ImageReader reader) {
    Image image = null;

    try {
      image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (computing) {
        image.close();
        return;
      }
      computing = true;

      Trace.beginSection("imageAvailable");

      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);

      final int yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();
      ImageUtils.convertYUV420ToARGB8888(
          yuvBytes[0],
          yuvBytes[1],
          yuvBytes[2],
          rgbBytes,
          previewWidth,
          previewHeight,
          yRowStride,
          uvRowStride,
          uvPixelStride,
          false);

      image.close();
    } catch (final Exception e) {
      if (image != null) {
        image.close();
      }
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }

    rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            showResultBorder(results);
            requestRender();
            computing = false;
          }
        });

    Trace.endSection();
  }

  private void showResultBorder(final List<Classifier.Recognition> results) {
    int receivedState = 0;

    if (results.get(0).getTitle().equals("melanoma") && results.get(0).getConfidence() > 0.6){
      receivedState = DetectionStateView.DETECTED_MELANOMA;
    } else if(results.get(0).getTitle().equals("non melanoma") && results.get(0).getConfidence() > 0.5) {
      receivedState = DetectionStateView.DETECTED_MOLE;
    } else if (results.get(0).getTitle().equals("skin")) {
      receivedState = DetectionStateView.SKIN;
    } else if (results.get(0).getTitle().equals("other")) {
      receivedState = DetectionStateView.UNDEFINED;
    }

//    int index = 0;
//    for (; index < detectionFrameHistory.size(); index++) {
//      if (detectionFrameHistory.get(index).equals(0)) break;
//    }
//    if (index == 1) {
//      for (; index >= 0; index--) {
//        if (detectionFrameHistory.get(index) != receivedState) {
//          detectionFrameHistory.remove(0);
//          detectionFrameHistory.add(1, receivedState);
//          break;
//        }
//      }
//      if (index == -1) {
        detectionStateView.setState(receivedState);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            detectionStateView.invalidate();
            displayResult(results);
          }
        });
//      }
//    } else {
//      detectionFrameHistory.add(receivedState);
//    }
  }

  private void displayResult(final List<Classifier.Recognition> results){

    int percents = (int)(results.get(0).getConfidence() * 100);
    String currentClass = results.get(0).getTitle();

//    if (!currentClass.equals(mPreviousClass) && !mPreviousClass.equals("")) {
//      Log.w("SMARTSCOPE", "previous prediction (" + mPreviousClass + ") doesnt match with current (" + currentClass + ")");
//      return;
//    }

    if(currentClass.equals("melanoma") && percents > 60) {
      mStateTV.setText("Melanoma detected: " + Integer.toString(percents) + "%");
      Toast.makeText(this, "Consult your doctor, please", mToastLength).show();
      mStateIV.setImageDrawable(getDrawable(R.drawable.doctor));
    } else if(currentClass.equals("non melanoma") && percents > 50){
      mStateTV.setText("Mole detected: " + Integer.toString(percents) + "%");
      Toast.makeText(this, "It seems ok", mToastLength).show();
      mStateIV.setImageDrawable(getDrawable(R.drawable.ic_ok));
    } else if (currentClass.equals("skin")) {
//      mStateTV.setText("Scanning mole " + Integer.toString(percents) + "%");
      mStateTV.setText("Scanning mole");
      mStateIV.setImageDrawable(getDrawable(R.drawable.ic_loupe));
    } else {
      mStateTV.setText("Point camera at mole");
      mStateIV.setImageDrawable(getDrawable(R.drawable.ic_loupe));
    }

    mPreviousClass = currentClass;
  }


  @Override
  public void onSetDebug(boolean debug) {
    classifier.enableStatLogging(debug);
  }

  private void renderDebug(final Canvas canvas) {
    if (!isDebug()) {
      return;
    }
    final Bitmap copy = cropCopyBitmap;
    if (copy != null) {
      final Matrix matrix = new Matrix();
      final float scaleFactor = 2;
      matrix.postScale(scaleFactor, scaleFactor);
      matrix.postTranslate(
          canvas.getWidth() - copy.getWidth() * scaleFactor,
          canvas.getHeight() - copy.getHeight() * scaleFactor);
      canvas.drawBitmap(copy, matrix, new Paint());

      final Vector<String> lines = new Vector<String>();
      if (classifier != null) {
        String statString = classifier.getStatString();
        String[] statLines = statString.split("\n");
        for (String line : statLines) {
          lines.add(line);
        }
      }

      lines.add("Frame: " + previewWidth + "x" + previewHeight);
      lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
      lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
      lines.add("Rotation: " + sensorOrientation);
      lines.add("Inference time: " + lastProcessingTimeMs + "ms");

      borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
    }
  }
}
