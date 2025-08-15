package ncut.csie.eegphone;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class EEGphone extends Activity{

	private static final String LOGGER_TAG			= "NEURO_SKY";
	private static final int REQUEST_ENABLE_BT		= 0x02;
	private BluetoothAdapter btAdapter;
	private NeuroSky neuroSky;
	BluetoothDevice device;
	private BluetoothAdapter btAdapter_1;
	private bluetooth1 bluetooth_1;
	private BluetoothAdapter btAdapter_2;
	private bluetooth2 bluetooth_2;
	static int bits = 9;
	
	FFT fft_1;
	double [] Real_1 = new double [512];
	double [] Imaginary_1 = new double [512];
	double [] Amplitude_1 = new double [512];
	int Count_1 = 0, channel_1;
	int [] Buffer_1 = new int [512];
	
	int EEG_6_count = 0,EEG_7_count = 0,EEG_9_count = 0,EEG_11_count = 0,EEG_13_count = 0;
	
	int  STEP = 0, point_count = 0, compare = 0, index = 0, signal = 200;
	PictureView pictureView;
	String phoneNumber = "";
	
	int FIRST = 0, FINAL = 0, MEDIAL = 0;
	
	int Hz[]={9,9,9,9,9,9,5};
	int a=0;
	
	private final Handler btHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			
				case (NeuroSky.MESSAGE_BT_DEVICE_NAME) :
					break;
				
				case (NeuroSky.MESSAGE_BT_STATUS_CHANGE) :
				    int changedStatus = msg.arg1;
				    if (changedStatus == NeuroSky.BT_STATE_NONE) {
				    	Log.i(LOGGER_TAG, "BT_STATE_NONE");
				    } else if (changedStatus == NeuroSky.BT_STATE_CONNECTING) {
				    	Log.i(LOGGER_TAG, "BT_STATE_CONNECTING");
				    } else if (changedStatus == NeuroSky.BT_STATE_CONNECTED) {
				    	Log.i(LOGGER_TAG, "BT_STATE_CONNECTED");				    	
				    }
					break;
				
				case (NeuroSky.MESSAGE_BT_TOAST) :
				    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
					break;
				
				case (NeuroStreamParser.MESSAGE_READ_DIGEST_DATA_PACKET) :
				    NeuroData data = (NeuroData) msg.obj;
					signal = data.getSignal();
					if(Count_1 == 512){
						for(int i = 0; i < 512; i++){
					    	Real_1[i]=Buffer_1[i];
					    	Imaginary_1[i]=0.0;
					    }
					    fft_1.doFFT(Real_1, Imaginary_1, true);
					    fft_1.doAmplitude(Real_1, Imaginary_1, Amplitude_1);
					    channel_1=fft_1.doMaxAmplitude(Amplitude_1);
					    /*channel_1=Hz[a];
					    if(a!=(Hz.length-1)){
					    	a++;
					    }*/
					    if((channel_1 == 6) || (channel_1 == 12)){
					    	if (EEG_6_count ==1){
					    		if(STEP == 0){
					    			STEP = 4;
					    		}
					    		else if(STEP == 2){
					    			if(point_count != 0){
					    				point_count = point_count - 30;
					    			}
					    		}
					    		else if(STEP == 3){
					    			FINAL = MEDIAL;
					    			MEDIAL = (FIRST + FINAL) / 2;
					    			if(FIRST == FINAL){
					    				Intent intentDial = new Intent("android.intent.action.CALL",Uri.parse("tel:"+pictureView.get_phoneNumber(MEDIAL)));
						    			startActivity(intentDial);
						    			System.exit(0);
					    			}
					    		}
					    		else if(STEP == 4){
					    			try {
										bluetooth_2.outStream.write(0x01);
										Log.e("tag", "0x01");
										Toast.makeText(getApplicationContext(), "台灯关指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    		}
					    		else if(STEP == 5){
					    			try {
										bluetooth_2.outStream.write(0x03);
										Log.e("tag", "0x03");
										Toast.makeText(getApplicationContext(), "风扇快转指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    		}
					    		else if(STEP == 6){
					    			try {
										bluetooth_1.outStream.write(0x06);
										Log.e("tag", "0x06");
										Toast.makeText(getApplicationContext(), "前进指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    		}
					    		EEG_6_count = 0;
					    	}
					    	else{
					    		EEG_6_count++;
					    	}
					    	EEG_7_count = 0;
					    	EEG_9_count = 0;
					    	EEG_11_count = 0;
					    	EEG_13_count = 0;
					    }
					    else if((channel_1 == 7) || (channel_1 == 14)){
					    	if (EEG_7_count == 1){
					    		if(STEP == 0){
					    			STEP = 5;
					    		}
					    	
					    		else if(STEP == 2){
					    			if((point_count + 30) < pictureView.get_point()){
					    				point_count = point_count + 30;
					    			}
					    		}
					    	
					    		else if(STEP == 5){
					    			try {
										bluetooth_2.outStream.write(0x04);
										Log.e("tag", "0x04");
										Toast.makeText(getApplicationContext(), "风扇慢转指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    		}
					    		else if(STEP == 6){
					    			try {
										bluetooth_1.outStream.write(0x08);
										Log.e("tag", "0x08");
										Toast.makeText(getApplicationContext(), "左转指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    		}
					    
					    		EEG_7_count = 0;
					    	}
					    	else{
					    		EEG_7_count++;
					    	}
					    	EEG_6_count = 0;
					    	EEG_9_count = 0;
					    	EEG_11_count = 0;
					    	EEG_13_count = 0;
					    }
					    else if((channel_1 == 9) || (channel_1 == 18)){
					    	if (EEG_9_count == 1){
					    		if(STEP == 0){
					    			if(pictureView.point > 30){
					    				STEP = 2;
					    			}
					    			else{
					    				FIRST = point_count;
						    			if((point_count + 30) < pictureView.get_point()){
						    				FINAL = point_count + 29;
						    			}
						    			else{
						    				FINAL = pictureView.get_point() - 1;
						    			}
						    			MEDIAL = (FIRST + FINAL) / 2;
						    			if(FIRST == FINAL){
						    				Intent intentDial = new Intent("android.intent.action.CALL",Uri.parse("tel:"+pictureView.get_phoneNumber(MEDIAL)));
							    			startActivity(intentDial);
							    			System.exit(0);
						    			}
						    			else{
							    			STEP = 3;
						    			}
					    			}
					    		}
					    
					    		else if(STEP == 2){
					    			FIRST = point_count;
					    			if((point_count + 30) < pictureView.get_point()){
					    				FINAL = point_count + 29;
					    			}
					    			else{
					    				FINAL = pictureView.get_point() - 1;
					    			}
					    			MEDIAL = (FIRST + FINAL) / 2;
					    			if(FIRST == FINAL){
					    				Intent intentDial = new Intent("android.intent.action.CALL",Uri.parse("tel:"+pictureView.get_phoneNumber(MEDIAL)));
						    			startActivity(intentDial);
						    			System.exit(0);
					    			}
					    			else{
						    			STEP = 3;
					    			}
					    		}
					    		else if(STEP == 3){
					    			FIRST = MEDIAL + 1;
					    			MEDIAL = (FIRST + FINAL) / 2;
					    			if(FIRST == FINAL){
					    				Intent intentDial = new Intent("android.intent.action.CALL",Uri.parse("tel:"+pictureView.get_phoneNumber(MEDIAL)));
						    			startActivity(intentDial);
						    			System.exit(0);
					    			}
					    		}
					    		else if(STEP == 4){
					    			try {
										bluetooth_2.outStream.write(0x02);
										Log.e("tag", "0x02");
										Toast.makeText(getApplicationContext(), "台灯开指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    			
					
					    		}
					    		else if(STEP == 5){
					    			try {
										bluetooth_2.outStream.write(0x05);
										Log.e("tag", "0x05");
										Toast.makeText(getApplicationContext(), "风扇关指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    		}
					    		else if(STEP == 6){
					    			try {
										bluetooth_1.outStream.write(0x07);
										Log.e("tag", "0x07");
										Toast.makeText(getApplicationContext(), "后退指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    		}
				
					    		EEG_9_count = 0;
					    	}
					    	else{
					    		EEG_9_count++;
					    	}
					    	EEG_6_count = 0;
					    	EEG_7_count = 0;
					    	EEG_11_count = 0;
					    	EEG_13_count = 0;
					    }
					    else if((channel_1 == 11) || (channel_1 == 22)){
					    	if (EEG_11_count == 1){
					    	if(STEP==0)	{
					    		STEP=6;
					    	}    		
					    	else if(STEP == 2){
					    			point_count = 0;
					    			phoneNumber = "";
					    			STEP = 0;
					    		}
					    		else if(STEP == 3){
					    			point_count = 0;
					    			phoneNumber = "";
					    			STEP = 0;
					    		}
					    		else if(STEP == 4){
					    			STEP = 0;
					    		}
					    		else if(STEP == 5){
					    			STEP = 0;
					    		}
					    		else if(STEP == 6){
					    			try {
										bluetooth_1.outStream.write(0x09);
										Log.e("tag", "0x09");
										Toast.makeText(getApplicationContext(), "右转指令已发出", Toast.LENGTH_LONG).show(); 
									} catch (IOException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
					    		}
					  
					    		EEG_11_count = 0;
					    	}
					    	else{
					    		EEG_11_count++;
					    	}
					    	EEG_6_count = 0;
					    	EEG_7_count = 0;
					    	EEG_9_count = 0;
					    	EEG_13_count = 0;
					    }
			
					    else if((channel_1 == 13) || (channel_1 == 26)){
					    	if (EEG_13_count == 1){
					    		if(STEP == 6){
					    			
					    			STEP = 0;
					    		}
					    	
					    		EEG_13_count = 0;
					    	}
					    	else{
					    		EEG_13_count++;
					    	}
					    	EEG_6_count = 0;
					    	EEG_7_count = 0;
					    	EEG_9_count = 0;
					    	EEG_11_count = 0;
					    }
					    else{
					    	EEG_6_count = 0;
					    	EEG_7_count = 0;
					    	EEG_9_count = 0;
					    	EEG_11_count = 0;
					    	EEG_13_count = 0;
					    }
					}
					Count_1 = 0;
					break;
				
				case (NeuroStreamParser.MESSAGE_READ_RAW_DATA_PACKET) :
				    NeuroRawData rawData = (NeuroRawData) msg.obj;
					if(Count_1==512){
						Count_1=0;
					}
					Buffer_1[Count_1] = rawData.getRawWaveValue();
					Count_1++;
					break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//⊿夹肈
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//ヰ痸&臂锣
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);//棵辊
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//SCREEN_ORIENTATION_LANDSCAPE SCREEN_ORIENTATION_PORTRAIT
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		fft_1 = new FFT(bits);
		pictureView = new PictureView(this);
		this.setContentView(pictureView);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (btAdapter.isEnabled()) {
			neuroSky = new NeuroSky(btAdapter, btHandler);
			
		} else {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
		if ((neuroSky != null) && (neuroSky.getStatus() == NeuroSky.BT_STATE_CONNECTED)) {
			neuroSky.start();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if ((neuroSky != null) && (neuroSky.getStatus() == NeuroSky.BT_STATE_CONNECTED)) {
			neuroSky.stop();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (neuroSky != null) neuroSky.stop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (neuroSky != null) neuroSky.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		switch (item.getItemId()) {
			case (R.id.item1) :
				//device = btAdapter.getRemoteDevice("00:1A:FF:09:05:47");//（新蓝牙）
				//device = btAdapter.getRemoteDevice("74:E5:43:BE:40:2F");
				device = btAdapter.getRemoteDevice("00:1A:FF:09:02:86");//(旧蓝牙)
				neuroSky.connect(device);
				neuroSky.start();
			    result = true;
				break;
			case (R.id.item2):
				btAdapter_2 = BluetoothAdapter.getDefaultAdapter();
		        bluetooth_2 = new bluetooth2(btAdapter_2,btAdapter_2.getRemoteDevice("00:1A:FF:09:05:30"));
		        break;
			case (R.id.item3):
				btAdapter_1 = BluetoothAdapter.getDefaultAdapter();
		        bluetooth_1 = new bluetooth1(btAdapter_1,btAdapter_1.getRemoteDevice("00:1A:FF:09:05:3E"));
		        break;
		        
		}
		return result;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			System.exit(0);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public int get_STEP(){
		return STEP;
	}
	
	public int get_point_count(){
		return point_count;
	}
	
	public String get_phoneNumber(){
		return phoneNumber;
	}
	
	public int get_FIRST(){
		return FIRST;
	}
	
	public int get_FINAL(){
		return FINAL;
	}
	
	public int get_MEDIAL(){
		return MEDIAL;
	}
	
	public int get_SIGNAL(){
		return signal;
	}
}
