package ncut.csie.eegphone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class NeuroSky
{
  public static final int BT_STATE_NONE = 0;
  public static final int BT_STATE_LISTEN = 1;
  public static final int BT_STATE_CONNECTING = 2;
  public static final int BT_STATE_CONNECTED = 3;
  public static final int MESSAGE_BT_STATUS_CHANGE = 1;
  public static final int MESSAGE_BT_SPP_READ = 2;
  public static final int MESSAGE_BT_SPP_WRITE = 3;
  public static final int MESSAGE_BT_DEVICE_NAME = 4;
  public static final int MESSAGE_BT_TOAST = 5;
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private Handler handler;
  private int btStatus;
  private static boolean isReadable;
  private static boolean isStart = false;
  private BluetoothAdapter btAdapter;
  private static ConnectThread btConnectThread;
  private static ConnectedThread btConnectedThread;

  public NeuroSky(BluetoothAdapter adapter, Handler handler)
  {
    this.btAdapter = adapter;
    this.handler = handler;
    this.btStatus = 0;
  }

  public static String getVersion()
  {
    return "version 1.0";
  }

  public synchronized void connect(BluetoothDevice btDevice)
  {
    setStart(false);

    if (btConnectThread != null) {
      btConnectThread.cancel();
      btConnectThread = null;
    }

    btConnectThread = new ConnectThread(btDevice);
    btConnectThread.start();
    setStatus(2);
  }

  private synchronized void connected(BluetoothSocket btSocket)
  {
    if (btConnectThread != null) {
      btConnectThread = null;
    }
    if (btConnectedThread != null) {
      btConnectedThread.cancel();
      btConnectedThread = null;
    }

    setReadable(true);

    btConnectedThread = new ConnectedThread(btSocket);
    btConnectedThread.start();
    setStatus(3);
  }

  public synchronized void close()
  {
    if (btConnectThread != null) {
      btConnectThread.cancel();
      btConnectThread = null;
    }
    if (btConnectedThread != null) {
      btConnectedThread.cancel();
      btConnectedThread = null;
    }
  }

  public synchronized void start()
  {
    setStart(true);
  }

  public synchronized void stop()
  {
    setStart(false);
  }

  private synchronized void setStatus(int btStatus)
  {
    this.btStatus = btStatus;
    this.handler.obtainMessage(1, btStatus, 0).sendToTarget();
  }

  public synchronized int getStatus()
  {
    return this.btStatus;
  }

  private synchronized void setReadable(boolean readable)
  {
    isReadable = readable;
  }

  private synchronized boolean getReadable()
  {
    return isReadable;
  }

  private synchronized void setStart(boolean start) {
    isStart = start;
  }

  private synchronized boolean getStart()
  {
    return isStart;
  }

  private class ConnectThread extends Thread
  {
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocketA;

    public ConnectThread(BluetoothDevice device)
    {
      this.btDevice = device;
      try {
        this.btSocketA = this.btDevice.createRfcommSocketToServiceRecord(NeuroSky.MY_UUID);
      } catch (IOException e) {
        NeuroSkyUtil.printStackTrace2Log("NEURO_SKY", e);
        NeuroSkyUtil.notify2Toast(NeuroSky.this.handler, 5, "连接失败");
      }
    }

    public void run()
    {
      Log.i("ConnectThread", "Begin ConnectThread !");
      setName("ConnectThread");

      if (NeuroSky.this.btAdapter.isDiscovering()) NeuroSky.this.btAdapter.cancelDiscovery(); 
      try
      {
        this.btSocketA.connect();
        NeuroSkyUtil.notify2Toast(NeuroSky.this.handler, 5, "连接成功");
      } catch (IOException e) {
        NeuroSky.this.setStatus(0);
        NeuroSkyUtil.printStackTrace2Log("NEURO_SKY", e);
        NeuroSkyUtil.notify2Toast(NeuroSky.this.handler, 5, "连接失败");
        try {
          this.btSocketA.close(); } catch (IOException localIOException1) {
        }
        return;
      }
      NeuroSky.this.connected(this.btSocketA);
    }

    public void cancel()
    {
      try {
        if (this.btSocketA != null) this.btSocketA.close(); 
      }
      catch (IOException localIOException) {
      }
    }
  }

  private class ConnectedThread extends Thread {
    private BluetoothSocket btSocketB;
    private InputStream inStream;
    private NeuroStreamParser parser;

    public ConnectedThread(BluetoothSocket socket) {
      this.btSocketB = socket;
      this.parser = new NeuroStreamParser(NeuroSky.this.handler);
    }

    public void run()
    {
      Log.i("ConnectedThread", "Begin ConnectedThread !");
      setName("ConnectedThread");
      try {
        this.inStream = this.btSocketB.getInputStream();
        byte[] buffer = new byte[1];
        do
        {
          if (NeuroSky.this.getStart()) {
            this.inStream.read(buffer);
            this.parser.parseByte(buffer[0]);
          }
        }
        while (
          NeuroSky.this.getReadable());

        this.inStream.close();
      }
      catch (IOException e) {
        NeuroSky.this.setStatus(0);

        NeuroSkyUtil.printStackTrace2Log("ConnectedThread", e);
        NeuroSkyUtil.notify2Toast(NeuroSky.this.handler, 5, "Unable to get input stream");
        return;
      } finally {
        try {
          this.btSocketB.close();
        }
        catch (IOException localIOException2) {
        }
      }
    }

    public void cancel() {
      NeuroSky.this.setReadable(false);
    }
  }
}
