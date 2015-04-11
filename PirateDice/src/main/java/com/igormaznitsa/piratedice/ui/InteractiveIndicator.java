package com.igormaznitsa.piratedice.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;
import com.igormaznitsa.piratedice.model.Model;
import com.igormaznitsa.piratedice.model.Type;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import java.util.Random;

public class InteractiveIndicator extends SurfaceView implements SurfaceHolder.Callback, Runnable, View.OnLongClickListener, Model.ModelListener {

  private SVG vertushka;
  private Bitmap vertushkaPic;

  private Picture strelkaPic;
  private SVG strelka;

  private Rect area = new Rect(0, 0, 100, 100);

  private int screenWidth = -1;
  private int screenHeight = -1;

  private Thread theThread;

  private SurfaceHolder holder;

  private float angle;

  private float angleSpeed;
  private float angleDecreaseStep;

  private static final Object gfxLock = new Object();

  private final Random r = new Random();

  private volatile boolean disposed = false;
  private volatile boolean paused = false;

  public InteractiveIndicator(Context context) {
    super(context);
    getHolder().addCallback(this);
    initRest();
  }

  public InteractiveIndicator(Context context, AttributeSet attrs) {
    super(context, attrs);
    getHolder().addCallback(this);
    initRest();
  }

  public InteractiveIndicator(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    getHolder().addCallback(this);
    initRest();
  }

  private void initRest() {
    setOnLongClickListener(this);
    Model.getInstance().addListener(this);

    theThread = new Thread(this);
    theThread.setDaemon(true);
    theThread.start();
  }

  @Override public void doStartTurn(final Model model) {
    this.startTurn();
  }

  @Override
  public void surfaceCreated(final SurfaceHolder surfaceHolder) {
    try {
      strelka = SVGParser.getSVGFromAsset(getContext().getAssets(), "svg/strelka.svg");
      updateForMode(getContext().getAssets());
    }
    catch (Exception ex) {
      throw new Error("Can't load svg", ex);
    }
  }

  private static Bitmap scaleSVG(final SVG svg, int width, int height, final ScaleCoeffs a) {

    final int origWidth = svg.getPicture().getWidth();
    final int origHeight = svg.getPicture().getHeight();

    if (width <= 0 && height <= 0) {
      return svg.resizePictureAsBitmap(origWidth, origHeight);
    }
    else {
      float resizeWidthCoeff = 1.0f;
      float resizeHeightCoeff = 1.0f;

      if (width > 0) {
        resizeWidthCoeff = (float) width / (float) origWidth;
      }

      if (height > 0) {
        resizeHeightCoeff = (float) height / (float) origHeight;
        if (width < 0) {
          resizeWidthCoeff = resizeHeightCoeff;
        }
      }
      else {
        resizeHeightCoeff = resizeWidthCoeff;
      }

      width = Math.round(origWidth * resizeWidthCoeff);
      height = Math.round(origHeight * resizeHeightCoeff);

      if (a != null) {
        a.setCoeffX(resizeWidthCoeff);
        a.setCoeffY(resizeHeightCoeff);
      }

      return svg.resizePictureAsBitmap(width, height);
    }
  }

  private void drawBack(final Canvas canvas) {
    if (vertushkaPic != null && screenWidth > 0 && screenHeight > 0) {
      canvas.drawBitmap(vertushkaPic, area.left, area.top, null);
    }
  }

  private void drawStrelka(final Canvas canvas, final int angle) {
    if (strelkaPic != null && screenWidth > 0 && screenHeight > 0) {
      canvas.save();

      final int mx = strelkaPic.getWidth() / 2;
      final int my = strelkaPic.getHeight() / 2;

      canvas.translate(area.left + area.width() / 2 - mx, area.top + area.height() / 2 - my);
      canvas.rotate(angle, mx, my);

      canvas.drawPicture(strelkaPic);

      canvas.restore();

    }
  }

  @Override
  public void surfaceChanged(final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
    synchronized (gfxLock) {
      screenWidth = width;
      screenHeight = height;

      holder = surfaceHolder;

      resize();

      drawForAngle(Math.round(angle));
    }
  }

  public void refresh() {
    drawForAngle(this.angle);
  }

  private void resize() {
    if (screenWidth > 0 && screenHeight > 0 && vertushka != null && strelka != null) {
      final int min = Math.min(screenWidth, screenHeight);

      final ScaleCoeffs a = new ScaleCoeffs();

      vertushkaPic = scaleSVG(vertushka, min, -1, a);
      strelkaPic = strelka.resizePicture(a.scaleY(strelka.getPicture().getHeight()), a.scaleX(strelka.getPicture().getWidth()));

      final int offsetX = (screenWidth - min) / 2;
      final int offsetY = (screenHeight - min) / 2;

      area.set(offsetX, offsetY, screenWidth - offsetX, screenHeight - offsetY);
    }
  }

  public void updateForMode(final AssetManager manager) {
    synchronized (gfxLock) {
      try {
        final Type type = Model.getInstance().getType();
        if (type != null){
          vertushka = SVGParser.getSVGFromAsset(manager, "svg/"+type.getResource());
        }
        resize();
      }
      catch (Exception ex) {
        throw new Error("Can't load svg", ex);
      }
      finally{
        refresh();
      }
    }
  }

  private void drawForAngle(final float angle) {
    synchronized (gfxLock) {
      if (holder != null) {
        final Canvas canvas = holder.lockCanvas();
        try {
          drawBack(canvas);
          drawStrelka(canvas, Math.round(angle));
        }
        finally {
          holder.unlockCanvasAndPost(canvas);
        }
      }
    }
  }

  @Override
  public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
    try {
      this.theThread.interrupt();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void startTurn(){
    if (angleSpeed == 0.0f) {
      startIteration(r.nextInt(300));
    }
  }

  @Override
  public boolean onLongClick(View view) {
    startTurn();
    return true;
  }

  private void startIteration(final long force) {
    angleSpeed = (float) r.nextInt(Math.max(1, (int) Math.min(force, 250))) + 35.6f;
    angleDecreaseStep = (float) r.nextInt(999) / 1000 + 0.7f;
  }

  @Override
  public void run() {
    while (!disposed && !Thread.currentThread().isInterrupted()) {
      if (this.paused){
        try {
          Thread.sleep(200L);
        }catch(InterruptedException ex){
          break;
        }
      }else {
        if (angleSpeed > 0.0f) {
          angle += angleSpeed;
          if (angle >= 360.0f) {
            angle -= 360.0f;
          }

          angleSpeed -= angleDecreaseStep;

          boolean ended = false;

          if (angleSpeed <= 0.0f) {
            angleSpeed = 0.0f;
            ended = true;
          }
          drawForAngle(Math.round(angle));

          if (ended) {
            System.gc();
          }
        }
      }
    }
  }

  @Override
  public void onModelChanged(Model m) {
    try {
      synchronized (gfxLock) {
        updateForMode(getContext().getAssets());
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void onPause(final Model m, final boolean pause) {
    try {
      this.paused = pause;
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void onDispose(final Model m) {
    try {
      this.disposed = true;
      this.theThread.interrupt();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
