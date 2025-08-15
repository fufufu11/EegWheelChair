package ncut.csie.eegphone;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NeuroSkyUtil
{

  public static int getRawWaveValue(byte highOrderByte, byte lowOrderByte)
  {
	int highOrderByte_temp = 0;
	int lowOrderByte_temp = 0;
	
	if(highOrderByte < 0){
		highOrderByte_temp = highOrderByte + 256;
	}
	else{
		highOrderByte_temp = highOrderByte;
	}
	if(lowOrderByte < 0){
		lowOrderByte_temp = lowOrderByte + 256;
	}
	else{
		lowOrderByte_temp = lowOrderByte;
	}
	  
    int value = highOrderByte_temp * 256 + lowOrderByte_temp;
    
    if(value>=32768){
    	value = value-65536;
	  }
    
    return value;
  }

  public static int getEEGPowerValue(byte highOrderByte, byte middleOrderByte, byte lowOrderByte)
  {
    int value = highOrderByte * 65536 + middleOrderByte * 256 + lowOrderByte;

    return value;
  }

  public static void printStackTrace2Log(String tag, Throwable exception)
  {
    StackTraceElement[] elements = exception.getStackTrace();
    StringBuffer stackTrace = new StringBuffer();
    for (int i = 0; i < elements.length; i++)
    {
      stackTrace.append("at ").append(elements[i].getClassName());
      stackTrace.append(".").append(elements[i].getMethodName());
      stackTrace.append("(").append(elements[i].getFileName());
      stackTrace.append(":").append(elements[i].getLineNumber()).append(")");
      Log.d(tag, stackTrace.toString());
      stackTrace.setLength(0);
    }
  }

  public static void notify2Toast(Handler handler, int notify, String text)
  {
    Message message = handler.obtainMessage(notify);
    Bundle bundle = new Bundle();
    bundle.putString("toast", text);
    message.setData(bundle);
    handler.sendMessage(message);
  }
}
