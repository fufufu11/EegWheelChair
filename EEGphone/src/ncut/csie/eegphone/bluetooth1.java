package ncut.csie.eegphone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class bluetooth1 extends Thread{
    
	
	static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	BluetoothAdapter btAdapter1;
	BluetoothDevice btDevice1;
    BluetoothSocket btSocket1;
	 OutputStream outStream;
	 InputStream InStream;
	
	
	public bluetooth1(BluetoothAdapter adapter,BluetoothDevice device){
		this.btAdapter1 = adapter;
		this.btDevice1 = device;
		try {
    		this.btSocket1 = this.btDevice1.createRfcommSocketToServiceRecord(bluetooth1.MY_UUID);
    	}catch (IOException e){
    	}
		if(bluetooth1.this.btAdapter1.isDiscovering()){
    		bluetooth1.this.btAdapter1.cancelDiscovery();
    	}
    	try{
    		this.btSocket1.connect();
    		Log.e("connect22", "lianjie");
    		
    		//msg.what =1;
    		//MainActivity.handler.sendMessage(msg);
    	}catch (IOException e){
    		try{
    			this.btSocket1.close(); 
    			//msg.what =2;
    			//handler.sendMessage(msg);
    		} catch (IOException localIOException1) {
    		}
    	}
    	
    	try{
    		this.outStream = this.btSocket1.getOutputStream();
    	}
    	catch(IOException e){
    		try{
    			this.btSocket1.close();
    		}
    		catch(IOException localIOException1){
    		}
    	}
	}



}