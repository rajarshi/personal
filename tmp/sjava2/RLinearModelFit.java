package sjava2;

public class RLinearModelFit {
    double[] coeff, res, fitted;
    int rank, dfResidual;

    public RLinearModelFit(double[] coeff, double[] res, double[] fitted, int rank, int df) {
        setCoefficients(coeff);
        setResiduals(res);
        setFitted(fitted);
        setRank(rank);
        setdfResidual(df);
    }
    public int getRank() { return(this.rank); }
    public void setRank(int v) { this.rank = v; };

    public double[] getResiduals() { return(this.res); }
    public void setResiduals(double[] v) { 
        this.res = new double[v.length];
        for (int i = 0; i < v.length; i++) this.res[i] = v[i];
    }

    public double[] getCoefficients() { return(this.coeff); }
    public void setCoefficients(double[] v) { 
        this.coeff = new double[v.length];
        for (int i = 0; i < v.length; i++) this.coeff[i] = v[i];
    }

    public int getdfResidual() { return(this.dfResidual); }
    public void setdfResidual(int df) { this.dfResidual = df; }

    public double[] getFitted() { return(this.fitted); }
    public void setFitted(double[] v) { 
        this.fitted = new double[v.length];
        for (int i = 0; i < v.length; i++) this.fitted[i] = v[i];
    }
}


    
