package com.fiberfox.fxt;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fiberfox.fxt.R;
import com.fiberfox.fxt.ble.device.splicer.bean.OFIDataBean;
import com.fiberfox.fxt.utils.excelItem;
import com.fiberfox.fxt.utils.ConstantUtil;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FusionDetailActivity extends MainAppcompatActivity {

    TextView mTextFusionSn;
    TextView mTextFusionCurrentArcCount;
    TextView mTextFusionTotalArcCount;
    TextView mTextFusionAppVer;
    TextView mTextFusionModel;
    TextView mTextFusionWorkTime;
    TextView mTextFusionSpliceModel;
    ImageView mFusionImage;
    CustomApplication customApplication;
    ActionBar mTitle;
    private PDFont font;
    private AssetManager assetManager;
    private File root;
    RelativeLayout rlProgress;
    private Map<String, OFIDataBean> mSpliceDataBeanMap;
    private List<OFIDataBean> mOFIDataBeanList;
    OFIDataBean dialogBean;
    String PREFS_NAME = "donotshow";
    InputMethodManager imm;
    LinearLayout llMain;
    EditText etMemo;
    boolean isBlockSearch;
    Handler handler;
    Runnable getNameRunnable;
    Button btnSave;
    Dialog custom_dialog;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fusion_detail_ofi);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if(!Environment.isExternalStorageManager()){
//                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                startActivity(intent);
//            }
//        }
        customApplication = (CustomApplication)getApplication();
        verifyStoragePermissions(this);
        mFusionImage = findViewById(R.id.fusion_image_iv);
        rlProgress = findViewById(R.id.rlProgress);
        etMemo = findViewById(R.id.etMemo);
        btnSave = findViewById(R.id.btnSave);
        handler = new Handler();
        initDialog();
        rlProgress.bringToFront();
        mSpliceDataBeanMap = new HashMap<>();
        mOFIDataBeanList = new ArrayList<>();
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("OFI History");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        llMain = findViewById(R.id.llMain);
        llMain.setOnClickListener(v -> imm.hideSoftInputFromWindow(etMemo.getWindowToken(), 0));
        initData();
        btnSave.setOnClickListener(v -> {
            handler.removeCallbacks(getNameRunnable);
            dialogBean.setMemo(etMemo.getText().toString());
            handler.postDelayed(getNameRunnable, 500);
        });
    }

    private void initData(){
        OFIDataBean mOFIDataBean = (OFIDataBean) getIntent().getSerializableExtra(ConstantUtil.StrConstant.BEAN);
        if (mOFIDataBean == null){
            return;
        }
        getNameRunnable = () -> customApplication.ofiDatabase.update(dialogBean);
        dialogBean = mOFIDataBean;
        mTextFusionSn = findViewById(R.id.fusion_sn_tv);
        mTextFusionCurrentArcCount = findViewById(R.id.fusion_current_arc_count_tv);
        mTextFusionTotalArcCount = findViewById(R.id.fusion_total_arc_count_tv);
        mTextFusionAppVer = findViewById(R.id.fusion_appver_tv);
        mTextFusionModel = findViewById(R.id.fusion_model_tv);
        mTextFusionWorkTime = findViewById(R.id.fusion_work_time_tv);
        mTextFusionSpliceModel = findViewById(R.id.fusion_splice_model_tv);
        String[] pathName = mOFIDataBean.getDataTime().split(" ");
        String date= pathName[0];
        String time= pathName[1];
        mTextFusionSn.setText(date);
        mTextFusionCurrentArcCount.setText(time);
        if(!mOFIDataBean.getFrequency().equals("CW")) {
            mTextFusionTotalArcCount.setText(mOFIDataBean.getFrequency());
        }else {
            mTextFusionTotalArcCount.setText(mOFIDataBean.getFrequency());
        }
        mTextFusionAppVer.setText(mOFIDataBean.getDirection());
        mTextFusionModel.setText(mOFIDataBean.getMeasure() + "dBm");
        mTextFusionWorkTime.setText(mOFIDataBean.getLocation());
        etMemo.setText(mOFIDataBean.getMemo());
        mTextFusionSpliceModel.setText("("+mOFIDataBean.getMemo().length()+"/200)");
        etMemo.setOnClickListener(v -> {
            isBlockSearch = true;
        });
        etMemo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(isBlockSearch) {
                    isBlockSearch = false;
                    return;
                }
//                handler.removeCallbacks(getNameRunnable);
//                dialogBean.setMemo(etMemo.getText().toString());
//                handler.postDelayed(getNameRunnable, 500);
            }
            @Override
            public void afterTextChanged(Editable s) {
                mTextFusionSpliceModel.setText("("+s.length()+"/200)");
            }
        });
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

//    public void showDialog() {
//        Dialog bottomDialog = new Dialog(this, R.style.BottomDialog);
//        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_content_normal, null);
//        bottomDialog.setContentView(contentView);
//        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
//        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
//        contentView.setLayoutParams(layoutParams);
//        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
//        bottomDialog.setCanceledOnTouchOutside(true);
//        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
//        contentView.findViewById(R.id.tvALL).setOnClickListener(v -> {
//            SaveTask saveTask = new SaveTask();
//            saveTask.execute();
//            saveExcel();
//        });
//
//        contentView.findViewById(R.id.tvPDF).setOnClickListener(v -> {
//            SaveTask saveTask = new SaveTask();
//            saveTask.execute();
//        });
//
//        contentView.findViewById(R.id.tvExcel).setOnClickListener(v -> {
//            saveExcel();
//        });
//        bottomDialog.show();
//    }

    @Override
    protected void onStart() {
        super.onStart();
        setup();
    }

    private void setup() {
        PDFBoxResourceLoader.init(getApplicationContext());
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        assetManager = getAssets();
        if (ContextCompat.checkSelfPermission(FusionDetailActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FusionDetailActivity.this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void saveExcel(){
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Drawing drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        CellStyle cs = workbook.createCellStyle();
        CellStyle csBold = workbook.createCellStyle();
        Font fontBold = workbook.createFont();
        fontBold.setColor(HSSFColor.BLACK.index);
        csBold.setFont(fontBold);
        fontBold.setBold(true);
        sheet.setDefaultColumnWidth(20);

        anchor.setCol1( 0 );
        anchor.setRow1( 16 );
        anchor.setCol2( 2 );
        anchor.setRow2( 16 );
        anchor.setDx1(0);
        anchor.setDx2(1000);
        anchor.setDy1(0);
        anchor.setDy2(1000);
        Row row = sheet.createRow(0);
        Cell cell;
        cell = row.createCell(0);
        cell.setCellValue("");

        ArrayList<excelItem> mItemsInfo = new ArrayList();
        excelItem item = new excelItem("REPORT", "");
        mItemsInfo.add(item);
        item = new excelItem("", "");
        mItemsInfo.add(item);
        item = new excelItem("INFO", "");
        mItemsInfo.add(item);
        item = new excelItem("Date", mTextFusionSn.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Time", mTextFusionCurrentArcCount.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Frequency", mTextFusionTotalArcCount.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Direction", mTextFusionAppVer.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Measure", mTextFusionModel.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Location", mTextFusionWorkTime.getText().toString());
        mItemsInfo.add(item);
        item = new excelItem("Memo", etMemo.getText().toString());
        mItemsInfo.add(item);

        for(int i = 0; i < mItemsInfo.size() ; i++){ // 데이터 엑셀에 입력
            row = sheet.createRow(i+1);
            cell = row.createCell(0);
            cell.setCellValue(mItemsInfo.get(i).getTitle());
            cell = row.createCell(1);
            cell.setCellValue(mItemsInfo.get(i).getValue());
        }
        OFIDataBean mOFIDataBean = (OFIDataBean) getIntent().getSerializableExtra(ConstantUtil.StrConstant.BEAN);

        String[] pathName = mOFIDataBean.getDataTime().split(" ");
        String pathTime= pathName[1].replaceAll(":", "_");
        String path = pathName[0]+"_"+pathTime +".xls";
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
            font = PDType0Font.load(document, assetManager.open("NanumGothicBold.ttf"));
        }
        catch (IOException e){
            Log.e("yot132", "error [font]", e);
        }
        PDPageContentStream contentStream;
        try {
            contentStream = new PDPageContentStream( document, page, true, true);
            int text_width = 470;
            int text_left = 70;
            String textN = "";

            String[][] contents = {
                    {"REPORT",    ""},
                    {"", ""},
                    {"INFO", ""},
                    {"Date", mTextFusionSn.getText().toString()},
                    {"Time", mTextFusionCurrentArcCount.getText().toString()},
                    {"Frequency", mTextFusionTotalArcCount.getText().toString()},
                    {"Direction", mTextFusionAppVer.getText().toString()},
                    {"Measure", mTextFusionModel.getText().toString()},
                    {"Location", mTextFusionWorkTime.getText().toString()},
                    {"Memo", etMemo.getText().toString()}
            };
            drawTable(page, contentStream, 780, 70, contents);

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
            contentStream.newLineAtOffset(text_left, 700);
            for (String line: lines)
            {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -leading);
            }
            contentStream.endText();
            contentStream.close();
            OFIDataBean mOFIDataBean = (OFIDataBean) getIntent().getSerializableExtra(ConstantUtil.StrConstant.BEAN);

            String[] pathName = mOFIDataBean.getDataTime().split(" ");
            String pathTime= pathName[1].replaceAll(":", "_");
            String path = root.getAbsolutePath() + "/" + pathName[0]+"_"+pathTime +".pdf";
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
            Toast.makeText(FusionDetailActivity.this, "Creating doc...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            Toast.makeText(FusionDetailActivity.this, path+" saved as a PDF file.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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


    private void drawLine(PDPageContentStream contentStream, float xStart, float yStart, float xEnd, float yEnd) throws IOException {
        contentStream.moveTo(xStart,yStart);
        contentStream.lineTo(xEnd,yEnd);
        contentStream.setStrokingColor(70,70,70);
        contentStream.stroke();
    }

    private void drawText(String text, PDFont font, int fontSize, float left, float bottom, PDPageContentStream contentStream) throws Exception {
        contentStream.beginText();
        contentStream.setNonStrokingColor(0f,0f,0f);
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