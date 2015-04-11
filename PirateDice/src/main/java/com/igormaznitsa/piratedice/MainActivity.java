package com.igormaznitsa.piratedice;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import com.igormaznitsa.piratedice.model.Model;
import com.igormaznitsa.piratedice.model.Type;
import com.igormaznitsa.piratedice.ui.InteractiveIndicator;

public class MainActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    setContentView(R.layout.main);
  }

  @Override public void onBackPressed() {
    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(0);
  }

  @Override public boolean onKeyUp(final int keyCode, final KeyEvent event) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_MENU: {
        return super.onKeyUp(keyCode, event);
    }
    case KeyEvent.KEYCODE_BACK:
    case KeyEvent.KEYCODE_HOME:
    case KeyEvent.KEYCODE_DPAD_UP:
    case KeyEvent.KEYCODE_DPAD_DOWN: {
      this.openOptionsMenu();
    }
    break;
    case KeyEvent.KEYCODE_DPAD_LEFT:
    case KeyEvent.KEYCODE_DPAD_RIGHT: {
      Model.getInstance().nextType();
    }
    break;
    case KeyEvent.KEYCODE_ENDCALL: {
      onBackPressed();
    }
    break;
    default: {
      Model.getInstance().startTurn();
    }
    break;
    }
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
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
  protected void onResume() {
    Model.getInstance().setPaused(false);
    super.onResume();
  }

  @Override
  protected void onPause() {
    Model.getInstance().setPaused(true);
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    Model.dispose();
    super.onDestroy();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    String version = "";

    try {
      version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    switch (item.getItemId()) {
    case R.id.action_about: {
      Toast.makeText(this, "The Dice for the 'Pirates' table game (author Vladimyr Golytsyn).\nAuthor: Igor Maznitsa (http://www.igormaznitsa.com)\nVersion: " + version,
        Toast.LENGTH_LONG).show();
      return true;
    }
    case R.id.action_help: {
      Toast.makeText(this, "Just press the screen for non-short time to start the dice. Enjoy the table game!", Toast.LENGTH_LONG).show();
      return true;
    }
    case R.id.action_pro: {
      Model.getInstance().setType(Type.PROFESSIONAL);
      return true;
    }
    case R.id.action_redesigned: {
      Model.getInstance().setType(Type.REDESIGNED);
      return true;
    }
    case R.id.action_standard: {
      Model.getInstance().setType(Type.STANDARD);
      return true;
    }
    case R.id.action_exit: {
      onBackPressed();
    }
    default: {
      return super.onOptionsItemSelected(item);
    }
    }
  }
}
