package ncut.csie.eegphone;

import java.io.Serializable;

public class NeuroRawData
  implements Serializable
{
  private static final long serialVersionUID = 6479439435293581926L;
  private int rawWaveValue;

  public NeuroRawData()
  {
  }

  public NeuroRawData(int rawWaveValue)
  {
	  this.rawWaveValue = rawWaveValue;
  }

  public int getRawWaveValue()
  {
    return this.rawWaveValue;
  }
  
  public void setRawWaveValue(int rawWaveValue) {
    this.rawWaveValue = rawWaveValue;
  }
  
  public String getRawWava(){
	  String retStr;
	  retStr = getRawWaveValue() + "";
	  return retStr;
  }
}