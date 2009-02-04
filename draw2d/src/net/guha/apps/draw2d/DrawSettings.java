/**
 * 
 * @author Rajarshi Guha
 */
package net.guha.apps.draw2d;

public class DrawSettings {
    private static DrawSettings ourInstance = new DrawSettings();

    public enum OutputFormat { PNG, JPEG, PDF;
        public String getSuffix() {
            switch(this) {
                case PNG:
                    return ".png";
                case JPEG:
                    return ".jpeg";
                case PDF:
                    return ".pdf";
            }
            return "";
        }
    }

    private boolean withH = false;
    private int width = 300;
    private int height = 300;
    private double scale = 0.9;

    private OutputFormat oformat = OutputFormat.PNG;
    private String outputDirectory = "./";

    private boolean tabular = false;
    private boolean props = false;
    private int ncol = 2;

    public boolean isWithH() {
        return withH;
    }

    public void setWithH(boolean withH) {
        this.withH = withH;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public OutputFormat getOformat() {
        return oformat;
    }

    public void setOformat(OutputFormat oformat) {
        this.oformat = oformat;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isTabular() {
        return tabular;
    }

    public void setTabular(boolean tabular) {
        this.tabular = tabular;
    }

    public boolean isProps() {
        return props;
    }

    public void setProps(boolean props) {
        this.props = props;
    }

    public int getNcol() {
        return ncol;
    }

    public void setNcol(int ncol) {
        this.ncol = ncol;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private boolean verbose = false;

    public static DrawSettings getInstance() {
        return ourInstance;
    }

    private DrawSettings() {
    }
}
