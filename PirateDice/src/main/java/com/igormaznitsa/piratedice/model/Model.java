package com.igormaznitsa.piratedice.model;

import java.util.ArrayList;
import java.util.List;

public class Model {

  public interface ModelListener {
        void onModelChanged(Model m);
        void onPause(Model m, boolean pause);
        void onDispose(Model m);
        void doStartTurn(Model m);
    }

    private static Model instance;

    private final List<ModelListener> listeners = new ArrayList<ModelListener>();
    private Type type = Type.STANDARD;
    private boolean paused;

    private void fireListeners(){
        for(final ModelListener l : listeners){
            l.onModelChanged(this);
        }
    }

    private void fireListenersForPause(final boolean flag){
        for(final ModelListener l : listeners){
            l.onPause(this, flag);
        }
    }

    private void fireListenersForDispose(){
        for(final ModelListener l : listeners){
            l.onDispose(this);
        }
    }

    public Type getType(){
        return this.type;
    }


  public void startTurn() {
    for(final ModelListener l : listeners){
      l.doStartTurn(this);
    }
  }

  public void nextType() {
    this.type = Type.next(this.type);
    fireListeners();
  }

  public void setType(final Type t){
        if (t==null) throw new NullPointerException();

        if (t != this.type){
            this.type = t;
            fireListeners();
        }
    }

    public void setPaused(final boolean paused){
        if (this.paused != paused){
            this.paused = paused;
            fireListenersForPause(this.paused);
        }
    }

    public synchronized static Model getInstance(){
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public static synchronized void dispose(){
        getInstance().fireListenersForDispose();
        instance = null;
    }

    public void addListener(final ModelListener l){
        if (l!=null && !listeners.contains(l)){
            listeners.add(l);
        }
    }

    public void removeListener(final ModelListener l){
        listeners.remove(l);
    }
}
