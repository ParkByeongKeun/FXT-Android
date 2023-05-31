package com.example.fxt;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.fxt.spinner.NiceCoreSpinner;
import com.example.fxt.spinner.NiceSpinner;
import com.example.fxt.spinner.SpinnerTextFormatter;
import com.example.fxt.utils.C_Permission;
import com.example.fxt.utils.EncrytData;
import com.example.fxt.utils.FNMSData;
import com.example.fxt.utils.HistoryData;
import com.example.fxt.utils.TagTechList;
import com.example.fxt.utils.TagWrapper;
import com.example.fxt.utils.Utils;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FNMSTAGActivity extends MainAppcompatActivity {

    boolean mWriteMode = false;
    boolean mReadOK = false;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    static private ArrayList<TagWrapper> tags = new ArrayList<TagWrapper>();
    static private int currentTagIndex = -1;
    CustomApplication customApplication;
    private ArrayList<String> dataList;
    private ArrayList<HistoryData> historyList;
    private int mSelectTagNo = -1;
    IntentFilter[] writeTagFilters;
    private boolean mEditMode = false;
    private Button mEditButton;
    private String morgTagContent = "";
    ConstraintLayout rlList;
    RelativeLayout rlReadTag;
    String key = "Fiberfox230104";
    ActionBar mTitle;
    NdefRecord[] globalRecords;
    String strUnit = "UNIT#1";
    String selectLeft = "72C1";
    NiceSpinner spinnerLeft;
    NiceCoreSpinner spinnerRight;
    boolean isErrorCheck = false;
    Dialog custom_dialog;
    Dialog custom_completed_dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fnms_nfc_tag);
        C_Permission.checkPermission(this);
        customApplication = (CustomApplication)getApplication();
        final RippleBackground rippleBackground = findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
        findViewById(R.id.tvNeedHelp).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.fiberfox.co.kr/"));
            startActivity(intent);
        });
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        rlList = findViewById(R.id.rlList);
        rlReadTag = findViewById(R.id.rlReadTag);
        spinnerLeft = findViewById(R.id.spinnerLeft);
        spinnerRight = findViewById(R.id.spinnerRight);
        initDialog();
        initCompletedDialog();
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("FNMS NFC Tag");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        dataList = new ArrayList<String>();
        InitListArray();
        historyList = new ArrayList<HistoryData>();
        mEditButton = findViewById(R.id.butEdit);
        if (mNfcAdapter == null){
            Toast.makeText(this, "Your device does not support NFC. Cannot run demo.", Toast.LENGTH_LONG).show();
            finish();
        }
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED );
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};
    }

    private void initFNMS() {
        setSpinnerCustomRight();
        setSpinnerCustomCable1();
        setSpinnerCustomCable2();
        setSpinnerCustomUnit();
        setSpinnerCustomLeft();
    }

    private void InitListArray() {
        int i;
        String a;
        for(i=1;i<13;i++) {
            a = Integer.toString(i);
            dataList.add(a);
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String device = dataList.get(position);
        }
    };

    @Override
    public  void onBackPressed() {
        if(mWriteMode) {
            findViewById(R.id.tvWrite).setVisibility(View.GONE);
            mWriteMode = false;
            Button btn = findViewById(R.id.button2);
            btn.setText("Write");
        }else {
            finish();
        }
    }

    private HistoryData parsingTag(String msg) {
        String d;
        if(msg.contains("en")) {
            // remove 'en'
            d = msg.substring(3);
        }else {
            d = msg;
        }

        // split string by comma
        String[] datapa = d.split(",");
        HistoryData h = new HistoryData();
        h.mIndex = Integer.parseInt(datapa[0]);
        h.mStrTag = datapa[2];

        if (datapa[1].equalsIgnoreCase("P")) {
            h.mHist = 0;
        } else {
            if (datapa[1].equalsIgnoreCase("H1")) {
                h.mHist = 1;
            } else {
                h.mHist = 2;
            }
        }
        return h;
    }

    private void addDdata(String data) {
        HistoryData h = parsingTag(data);
        String viewdata;
        viewdata = h.mIndex + " " + h.mStrTag;
        historyList.add(h);
        if (h.mHist == 0) {
            dataList.set(h.mIndex - 1, viewdata);
//            dataAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        customApplication.isFNMSCheck = false;
        Log.i("FNMSActivity", "onResume: ");
        if (!mNfcAdapter.isEnabled()) {
            return;
        }

        if (mNfcPendingIntent == null) {
            mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        }
        showTag();
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
        if(customApplication.selectCore.equals("144C") && customApplication.isLevelCheck) {
            customApplication.selectCore ="144C";
            spinnerRight.setSelectedIndex(0);
            customApplication.isLevelCheck = false;
            editFNMSData(selectLeft,customApplication.selectCore);
            notifyFNMSData();
        }
        initFNMS();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    public void onClickEdit(View v) {
        Button btn = findViewById(R.id.button2);
        if(mWriteMode) {
            findViewById(R.id.tvWrite).setVisibility(View.GONE);
            mWriteMode = false;
            btn.setText("Write");
        }

        if(customApplication.arrBleAddress.size() != 0) {
            customApplication.isFNMSCheck = true;
            Intent intent = new Intent(FNMSTAGActivity.this, MainActivity.class);
            startActivity(intent);
        }else {
            customApplication.isFNMSCheck = true;
            Intent intent = new Intent(FNMSTAGActivity.this, AddDeviceActivity.class);
            startActivity(intent);
        }
    }

    private HistoryData GetTagInfoFromHistory(int SelectTagNo, int mHistory) {
        HistoryData obj;
        int iNo;
        int i;
        iNo = historyList.size();
        for(i=0;i<iNo;i++) {
            obj = historyList.get(i);
            if (obj.mIndex == SelectTagNo)
                if(obj.mHist == mHistory)
                    return obj;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickWrite(View v) {
        Button btn = findViewById(R.id.button2);
        if(mWriteMode) {
            findViewById(R.id.tvWrite).setVisibility(View.GONE);
            mWriteMode = false;
            btn.setText("Write");
        }else {
            int recNo = historyList.size();
            globalRecords = new NdefRecord[recNo];
            for(int i=0;i<recNo;i++) {
                try {
                    String encrypt = EncrytData.encrypt(historyList.get(i).getTagPayload());
                    globalRecords[i] = createRecord(encrypt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            btn.setText("Cancel");
            findViewById(R.id.tvWrite).setVisibility(View.VISIBLE);
            mWriteMode = true;
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, writeTagFilters, null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "Discovered tag with intent " + intent);
        if (mWriteMode) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(globalRecords == null)
                return;

            NdefMessage message = new NdefMessage(globalRecords);
            Ndef ndef = Ndef.get(tag);
            // Enable I/O
            if (ndef != null) {
                try {
                    ndef.connect();
                    // Write the message
                    NdefMessage oldMessage = ndef.getNdefMessage(); // 기존에 쓰여진 NdefMessage를 읽어옴
                    ndef.writeNdefMessage(message);
                    // Close the connection
                    ndef.close();
                    findViewById(R.id.tvWrite).setVisibility(View.GONE);
                    mWriteMode = false;
                    Button btn = findViewById(R.id.button2);
                    btn.setText("Write");
                    spinnerLeft.setTextColor(getResources().getColor(R.color.white));
                    spinnerRight.setTextColor(getResources().getColor(R.color.white));
                    showDialog();
//                    MessageDialog.show("Write completed.", "Would you like to send data to the server?", "YES","NO")
//                            .setOkButtonClickListener((dialog, v) -> {
//                                TipDialog.show("Send completed.", WaitDialog.TYPE.SUCCESS,2000);
//                                return false;
//                            });
                    notifyFNMSData();
                } catch (IOException | FormatException e) {
                    Log.d("yot132","error = " + e);
                    Toast.makeText(getApplicationContext(),"Failed NFC tag.(please try again)1",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }else {
                NdefFormatable ndefFormatable = NdefFormatable.get(tag);
                if (ndefFormatable != null) {
                    // initialize tag with new NDEF message
                    try {
                        ndefFormatable.connect();
                        ndefFormatable.format(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Failed NFC tag.(please try again)2",Toast.LENGTH_SHORT).show();
                    } catch (FormatException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            ndefFormatable.close();
                        } catch (Exception e) {}
                    }
                }
            }
        } else {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String tagId = Utils.bytesToHex(tag.getId());
            TagWrapper tagWrapper = new TagWrapper(tagId);
            ArrayList<String> misc = new ArrayList<String>();
            misc.add("scanned at: " + Utils.now());
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            String tagData = "";

            if (rawMsgs != null) {

                NdefMessage msg = (NdefMessage) rawMsgs[0];
                NdefRecord cardRecord = msg.getRecords()[0];
                try {
                    tagData = readRecord(cardRecord.getPayload());
                } catch (UnsupportedEncodingException e) {
                    Log.e("TagScan", e.getMessage());
                    return;
                }
            }


            misc.add("tag data: " + tagData);
            tagWrapper.techList.put("Misc", misc);

            for (String tech : tag.getTechList()) {
                tech = tech.replace("android.nfc.tech.", "");
                List<String> info = getTagInfo(tag, tech);
                tagWrapper.techList.put("Technology: " + tech, info);
            }

            if(isErrorCheck) {
                Toast.makeText(getApplicationContext(),"Failed NFC tag.(please try again)",Toast.LENGTH_SHORT).show();
                isErrorCheck = false;
                return;
            }else {
                if (tags.size() == 1) {
//                    Toast.makeText(this, "Swipe right to see previous tags", Toast.LENGTH_LONG).show();
                }
                tags.add(tagWrapper);
                currentTagIndex = tags.size() - 1;
                showTag();
                rlList.setVisibility(View.VISIBLE);
                rlReadTag.setVisibility(View.GONE);
                notifyFNMSData();
                mReadOK = true;
            }
        }
    }

    public void notifyFNMSData() {
        customApplication.fnmsDataList.clear();
        for(int i = 0 ; i < historyList.size() ; i ++) {
            String device = historyList.get(i).mStrTag;
            String[] array = device.split("]");
            ArrayList<String> list1 = new ArrayList<>();
            ArrayList<String> list2 = new ArrayList<>();
            for(int j = 0 ; j < array.length ; j ++) {
                if(j%3 == 1) {
                    String[] spl = array[1].split("\\[");
                    list1.add(spl[0]);
                }else if(j%3 == 2) {
                    list2.add(array[j]);
                }
            }
            if(list1.size() > 0) {
                String left = list1.get(0);
                String right = "";
                if(list2.size() > 0) {
                    right = list2.get(0);

                }
                customApplication.fnmsDataList.add(new FNMSData(left.replaceAll("\\s",""),right.replaceAll("\\s","")));
            }
        }
    }
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }

    String readRecord(byte[] payload) throws UnsupportedEncodingException {
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        int languageCodeLength = payload[0] & 63;

        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    private void showPreviousTag() {
        if (--currentTagIndex < 0) currentTagIndex = tags.size() - 1;

        showTag();
    }

    private void showNextTag() {
        if (++currentTagIndex >= tags.size()) currentTagIndex = 0;

        showTag();
    }

    private void showTag() {
        if (tags.size() == 0) return;
        final TagWrapper tagWrapper = tags.get(currentTagIndex);
        final TagTechList techList = tagWrapper.techList;
        /*mNFCReadView.setText("Tag " + tagWrapper.getId() +
                " (" + (currentTagIndex+1) + "/" + tags.size() + ")");*/
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private final List<String> getTagInfo(final Tag tag, final String tech) {
        List<String> info = new ArrayList<String>();

        switch (tech) {
            case "NfcA":
                info.add("aka ISO 14443-3A");

                NfcA nfcATag = NfcA.get(tag);
                info.add("atqa: " + Utils.bytesToHexAndString(nfcATag.getAtqa()));
                info.add("sak: " + nfcATag.getSak());
                info.add("maxTransceiveLength: " + nfcATag.getMaxTransceiveLength());
                break;

            case "NfcF":
                info.add("aka JIS 6319-4");

                NfcF nfcFTag = NfcF.get(tag);
                info.add("manufacturer: " + Utils.bytesToHex(nfcFTag.getManufacturer()));
                info.add("systemCode: " + Utils.bytesToHex(nfcFTag.getSystemCode()));
                info.add("maxTransceiveLength: " + nfcFTag.getMaxTransceiveLength());
                break;

            case "NfcV":
                info.add("aka ISO 15693");

                NfcV nfcVTag = NfcV.get(tag);
                info.add("dsfId: " + nfcVTag.getDsfId());
                info.add("responseFlags: " + nfcVTag.getResponseFlags());
                info.add("maxTransceiveLength: " + nfcVTag.getMaxTransceiveLength());
                break;

            case "Ndef":
                Ndef ndefTag = Ndef.get(tag);
                NdefMessage ndefMessage = null;

                try {
                    ndefTag.connect();
                    ndefMessage = ndefTag.getNdefMessage();
                    ndefTag.close();
                    dataList.clear();
                    InitListArray();
                    historyList.clear();

                    try {
                        for (final NdefRecord record : ndefMessage.getRecords()) {
                            String decrypt = "";
                            try {
                                decrypt = EncrytData.decrypt(Utils.bytesToString(record.getPayload()).substring(3));
                                final String id = record.getId().length == 0 ? "null" : Utils.bytesToHex(record.getId());
                                info.add("record[" + id + "].tnf: " + record.getTnf());
                                info.add("record[" + id + "].type: " + Utils.bytesToHexAndString(record.getType()));
                                info.add("record[" + id + "].payload: " + Utils.bytesToHexAndString(record.getPayload()));
                                Log.d("yot132","decrypt = " + decrypt);
                                addDdata(decrypt);
                            } catch (Exception e) {
                                final String id = record.getId().length == 0 ? "null" : Utils.bytesToHex(record.getId());
                                info.add("record[" + id + "].tnf: " + record.getTnf());
                                info.add("record[" + id + "].type: " + Utils.bytesToHexAndString(record.getType()));
                                info.add("record[" + id + "].payload: " + Utils.bytesToHexAndString(record.getPayload()));
                                addDdata(Utils.bytesToString(record.getPayload()));
                                e.printStackTrace();
                                isErrorCheck = true;
                            }
                        }
                    }catch (NullPointerException e) {
                        e.printStackTrace();
                        isErrorCheck = true;
                    }

                    info.add("messageSize: " + ndefMessage.getByteArrayLength());

                } catch (final Exception e) {
                    e.printStackTrace();
//                    isErrorCheck = true;
                    info.add("error reading message: " + e.toString());
                }

                HashMap<String, String> typeMap = new HashMap<String, String>();
                typeMap.put(Ndef.NFC_FORUM_TYPE_1, "typically Innovision Topaz");
                typeMap.put(Ndef.NFC_FORUM_TYPE_2, "typically NXP MIFARE Ultralight");
                typeMap.put(Ndef.NFC_FORUM_TYPE_3, "typically Sony Felica");
                typeMap.put(Ndef.NFC_FORUM_TYPE_4, "typically NXP MIFARE Desfire");

                String type = ndefTag.getType();
                if (typeMap.get(type) != null) {
                    type += " (" + typeMap.get(type) + ")";
                }
                info.add("type: " + type);

                info.add("canMakeReadOnly: " + ndefTag.canMakeReadOnly());
                info.add("isWritable: " + ndefTag.isWritable());
                info.add("maxSize: " + ndefTag.getMaxSize());
                break;

            case "NdefFormatable":
                info.add("nothing to read");

                break;

            case "MifareUltralight":
                MifareUltralight mifareUltralightTag = MifareUltralight.get(tag);
                info.add("type: " + mifareUltralightTag.getType());
                info.add("tiemout: " + mifareUltralightTag.getTimeout());
                info.add("maxTransceiveLength: " + mifareUltralightTag.getMaxTransceiveLength());
                break;

            case "IsoDep":
                info.add("aka ISO 14443-4");

                IsoDep isoDepTag = IsoDep.get(tag);
                info.add("historicalBytes: " + Utils.bytesToHexAndString(isoDepTag.getHistoricalBytes()));
                info.add("hiLayerResponse: " + Utils.bytesToHexAndString(isoDepTag.getHiLayerResponse()));
                info.add("timeout: " + isoDepTag.getTimeout());
                info.add("extendedLengthApduSupported: " + isoDepTag.isExtendedLengthApduSupported());
                info.add("maxTransceiveLength: " + isoDepTag.getMaxTransceiveLength());
                break;

            default:
                info.add("unknown tech!");
        }

        return info;
    }

//    class ListTableAdapter extends BaseAdapter {
//        Context context;
//        List<String> devices;
//        LayoutInflater inflater;
//        private ArrayList<TextView> item;
//
//        public ListTableAdapter(Context context, List<String> devices) {
//            this.context = context;
//            inflater = LayoutInflater.from(context);
//            this.devices = devices;
//            item = new ArrayList<>();
//        }
//
//        @Override
//        public int getCount() {
//            return devices.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return devices.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewGroup vg;
//
//            if (convertView != null) {
//                vg = (ViewGroup) convertView;
//            } else {
//                vg = (ViewGroup) inflater.inflate(R.layout.fnms_data_element, null);
//            }
//
//            String device = devices.get(position);
//            final TextView tv_data1 = ((TextView) vg.findViewById(R.id.disp_data));
//            final TextView tv_data2 = ((TextView) vg.findViewById(R.id.disp_data2));
//
//            String[] array = device.split("]");
//            ArrayList<String> list1 = new ArrayList<>();
//            ArrayList<String> list2 = new ArrayList<>();
//            for(int i = 0 ; i < array.length ; i ++) {
//                if(i%3 == 1) {
//                    String[] spl = array[1].split("\\[");
//                    list1.add(spl[0]);
//                }else if(i%3 == 2) {
//                    list2.add(array[i]);
//                }
//            }
//            if(list1.size() > 0) {
//                tv_data1.setText(list1.get(0));
//                if(list2.size() > 0) {
//                    tv_data2.setText(list2.get(0));
//                }else {
//                    tv_data2.setText("");
//                }
//
//            }
//
//            DisplayMetrics dm = getResources().getDisplayMetrics();
//            int padding = Math.round(7 * dm.density);
//            tv_data2.setOnClickListener(v -> {
//                item.add(tv_data2);
//                if(item.size() >= 2) {
//                    for(int i = 0 ; i < item.size() ; i++) {
//                        item.get(i).setBackground(getDrawable(R.drawable.rounded_tag));
//                        item.get(i).setPadding(padding,padding,padding,padding);
//                    }
//                }
//                tv_data2.setBackground(getDrawable(R.drawable.rounded_tag_select));
//                tv_data2.setPadding(padding,padding,padding,padding);
//                this.notifyDataSetChanged();
////                clickStr1 = tv_data1.getText().toString();
////                clickStr2 = tv_data2.getText().toString();
//
//                if(device.length() < 4) {
//                    String aa = position + 1 + " Selected";
//                    Log.d("FNMS(ClickListner)1", aa);
//                    mSelectTagNo = position + 1;
//
////                mNFCReadView.setText("P : " );
//                } else {
//                    String[] tagContent = device.split(" ");
//                    Log.d("FNMS(ClickListner)2", device.length() + " " +device);
//                    Log.d("FNMS(ClickListner)2", tagContent[0]);
//
//
//                    Log.d("FNMS(ClickListner)2", tagContent[1]);
//                    GlobalVariable.gNFCData = tagContent[1];
////                mNFCReadView.setText("P : " + tagContent[1]);
//                    //////////////////////////////////////////////////////////////////////////
//                    // Need to Modify
//                    // tagContent 저장 필요
//                    // tagContent ==? [xxxx]1234[xxxx]5678
//                    // UI 상으로 하나의 EditBox로는 힘듬
//                    // 두개로 ....
//                    // 저장시 다시...
//                    morgTagContent = tagContent[1];
//
//                    //String newTagContent[] = MakeShortContent(aaa);
//                    String[] strSplit = tagContent[1].split("[\\[\\]]+");
////                    Log.d("FNMS(ClickListner)3", strSplit[2]);
////                    Log.d("FNMS(ClickListner)3", strSplit[4]);
//
//
//
//                    ////////////////////////////////////////////////////////////////////
//                }
//
//
//                mSelectTagNo = position + 1;
//                HistoryData tagHistory1 = GetTagInfoFromHistory(mSelectTagNo, 1);
//                HistoryData tagHistory2 = GetTagInfoFromHistory(mSelectTagNo, 2);
//
//                if (tagHistory1 != null) {
////                mNFCReadView.append("\n H1 : ");
////                mNFCReadView.append(tagHistory1.mStrTag);
//                }
//                if (tagHistory2 != null) {
////                mNFCReadView.append("\n H2 : ");
////                mNFCReadView.append(tagHistory2.mStrTag);
//                }
//            });
//
//            tv_data1.setOnClickListener(v -> {
//                item.add(tv_data1);
//                if(item.size() >= 2) {
//                    for(int i = 0 ; i < item.size() ; i++) {
//                        item.get(i).setBackground(getDrawable(R.drawable.rounded_tag));
//                        item.get(i).setPadding(padding,padding,padding,padding);
//                    }
//                }
//                tv_data1.setBackground(getDrawable(R.drawable.rounded_tag_select));
//                tv_data1.setPadding(padding,padding,padding,padding);
//                this.notifyDataSetChanged();
////                clickStr2 = tv_data2.getText().toString();
////                clickStr1 = tv_data1.getText().toString();
//
//                if(device.length() < 4) {
//                    String aa = position + 1 + " Selected";
//                    Log.d("FNMS(ClickListner)1", aa);
//                    mSelectTagNo = position + 1;
//
//
////                mNFCReadView.setText("P : " );
//                } else {
//                    String[] tagContent = device.split(" ");
//                    Log.d("FNMS(ClickListner)2", device.length() + " " +device);
//                    Log.d("FNMS(ClickListner)2", tagContent[0]);
//
//
//                    Log.d("FNMS(ClickListner)2", tagContent[1]);
//                    GlobalVariable.gNFCData = tagContent[1];
//                    morgTagContent = tagContent[1];
//
//                    //String newTagContent[] = MakeShortContent(aaa);
//                    String[] strSplit = tagContent[1].split("[\\[\\]]+");
////                    Log.d("FNMS(ClickListner)3", strSplit[2]);
////                    Log.d("FNMS(ClickListner)3", strSplit[4]);
//                }
//
//
//                mSelectTagNo = position + 1;
//                HistoryData tagHistory1 = GetTagInfoFromHistory(mSelectTagNo, 1);
//                HistoryData tagHistory2 = GetTagInfoFromHistory(mSelectTagNo, 2);
//
//                if (tagHistory1 != null) {
////                mNFCReadView.append("\n H1 : ");
////                mNFCReadView.append(tagHistory1.mStrTag);
//                }
//                if (tagHistory2 != null) {
////                mNFCReadView.append("\n H2 : ");
////                mNFCReadView.append(tagHistory2.mStrTag);
//                }
//            });
//            return vg;
//        }
//    }


//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public static String encryptAES256(String msg, String key) throws Exception {
//        SecureRandom random = new SecureRandom();
//        byte bytes[] = new byte[20];
//        random.nextBytes(bytes);
//        byte[] saltBytes = bytes;
//
//        // Password-Based Key Derivation function 2
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//
//        // 70000번 해시하여 256 bit 길이의 키를 만든다.
//        PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), saltBytes, 1, 256);
////        SecretKey secureKey = new SecretKeySpec(keyData, "AES");
//
//        SecretKey secretKey = factory.generateSecret(spec);
//        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
//
//        // 알고리즘/모드/패딩
//        // CBC : Cipher Block Chaining Mode
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        cipher.init(Cipher.ENCRYPT_MODE, secret);
//        AlgorithmParameters params = cipher.getParameters();
//
//        // Initial Vector(1단계 암호화 블록용)
//        byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
//
//        byte[] encryptedTextBytes = cipher.doFinal(msg.getBytes("UTF-8"));
//
//        byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];
//        System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
//        System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
//        System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length, encryptedTextBytes.length);
//
//        return Base64.getEncoder().encodeToString(buffer);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public static String decryptAES256(String msg, String key) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(msg));
//
//        byte[] saltBytes = new byte[20];
//        buffer.get(saltBytes, 0, saltBytes.length);
//        byte[] ivBytes = new byte[cipher.getBlockSize()];
//        buffer.get(ivBytes, 0, ivBytes.length);
//        byte[] encryoptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes.length];
//        buffer.get(encryoptedTextBytes);
//
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//        PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), saltBytes, 1, 256);
//
//        SecretKey secretKey = factory.generateSecret(spec);
//        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
//
//        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));
//
//        byte[] decryptedTextBytes = cipher.doFinal(encryoptedTextBytes);
//        return new String(decryptedTextBytes);
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setSpinnerCustomCable1() {
        final NiceSpinner spinner = findViewById(R.id.spinnerCable1);
        List<String> data = new ArrayList<>();
        data.add("100001239183");
        data.add("100001239183");
        SpinnerTextFormatter textFormatter = (SpinnerTextFormatter<String>) data1 -> new SpannableString(data1);
        spinner.setTextColor(getResources().getColor(R.color.white));
        spinner.setSpinnerTextFormatter(textFormatter);
        spinner.setSelectedTextFormatter(textFormatter);
        spinner.setBackgroundColor(getColor(R.color.dark_gray));
        spinnerRight.setBackgroundColor(getColor(R.color.dark_gray));
        spinner.setPadding(10,20,10,20);
        spinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            String data12 = (String) spinner.getSelectedItem();
//            Toast.makeText(FNMSTAGActivity.this, "Selected: " + data12, Toast.LENGTH_SHORT).show();
        });
        spinner.attachDataSource(data);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setSpinnerCustomCable2() {
        final NiceSpinner spinner = findViewById(R.id.spinnerCable2);
        List<String> data = new ArrayList<>();
        data.add("601185866");
        data.add("601185866");
        SpinnerTextFormatter textFormatter = (SpinnerTextFormatter<String>) data1 -> new SpannableString(data1);
        spinner.setTextColor(getResources().getColor(R.color.white));
        spinner.setSpinnerTextFormatter(textFormatter);
        spinner.setSelectedTextFormatter(textFormatter);
        spinner.setBackgroundColor(getColor(R.color.dark_gray));
        spinner.setPadding(10,20,10,20);
        spinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            String data12 = (String) spinner.getSelectedItem();
//            Toast.makeText(FNMSTAGActivity.this, "Selected: " + data12, Toast.LENGTH_SHORT).show();
        });
        spinner.attachDataSource(data);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setSpinnerCustomUnit() {
        final NiceSpinner spinner = findViewById(R.id.spinnerUnit);
        List<String> data = new ArrayList<>();
        for(int i = 0 ; i < 12 ; i++) {
            data.add("UNIT#"+ (i +1));
        }
        SpinnerTextFormatter textFormatter = (SpinnerTextFormatter<String>) data1 -> new SpannableString(data1);
        spinner.setTextColor(getResources().getColor(R.color.white));
        spinner.setSpinnerTextFormatter(textFormatter);
        spinner.setSelectedTextFormatter(textFormatter);
        spinner.setBackgroundColor(getColor(R.color.dark_gray));
        spinner.setPadding(10,20,10,20);
        spinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            String data12 = (String) spinner.getSelectedItem();
            strUnit = data12;
            setSpinnerCustomLeft();
            setSpinnerCustomRight();
            switch (position) {
                case 0 : {
                    selectLeft = "72C1";
                    break;
                }
                case 1 : {
                    selectLeft = "72C13";
                    break;
                }
                case 2 : {
                    selectLeft = "72C25";
                    break;
                }
                case 3 : {
                    selectLeft = "72C37";
                    break;
                }
                case 4 : {
                    selectLeft = "72C49";
                    break;
                }
                case 5 : {
                    selectLeft = "72C61";
                    break;
                }
                case 6 : {
                    selectLeft = "72C73";
                    break;
                }
                case 7 : {
                    selectLeft = "72C85";
                    break;
                }
                case 8 : {
                    selectLeft = "72C97";
                    break;
                }
                case 9 : {
                    selectLeft = "72C109";
                    break;
                }
                case 10 : {
                    selectLeft = "72C121";
                    break;
                }
                case 11 : {
                    selectLeft = "72C133";
                    break;
                }
                default: {
                    break;
                }
            }
            checkCore();
        });
        checkCore();
        boolean isCheck = false;
        for(int i = 0 ; i < customApplication.fnmsDataList.size() ; i ++) {
            if(selectLeft.equals(customApplication.fnmsDataList.get(i).getLeft())) {
                isCheck = true;
            }
        }
        if(!isCheck) {
            customApplication.selectCore ="144C";
            spinnerRight.setSelectedIndex(0);
            spinnerRight.setTextColor(getResources().getColor(R.color.red));
//                spinnerRight.setData();
        }
        notifyFNMSData();

        spinner.attachDataSource(data);
    }
    public void checkCore() {
        for(int i = 0 ; i < customApplication.fnmsDataList.size() ; i ++) {
            if(customApplication.fnmsDataList.get(i).getLeft().equals(selectLeft)) {
                if(customApplication.fnmsDataList.get(i).getRight().contains("144C")) {
                    String[] splRight = customApplication.fnmsDataList.get(i).getRight().split("C");
                    if(splRight.length <= 1) {
                        return;
                    }
                    spinnerRight.setSelectedIndex(Integer.parseInt(splRight[1]));
                    spinnerRight.setTextColor(getResources().getColor(R.color.white));
                    customApplication.selectCore = customApplication.fnmsDataList.get(i).getRight();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setSpinnerCustomLeft() {
        List<String> data = new ArrayList<>();
        int first = 0;
        int end = 0;
        spinnerLeft.setTextColor(getResources().getColor(R.color.white));
        if(strUnit.equals("UNIT#1")) {
            first = 0;
            end = 12;
        }else if(strUnit.equals("UNIT#2")) {
            first = 12;
            end = 24;
        }else if(strUnit.equals("UNIT#3")) {
            first = 24;
            end = 36;
        }else if(strUnit.equals("UNIT#4")) {
            first = 36;
            end = 48;
        }else if(strUnit.equals("UNIT#5")) {
            first = 48;
            end = 60;
        }else if(strUnit.equals("UNIT#6")) {
            first = 60;
            end = 72;
        }else if(strUnit.equals("UNIT#7")) {
            first = 72;
            end = 84;
        }else if(strUnit.equals("UNIT#8")) {
            first = 84;
            end = 96;
        }else if(strUnit.equals("UNIT#9")) {
            first = 96;
            end = 108;
        }else if(strUnit.equals("UNIT#10")) {
            first = 108;
            end = 120;
        }else if(strUnit.equals("UNIT#11")) {
            first = 120;
            end = 132;
        }else if(strUnit.equals("UNIT#12")) {
            first = 132;
            end = 144;
        }else {
            first = -1;
        }
        if(first == -1) {
//            data.add("72C");
        }else {
            for(int i = first ; i < end ; i++) {
                data.add("72C"+ (i +1));
            }
        }

        SpinnerTextFormatter textFormatter = (SpinnerTextFormatter<String>) data1 -> new SpannableString(data1);
        spinnerLeft.setSpinnerTextFormatter(textFormatter);
        spinnerLeft.setSelectedTextFormatter(textFormatter);
        spinnerLeft.setBackgroundColor(getColor(R.color.dark_gray));
        spinnerLeft.setPadding(10,20,10,20);
        spinnerLeft.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {

            String data12 = (String) spinnerLeft.getSelectedItem();
            selectLeft = data12;
            int textColor = getResources().getColor(R.color.white);
//            spinnerRight.deleteData = "";
            for(int i = 0 ; i < customApplication.fnmsDataList.size() ; i ++) {
                if(customApplication.fnmsDataList.get(i).getLeft().equals(selectLeft)) {
                    if(customApplication.fnmsDataList.get(i).getRight().contains("144C")) {
                        String[] splRight = customApplication.fnmsDataList.get(i).getRight().split("C");

                        if(splRight.length <= 1) {
                            return;
                        }
                        spinnerRight.setSelectedIndex(Integer.parseInt(splRight[1]));
                        spinnerRight.setTextColor(getResources().getColor(R.color.white));
                        customApplication.selectCore = spinnerRight.getSelectedItem().toString();
//                        spinnerRight.setData();
//                        spinnerRight.deleteData = customApplication.selectCore;
                    }
                }
            }
            spinnerLeft.setTextColor(textColor);
            boolean isCheck = false;
            for(int i = 0 ; i < customApplication.fnmsDataList.size() ; i ++) {
                if(selectLeft.equals(customApplication.fnmsDataList.get(i).getLeft())) {
                    isCheck = true;
                }
            }
            if(!isCheck) {
                customApplication.selectCore ="144C";
                spinnerRight.setSelectedIndex(0);
                spinnerRight.setTextColor(getResources().getColor(R.color.red));
//                spinnerRight.setData();
            }
            notifyFNMSData();
        });
        spinnerLeft.attachDataSource(data);
//        String[] splLeft = selectLeft.split("C");
//        spinnerLeft.setSelectedIndex(Integer.parseInt(splLeft[1])-1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setSpinnerCustomRight() {
        spinnerRight.setTextColor(getResources().getColor(R.color.red));
        List<String> data = new ArrayList<>();
        data.add("144C");
        for(int i = 0 ; i < 144 ; i++) {
            data.add("144C"+ (i +1));
        }

        SpinnerTextFormatter textFormatter = (SpinnerTextFormatter<String>) data1 -> new SpannableString(data1);
        spinnerRight.setSpinnerTextFormatter(textFormatter);
        spinnerRight.setSelectedTextFormatter(textFormatter);
        spinnerRight.setBackgroundColor(getColor(R.color.dark_gray));
        spinnerRight.setPadding(10,20,10,20);
        spinnerRight.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            String data12 = (String) spinnerRight.getSelectedItem();
            for(int i = 0 ; i < customApplication.fnmsDataList.size() ; i ++) {
                if(customApplication.fnmsDataList.get(i).getRight().equals(data12)) {
                    if(customApplication.fnmsDataList.get(i).getLeft().contains("72C")) {
                        if(!customApplication.fnmsDataList.get(i).getRight().equals(customApplication.selectCore)) {
//                            TipDialog.show("alerady core("+customApplication.fnmsDataList.get(i).getLeft()+")", WaitDialog.TYPE.ERROR,2000);
                            showCompletedDialog("alerady core");
                            spinnerRight.setTextColor(getResources().getColor(R.color.red));
                            Button btn = findViewById(R.id.button2);
                            btn.setEnabled(false);
                        }else {
                            customApplication.selectCore = data12;
                            editFNMSData(selectLeft,data12);
                            spinnerRight.setTextColor(getResources().getColor(R.color.white));
                            Button btn = findViewById(R.id.button2);
                            btn.setEnabled(true);
                        }
                        return;
                    }
                }
            }
            Button btn = findViewById(R.id.button2);
            btn.setEnabled(true);
            if(position != 0) {
                spinnerRight.setTextColor(getResources().getColor(R.color.white));
            }
            editFNMSData(selectLeft,data12);
            notifyFNMSData();
        });
        spinnerRight.attachDataSource(data);
    }

    public void editFNMSData(String left, String right) {
//        if(left.equals("72C") || right.equals("144C")) {
//            return;
//        }
        if(left.equals("72C")) {
            return;
        }
        if(right.equals("144C")) {
            right = "";
//            customApplication.selectCore ="144C";
//            spinnerRight.setSelectedIndex(0);
//            spinnerRight.setTextColor(getResources().getColor(R.color.red));
        }
        // Add Data to History
        // 1. P 저장
//            dialog.setInputHintText()
        int mSaveTagNo = mSelectTagNo;

//        String strTag1;
//        String strTag2;
//        if(inputStr.equals("")) {
//            Toast.makeText(getApplicationContext(),"Input Data",Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        if (isClickFirst) {
//            strTag1 = inputStr;
//            strTag2 = clickStr2;
//        }else {
//            strTag1 = clickStr1;
//            strTag2 = inputStr;
//        }
//            clickStr1 = null;
//            clickStr2 = null;
//            dataAdapter = new ListTableAdapter(getApplicationContext(), dataList);
//            ListView newDevicesListView = (ListView) findViewById(R.id.fnms_list);
//            newDevicesListView.setAdapter(dataAdapter);
        String writeLeft = left;
        String writeRight = right;
        int index = 3; // 5번째 인덱스에 공백 추가
        writeLeft = writeLeft.substring(0, index) + " " + writeLeft.substring(index); // 공백 추가
        index = 4;
        if(right != "") {
            writeRight = writeRight.substring(0, index) + " " + writeRight.substring(index); // 공백 추가
            Log.d("yot132","le = " + writeLeft + ", " + writeRight);
        }


        String[] strSplit = morgTagContent.split("[\\[\\]]+");
        StringBuffer strNewBuf = new StringBuffer();
        String newTag;

        strNewBuf.append("[");
        strNewBuf.append("100001239183");
        strNewBuf.append("]");
        strNewBuf.append(writeLeft);
        strNewBuf.append("[");
        strNewBuf.append("601185866");
        strNewBuf.append("]");
        strNewBuf.append(writeRight);
        newTag = strNewBuf.toString();
        HistoryData tagPrimary = GetTagInfoFromHistory(mSaveTagNo, 0);
        String[] splLeft = writeLeft.split("C");

        tagPrimary = new HistoryData();
        if(splLeft.length <= 1)
            return;
        tagPrimary.mIndex = 1;
        tagPrimary.mHist = 0;
        tagPrimary.mStrTag = newTag;
        boolean isCheck = false;
        for(int i = 0 ; i < historyList.size() ; i ++) {
            if(writeRight.equals("")) {
                if(historyList.get(i).mStrTag.contains(writeLeft)) {
                    historyList.remove(i);
                    return;
                }
            }
            if(historyList.get(i).mStrTag.contains(writeLeft)) {
                historyList.set(i,tagPrimary);
                isCheck = true;
            }
        }
        if(!isCheck) {
            historyList.add(tagPrimary);
        }
        mEditMode = false;
    }


    public void initDialog() {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_base);
        ImageView iv = custom_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_pop_write);
        TextView tv = custom_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_dialog.findViewById(R.id.tvSubTitle);
        tv.setText("Write Completed");
        subTv.setText("Would you like to send data to server?");
    }

    public void showDialog() {
        custom_dialog.show();
        custom_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            custom_dialog.dismiss();
            runOnUiThread(() -> {
                showCompletedDialog("Send Completed.");
            });
        });
    }


    public void initCompletedDialog() {
        custom_completed_dialog = new Dialog(this);
        custom_completed_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_completed_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_completed_dialog.setContentView(R.layout.custom_dialog_completed);

    }

    public void showCompletedDialog(String title) {
        TextView tv = custom_completed_dialog.findViewById(R.id.tvTitle);
        tv.setText(title);
        custom_completed_dialog.show();
        custom_completed_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            custom_completed_dialog.dismiss();
        });
    }
}