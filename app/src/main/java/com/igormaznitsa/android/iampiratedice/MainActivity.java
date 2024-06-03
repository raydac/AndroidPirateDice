package com.igormaznitsa.android.iampiratedice;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity implements Model.ModelListener {

  private final AtomicReference<Thread> currentThread = new AtomicReference<>();
  private final Random random = new Random(System.currentTimeMillis());
  private ImageView imagePlate;
  private ImageView imageArrow;

  @Override
  public void onRestore(Model m) {
    this.stopThread();
  }

  @Override
  public void onPause(Model m, boolean pause) {
    if (pause) {
      this.stopThread();
    }
  }

  private void stopThread() {
    final Thread thread = this.currentThread.getAndSet(null);
    if (thread != null) {
      thread.interrupt();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    Model.getInstance().setPaused(true);
  }

  @Override
  public void onDispose(Model m) {
    this.stopThread();
  }

  @Override
  public void doStartTurn(Model m) {
    final ImageView imageView = this.imageArrow;

    final AtomicReference<Float> delta = new AtomicReference<>((random.nextFloat() + 0.08f) * 100f);

    final Thread newThread = new Thread(() -> {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          final float angle = imageView.getRotation();
          float newAngle = angle + delta.get();
          if (newAngle > 360.0f) newAngle -= 360.0f;

          final float resultAngle = newAngle;
          imageView.post(() -> imageView.setRotation(resultAngle));

          delta.set(delta.get() - 0.8f);
          if (delta.get() <0.0f) {
            break;
          }
          try {
            Thread.sleep(25L);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      } finally {
        this.currentThread.set(null);
      }
    }, "main-activity-dice-thread");
    newThread.setDaemon(true);
    if (this.currentThread.compareAndSet(null, newThread)) {
      newThread.start();
    }
  }

  @Override
  public void onModelChanged(Model m) {
    this.imagePlate.setImageResource(m.getType().getResourceId());
  }

  private boolean isHotSpot(final float x, final float y) {
    if (this.imageArrow == null) return false;
    final int centerX = this.imageArrow.getWidth() / 2;
    final int centerY = this.imageArrow.getHeight() / 2;
    final double dx = centerX - x;
    final double dy = centerY - y;

    final double distanceInDp = Math.sqrt(dx * dx + dy * dy) /
        (getResources().getDisplayMetrics().densityDpi / (double)DisplayMetrics.DENSITY_DEFAULT);

    return  distanceInDp < 48.0d;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Model.getInstance().addListener(this);
    getSupportActionBar().setTitle("I'am Pirate Dice");
  }

  @Override
  protected void onResume() {
    super.onResume();
    this.imageArrow = this.findViewById(R.id.ImageArrow);
    this.imagePlate = this.findViewById(R.id.ImagePlate);

    this.imageArrow.setOnClickListener(v -> Model.getInstance().startTurn());

    this.imageArrow.setOnTouchListener((v, event) -> {
      if (event.getAction() == MotionEvent.ACTION_UP && this.isHotSpot(event.getX(), event.getY())) {
        Model.getInstance().startTurn();
      }
      return true;
    });

    Model.getInstance().setPaused(false);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Model.getInstance().removeListener(this);
  }

  private void showInfoDialog(final String title, final String message) {
    new AlertDialog.Builder(MainActivity.this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss())
        .show();
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    String version = "";

    try {
      version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    } catch (Exception ex) {
      Log.i("onOptionsItemSelected", "Error during get version", ex);
    }

    final int itemId = item.getItemId();
    if (itemId == R.id.action_about) {
      this.showInfoDialog(getResources().getString(R.string.title_menu_about),
          String.format(getResources().getString(R.string.about_text), version));
      return true;
    } else if (itemId == R.id.action_help) {
      this.showInfoDialog(getResources().getString(R.string.title_menu_help),
          getString(R.string.help_text));
      return true;
    } else if (itemId == R.id.action_exit) {
      this.finishAndRemoveTask();
      return true;
    } else if (itemId == R.id.action_standard) {
      Model.getInstance().setType(Type.STANDARD);
      return true;
    } else if (itemId == R.id.action_redesigned) {
      Model.getInstance().setType(Type.REDESIGNED);
      return true;
    } else if (itemId == R.id.action_pro) {
      Model.getInstance().setType(Type.PROFESSIONAL);
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }


  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    final Type type = Model.getInstance().getType();

    switch (type) {
      case PROFESSIONAL: {
        menu.findItem(R.id.action_standard).setVisible(false);
        menu.findItem(R.id.action_pro).setVisible(false);
        menu.findItem(R.id.action_redesigned).setVisible(true);
      }
      break;
      case REDESIGNED: {
        menu.findItem(R.id.action_standard).setVisible(true);
        menu.findItem(R.id.action_pro).setVisible(false);
        menu.findItem(R.id.action_redesigned).setVisible(false);
      }
      break;
      case STANDARD: {
        menu.findItem(R.id.action_standard).setVisible(false);
        menu.findItem(R.id.action_pro).setVisible(true);
        menu.findItem(R.id.action_redesigned).setVisible(false);
      }
      break;
    }
    return true;
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
}

