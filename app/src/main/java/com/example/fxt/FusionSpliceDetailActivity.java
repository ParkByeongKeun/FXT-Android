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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import com.example.fxt.RestApi.ApiService;
import com.example.fxt.RestApi.ResponseImage;
import com.example.fxt.ble.api.BleAPI;
import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.api.callback.BleConnectionCallBack;
import com.example.fxt.ble.device.BleDeviceFactory;
import com.example.fxt.ble.device.splicer.BleSplicerCallback;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.example.fxt.ble.util.SpliceDataParseUtil;
import com.example.fxt.utils.ToastUtil;
import com.example.fxt.utils.excelItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    TextView mTextFusionWorkLocation;
    TextView mTextFusionWorkUser;
    TextView mTextViewCoreAngle;
    TextView mTextViewLeftAngle;
    TextView mTextViewRightAngle;
    TextView mTextViewLoss;
    TextView mTextViewPassFail;
    TextView tvPassFailTitle;
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
    boolean isAnomaly = false;
    String strPassFail = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fusion_detail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()){
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
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

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

// Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
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
        mTextFusionWorkLocation = findViewById(R.id.fusion_work_location_tv);
        mTextFusionWorkUser = findViewById(R.id.fusion_work_user_tv);
        mTextViewCoreAngle = findViewById(R.id.tvCoreAngle);
        mTextViewLeftAngle = findViewById(R.id.tvAngleLeft);
        mTextViewRightAngle = findViewById(R.id.tvAngleRight);
        mTextViewLoss = findViewById(R.id.tvLoss);
        mTextViewPassFail = findViewById(R.id.tvPassFail);
        tvPassFailTitle = findViewById(R.id.tvPassFailTitle);
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
        mTextFusionWorkLocation.setText(mSpliceDataBean.getFpgaVer());
        mTextFusionWorkUser.setText("fiberfox");
        if (mSpliceDataBean.getFiberBean().getFuseImagePath() == null){
            return;
        }
        Bitmap bitmap = getLocalBitmap(mSpliceDataBean.getFiberBean().getFuseImagePath());
        mFusionImage.setImageBitmap(bitmap);
        File imageFile = null;
        try {
            imageFile = createFileFromBitmap(bitmap);
        }catch (IOException e) {
            e.printStackTrace();
        }
        updateImage(imageFile);
        float loss = Float.parseFloat(mSpliceDataBean.getFiberBean().getLoss());
        float leftAngle = mSpliceDataBean.getFiberBean().getLeftAngle();
        float rightAngle = mSpliceDataBean.getFiberBean().getRightAngle();
        float coreAngle = mSpliceDataBean.getFiberBean().getCoreAngle();
        if(loss >= customApplication.lossThreshold) {
            mTextFusionLoss.setTextColor(getResources().getColor(R.color.red));
            mTextViewLoss.setTextColor(getResources().getColor(R.color.red));
            strPassFail += " (Loss)";
        }
        if(leftAngle >= 0.5) {
            mTextFusionLeftAngle.setTextColor(getResources().getColor(R.color.red));
            mTextViewLeftAngle.setTextColor(getResources().getColor(R.color.red));
            strPassFail += " (L.Angle)";
        }
        if(rightAngle >= 0.5) {
            mTextFusionRightAngle.setTextColor(getResources().getColor(R.color.red));
            mTextViewRightAngle.setTextColor(getResources().getColor(R.color.red));
            strPassFail += " (R.Angle)";
        }
        if(coreAngle >= customApplication.angleThreshold) {
            mTextFusionCoreAngle.setTextColor(getResources().getColor(R.color.red));
            mTextViewCoreAngle.setTextColor(getResources().getColor(R.color.red));
            strPassFail += " (C.Angle)";
        }
        if(isAnomaly) {
            strPassFail += " (AI)";
        }
//        mTextViewPassFail.setTextColor(getResources().getColor(R.color.white));
        if(loss >= customApplication.lossThreshold | leftAngle >= 0.5 | rightAngle >= 0.5 | coreAngle >= customApplication.angleThreshold | isAnomaly) {
            tvPassFailTitle.setText("FAIL");
            mTextViewPassFail.setText(strPassFail);
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
        }else {
            tvPassFailTitle.setText("PASS");
            mTextViewPassFail.setText("none");
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
        CellStyle cs = workbook.createCellStyle();
        CellStyle csBold = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(HSSFColor.RED.index);
        cs.setFont(font);
        Font fontBold = workbook.createFont();
        fontBold.setColor(HSSFColor.BLACK.index);
        csBold.setFont(fontBold);
        fontBold.setBold(true);
        Drawing drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        sheet.setDefaultColumnWidth(20);
        anchor.setCol1( 0 );
        anchor.setRow1( 22 );
        anchor.setCol2( 1 );
        anchor.setRow2( 29 );
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
        resized.compress(Bitmap.CompressFormat.JPEG,100,bos);
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
        cell.setCellValue("REPORT");
        cell.setCellStyle(csBold);
        row = sheet.createRow(2);
        cell = row.createCell(0);
        cell.setCellValue("INFO");
        cell.setCellStyle(csBold);
        row = sheet.createRow(12);
        cell = row.createCell(0);
        cell.setCellValue("FUSION");
        cell.setCellStyle(csBold);
        row = sheet.createRow(20);
        cell = row.createCell(0);
        cell.setCellValue("IMAGE");
        cell.setCellStyle(csBold);
        row = sheet.createRow(31);
        cell = row.createCell(0);
        cell.setCellStyle(csBold);
        cell.setCellValue(tvPassFailTitle.getText().toString());
        row = sheet.createRow(32);
        cell = row.createCell(0);
        cell.setCellValue(mTextViewPassFail.getText().toString());

        ArrayList<excelItem> mItemsInfo = new ArrayList();
        excelItem item = new excelItem("Work User", mTextFusionWorkUser.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Work Location", mTextFusionWorkLocation.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Work Time", mTextFusionWorkTime.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Serial Number", mTextFusionSn.getText().toString());
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
        for(int i = 2; i < 2 + mItemsInfo.size() ; i++){ // 데이터 엑셀에 입력
            row = sheet.createRow(i+1);
            cell = row.createCell(0);
            cell.setCellValue(mItemsInfo.get(i- 2).getTitle());
            cell = row.createCell(1);
            cell.setCellValue(mItemsInfo.get(i- 2).getValue());
        }
        Cell cellTitle;
        for(int i = 12; i < 12 + mItemsFusion.size() ; i++){ // 데이터 엑셀에 입력
            String title;
            row = sheet.createRow(i+1);
            cellTitle = row.createCell(0);
            cellTitle.setCellValue(mItemsFusion.get(i- 12).getTitle());
            title = mItemsFusion.get(i- 12).getTitle();
            cell = row.createCell(1);
            cell.setCellValue(mItemsFusion.get(i- 12).getValue());
            if(title.equals("Loss Estimation")) {
                if(mTextFusionLoss.getCurrentTextColor() == getResources().getColor(R.color.red)){
                    cell.setCellStyle(cs);
                    cellTitle.setCellStyle(cs);
                }
            }
            if(title.equals("Cleaved Angle, Left")) {
                if(mTextFusionLeftAngle.getCurrentTextColor() == getResources().getColor(R.color.red)){
                    cell.setCellStyle(cs);
                    cellTitle.setCellStyle(cs);
                }
            }
            if(title.equals("Cleaved Angle, Right")) {
                if(mTextFusionRightAngle.getCurrentTextColor() == getResources().getColor(R.color.red)){
                    cell.setCellStyle(cs);
                    cellTitle.setCellStyle(cs);
                }
            }
            if(title.equals("Core Angle")) {
                if(mTextFusionCoreAngle.getCurrentTextColor() == getResources().getColor(R.color.red)){
                    cell.setCellStyle(cs);
                    cellTitle.setCellStyle(cs);
                }
            }
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
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        try{
            font = PDType0Font.load(document, assetManager.open("NanumGothicBold.ttf"));
        }
        catch (IOException e){
            Log.e("yot132", "error [font]", e);
        }
        PDPageContentStream contentStream;
        try {
            contentStream = new PDPageContentStream( document, page);
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
            contentStream.drawImage(pdImage, 70, y_adjusted- 420, image_w, image_h);

//            Bitmap bigPictureBitmap  = BitmapFactory.decodeResource(getResources(), R.drawable.pdf_bg);
//            PDImageXObject pdImage1 = LosslessFactory.createFromImage(document, bigPictureBitmap);
//            contentStream.drawImage(pdImage1, 50, 300, 500, 460);
//            contentStream.drawImage(pdImage1, 0, 665, 545, 790);

            int text_width = 470;
            int text_left = 70;
            String[][] contents = {
                    {"REPORT",    ""},
                    {"", ""},
                    {"INFO", ""},
                    {"Work User", mTextFusionWorkUser.getText().toString()},
                    {"Work Location", mTextFusionWorkLocation.getText().toString()},
                    {"Work Time", mTextFusionWorkTime.getText().toString()},
                    {"Serial Number", mTextFusionSn.getText().toString()},
                    {"Current Arc Count", mTextFusionCurrentArcCount.getText().toString()},
                    {"Total Arc Count", mTextFusionTotalArcCount.getText().toString()},
                    {"SW Version", mTextFusionAppVer.getText().toString()},
                    {"Model", mTextFusionModel.getText().toString()},
                    {"",""},
                    {"FUSION",""},
                    {"Splice Mode", mTextFusionSpliceModel.getText().toString()},
                    {"Loss Estimation", mTextFusionLoss.getText().toString()},
                    {"Cleaved Angle, Left", mTextFusionLeftAngle.getText().toString()},
                    {"Cleaved Angle, Right", mTextFusionRightAngle.getText().toString()},
                    {"Core Angle", mTextFusionCoreAngle.getText().toString()},
                    {"Core Offset", mTextFusionCoreOffset.getText().toString()},
                    {"",""},
                    {"IMAGE",""}
            };
            drawTable(page, contentStream, 800, 70, contents);
            String[][] contentsPass = {
                    {tvPassFailTitle.getText().toString(),    ""},
                    {mTextViewPassFail.getText().toString(),    ""}
            };
            drawTable(page, contentStream, 160, 70, contentsPass);

            String textN = ""+"\n";
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
        } catch (Exception e) {
            e.printStackTrace();
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

    boolean updateImage(final File file) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("Content-Type", "multipart/form-data").build();
            return chain.proceed(request);
        });
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.36.30.159:8088")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        service.upload(fileToUpload).enqueue(new Callback<ResponseImage>() {
            @Override
            public void onResponse(Call<ResponseImage> call, Response<ResponseImage> response) {
                ResponseImage result = response.body();
                Log.d("yot132","isAnomaly = " + result.getAnomaly()+", loss = " + result.getLoss());
                isAnomaly = result.getAnomaly();
            }

            @Override
            public void onFailure(Call<ResponseImage> call, Throwable t) {
            }
        });
        return isAnomaly;
    }

    private File createFileFromBitmap(Bitmap bitmap) throws IOException {
        File newFile = new File(this.getFilesDir(), makeImageFileName());
        FileOutputStream fileOutputStream = new FileOutputStream(newFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        fileOutputStream.close();
        return newFile;
    }

    private String makeImageFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
        Date date = new Date();
        String strDate = simpleDateFormat.format(date);
        return strDate + ".png";
    }

    private void drawLine(PDPageContentStream contentStream, float xStart, float yStart, float xEnd, float yEnd) throws IOException {
        contentStream.moveTo(xStart,yStart);
        contentStream.lineTo(xEnd,yEnd);
        contentStream.setStrokingColor(255,255,255);
        contentStream.stroke();
    }

    boolean isCheck = false;
    private void drawText(String text, PDFont font, int fontSize, float left, float bottom, PDPageContentStream contentStream) throws Exception {
        contentStream.beginText();
        contentStream.setNonStrokingColor(0f,0f,0f);
        if(isCheck) {
            isCheck = false;
            contentStream.setNonStrokingColor(1f,0f,0f);
        }
        if(text.equals("Loss Estimation")) {
            if(mTextFusionLoss.getCurrentTextColor() == getResources().getColor(R.color.red)){
                contentStream.setNonStrokingColor(1f,0f,0f);
                isCheck = true;
            }else {
                contentStream.setNonStrokingColor(0f,0f,0f);
            }
        }
        if(text.equals("Cleaved Angle, Left")) {
            if(mTextFusionLeftAngle.getCurrentTextColor() == getResources().getColor(R.color.red)){
                contentStream.setNonStrokingColor(1f,0f,0f);
                isCheck = true;
            }else {
                contentStream.setNonStrokingColor(0f,0f,0f);
            }
        }
        if(text.equals("Cleaved Angle, Right")) {
            if(mTextFusionRightAngle.getCurrentTextColor() == getResources().getColor(R.color.red)){
                contentStream.setNonStrokingColor(1f,0f,0f);
                isCheck = true;
            }else {
                contentStream.setNonStrokingColor(0f,0f,0f);
            }
        }
        if(text.equals("Core Angle")) {
            if(mTextFusionCoreAngle.getCurrentTextColor() == getResources().getColor(R.color.red)){
                contentStream.setNonStrokingColor(1f,0f,0f);
                isCheck = true;
            }else {
                contentStream.setNonStrokingColor(0f,0f,0f);
            }
        }
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(left, bottom);
        contentStream.showText(text);
        contentStream.endText();
    }

    public void drawTable(PDPage page, PDPageContentStream contentStream, float y, float margin, String[][] content) throws Exception {
        final int rows = content.length;
        final int cols = content[0].length;

        final float rowHeight = 20f;
        final float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
        final float tableHeight = rowHeight * rows;

        final float colWidth = tableWidth / (float)cols;
        final float cellMargin = 5f;

        // 행을 그린다.
        float nexty = y ;
        for(int i = 0; i <= rows; i++) {
            drawLine(contentStream, margin, nexty, margin + tableWidth, nexty);
            nexty -= rowHeight;
        }

        // 열을 그린다.
        float nextx = margin;
        for(int i = 0; i <= cols; i++) {
            drawLine(contentStream, nextx, y, nextx, y - tableHeight);
            nextx += colWidth;
        }

        float textx = margin + cellMargin;
        float texty = y - 15;
        for(int i = 0; i < content.length; i++) {
            for(int j = 0 ; j < content[i].length; j++) {
                String text = content[i][j];
                drawText(text, font, 14, textx, texty, contentStream);
                textx += colWidth;
            }
            texty -= rowHeight;
            textx = margin + cellMargin;
        }
    }
}