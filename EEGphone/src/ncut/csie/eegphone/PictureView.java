package ncut.csie.eegphone;


import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("WrongCall")
public class PictureView extends SurfaceView implements SurfaceHolder.Callback{
	
	private EEGphone activity;
	private TutorialThread thread;
	private Bitmap Picture_1,Picture_2,Picture_3,Red,Green,Yellow,Picture_5;
	private Paint paint_1,paint_2;
	private Handler handler1 = new Handler();
	private Handler handler2 = new Handler();
	private Handler handler3 = new Handler();
	private Handler handler4 = new Handler();
	private Handler handler5 = new Handler();
	
	boolean F1=false,F2=false,F3=false,F4=false,F5=false;
		
	String [] name = new String [10000];
	String [] phoneNumber = new String [10000];
	int point = 0;
	
	
	private static Paint paint_3;
	
	
	public PictureView(EEGphone activity) {
		super(activity);
		this.activity = activity;
		getHolder().addCallback(this);
        this.thread = new TutorialThread(getHolder(), this);
        paint_1 = new Paint();
        paint_2 = new Paint();
        paint_3 = new Paint();
        Picture_1 = BitmapFactory.decodeResource(getResources(), R.drawable.white);
        Picture_5 = BitmapFactory.decodeResource(getResources(), R.drawable.white8);
      
        Picture_2 = BitmapFactory.decodeResource(getResources(), R.drawable.home5);
        BitmapFactory.decodeResource(getResources(), R.drawable.home2);
        Red = BitmapFactory.decodeResource(getResources(), R.drawable.redlight);
        Green = BitmapFactory.decodeResource(getResources(), R.drawable.greenlight);
        Yellow = BitmapFactory.decodeResource(getResources(), R.drawable.yellowlight);
        handler1.removeCallbacks(updateTimer1);
        handler2.removeCallbacks(updateTimer2);
        handler3.removeCallbacks(updateTimer3);
        handler4.removeCallbacks(updateTimer4);
        handler5.removeCallbacks(updateTimer5);
        
        handler1.postDelayed(updateTimer1, 83);
        handler2.postDelayed(updateTimer2, 71);
        handler3.postDelayed(updateTimer3, 56);
        handler4.postDelayed(updateTimer4, 45);
        handler5.postDelayed(updateTimer5, 38);
        
        paint_1.setColor(Color.WHITE);
        paint_1.setTextSize(50);
        paint_2.setColor(Color.WHITE);
        paint_2.setTextSize(75);
        paint_3.setColor(Color.WHITE);
        paint_3.setTextSize(50);
        
        Cursor contacts_name = activity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "sort_key_alt");
		Cursor contacts_number = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
		while (contacts_name.moveToNext()) {
			name[point] = contacts_name.getString(contacts_name.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	        long id = contacts_name.getLong(contacts_name.getColumnIndex(ContactsContract.Contacts._ID));
	        contacts_number = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + Long.toString(id),null,null);
	            
	        while (contacts_number.moveToNext()) {
	            phoneNumber[point] = contacts_number.getString(contacts_number.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	        }
	        contacts_number.close();
	        point++;
		}
		contacts_name.close();
		Picture_3 = zoomBitmap(Picture_2,Picture_2.getWidth()*2,Picture_2.getHeight()*2);
	}
	
	public void onDraw(Canvas canvas){
		if(activity.get_STEP() == 0){
			canvas.drawColor(Color.BLACK);
			if(F1){
				canvas.drawBitmap(Picture_1, 0, 790, paint_1);
			}
			//if(!F1){
			//	canvas.drawBitmap(Picture_5, 50, 840, paint_1);
			//}
			if(F2){
				canvas.drawBitmap(Picture_1, 1470, 0, paint_1);
			}
			//if(!F2){
			//	canvas.drawBitmap(Picture_5, 1520, 50, paint_1);
			//}
			if(F3){
				canvas.drawBitmap(Picture_1, 1470, 790, paint_1);
			}
			//if(!F3){
			//	canvas.drawBitmap(Picture_5, 1520, 840, paint_1);
			//}
			if(F4){
				canvas.drawBitmap(Picture_1, 0, 0, paint_1);
			}
		//	if(!F4){
			//	canvas.drawBitmap(Picture_5, 50,50, paint_1);
			//}
			canvas.drawBitmap(Picture_3, 525,150, paint_1);
			if(activity.get_SIGNAL() == 0){
				canvas.drawBitmap(Green, 900, 0, paint_1);
			}
			else if(activity.get_SIGNAL() == 200){
				canvas.drawBitmap(Red, 900, 0, paint_1);
			}
			else{
				canvas.drawBitmap(Yellow, 900, 0, paint_1);
			}
			canvas.drawText("台灯控制", 100,  780, paint_1);
			canvas.drawText("风扇控制", 1570,  450, paint_1);
			canvas.drawText("电话簿姓名模式", 1470,  780, paint_1);
			canvas.drawText("小车控制",100,  450, paint_1);
		}
		else if(activity.get_STEP() == 2){
			canvas.drawColor(Color.BLACK);
			if(F1){
				canvas.drawBitmap(Picture_1, 0, 0, paint_1);
			}
		//	if(!F1){
			//	canvas.drawBitmap(Picture_5, 50, 50, paint_1);
			//}
			if(F2){
				canvas.drawBitmap(Picture_1, 1470, 0, paint_1);
			}
		//	if(!F2){
			//	canvas.drawBitmap(Picture_5, 1520, 50, paint_1);
		//	}
			if(F3){
				canvas.drawBitmap(Picture_1, 0, 790, paint_1);
			}
			//if(!F3){
			//	canvas.drawBitmap(Picture_5, 50, 840, paint_1);
			//}
			if(F4){
				canvas.drawBitmap(Picture_1, 1470,790, paint_1);
			}
			//if(!F4){
			//	canvas.drawBitmap(Picture_5, 1520,840, paint_1);
		//	}
			if((activity.get_point_count() + 30)> point){
				for(int i = activity.get_point_count(); i < point; i++){
					if(i < (activity.get_point_count() + 15)){
						canvas.drawText(name[i], 650, 50 + (i - activity.get_point_count()) * 50, paint_3);
					}
					else{
						canvas.drawText(name[i], 1100, 50 + (i - activity.get_point_count() - 15) * 50, paint_3);
					}
				}
			}
			else{
				for(int i = activity.get_point_count(); i < activity.get_point_count() + 30; i++){
					if(i < (activity.get_point_count() + 15)){
						canvas.drawText(name[i], 650, 50 + (i - activity.get_point_count()) * 50, paint_3);
					}
					else{
						canvas.drawText(name[i], 1100, 50 + (i - activity.get_point_count() - 15) * 50, paint_3);
					}
				}
			}
			canvas.drawText("上一页", 0,  450, paint_1);
			canvas.drawText("下一页", 1470,  450, paint_1);
			canvas.drawText("确定", 0,  780, paint_1);
			canvas.drawText("返回首页", 1470,  790, paint_1);
		}
		else if(activity.get_STEP() == 3){
			canvas.drawColor(Color.BLACK);
			if(F1){
				canvas.drawBitmap(Picture_1, 0, 0, paint_1);
			}
			//if(!F1){
				//canvas.drawBitmap(Picture_5, 50, 50, paint_1);
			//}
			if(F3){
				canvas.drawBitmap(Picture_1, 1470, 0, paint_1);
			}
		//	if(!F3){
			//	canvas.drawBitmap(Picture_5, 1520, 50, paint_1);
			//}
			if(F4){
				canvas.drawBitmap(Picture_1, 1470,790, paint_1);
			}
			//if(!F4){
			//	canvas.drawBitmap(Picture_5, 1520,840, paint_1);
			//}
			for(int i = activity.get_FIRST(); i <= activity.get_MEDIAL(); i++){
				canvas.drawText(name[i], 650, 50 + (i - activity.get_FIRST()) * 50, paint_3);
			}
			for(int i = activity.get_MEDIAL() + 1; i <= activity.get_FINAL(); i++){
				canvas.drawText(name[i], 1100, 50 + (i - (activity.get_MEDIAL() + 1)) * 50, paint_3);
			}
			canvas.drawText("左边", 0,  450, paint_1);
			canvas.drawText("右边", 1470,  450, paint_1);
			canvas.drawText("返回首页", 1470,  780, paint_1);
		}
		else if(activity.get_STEP() == 4){
			canvas.drawColor(Color.BLACK);
			if(F1){
				canvas.drawBitmap(Picture_1, 0, 0, paint_1);
			}
			//if(!F1){
				//canvas.drawBitmap(Picture_5,50, 50, paint_1);
		//	}
			if(F3){
				canvas.drawBitmap(Picture_1, 1470, 0, paint_1);
			}
			//if(!F3){
			//	canvas.drawBitmap(Picture_5, 1520, 50, paint_1);
			//}
			if(F4){
				canvas.drawBitmap(Picture_1, 1470, 790, paint_1);
			}
			//if(!F4){
				//canvas.drawBitmap(Picture_5, 1520, 840, paint_1);
			//}
			canvas.drawText("关", 175,  450, paint_1);
			canvas.drawText("开", 1645,  450, paint_1);
			canvas.drawText("返回首页", 1470,  780, paint_1);
			canvas.drawText("台灯控制", 825,  150, paint_1);
		}
		else if(activity.get_STEP() == 5){
			canvas.drawColor(Color.BLACK);
			if(F1){
				canvas.drawBitmap(Picture_1, 0, 0, paint_1);
			}
			//if(!F1){
				//canvas.drawBitmap(Picture_5, 50, 50, paint_1);
			//}
			if(F2){
				canvas.drawBitmap(Picture_1, 1470, 0, paint_1);
			}
			//if(!F2){
				//canvas.drawBitmap(Picture_5, 1520, 50, paint_1);
			//}
			if(F3){
				canvas.drawBitmap(Picture_1, 0, 790, paint_1);
			}
			//if(!F3){
			//	canvas.drawBitmap(Picture_5, 50, 840, paint_1);
			//}
			if(F4){
				canvas.drawBitmap(Picture_1, 1470, 790, paint_1);
			}
			//if(!F4){
				//canvas.drawBitmap(Picture_5, 1520, 840, paint_1);
			//}
			canvas.drawText("快", 175,  450, paint_1);
			canvas.drawText("慢", 1645,  450, paint_1);
			canvas.drawText("关闭", 165,  780, paint_1);
			canvas.drawText("返回首页", 1470,  780, paint_1);	
			canvas.drawText("风扇控制", 825,  150, paint_1);
		}
		else if(activity.get_STEP() == 6){
			canvas.drawColor(Color.BLACK);
			if(F1){
				canvas.drawBitmap(Picture_1, 0, 0, paint_1);
			}
			if(F2){
				canvas.drawBitmap(Picture_1, 0,790, paint_1);
			}
			//if(!F1){
			//	canvas.drawBitmap(Picture_5,50, 50, paint_1);
			//}
			if(F3){
				canvas.drawBitmap(Picture_1, 1470, 0, paint_1);
			}
			//if(!F3){
				//canvas.drawBitmap(Picture_5, 1520, 50, paint_1);
			//}
			if(F4){
				canvas.drawBitmap(Picture_1, 1470, 790, paint_1);
			}
			if(F5){
				canvas.drawBitmap(Picture_1, 725, 790, paint_1);
			}
			canvas.drawText("前进", 175,  450, paint_1);
			canvas.drawText("后退", 1645,  450, paint_1);
			canvas.drawText("右转", 1645,  780, paint_1);
			canvas.drawText("左转", 175,  780, paint_1);
			canvas.drawText("返回首页", 825,  780, paint_1);
			canvas.drawText("小车控制", 825,  150, paint_1);
		}
		
	}
	
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

	}

	public void surfaceCreated(SurfaceHolder holder) {
		this.thread.setFlag(true);
        this.thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
	
	class TutorialThread extends Thread{
		private SurfaceHolder surfaceHolder;
		private PictureView confusedView;
		private boolean flag = false;
        public TutorialThread(SurfaceHolder surfaceHolder, PictureView confusedView) {
            this.surfaceHolder = surfaceHolder;
            this.confusedView = confusedView;
        }
        public void setFlag(boolean flag) {
        	this.flag = flag;
        }
       
		public void run() {
			Canvas c;
			while (this.flag) {
	            c = null;
	            try {
	                c = this.surfaceHolder.lockCanvas(null);
	                synchronized (this.surfaceHolder) {
	                	confusedView.onDraw(c);
	                }
	            } finally {
	                if (c != null) {
	                    this.surfaceHolder.unlockCanvasAndPost(c);
	                }
	            }
			}
		}
	}

	private Runnable updateTimer1 = new Runnable() {
		public void run() {
			F1=!F1;
			handler1.postDelayed(this, 83);
		}
	};
	
	private Runnable updateTimer2 = new Runnable() {
		public void run() {
			F2=!F2;
			handler2.postDelayed(this, 71);
		}
	};
	
	private Runnable updateTimer3 = new Runnable() {
		public void run() {
			F3=!F3;
			handler3.postDelayed(this, 56);
		}
	};
	
	private Runnable updateTimer4 = new Runnable() {
		public void run() {
			F4=!F4;
			handler4.postDelayed(this, 45);
		}
	};
	
	private Runnable updateTimer5 = new Runnable() {
		public void run() {
			F5=!F5;
			handler5.postDelayed(this, 38);
		}
	};
	
	public int get_point(){
		return point;
	}
	
	public String get_phoneNumber(int a){
		return phoneNumber[a];
	}
	
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) { 
		int width = bitmap.getWidth(); 
		int height = bitmap.getHeight(); 
		Matrix matrix = new Matrix(); 
		float scaleWidht = ((float) w / width); 
		float scaleHeight = ((float) h / height); 
		matrix.postScale(scaleWidht, scaleHeight); 
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true); 
		return newbmp; 
	}
}
