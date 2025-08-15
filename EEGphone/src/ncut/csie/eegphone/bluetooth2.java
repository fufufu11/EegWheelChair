package ncut.csie.eegphone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class bluetooth2 extends Thread{
    
	
	static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	BluetoothAdapter btAdapter2;
	BluetoothDevice btDevice2;
    BluetoothSocket btSocket2;
	 OutputStream outStream;
	 InputStream InStream;
	
	
	public bluetooth2(BluetoothAdapter adapter,BluetoothDevice device){
		this.btAdapter2 = adapter;
		this.btDevice2 = device;
		try {
    		this.btSocket2 = this.btDevice2.createRfcommSocketToServiceRecord(bluetooth2.MY_UUID);
    	}catch (IOException e){
    	}
		if(bluetooth2.this.btAdapter2.isDiscovering()){
    		bluetooth2.this.btAdapter2.cancelDiscovery();
    	}
    	try{
    		this.btSocket2.connect();
    		Log.e("connect2", "lianjie");
    		
    		//msg.what =1;
    		//MainActivity.handler.sendMessage(msg);
    	}catch (IOException e){
    		try{
    			this.btSocket2.close(); 
    			//msg.what =2;
    			//handler.sendMessage(msg);
    		} catch (IOException localIOException1) {
    		}
    	}
    	
    	try{
    		this.outStream = this.btSocket2.getOutputStream();
    	}
    	catch(IOException e){
    		try{
    			this.btSocket2.close();
    		}
    		catch(IOException localIOException2){
    		}
    	}
	}
}