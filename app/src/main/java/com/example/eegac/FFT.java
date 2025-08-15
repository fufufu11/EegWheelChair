package com.example.eegac;

public class FFT {
    private static final double TWOPI = 2.0 * Math.PI;
    private static final int LOG2_MAXFFTSIZE = 15;
    private static final int MAXFFTSIZE = 1 << LOG2_MAXFFTSIZE;
    private final int bits;
    private final int[] bitreverse = new int[MAXFFTSIZE];

    public FFT(int bits) {
        this.bits = bits;
        if (bits > LOG2_MAXFFTSIZE) throw new IllegalArgumentException("bits too big");
        for (int i = (1 << bits) - 1; i >= 0; --i) {
            int k = 0;
            for (int j = 0; j < bits; ++j) {
                k *= 2;
                if ((i & (1 << j)) != 0) k++;
            }
            bitreverse[i] = k;
        }
    }

    public void doFFT(double[] xr, double[] xi, boolean inverse) {
        int n, n2, i, k, kn2, l, p;
        double ang, s, c, tr, ti;
        n2 = (n = (1 << this.bits)) / 2;
        for (l = 0; l < this.bits; ++l) {
            for (k = 0; k < n; k += n2) {
                for (i = 0; i < n2; ++i, ++k) {
                    p = bitreverse[k / n2];
                    ang = TWOPI * p / n;
                    c = Math.cos(ang);
                    s = Math.sin(ang);
                    kn2 = k + n2;
                    if (inverse) s = -s;
                    tr = xr[kn2] * c + xi[kn2] * s;
                    ti = xi[kn2] * c - xr[kn2] * s;
                    xr[kn2] = xr[k] - tr;
                    xi[kn2] = xi[k] - ti;
                    xr[k] += tr;
                    xi[k] += ti;
                }
            }
            n2 /= 2;
        }
        for (k = 0; k < n; k++) {
            if ((i = bitreverse[k]) <= k) continue;
            tr = xr[k]; ti = xi[k];
            xr[k] = xr[i]; xi[k] = xi[i];
            xr[i] = tr; xi[i] = ti;
        }
        if (!inverse) {
            double f = 1.0 / n;
            for (i = 0; i < n; i++) { xr[i] *= f; xi[i] *= f; }
        }
    }

    public void computeAmplitude(double[] xr, double[] xi, double[] xa) {
        int len = Math.min(xa.length, xr.length);
        for (int i = 1; i < len; i++) {
            xa[i] = Math.sqrt(xr[i] * xr[i] + xi[i] * xi[i]);
        }
    }

    public int findMaxIndex(double[] xa, int fromInclusive, int toExclusive) {
        double max = 0.0;
        int idx = fromInclusive;
        for (int i = fromInclusive; i < toExclusive && i < xa.length; i++) {
            if (xa[i] >= max) { max = xa[i]; idx = i; }
        }
        return idx;
    }
}


