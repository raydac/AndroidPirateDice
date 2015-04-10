package com.igormaznitsa.piratedice.ui;

public class ScaleCoeffs {
    private float coeffX;
    private float coeffY;

    public ScaleCoeffs(final float coeffX, final float coeffY) {
        this.coeffX = coeffX;
        this.coeffY = coeffY;
    }

    public ScaleCoeffs() {
        this(1.0f, 1.0f);
    }

    public void setCoeffX(final float val) {
        this.coeffX = val;
    }

    public void setCoeffY(final float val) {
        this.coeffY = val;
    }

    public float getCoeffX() {
        return this.coeffX;
    }

    public float getCoeffY() {
        return this.coeffY;
    }

    public int scaleX(final int val) {
        return Math.round((float) val * coeffX);
    }

    public int scaleY(final int val) {
        return Math.round((float) val * coeffY);
    }
}
