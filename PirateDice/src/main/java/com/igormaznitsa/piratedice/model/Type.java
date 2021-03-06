package com.igormaznitsa.piratedice.model;

public enum Type {
    STANDARD("vertushka.svg"),
    PROFESSIONAL("vertushka_pro.svg"),
    REDESIGNED("redesigned.svg");

    private final String resource;

    private Type(final String resource){
      this.resource = resource;
    }

    public String getResource(){
      return this.resource;
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
