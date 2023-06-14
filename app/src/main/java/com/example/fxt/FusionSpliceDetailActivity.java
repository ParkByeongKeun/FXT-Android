package com.example.fxt;

import static com.example.fxt.ble.api.util.ByteUtil.getAsciiString;
import static com.example.fxt.utils.ConstantUtil.StrConstant.BEAN;
import static com.example.fxt.utils.FileUtil.getLocalBitmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fxt.ble.api.BleAPI;
import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.api.callback.BleConnectionCallBack;
import com.example.fxt.ble.device.BleDeviceFactory;
import com.example.fxt.ble.device.splicer.BleSplicerCallback;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.example.fxt.ble.util.SpliceDataParseUtil;
import com.example.fxt.utils.ToastUtil;
import com.example.fxt.utils.excelItem;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FusionSpliceDetailActivity extends MainAppcompatActivity {

    TextView mTextFusionSn;
    TextView mTextFusionCurrentArcCount;
    TextView mTextFusionTotalArcCount;
    TextView mTextFusionAppVer;
    TextView mTextFusionModel;
    TextView mTextFusionWorkTime;
    TextView mTextFusionSpliceModel;
    TextView mTextFusionLoss;
    TextView mTextFusionLeftAngle;
    TextView mTextFusionRightAngle;
    TextView mTextFusionCoreAngle;
    TextView mTextFusionCoreOffset;
    ImageView mFusionImage;
    CustomApplication customApplication;
    Button btnAnalysis;
    ActionBar mTitle;
    private PDFont font;
    private AssetManager assetManager;
    private File root;
    RelativeLayout rlProgress;
    private Map<String, SpliceDataBean> mSpliceDataBeanMap;
    private List<SpliceDataBean> mSpliceDataBeanList;
    SpliceDataBean dialogBean;
    String PREFS_NAME = "donotshow";
    Dialog custom_dialog;
    Dialog custom_loading_dialog;
    Dialog custom_check_dialog;
    private Timer timer;
    private final android.os.Handler handler = new android.os.Handler();
    int num = 1;
    boolean isFirstStart = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fusion_detail);
        customApplication = (CustomApplication)getApplication();
        btnAnalysis = findViewById(R.id.btnAnalysis);
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.bringToFront();
        mSpliceDataBeanMap = new HashMap<>();
        mSpliceDataBeanList = new ArrayList<>();
        initDialog();
        initLoadingDialog();
        initCheckDialog();
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Splice History");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        btnAnalysis.setOnClickListener(v -> {
            //TODO: AI Analysis
            Toast.makeText(getApplicationContext(),"No data.",Toast.LENGTH_SHORT).show();
        });
        initData();

        BleAPI.startConnectBle(customApplication.connectBLEAddress, new BleConnectionCallBack() {
            @Override
            public void onReceive(BluetoothGattCharacteristic data_char) {
                rlProgress.setVisibility(View.GONE);
            }
            @Override
            public void onConnectFail(String errMsg) {
                ToastUtil.showToast(getApplicationContext(), errMsg);
                rlProgress.setVisibility(View.GONE);
            }
            @Override
            public void onConnectSuccess(BluetoothGatt bluetoothGatt) {
                runOnUiThread(() -> {
                    rlProgress.setVisibility(View.GONE);
                });
            }
            @Override
            public void onDisconnect(boolean isActive) {
                runOnUiThread(() -> {
                    ToastUtil.showToast(getApplicationContext(), "Server disconnected");
                    rlProgress.setVisibility(View.GONE);
                    FusionSpliceDetailActivity.super.activityFinish();
                    Intent intent = new Intent(FusionSpliceDetailActivity.this, OFIFNMSActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
        BleDeviceFactory.getSplicerDevice(this,customApplication.connectBLEAddress,"aa").setReceiveCallback(new BleSplicerCallback() {
            @Override
            public void onSuccess(BleResultBean resultBean) {
            }

            @Override
            public void onReceiveSuccess(BleResultBean bleResultBean) {
                rlProgress.setVisibility(View.VISIBLE);
                String id = bleResultBean.getIdStr();
                if (bleResultBean.getType() == 1){
                    mSpliceDataBeanMap.put(id, SpliceDataParseUtil.parseSpliceImage(getApplicationContext(), mSpliceDataBeanMap.get(id), bleResultBean));
                }else if (bleResultBean.getType() == 0){
                }else if (bleResultBean.getType() == 2) {
                    mSpliceDataBeanMap.put(id, SpliceDataParseUtil.parseSpliceData(getApplicationContext(),mSpliceDataBeanMap.get(id), bleResultBean));
                }else if (bleResultBean.getType() == 4) {
                    if(!isFirstStart) {
                        String strJson = getAsciiString(bleResultBean.getPayload(),0,bleResultBean.getPayload().length);
                        try {
                            // 最外层的JSONObject对象
                            JSONObject object = new JSONObject(strJson);
                            String SN = object.getString("SN");
                            customApplication.swVersion = object.getString("machineSoftVersion");
                            rlProgress.setVisibility(View.GONE);
                            customApplication.connectSerial = SN;
                            mSpliceDataBeanList.clear();
                            for (Map.Entry<String, SpliceDataBean> map : mSpliceDataBeanMap.entrySet()) {
                                mSpliceDataBeanList.add(map.getValue());
                            }
                            isFirstStart = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                mSpliceDataBeanList.clear();
                for (Map.Entry<String, SpliceDataBean> map : mSpliceDataBeanMap.entrySet()) {
                    mSpliceDataBeanList.add(map.getValue());
                }
                if(mSpliceDataBeanList.size() != 0) {
                    if(mSpliceDataBeanList.get(0).getFiberBean() != null) {
                        if(mSpliceDataBeanList.get(0).getFiberBean().getFuseImagePath() != null) {
                            customApplication.database.insert(mSpliceDataBeanList.get(0));
                            mSpliceDataBeanList.clear();
                            mSpliceDataBeanMap.clear();
                            rlProgress.setVisibility(View.GONE);
                        }
                    }
                }
            }
            @Override
            public void onFailed(int code, String msg) {
                ToastUtil.showToastLong(getApplicationContext(), msg);
            }
        });
    }

    private void initData(){
        SpliceDataBean mSpliceDataBean = (SpliceDataBean) getIntent().getSerializableExtra(BEAN);
        if (mSpliceDataBean == null){
            return;
        }
        dialogBean = mSpliceDataBean;
        mTextFusionSn = findViewById(R.id.fusion_sn_tv);
        mTextFusionCurrentArcCount = findViewById(R.id.fusion_current_arc_count_tv);
        mTextFusionTotalArcCount = findViewById(R.id.fusion_total_arc_count_tv);
        mTextFusionAppVer = findViewById(R.id.fusion_appver_tv);
        mTextFusionModel = findViewById(R.id.fusion_model_tv);
        mTextFusionWorkTime = findViewById(R.id.fusion_work_time_tv);
        mTextFusionSpliceModel = findViewById(R.id.fusion_splice_model_tv);
        mTextFusionLoss = findViewById(R.id.fusion_loss_tv);
        mTextFusionLeftAngle = findViewById(R.id.fusion_left_angle_tv);
        mTextFusionRightAngle = findViewById(R.id.fusion_right_angle_tv);
        mTextFusionCoreAngle = findViewById(R.id.fusion_core_angle_tv);
        mTextFusionCoreOffset = findViewById(R.id.fusion_core_offset_tv);
        mFusionImage = findViewById(R.id.fusion_image_iv);
        mTextFusionSn.setText(mSpliceDataBean.getSn());
        mTextFusionCurrentArcCount.setText(String.valueOf(mSpliceDataBean.getManufacturer()));
        mTextFusionTotalArcCount.setText(String.valueOf(mSpliceDataBean.getBrand()));
        mTextFusionAppVer.setText(mSpliceDataBean.getAppVer());
        mTextFusionModel.setText(mSpliceDataBean.getModel());
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mTextFusionWorkTime.setText(dateformat.format(mSpliceDataBean.getUpdateTime()));
        mTextFusionSpliceModel.setText(mSpliceDataBean.getSpliceName());
        if (mSpliceDataBean.getFiberBean() == null){
            return;
        }
        mTextFusionLoss.setText(mSpliceDataBean.getFiberBean().getLoss());
        mTextFusionLeftAngle.setText(String.valueOf(mSpliceDataBean.getFiberBean().getLeftAngle()));
        mTextFusionRightAngle.setText(String.valueOf(mSpliceDataBean.getFiberBean().getRightAngle()));
        mTextFusionCoreAngle.setText(String.valueOf(mSpliceDataBean.getFiberBean().getCoreAngle()));
        mTextFusionCoreOffset.setText(String.valueOf(mSpliceDataBean.getFiberBean().getCoreOffset()));
        if (mSpliceDataBean.getFiberBean().getFuseImagePath() == null){
            return;
        }
        Bitmap bitmap = getLocalBitmap(mSpliceDataBean.getFiberBean().getFuseImagePath());
        mFusionImage.setImageBitmap(bitmap);

        float loss = Float.parseFloat(mSpliceDataBean.getFiberBean().getLoss());
        float leftAngle = mSpliceDataBean.getFiberBean().getLeftAngle();
        float rightAngle = mSpliceDataBean.getFiberBean().getRightAngle();
        float coreAngle = mSpliceDataBean.getFiberBean().getCoreAngle();
        if(loss >=  0.2 | leftAngle >= 3.0 | rightAngle >= 3.0 | coreAngle >= 1.0) {
            ArrayList<String> check = getStringArrayPref(this,PREFS_NAME);
            boolean checkDialog = false;

            for(int i = 0 ; i < check.size() ; i++) {
                if (dialogBean.getId().equals(check.get(i))) {
                    checkDialog = true;
                }
            }
            if(!checkDialog) {

                showLoadingDialog();
//                StartMainAlertDialog();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gen_doc,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_gen: {
                showDialog();
                return true;
            }
            default: {
                finish();
                return true;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setup();
    }

    private void setup() {
        PDFBoxResourceLoader.init(getApplicationContext());
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        assetManager = getAssets();
        if (ContextCompat.checkSelfPermission(FusionSpliceDetailActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FusionSpliceDetailActivity.this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void saveExcel(){
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Drawing drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1( 0 );
        anchor.setRow1( 16 );
        anchor.setCol2( 2 );
        anchor.setRow2( 16 );
        anchor.setDx1(0);
        anchor.setDx2(1000);
        anchor.setDy1(0);
        anchor.setDy2(1000);
        Drawable drawable = mFusionImage.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        int image_width = bitmap.getWidth();
        int A4_width = (int) PDRectangle.A4.getWidth();
        float scale = (float) (A4_width/(float)image_width*0.4);
        int image_w = (int) (bitmap.getWidth() * scale);
        int image_h = (int) (bitmap.getHeight() * scale);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG,0,bos);
        byte[] bitmapdata = bos.toByteArray();
        InputStream is = null;
        is = new ByteArrayInputStream(bitmapdata);
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(is);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
        try {
            is.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        Row row = sheet.createRow(0);
        Cell cell;
        cell = row.createCell(0);
        cell.setCellValue("INFO");
        row = sheet.createRow(6);
        cell = row.createCell(0);
        cell.setCellValue("FUSION");
        row = sheet.createRow(14);
        cell = row.createCell(0);
        cell.setCellValue("IMAGE");

        ArrayList<excelItem> mItemsInfo = new ArrayList();
        excelItem item = new excelItem("Serial Number", mTextFusionSn.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Current Arc Count", mTextFusionCurrentArcCount.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Total Arc Count", mTextFusionTotalArcCount.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("SW Version", mTextFusionAppVer.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Model", mTextFusionModel.getText().toString());
        mItemsInfo.add(item);

        ArrayList<excelItem> mItemsFusion = new ArrayList();
        item = new excelItem("Work Time", mTextFusionWorkTime.getText().toString());
        mItemsFusion.add(item);
        item = new excelItem("Splice Mode", mTextFusionSpliceModel.getText().toString());
        mItemsFusion.add(item);
        item = new excelItem("Loss Estimation", mTextFusionLoss.getText().toString());
        mItemsFusion.add(item);
        item = new excelItem("Cleaved Angle, Left", mTextFusionLeftAngle.getText().toString());
        mItemsFusion.add(item);
        item = new excelItem("Cleaved Angle, Right", mTextFusionRightAngle.getText().toString());
        mItemsFusion.add(item);
        item = new excelItem("Core Angle", mTextFusionCoreAngle.getText().toString());
        mItemsFusion.add(item);
        item = new excelItem("Core Offset", mTextFusionCoreOffset.getText().toString());
        mItemsFusion.add(item);
        for(int i = 0; i < mItemsInfo.size() ; i++){ // 데이터 엑셀에 입력
            row = sheet.createRow(i+1);
            cell = row.createCell(0);
            cell.setCellValue(mItemsInfo.get(i).getTitle());
            cell = row.createCell(1);
            cell.setCellValue(mItemsInfo.get(i).getValue());
        }
        for(int i = 6; i < 6 + mItemsFusion.size() ; i++){ // 데이터 엑셀에 입력
            row = sheet.createRow(i+1);
            cell = row.createCell(0);
            cell.setCellValue(mItemsFusion.get(i- 6).getTitle());
            cell = row.createCell(1);
            cell.setCellValue(mItemsFusion.get(i- 6).getValue());
        }
        String[] pathName = mTextFusionWorkTime.getText().toString().split(" ");
        String pathTime= pathName[1].replaceAll(":", "_");
        String path = mTextFusionSn.getText().toString() + "_" + pathName[0]+"_"+pathTime +".xls";
        File excelFile = new File(root.getAbsolutePath(),path);
        try{
            FileOutputStream os = new FileOutputStream(excelFile);
            workbook.write(os);
        }catch (IOException e){
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(),excelFile.getAbsolutePath()+" saved as a Excel file.",Toast.LENGTH_SHORT).show();
    }

    public String createPdf() {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        try{
            font = PDType0Font.load(document, assetManager.open("NanumBarunGothicLight.ttf"));
        }
        catch (IOException e){
            Log.e("yot132", "error [font]", e);
        }
        PDPageContentStream contentStream;
        try {
            contentStream = new PDPageContentStream( document, page, true, true);
            Drawable drawable = mFusionImage.getDrawable();
            if(drawable == null ) {
                return "error";
            }
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            int image_width = bitmap.getWidth();
            int image_height = bitmap.getHeight();
            int A4_width = (int) PDRectangle.A4.getWidth();
            int A4_height = (int) PDRectangle.A4.getHeight();
            float scale = (float) (A4_width/(float)image_width*0.4);
            int image_w = (int) (bitmap.getWidth() * scale);
            int image_h = (int) (bitmap.getHeight() * scale);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, resized);
            float x_pos = page.getCropBox().getWidth();
            float y_pos = page.getCropBox().getHeight();
            float x_adjusted = (float) (( x_pos - image_w ) * 0.5 + page.getCropBox().getLowerLeftX());
            float y_adjusted = (float) ((y_pos - image_h) * 0.9 + page.getCropBox().getLowerLeftY());
            contentStream.drawImage(pdImage, 70, y_adjusted- 350, image_w, image_h);
            int text_width = 470;
            int text_left = 70;
            String textN = "INFO" + "\n" +
                    "1. Serial Number          " + mTextFusionSn.getText().toString() + "\n" +
                    "2. Current Arc Count      " + mTextFusionCurrentArcCount.getText().toString() + "\n" +
                    "3. Total Arc Count        " + mTextFusionTotalArcCount.getText().toString() + "\n" +
                    "4. SW Version             " + mTextFusionAppVer.getText().toString() + "\n" +
                    "5. Model                  " + mTextFusionModel.getText().toString() + "\n" +
                    "FUSION" + "\n" +
                    "1. Work Time              " + mTextFusionWorkTime.getText().toString() + "\n" +
                    "2. Splice Mode            " + mTextFusionSpliceModel.getText().toString() + "\n" +
                    "3. Loss Estimation        " + mTextFusionLoss.getText().toString() + "\n" +
                    "4. Cleaved Angle, Left    " + mTextFusionLeftAngle.getText().toString() + "\n" +
                    "5. Cleaved Angle, Right   " + mTextFusionRightAngle.getText().toString() + "\n" +
                    "6. Core Angle             " + mTextFusionCoreAngle.getText().toString() + "\n" +
                    "7. Core Offset            " + mTextFusionCoreOffset.getText().toString() + "\n" +
                    "IMAGE" + "\n";
            int fontSize = 17;
            float leading = 1.5f * fontSize;
            List<String> lines = new ArrayList<String>();
            int lastSpace = -1;
            for (String text : textN.split("\n")) {
                while (text.length() > 0) {
                    int spaceIndex = text.indexOf(' ', lastSpace + 1);
                    if (spaceIndex < 0)
                        spaceIndex = text.length();
                    String subString = text.substring(0, spaceIndex);
                    float size = fontSize * font.getStringWidth(subString) / 1000;
                    if (size > text_width) {
                        if (lastSpace < 0)
                            lastSpace = spaceIndex;
                        subString = text.substring(0, lastSpace);
                        lines.add(subString);
                        text = text.substring(lastSpace).trim();
                        lastSpace = -1;
                    } else if (spaceIndex == text.length()) {
                        lines.add(text);
                        text = "";
                    } else {
                        lastSpace = spaceIndex;
                    }
                }
            }
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(text_left, y_adjusted+200);
            for (String line: lines)
            {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -leading);
            }
            contentStream.endText();
            contentStream.close();
            String[] pathName = mTextFusionWorkTime.getText().toString().split(" ");
            String pathTime= pathName[1].replaceAll(":", "_");
            String path = root.getAbsolutePath() + "/" + mTextFusionSn.getText().toString() + "_" + pathName[0]+"_"+pathTime +".pdf";
            document.save(path);
            document.close();
            return path;
        } catch (IOException e) {
            Log.e("yot132", "Exception thrown while creating PDF", e);
        }
        return "error";
    }

    class SaveTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String path = createPdf();
            return path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(FusionSpliceDetailActivity.this, "Creating doc...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            Toast.makeText(FusionSpliceDetailActivity.this, path+" saved as a PDF file.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void StartMainAlertDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(FusionSpliceDetailActivity.this);
        LayoutInflater inflater=getLayoutInflater();
        final View eulaLayout= inflater.inflate(R.layout.checkbox, null);
        CheckBox dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);

        adb.setTitle("Alarm");
        adb.setMessage("Splice again?");
        adb.setPositiveButton("Yes", (dialog, which) -> {
            ArrayList<String> check = getStringArrayPref(this,PREFS_NAME);
            if (dontShowAgain.isChecked()){
                check.add(dialogBean.getId());
                setStringArrayPref(this,PREFS_NAME,check);
            }
            finish();
        });

        adb.setNegativeButton("No", (dialog, which) -> {
            ArrayList<String> check = getStringArrayPref(this,PREFS_NAME);
            if (dontShowAgain.isChecked()){
                check.add(dialogBean.getId());
                setStringArrayPref(this,PREFS_NAME,check);
            }
        });
        adb.setView(eulaLayout);
        adb.show();
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

    private ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FusionSpliceDetailActivity.this, SpliceHistoryActivity.class);
        startActivity(intent);
        finish();
    }


    public void initDialog() {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_print);
    }

    public void showDialog() {
        custom_dialog.show();
        custom_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnAll).setOnClickListener(v -> {
            SaveTask saveTask = new SaveTask();
            saveTask.execute();
            saveExcel();
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnPDF).setOnClickListener(v -> {
            SaveTask saveTask = new SaveTask();
            saveTask.execute();
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnExcel).setOnClickListener(v -> {
            saveExcel();
            custom_dialog.dismiss();
        });
    }

    public void initCheckDialog() {
        custom_check_dialog = new Dialog(this);
        custom_check_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_check_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_check_dialog.setContentView(R.layout.custom_dialog_base);
        ImageView iv = custom_check_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_pop_w);
        TextView tv = custom_check_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_check_dialog.findViewById(R.id.tvSubTitle);
        tv.setText("Splice Again");
        subTv.setText("Do you want to splice again?");
    }

    public void showCheckDialog() {
        runOnUiThread(() -> {
            custom_check_dialog.show();
            custom_check_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
                custom_check_dialog.dismiss();
            });
            custom_check_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
                custom_check_dialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), SpliceHistoryActivity.class);
                startActivity(intent);
                finish();
            });
        });

    }

    public void initLoadingDialog() {
        custom_loading_dialog = new Dialog(this);
        custom_loading_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_loading_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_loading_dialog.setContentView(R.layout.custom_dialog_loading);
        custom_loading_dialog.setCancelable(false);
    }

    public void showLoadingDialog() {
        custom_loading_dialog.show();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                update();
                ProgressBar progressBar = custom_loading_dialog.findViewById(R.id.progress);
                progressBar.setProgress(50 + num);
                if(progressBar.getProgress() >= 100) {
                    timer.cancel();
                    custom_loading_dialog.dismiss();
                    showCheckDialog();
                }
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 50);
    }

    private void update(){
        Runnable runnable = () -> {
            if(num>50){
                timer.cancel();
            }else{
                num += 2;
            }
        };
        handler.post(runnable);
    }
}