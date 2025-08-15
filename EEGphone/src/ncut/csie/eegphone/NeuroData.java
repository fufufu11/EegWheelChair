package ncut.csie.eegphone;

import java.io.Serializable;

public class NeuroData
  implements Serializable
{
  private static final long serialVersionUID = 5854186949994905280L;
  private int signal = 0;
  private int delta = 0;
  private int theta = 0;
  private int lowAlpha = 0;
  private int highAlpha = 0;
  private int lowBeta = 0;
  private int highBeta = 0;
  private int lowGamma = 0;
  private int midGamma = 0;
  private int attention = 0;
  private int meditation = 0;

  public NeuroData()
  {
  }

  public NeuroData(int signal, int delta, int theta, int lowAlpha, int highAlpha, int lowBeta, int highBeta, int lowGamma, int midGamma, int attention, int meditation)
  {
    this.signal = signal;
    this.delta = delta;
    this.theta = theta;
    this.lowAlpha = lowAlpha;
    this.highAlpha = highAlpha;
    this.lowBeta = lowBeta;
    this.highBeta = highBeta;
    this.lowGamma = lowGamma;
    this.midGamma = midGamma;
    this.attention = attention;
    this.meditation = meditation;
  }

  public int getSignal(){ return this.signal; } 
  public int getDelta() { return this.delta; } 
  public int getTheta() { return this.theta; } 
  public int getLowAlpha() { return this.lowAlpha; } 
  public int getHighAlpha() { return this.highAlpha; } 
  public int getLowBeta() { return this.lowBeta; } 
  public int getHighBeta() { return this.highBeta; } 
  public int getLowGamma() { return this.lowGamma; } 
  public int getMidGamma() { return this.midGamma; } 
  public int getAttention() { return this.attention; } 
  public int getMeditation() { return this.meditation; }

  public void setSignal(int arg) { this.signal = arg; } 
  public void setDelta(int arg) { this.delta = arg; } 
  public void setTheta(int arg) { this.theta = arg; } 
  public void setLowAlpha(int arg) { this.lowAlpha = arg; } 
  public void setHighAlpha(int arg) { this.highAlpha = arg; } 
  public void setLowBeta(int arg) { this.lowBeta = arg; } 
  public void setHighBeta(int arg) { this.highBeta = arg; } 
  public void setLowGamma(int arg) { this.lowGamma = arg; } 
  public void setMidGamma(int arg) { this.midGamma = arg; } 
  public void setAttention(int arg) { this.attention = arg; } 
  public void setMeditation(int arg) { this.meditation = arg;
  }

  public String getAllString()
  {
    String retStr = getSignal() + 
      ", " +getDelta() + 
      ", " + getTheta() + 
      ", " + getLowAlpha() + 
      ", " + getHighAlpha() + 
      ", " + getLowBeta() + 
      ", " + getHighBeta() + 
      ", " + getLowGamma() + 
      ", " + getMidGamma() + 
      ", " + getAttention() + 
      ", " + getMeditation();
    return retStr;
  }
}
