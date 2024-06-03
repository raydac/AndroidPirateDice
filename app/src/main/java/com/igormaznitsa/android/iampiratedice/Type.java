package com.igormaznitsa.android.iampiratedice;

public enum Type {
    STANDARD(R.drawable.vertushka),
    PROFESSIONAL(R.drawable.vertushka_pro),
    REDESIGNED(R.drawable.redesigned);

    private final int resourceId;

    Type(final int resourceId){
      this.resourceId = resourceId;
    }

    public int getResourceId(){
      return this.resourceId;
    }

  public static Type next(final Type type) {
    if (type == null) return STANDARD;
    int index = type.ordinal()+1;
    if (index>=Type.values().length){
      index = 0;
    }
    return Type.values()[index];
  }
}
