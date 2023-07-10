package com.example.fxt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.example.fxt.ble.BLeSerialPortService;
import com.example.fxt.ble.api.BleAPI;
import com.example.fxt.ble.device.splicer.bean.OFIDataBean;
import com.example.fxt.ofi.GlobalVariable;
import com.example.fxt.ofi.OFIData;
import com.example.fxt.utils.ToastUtil;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OfiInfoActivity extends MainAppcompatActivity implements BLeSerialPortService.Callback, View.OnClickListener{

    private static final int READ_REQUEST_CODE = 0x000000DD;
    private final int CONN_DISCONNECTED = 0;
    private final int CONN_CONNECTING = 1;
    private final int CONN_CONNECTED = 2;
    private final int CONN_ERROR = 3;
    CustomApplication customApplication;
    private BLeSerialPortService serialPort;
    private int mConnectStatus = CONN_DISCONNECTED;
    private boolean mRecord = false;
    private  int rindex = 0;
    TextView tvTone;
    TextView tvValue;
    ImageView ivLeft;
    ImageView ivRight;
    TextView tvNoSavedData;
    private OFIData mCurData;
    List<OFIData> dataList;
    private ListTableAdapter dataAdapter;
    private String mStrDataFolder;
    private List<OFIDataBean> mOFIList;
    private GpsTracker gpsTracker;
    boolean isSaved;
    RelativeLayout rlProgress;
    String savedData = "";
    Dialog custom_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_f_i);
        customApplication = (CustomApplication)getApplication();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        initDialog();
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.GONE);
        mOFIList = new ArrayList<>();
        mCurData = new OFIData();
        dataList = new ArrayList<OFIData>();
        dataAdapter = new ListTableAdapter(this, dataList);
        tvTone = findViewById(R.id.tvTone);
        tvValue = findViewById(R.id.tvValue);
        ivLeft = findViewById(R.id.ivLeft);
        ivRight = findViewById(R.id.ivRight);
        tvNoSavedData = findViewById(R.id.tvNoSavedData);
        ListView newDevicesListView = findViewById(R.id.dataList);
        newDevicesListView.setAdapter(dataAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        mStrDataFolder = getExternalFilesDir(null).toString();
        Button btnHistory = findViewById(R.id.btnHistory);
        if(customApplication.isFNMSCheck) {
            btnHistory.setText("CHECK");
            btnHistory.setOnClickListener(v -> {
                if(!savedData.equals("")) {
                    if(savedData.contains("Lo")) {
                        savedData = "0";
                    }
                    if(Float.parseFloat(savedData) <= (-30) || Float.parseFloat(savedData) == 0) {
                        customApplication.selectCore = "144C";
                        customApplication.isLevelCheck = true;
                        finish();
                    }else {
                        finish();
                    }
                }
            });
        }else {
            btnHistory.setText("HISTORY");
            btnHistory.setOnClickListener(v -> {
                Intent historyintent = new Intent(OfiInfoActivity.this, OfiHistoryActivity.class);
                startActivity(historyintent);
            });
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            OFIData device = dataList.get(position);
            String str;
            if (device.mDetect == 1) {
                GlobalVariable.gDataTime = dataList.get(position).mDateTime;
                GlobalVariable.gDetect = dataList.get(position).mDetect;
                str = "Select : " + position;
                if (device.mMode == 0) {
                    GlobalVariable.gMode = "CW";
                } else if (device.mMode == 1) {
                    GlobalVariable.gMode = "270";
                } else if (device.mMode == 2) {
                    GlobalVariable.gMode = "1k";
                } else if (device.mMode == 3) {
                    GlobalVariable.gMode = "2k";
                }

                if (device.mDir == 0) {
                    GlobalVariable.gDir = "Right";
                } else {
                    GlobalVariable.gDir = "Left";
                }

                if (device.mWaveLength == 0) {
                    GlobalVariable.gWaveLength = "850";
                } else if (device.mWaveLength == 1) {
                    GlobalVariable.gWaveLength = "1300";
                } else if (device.mWaveLength == 2) {
                    GlobalVariable.gWaveLength = "1310";
                } else if (device.mWaveLength == 3) {
                    GlobalVariable.gWaveLength = "1490";
                } else if (device.mWaveLength == 4) {
                    GlobalVariable.gWaveLength = "1550";
                } else if (device.mWaveLength == 5) {
                    GlobalVariable.gWaveLength = "1625";
                }

                GlobalVariable.gSigSt = device.mSigSt;
                writeLine(str);
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            showMessage("Called onServiceConnected");
            serialPort = ((BLeSerialPortService.LocalBinder) rawBinder).getService()
                    .setContext(getApplicationContext())
                    .registerCallback(OfiInfoActivity.this);
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            connectDevice();
        }

        public void onServiceDisconnected(ComponentName classname) {
            serialPort.unregisterCallback(OfiInfoActivity.this)
                    .close();
        }
    };


    @Override
    public  void onBackPressed() {
        if(customApplication.isFNMSCheck) {
            Intent intent = new Intent(OfiInfoActivity.this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    public void connectDevice() {
        if (GlobalVariable.gDeviceIs) {
            String deviceAddress = GlobalVariable.gDeviceAdd;
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
            showMessage(device.getName());
            serialPort.connect(device);
            mConnectStatus = CONN_CONNECTING;
        }
    }

    private void UpdateData() {
        String str;
        if(mCurData.mPasingOK) {
            if (mCurData.mDetect == 0) {
                tvTone.setText("No Signal");
                ivLeft.setAlpha(0.6f);
                ivRight.setAlpha(0.6f);
                tvValue.setText("--.-");
                Intent bindIntent = new Intent(this, BLeSerialPortService.class);
                bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            } else {
                if (mCurData.mMode == 0) { // CW
                    tvTone.setText("CW");
                    str = mCurData.mSigSt;
                    tvValue.setText(str);
                    if (mCurData.mDir == 0) {
                        ivLeft.setAlpha(0.6f);
                        ivRight.setAlpha(1f);
                    } else {
                        ivLeft.setAlpha(1f);
                        ivRight.setAlpha(0.6f);
                    }
                } else {
                    showMessage("Tone");
                    str = "";
                    if (mCurData.mMode == 1)
                        str = "270Hz";
                    else if (mCurData.mMode == 2)
                        str = "1kHz";
                    else if (mCurData.mMode == 3)
                        str = "2kHz";
                    tvTone.setText(str);
                    str = mCurData.mSigSt;
                    tvValue.setText(str);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent bindIntent = new Intent(this, BLeSerialPortService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if(dataList.size() > 0 ) {
            tvNoSavedData.setVisibility(View.GONE);
        }else {
            tvNoSavedData.setVisibility(View.VISIBLE);
        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            if(customApplication.isFNMSCheck) {
                Intent intent = new Intent(OfiInfoActivity.this, MainActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    private void showMessage(String msg) {
    }

    private void writeLine(final CharSequence text) {
        runOnUiThread(() -> {
        });
    }

    private void ParsingData(String msg) {
        mCurData.parsing(msg);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("OFIAct", "onStop");
//        serialPort.disconnect();
        serialPort.stopSelf();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("OFIAct", "onDestroy");
        serialPort.disconnect();
    }


    @Override
    public void onClick(View v) {
    }

    @Override
    public void onConnected(Context context) {
        mConnectStatus = CONN_CONNECTED;
        // when serial port device is connected
        writeLine("Connected!");
        showMessage("Device Connected");
        // update the send button text to connect
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }


    @Override
    public void onConnectFailed(Context context) {
        mConnectStatus = CONN_ERROR;
        // when some error occured which prevented serial port connection from completing.
        writeLine("Error connecting to device!");
        // update the send button text to connect
        runOnUiThread(() -> {

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(mServiceConnection != null)
//            unbindService(mServiceConnection);
    }

    @Override
    public void onDisconnected(Context context) {
//        if(mServiceConnection != null)
//            unbindService(mServiceConnection);
        mConnectStatus = CONN_DISCONNECTED;
        writeLine("Disconnected!");
//        Intent bindIntent = new Intent(this, BLeSerialPortService.class);
//        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onReceive(Context context, BluetoothGattCharacteristic rx) {
        String msg = rx.getStringValue(0);
        rindex = rindex + msg.length();
        writeLine("> " + rindex + ":" + msg );
        mCurData.parsing(msg);
        mCurData.SetCurTime();
        runOnUiThread(() -> UpdateData());
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        writeLine("Found device : " + device.getAddress());
        writeLine("Waiting for a connection ...");
        showMessage("Found device : " + device.getAddress());
    }

    @Override
    public void onDeviceInfoAvailable() {
        writeLine(serialPort.getDeviceInfo());
    }

    @Override
    public void onCommunicationError(int status, String msg) {
        if (status > 0) {
        }// when the send process found error, for example the send thread  time out
        else {
            writeLine("send error status = " + status);
        }
    }

    private void loadFile(String strFileName) {
        File logFile = new File(strFileName);
        Log.d("OFIAct", "loadFile --> " + strFileName);

        try{
            dataList.clear();
            if (logFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line="";
                while((line=reader.readLine())!=null){
                    if (line.length() > 10){
                        OFIData ddate = new OFIData();
                        ddate.setData(line);
                        addDdata(ddate);
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickRecord(View v) {
        new Handler().postDelayed(() -> {
            isSaved = false;
            rlProgress.setVisibility(View.GONE);
            if(customApplication.isFNMSCheck){
                if(!mCurData.mSigSt.isEmpty()) {
                    if(mCurData.mDetect == 0) {
                        showDialog(R.drawable.ic_disconnected,"Lo", "Core Disconnected");
//                        TipDialog.show("Lo dBm\nCore Disconnect", WaitDialog.TYPE.ERROR,2000);
                    }else {
                        if(Float.parseFloat(mCurData.mSigSt) <= -30) {
                            showDialog(R.drawable.ic_disconnected,mCurData.mSigSt, "Core Disconnected");
//                            TipDialog.show(mCurData.mSigSt + " dBm\nCore Disconnect", WaitDialog.TYPE.ERROR,2000);
                        }else {
                            showDialog(R.drawable.ic_connected,mCurData.mSigSt, "Core Connected");
//                            TipDialog.show(mCurData.mSigSt + " dBm\nCore Connected", WaitDialog.TYPE.SUCCESS,2000);
                        }
                    }
                }
            }
        }, 1000);

        if(!isSaved) {
            isSaved = true;
            // List Data Add
            if(mConnectStatus != CONN_CONNECTED) {
                Toast.makeText(getApplicationContext(),"No Connected Devices Found",Toast.LENGTH_SHORT).show();
                return;
            }
            rlProgress.setVisibility(View.VISIBLE);


            OFIData recorddate;
            recorddate = new OFIData();
            recorddate.mPasingOK = mCurData.mPasingOK;
            recorddate.mSigSt = mCurData.mSigSt;
            recorddate.mWaveLength = mCurData.mWaveLength;
            recorddate.mDir = mCurData.mDir;
            recorddate.mMode = mCurData.mMode;
            recorddate.mDetect = mCurData.mDetect;
            recorddate.mDateTime = mCurData.mDateTime;
//            if (recorddate.mDetect == 0) {
//                return;
//            }else {
//
//            }
            if(tvNoSavedData.getVisibility() != View.GONE) {
                tvNoSavedData.setVisibility(View.GONE);
            }
            addDdata(recorddate);
            OFIDataBean ofiDataBean = new OFIDataBean();

            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String getTime = sdf.format(date);

            ofiDataBean.setDataTime(getTime);
            String str = "";
            if (recorddate.mDir == 0) {
                str = "Right";
            }
            else {
                str = "Left";
            }
            ofiDataBean.setDirection(str);

            if (recorddate.mDetect == 1) {
                if (recorddate.mMode == 0) {
                    str = "CW";
                } else if (recorddate.mMode == 1) {
                    str = "270Hz";
                } else if (recorddate.mMode == 2) {
                    str = "1kHz";
                } else if (recorddate.mMode == 3) {
                    str = "2kHz";
                }
            } else{
                str = "No Signal";
            }

            ofiDataBean.setFrequency(str);

            gpsTracker = new GpsTracker(OfiInfoActivity.this);
            String address = getAddress(gpsTracker.getLatitude(),gpsTracker.getLongitude());
            Log.d("yot132","11 = " + gpsTracker.getLatitude() +"," +gpsTracker.getLongitude());
            ofiDataBean.setLocation(address);

            if (recorddate.mDetect == 1) {
                str = recorddate.mSigSt;
            }else {
                str = "Lo ";
            }

            savedData = str;
            ofiDataBean.setMeasure(str);
            ofiDataBean.setId("");
            ofiDataBean.setMemo("");
            ofiDataBean.setNote("");
            ofiDataBean.setSerial(customApplication.connectBLEAddress);
            if(!customApplication.isFNMSCheck) {
                mOFIList.add(ofiDataBean);
                customApplication.ofiDatabase.insert(mOFIList.get(0));
                mOFIList.clear();
            }
        }
    }

    private void saveFile(OFIData recorddate) {
        Calendar cal = Calendar.getInstance();
        Date dateNow = cal.getTime();
        SimpleDateFormat dateDay = new SimpleDateFormat("yyyyMMdd");

        String strDate = dateDay.format(dateNow);

        String dateFileName = "data_" + strDate + ".csv";
        String logFileName = mStrDataFolder + "/" + dateFileName;

        Log.d("OFIAct", "savefile file " + logFileName);

        File logFile = new File(logFileName);

        try{
            if (!logFile.exists())
            {
                logFile.createNewFile();
            }
            String outStr = recorddate.toStr();
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.newLine();
            buf.append(outStr);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addDdata(OFIData data) {
        if(customApplication.isFNMSCheck) {
            if(dataList.size() == 0) {
                dataList.add(0, data);
            }else {
                dataList.set(0, data);
            }
        }else {
            dataList.add(0, data);
        }
        dataAdapter.notifyDataSetChanged();
    }

    class ListTableAdapter extends BaseAdapter {
        Context context;
        List<OFIData> devices;
        LayoutInflater inflater;

        public ListTableAdapter(Context context, List<OFIData> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.ofi_data_element, null);
            }

            OFIData device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.disp_data));
            String str;

            str = "("+(position+1)+")" + " " + device.mDateTime;

            if (device.mDetect == 1) {
                if (device.mMode == 0) {
                    str = str+ " CW";
                } else if (device.mMode == 1) {
                    str = str+ " 270Hz";
                } else if (device.mMode == 2) {
                    str = str+ " 1KHz";
                } else if (device.mMode == 3) {
                    str = str+ " 2KHz";
                }

                if (device.mDir == 0) {
                    str = str + " R";
                } else {
                    str = str + " L";
                }

                String str2;
                str2 = " " + device.mSigSt +"dBm";
                str = str + str2;
            } else
                str = str + " Lo";

            tvadd.setText(str);
            showMessage(str);

            return vg;
        }
    }

    public void showAlertDialog(String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete Device");
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK", (dialog, which) -> {
            onDisconnectClick();
            for(int i = 0 ; i < customApplication.arrBleAddress.size() ; i ++) {
                if(customApplication.arrBleAddress.get(i).equals(customApplication.connectBLEAddress)) {
                    customApplication.arrBleAddress.remove(i);
                    customApplication.arrBleSerial.remove(i);
                    setStringArrayPref(getApplicationContext(),"arrBleAddress",customApplication.arrBleAddress);
                    setStringArrayPref(getApplicationContext(),"arrBleSerial",customApplication.arrBleSerial);
                    if(customApplication.isFNMSCheck) {
                        Intent intent = new Intent(OfiInfoActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    finish();
                }
            }
            dialog.cancel();
        });
        runOnUiThread(() -> alertDialog.show());
    }


    public void onDisconnectClick(){
        if (customApplication.arrBleAddress.size() == 0){
            ToastUtil.showToast(getApplicationContext(),"No bluetooth device, please go back");
            return;
        }
        if (BleAPI.bleIsConnected()){
            BleAPI.disconnectBle();
        }
    }

    private void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();
    }

    public String getAddress(double Latitude, double Longitude) {
        double latitude = Latitude;
        double longitude = Longitude;

        String address = getCurrentAddress(latitude, longitude);

        return address;
    }



    public String getCurrentAddress( double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    3);
            for(int i = 0 ; i< addresses.size() ; i++) {
                Log.d("yot132","addresses = " + addresses.get(i));
            }
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "geocoder error(network)", Toast.LENGTH_LONG).show();
            return "geocoder error";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "gps error", Toast.LENGTH_LONG).show();
            return "gps error";
        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "No Address", Toast.LENGTH_LONG).show();
            return "No Address";
        }
        Address address = addresses.get(1);
        return address.getAddressLine(0).toString()+"\n";
    }


    public void initDialog() {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_checker);
    }

    public void showDialog(int image, String value, String title) {
        ImageView iv = custom_dialog.findViewById(R.id.iv);
        TextView tvValue = custom_dialog.findViewById(R.id.tvValue);
        TextView tvTitle = custom_dialog.findViewById(R.id.tvtitleChecker);
        iv.setImageResource(image);
        tvValue.setText(value);
        tvTitle.setText(title);
        custom_dialog.show();

        custom_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
    }
}