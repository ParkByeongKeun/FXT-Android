package com.fiberfox.fxt;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fiberfox.fxt.ble.device.splicer.bean.EnclosureDataBean;
import com.fiberfox.fxt.ble.device.splicer.bean.OFIDataBean;
import com.fiberfox.fxt.utils.ConstantUtil;
import com.fiberfox.fxt.utils.excelItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnclosureDetailActivity extends MainAppcompatActivity {

    CustomApplication customApplication;
    ActionBar mTitle;
    private PDFont font;
    private AssetManager assetManager;
    private File root;
    OFIDataBean dialogBean;
    String PREFS_NAME = "donotshow";
    InputMethodManager imm;
    boolean isBlockSearch;
    Handler handler;
    Runnable getNameRunnable;
    Dialog custom_dialog;
    private List<EnclosureDataBean> mEnclosureList;
    private ListView listView;
    private List<Bitmap> croppedImages;
    List<String> croppedTitle;
    Dialog custom_check_dialog;
    ImageView imageView;
    String date;
    boolean isAnomaly = false;
    TextView tvCheck;
    TextView tvLine;

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
        setContentView(R.layout.activity_detail_enclosure);
        List<List<Float>> items = (List<List<Float>>) getIntent().getSerializableExtra(ConstantUtil.StrConstant.YOLO);
        Log.d("yot132","items= " + items);
        Intent intent = getIntent();
        date = intent.getStringExtra("date");
        String imagePath = intent.getStringExtra("img");
        listView = findViewById(R.id.listView);
        tvCheck = findViewById(R.id.tvCheck);
        tvLine = findViewById(R.id.tvLine);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Log.d("yot132","w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
        mEnclosureList = new ArrayList<>();
        customApplication = (CustomApplication)getApplication();
        verifyStoragePermissions(this);
        handler = new Handler();
        initDialog();
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Enclosure");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        initData();
        initCheckDialog();

        if(items != null) {
            EnclosureDataBean enclosureDataBean = new EnclosureDataBean();
            enclosureDataBean.setId("");
            enclosureDataBean.setNote("");
            enclosureDataBean.setImagePath(imagePath);
            enclosureDataBean.setCoordinate(convertListToString(items));
            enclosureDataBean.setUser(customApplication.login_id);
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String getTime = sdf.format(date);
            enclosureDataBean.setDataTime(getTime);
            if(!customApplication.isFNMSCheck) {
                mEnclosureList.add(enclosureDataBean);
                customApplication.enclosureDatabase.insert(mEnclosureList.get(0));
                mEnclosureList.clear();
            }
            this.date = getTime;
            Bitmap overlayBitmap = overlay(bitmap, items);
            imageView = findViewById(R.id.imageView);
            displayOverlay(imageView, overlayBitmap);
            croppedImages = createDummyCroppedImages(bitmap,items);
        }else {
            String coordinate = intent.getStringExtra("coordinate");
            items = convertStringToList(coordinate);
            Bitmap overlayBitmap = overlay(bitmap, items);
            imageView = findViewById(R.id.imageView);
            displayOverlay(imageView, overlayBitmap);
            croppedImages = createDummyCroppedImages(bitmap,items);
        }

        ImageAdapter adapter = new ImageAdapter();
        listView.setAdapter(adapter);


    }

    private void initData(){
        OFIDataBean mOFIDataBean = (OFIDataBean) getIntent().getSerializableExtra(ConstantUtil.StrConstant.BEAN);
        if (mOFIDataBean == null){
            return;
        }
//        getNameRunnable = () -> customApplication.ofiDatabase.update(dialogBean);
//        dialogBean = mOFIDataBean;

        String[] pathName = mOFIDataBean.getDataTime().split(" ");
        String date= pathName[0];
        String time= pathName[1];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gen_doc,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isAnomaly) {
            showCheckDialog();
            return true;
        }
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
        if (ContextCompat.checkSelfPermission(EnclosureDetailActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EnclosureDetailActivity.this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void saveExcel(){
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        CellStyle cs = workbook.createCellStyle();
        CellStyle csBold = workbook.createCellStyle();
        CellStyle csPass = workbook.createCellStyle();
        Font fontPass = workbook.createFont();
        fontPass.setColor(HSSFColor.BLUE.index);
        csPass.setFont(fontPass);
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
        sheet.setDefaultColumnWidth(40);
        anchor.setCol1( 0 );
        anchor.setRow1( 3 );
        anchor.setCol2( 1 );
        anchor.setRow2( 28 );
        anchor.setDx1(0);
        anchor.setDx2(500);
        anchor.setDy1(0);
        anchor.setDy2(500);
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        int image_width = bitmap.getWidth();
        int A4_width = (int) PDRectangle.A4.getWidth();
        float scale = (float) (A4_width/(float)image_width*0.4);
        int image_w = 640;
        int image_h = 640;
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
//        row = sheet.createRow(30);
//        cell = row.createCell(0);
//        cell.setCellValue("Enclosure");
//        cell.setCellStyle(csBold);

        String[] pathName = date.split(" ");
        String pathTime= pathName[1].replaceAll(":", "_");
        String path = customApplication.login_id + "_" + pathName[0]+"_"+pathTime +".xls";
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
            Drawable drawable = imageView.getDrawable();
            if(drawable == null ) {
                return "error";
            }
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            int image_width = bitmap.getWidth();
            int image_height = bitmap.getHeight();
            int A4_width = (int) PDRectangle.A4.getWidth();
            int A4_height = (int) PDRectangle.A4.getHeight();
            float scale = (float) (A4_width/(float)image_width*0.4);

            int image_w = 320;
            int image_h = 240;
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, resized);
            float x_pos = page.getCropBox().getWidth();
            float y_pos = page.getCropBox().getHeight();
            float x_adjusted = (float) (( x_pos - image_w ) * 0.5 + page.getCropBox().getLowerLeftX());
            float y_adjusted = (float) ((y_pos - image_h) * 0.9 + page.getCropBox().getLowerLeftY());

//            Bitmap bigPictureBitmap  = BitmapFactory.decodeResource(getResources(), R.drawable.pdf_bg);
//            PDImageXObject pdImage1 = LosslessFactory.createFromImage(document, bigPictureBitmap);
//            contentStream.drawImage(pdImage1, 50, 300, 500, 460);
//            contentStream.drawImage(pdImage1, 0, 665, 545, 790);

            int text_width = 470;
            int text_left = 70;

            String[][] contents = {
                    {"REPORT",    ""},
                    {"", ""}
            };

            drawTable(page, contentStream, 800, 70, contents);
//            contentStream.drawImage(pdImage, 70, y_adjusted- 370, image_w, image_h);
            contentStream.drawImage(pdImage, 70, 520, image_w, image_h);

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
//            for (String line: lines)
//            {
//                contentStream.showText(line);
//                contentStream.newLineAtOffset(0, -leading);
//            }
            contentStream.endText();
            contentStream.close();

            String[] pathName = date.split(" ");
            String pathTime= pathName[1].replaceAll(":", "_");
            String path = root.getAbsolutePath() + "/" + customApplication.login_id + "_" + pathName[0]+"_"+pathTime +".pdf";
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
            Toast.makeText(EnclosureDetailActivity.this, "Creating doc...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            Toast.makeText(EnclosureDetailActivity.this, path+" saved as a PDF file.", Toast.LENGTH_LONG).show();
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
//        float nexty = y ;
//        for(int i = 0; i <= rows; i++) {
//            drawLine(contentStream, margin, nexty, margin + tableWidth, nexty);
//            nexty -= rowHeight;
//        }
//
//        // 열을 그린다.
//        float nextx = margin;
//        for(int i = 0; i <= cols; i++) {
//            drawLine(contentStream, nextx, y, nextx, y - tableHeight);
//            nextx += colWidth;
//        }

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

    public Bitmap overlay(Bitmap originalBitmap, List<List<Float>> detectionResults) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        for (List<Float> result : detectionResults) {
            int x1 = (int) (float) result.get(0);
            int y1 = (int) (float) result.get(1);
            int x2 = (int) (float) result.get(2);
            int y2 = (int) (float) result.get(3);
            int classId = (int) (float) result.get(5);



            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(18);
            textPaint.setStyle(Paint.Style.FILL); // 배경 색상을 설정하기 위해 FILL로 설정
            textPaint.setColor(Color.RED);
            String class_name = "";
            if(classId > 19) {
                if(classId == 60) {
                    class_name = "No Gasket";
                }
                if (classId == 28) {
                    class_name = "No Grommet";
                }
                if (classId == 29) {
                    class_name = "No Grommet";
                }
                if (classId == 30) {
                    class_name = "No Grommet";
                }
                if (classId == 31) {
                    class_name = "No Grommet";
                }

                if (classId >= 20 && classId <= 27) {
                    class_name = "No Screw";
                }
                if (classId >= 32 && classId <= 39) {
                    class_name = "No Screw";
                }
            }


            float padding = 2;
            RectF backgroundRect = new RectF(x1 - padding, y1 - 20 - textPaint.getTextSize() - padding,
                    x1 + textPaint.measureText(class_name) + padding,
                    y1 - 20 + textPaint.getTextSize() + padding);


            if(!class_name.equals("")) {
                Rect rect = new Rect(x1, y1, x2, y2);
                canvas.drawRect(rect, paint);
                canvas.drawRect(backgroundRect, textPaint);
                textPaint.setColor(Color.WHITE); // 텍스트 색상 설정
                canvas.drawText(class_name, x1, y1 - 20, textPaint);
                runOnUiThread(() -> {
                    showCheckDialog();
                });
            }
        }
        return bitmap;
    }

    // ImageView에 오버레이된 이미지를 표시하는 메서드
    public static void displayOverlay(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public List<Bitmap> cropAndDisplay(Bitmap originalBitmap, List<List<Float>> detectionResults) {
        List<Bitmap> croppedImages = new ArrayList<>();
        croppedTitle = new ArrayList<>();

        for (List<Float> result : detectionResults) {
            int x1 = (int) (float) result.get(0);
            int y1 = (int) (float) result.get(1);
            int x2 = (int) (float) result.get(2);
            int y2 = (int) (float) result.get(3);
            int classId = (int) (float) result.get(5);

            String class_name = "";
            if(classId > 19) {
                if(classId == 60) {
                    class_name = "No Gasket";
                }
                if (classId == 28) {
                    class_name = "No Grommet";
                }
                if (classId == 29) {
                    class_name = "No Grommet";
                }
                if (classId == 30) {
                    class_name = "No Grommet";
                }
                if (classId == 31) {
                    class_name = "No Grommet";
                }

                if (classId >= 20 && classId <= 27) {
                    class_name = "No Screw";
                }
                if (classId >= 32 && classId <= 39) {
                    class_name = "No Screw";
                }
            }
            // 좌표로 이미지를 크롭
            Bitmap croppedImage = Bitmap.createBitmap(originalBitmap, x1, y1, x2 - x1, y2 - y1);
            if(class_name.equals("No Gasket")) {
                croppedTitle.add("3");
            }else if(class_name.equals("No Grommet")) {
                croppedTitle.add("2");
            }else if(class_name.equals("No Screw")) {
                croppedTitle.add("1");
            }
            if(!class_name.equals("")) {
                croppedImages.add(croppedImage);
            }
        }

        return croppedImages;
    }

    private List<Bitmap> createDummyCroppedImages(Bitmap originalBitmap, List<List<Float>> detectionResults) {
        // 여기서는 더미 데이터를 생성하고 있습니다. 실제로는 cropAndDisplay 함수를 사용하여 생성한 리스트를 사용하세요.
        // 예시로 5개의 더미 이미지를 생성하고 있습니다.
        // 실제로는 크롭된 이미지들의 리스트를 여기에 설정하세요.
        // (croppedImages = cropAndDisplay(originalBitmap, detectionResults); 와 같이 사용 가능합니다.)
        List<Bitmap> dummyImages = cropAndDisplay(originalBitmap, detectionResults);
        return dummyImages;
    }

    // 어댑터 클래스 정의
    private class ImageAdapter extends ArrayAdapter<Bitmap> {
        public ImageAdapter() {
            super(EnclosureDetailActivity.this, R.layout.list_item, croppedImages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            }
            ImageView imageView = convertView.findViewById(R.id.imageView);
            TextView textView = convertView.findViewById(R.id.textView);

            if(croppedTitle.size() == 0) {
//                textView.setTextColor(getResources().getColor(R.color.white));
//                textView.setTextSize(20f);
//                textView.setText("No Faults Detected");
            }else {
                String title = "";
                if(croppedTitle.get(position).equals("3")) {//gasket
                    title = "No Gasket";
                }else if(croppedTitle.get(position).equals("2")) {// grommet
                    title = "No Grommet";
                }else {//screw
                    title = "No Screw";
                }
                textView.setTextColor(getResources().getColor(R.color.red));
//                textView.setTextSize(20f);
                imageView.setImageBitmap(croppedImages.get(position));
                textView.setText(title);
            }

//            Collections.sort(croppedTitle);

            return convertView;
        }
    }

    public static String convertListToString(List<List<Float>> inputList) {
        Gson gson = new Gson();
        return gson.toJson(inputList);
    }

    public static List<List<Float>> convertStringToList(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<List<Float>>>(){}.getType();
        return gson.fromJson(jsonString, type);
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
        tv.setText("Enclosure Again");
        subTv.setText("Do you want to Enclosure again?");
    }

    public void showCheckDialog() {
        isAnomaly = true;
        tvCheck.setText("Corrective Actions Required");
        tvCheck.setTextColor(getResources().getColor(R.color.red));
        tvLine.setBackgroundColor(getResources().getColor(R.color.red));
        runOnUiThread(() -> {
            custom_check_dialog.show();
            custom_check_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
                custom_check_dialog.dismiss();
            });
            custom_check_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
                custom_check_dialog.dismiss();
                finish();
            });
        });

    }
}