package sjava2;

public class RLinearModelPredict {
    double[] pred, lwr, upr, sefit;
    int df;
    double residualScale;

    public RLinearModelPredict(double[] p, double[] se,
            double[] l, double[] u,
            int df, double residualScale) {
        setPredicted(p);
        setSEFit(se);
        setLower(l);
        setUpper(u);
        setDF(df);
        setResidualScale(residualScale);
    }

    public int getDF() { return(this.df); }
    public void setDF(int df) { this.df = df; }

    public double getResidualScale() { return(this.residualScale); }
    public void setResidualScale(double scale) { this.residualScale = scale; }

    public double[] getPredicted() { return(this.pred); }
    public void setPredicted(double[] v) { 
        this.pred = new double[v.length];
        for (int i = 0; i < v.length; i++) this.pred[i] = v[i];
    }
    public double[] getLower() { return(this.lwr); }
    public void setLower(double[] v) { 
        this.lwr = new double[v.length];
        for (int i = 0; i < v.length; i++) this.lwr[i] = v[i];
    }
    public double[] getUpper() { return(this.upr); }
    public void setUpper(double[] v) { 
        this.upr = new double[v.length];
        for (int i = 0; i < v.length; i++) this.upr[i] = v[i];
    }
    public double[] getSEFit() { return(this.sefit); }
    public void setSEFit(double[] v) { 
        this.sefit = new double[v.length];
        for (int i = 0; i < v.length; i++) this.sefit[i] = v[i];
    }

}


