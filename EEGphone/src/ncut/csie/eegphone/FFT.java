package ncut.csie.eegphone;

public class FFT {
	private static final double TWOPI = 2.0 * Math.PI;
	private static final int LOG2_MAXFFTSIZE = 15;
    private static final int MAXFFTSIZE = 1 << LOG2_MAXFFTSIZE;
    private int bits;
    private int [] bitreverse = new int[MAXFFTSIZE];
    
    public FFT(int bits) {

        this.bits = bits;

        if (bits > LOG2_MAXFFTSIZE) {
            System.out.println("" + bits + " is too big");
            System.exit(1);
        }
        for (int i = (1 << bits) - 1; i >= 0; --i) {
            int k = 0;
            for (int j = 0; j < bits; ++j) {
                k *= 2;
                if ((i & (1 << j)) != 0)
                    k++;
            }
            this.bitreverse[i] = k;
        }
    }
    
    public void doFFT(double [] xr, double [] xi, boolean invFlag) {
        int n, n2, i, k, kn2, l, p;
        double ang, s, c, tr, ti;

        n2 = (n = (1 << this.bits)) / 2;
        for (l = 0; l < this.bits; ++l) {
            for (k = 0; k < n; k += n2) {
                for (i = 0; i < n2; ++i, ++k) {
                    p = this.bitreverse[k / n2];
                    ang = TWOPI * p / n;
                    c = Math.cos(ang);
                    s = Math.sin(ang);
                    kn2 = k + n2;

                    if (invFlag)
                            s = -s;
   
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
            if ((i = this.bitreverse[k]) <= k)
                    continue;

            tr = xr[k];
            ti = xi[k];
            xr[k] = xr[i];
            xi[k] = xi[i];
            xr[i] = tr;
            xi[i] = ti;
        }
        if (!invFlag) {
            double f = 1.0 / n;

            for (i = 0; i < n ; i++) {
                    xr[i] *= f;
                    xi[i] *= f;
            }
        }
    }
    
    public void doAmplitude(double [] xr, double [] xi, double [] xa) {
    	for(int i = 1; i < 64 ; i++){
    		xa[i]=Math.sqrt(Math.pow(xr[i], 2)+Math.pow(xi[i], 2));
    	}
    }
    
    public int doMaxAmplitude(double [] xa){
    	double Temporary = 0.0;
    	int Index = 0;
    	for(int i = 1; i < 64; i++){
    		if(xa[i] >= Temporary){
    			Temporary = xa[i];
    			Index = i;
    		}
    	}
    	return Index;
    }
}
