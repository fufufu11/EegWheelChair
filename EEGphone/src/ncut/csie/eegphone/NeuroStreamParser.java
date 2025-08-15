package ncut.csie.eegphone;

import android.os.Handler;

public class NeuroStreamParser
{
  public static final int PARSER_CODE_POOR_SIGNAL = 2;
  public static final int PARSER_CODE_ATTENTION = 4;
  public static final int PARSER_CODE_MEDITATION = 5;
  public static final int PARSER_CODE_RAW = 128;
  public static final int PARSER_CODE_EEG_POWERS = 131;
  public static final int PST_PACKET_CHECKSUM_FAILED = -2;
  public static final int PST_NOT_YET_COMPLETE_PACKET = 0;
  public static final int PST_PACKET_PARSED_SUCCESS = 1;
  public static final int MESSAGE_READ_RAW_DATA_PACKET = 17;
  public static final int MESSAGE_READ_DIGEST_DATA_PACKET = 18;
  private Handler handler;
  private int parserStatus;
  private int payloadSum;
  private int checksum;
  private int delta; 
  private int theta; 
  private int lowAlpha;
  private int highAlpha; 
  private int lowBeta; 
  private int highBeta; 
  private int lowGammma; 
  private int midGamma;
  private int signal; 
  private int attention; 
  private int meditation;
  private int rawWaveValue;
  private byte[] payload = new byte[5];

  public NeuroStreamParser() {
  }

  public NeuroStreamParser(Handler handler) {
    this.handler = handler;
    this.parserStatus = 1;
  }

  public int parseByte(byte buffer)
  {
    switch (this.parserStatus)
    {
    case 1:
    	if ((buffer & 0xFF) == 170){
    		this.parserStatus = 2;
    	}
    	else {
    		this.parserStatus = 1;
    	}
    	break;
    case 2:
    	if ((buffer & 0xFF) == 170){
    		this.parserStatus = 3;
    	}
    	else {
    		this.parserStatus = 1;
    	}
    	break;
    case 3:
    	if ((buffer & 0xFF) == 32){
    		this.parserStatus = 4;
    		this.payloadSum = 0;
    	}
    	else if((buffer & 0xFF) == 4){
    		this.parserStatus = 37;
    		this.payloadSum = 0;
    	}
    	else {
    		this.parserStatus = 1;
    	}
    	break;
    case 4:
    	if ((buffer & 0xFF) == 2){
    		this.parserStatus = 5;
        	this.payloadSum += (buffer & 0xFF);        
      	}
	  	else {
	  		this.parserStatus = 1;
      	}
      	break;
    case 5:
    	signal=(buffer & 0xFF);
    	NeuroData data1 = new NeuroData(signal, delta, theta, lowAlpha, highAlpha, lowBeta, highBeta, lowGammma, midGamma, attention, meditation);
		this.handler.obtainMessage(18, data1).sendToTarget();
      	this.payloadSum += (buffer & 0xFF);
      	this.parserStatus = 1;
      	break;
    case 6:
    	if ((buffer & 0xFF) == 131){
    		this.parserStatus = 7;
    		this.payloadSum += (buffer & 0xFF);        
    	}
    	else {
    		this.parserStatus = 1;
    	}
    	break;
    case 7:
		if ((buffer & 0xFF) == 24){
			this.parserStatus = 8;
			this.payloadSum += (buffer & 0xFF);        
		}
		else {
			this.parserStatus = 1;
		}
		break;
    case 8:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 9;
		break;
    case 9:
    	this.payload[2]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 10;
		break;
    case 10:
    	this.payload[3]=(byte) (buffer & 0xFF);
    	//delta = NeuroSkyUtil.getEEGPowerValue(this.payload[1], this.payload[2], this.payload[3]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 11;
		break;
    case 11:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 12;
		break;
    case 12:
    	this.payload[2]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 13;
		break;
    case 13:
    	this.payload[3]=(byte) (buffer & 0xFF);
    	//theta = NeuroSkyUtil.getEEGPowerValue(this.payload[1], this.payload[2], this.payload[3]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 14;
		break;
    case 14:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 15;
		break;
    case 15:
    	this.payload[2]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 16;
		break;
    case 16:
    	this.payload[3]=(byte) (buffer & 0xFF);
    	//lowAlpha = NeuroSkyUtil.getEEGPowerValue(this.payload[1], this.payload[2], this.payload[3]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 17;
		break;
    case 17:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 18;
		break;
    case 18:
    	this.payload[2]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 19;
		break;
    case 19:
    	this.payload[3]=(byte) (buffer & 0xFF);
    	//highAlpha = NeuroSkyUtil.getEEGPowerValue(this.payload[1], this.payload[2], this.payload[3]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 20;
		break;
    case 20:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 21;
		break;
    case 21:
    	this.payload[2]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 22;
		break;
    case 22:
    	this.payload[3]=(byte) (buffer & 0xFF);
    	//lowBeta = NeuroSkyUtil.getEEGPowerValue(this.payload[1], this.payload[2], this.payload[3]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 23;
		break;
    case 23:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 24;
		break;
    case 24:
    	this.payload[2]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 25;
		break;
    case 25:
    	this.payload[3]=(byte) (buffer & 0xFF);
    	//highBeta = NeuroSkyUtil.getEEGPowerValue(this.payload[1], this.payload[2], this.payload[3]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 26;
		break;
    case 26:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 27;
		break;
    case 27:
    	this.payload[2]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 28;
		break;
    case 28:
    	this.payload[3]=(byte) (buffer & 0xFF);
    	//lowGammma = NeuroSkyUtil.getEEGPowerValue(this.payload[1], this.payload[2], this.payload[3]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 29;
		break;
    case 29:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 30;
		break;
    case 30:
    	this.payload[2]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 31;
		break;
    case 31:
    	this.payload[3]=(byte) (buffer & 0xFF);
    	//midGamma = NeuroSkyUtil.getEEGPowerValue(this.payload[1], this.payload[2], this.payload[3]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 32;
		break;
    case 32:
    	if ((buffer & 0xFF) == 4){
    		this.parserStatus = 33;
        	this.payloadSum += (buffer & 0xFF);        
      	}
	  	else {
	  		this.parserStatus = 1;
      	}
      	break;
    case 33:
    	attention=(buffer & 0xFF);
      	this.payloadSum += (buffer & 0xFF);
      	this.parserStatus = 34;
      	break;
    case 34:
    	if ((buffer & 0xFF) == 5){
    		this.parserStatus = 35;
        	this.payloadSum += (buffer & 0xFF);        
      	}
	  	else {
	  		this.parserStatus = 1;
      	}
      	break;
    case 35:
    	meditation=(buffer & 0xFF);
      	this.payloadSum += (buffer & 0xFF);
      	this.parserStatus = 36;
      	break;
    case 36:
    	this.checksum = (buffer & 0xFF);
    	if(this.checksum == ((this.payloadSum ^ 0xFFFFFFFF) & 0xFF)) {

        } 
    	this.parserStatus = 1;
    	break;
    case 37:
    	if ((buffer & 0xFF) == 128){
    		this.parserStatus = 38;
        	this.payloadSum += (buffer & 0xFF);        
      	}
	  	else {
	  		this.parserStatus = 1;
      	}
    	break;
    case 38:
    	if ((buffer & 0xFF) == 2){
    		this.parserStatus = 39;
        	this.payloadSum += (buffer & 0xFF);        
      	}
	  	else {
	  		this.parserStatus = 1;
      	}
    	break;
    case 39:
    	this.payload[1]=(byte) (buffer & 0xFF);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 40;
		break;
    case 40:
    	this.payload[2]=(byte) (buffer & 0xFF);
    	rawWaveValue = NeuroSkyUtil.getRawWaveValue(this.payload[1], this.payload[2]);
		this.payloadSum += (buffer & 0xFF);
		this.parserStatus = 41;
		break;
    case 41:
    	this.checksum = (buffer & 0xFF);
    	if(this.checksum == ((this.payloadSum ^ 0xFFFFFFFF) & 0xFF)) {
    		NeuroRawData data2 = new NeuroRawData(rawWaveValue);
    		this.handler.obtainMessage(17, data2).sendToTarget();
        } 
    	this.parserStatus = 1;
    	break;
    }
    return 0;
  }
}

