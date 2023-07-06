package com.example.fxt.ble.util;

import static com.example.fxt.ble.api.util.ByteUtil.getAsciiString;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import com.example.fxt.CustomApplication;
import com.example.fxt.GpsTracker;
import com.example.fxt.OfiInfoActivity;
import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.api.util.BleHexConvert;
import com.example.fxt.ble.device.splicer.bean.FiberBean;
import com.example.fxt.ble.device.splicer.bean.InfoBean;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.example.fxt.utils.Byte2Image;
import com.example.fxt.utils.FileUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 熔接机上报数据解析工具类
 */
public class SpliceDataParseUtil {

    /**
     * 解析数据
     *
     * @param spliceDataBean bean
     * @param bleDataBean 熔接机上报的数据
     * @return bean
     */

    static Context mContext;

    public static InfoBean parseInfo(InfoBean spliceDataBean, BleResultBean bleDataBean) {
        if (spliceDataBean == null){
            spliceDataBean = new InfoBean();
        }

        String strJson = getAsciiString(bleDataBean.getPayload(),0,bleDataBean.getPayload().length);
        if (strJson == null){
            return spliceDataBean;
        }

        spliceDataBean.setId(bleDataBean.getIdStr());
        // 设置接收时间 更新时间

        try {
            // 最外层的JSONObject对象
            JSONObject object = new JSONObject(strJson);

            // 通过MINFO字段获取其所包含的JSONObject对象
            spliceDataBean.setSn(object.getString("SN"));
            spliceDataBean.setMachineTypeMarket(object.getString("machineTypeMarket"));
            spliceDataBean.setMachineSoftVersion(object.getString("machineSoftVersion"));
            spliceDataBean.setBlueToothMAC(object.getString("blueToothMAC"));
            spliceDataBean.setActivateStatus(object.getString("activateStatus"));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return spliceDataBean;
    }

    private static CustomApplication customApplication;
    static long mNow;
    static Date mDate;
    static SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    public static SpliceDataBean parseSpliceData(Context context,SpliceDataBean spliceDataBean, BleResultBean bleDataBean) {
        if (spliceDataBean == null){
            spliceDataBean = new SpliceDataBean();
        }
        customApplication = (CustomApplication) context.getApplicationContext();
        mContext = context;
        String strJson = getAsciiString(bleDataBean.getPayload(),0,bleDataBean.getPayload().length);
        if (strJson == null){
            return spliceDataBean;
        }

        spliceDataBean.setId(bleDataBean.getIdStr());
        // 设置接收时间 更新时间
        spliceDataBean.setCreateTime(new Date());
        spliceDataBean.setUpdateTime(new Date());
        Log.d("yot132","@#@#@@@@@@@@121212  = = = " + strJson.substring(4));
        try {
            // 最外层的JSONObject对象
            JSONObject object = new JSONObject(strJson.substring(4));

            GpsTracker gpsTracker = new GpsTracker(context);
            String address = getAddress(gpsTracker.getLatitude(),gpsTracker.getLongitude());
            Log.d("yot132","tracker = " + gpsTracker.getLatitude() +"," +gpsTracker.getLongitude());

            Log.d("yot132","123 = " + object);
            // 通过MINFO字段获取其所包含的JSONObject对象
//            JSONObject minfo = object.getJSONObject("MINFO");
            spliceDataBean.setSn(object.getString("serial_num"));
            spliceDataBean.setAppVer(customApplication.swVersion);
            spliceDataBean.setFpgaVer(address);//@@@@@@
            spliceDataBean.setManufacturer(object.getString("cur_arc_cnt"));
            spliceDataBean.setBrand(object.getString("total_arc_cnt"));
            spliceDataBean.setModel(object.getString("module"));

            FiberBean fiberBean = new FiberBean();
            // 通过RESULT字段获取其所包含的JSONObject对象
            JSONObject result = object.getJSONObject("fiber_1");
            fiberBean.setSpliceResult(0);//@@@@@@
            fiberBean.setErrorValue(result.getInt("err"));

            // 通过FUSION字段获取其所包含的JSONObject对象
            JSONObject fiber_1 = object.getJSONObject("fiber_1");
            spliceDataBean.setSpliceName(object.getString("splice_mode"));
            spliceDataBean.setDataTime(getTime());
            fiberBean.setLoss(fiber_1.getString("loss"));
            fiberBean.setLeftAngle(Float.parseFloat(Double.toString(fiber_1.getDouble("left_angle"))));
            fiberBean.setRightAngle(Float.parseFloat(Double.toString(fiber_1.getDouble("right_angle"))));
            fiberBean.setCoreAngle(Float.parseFloat(Double.toString(fiber_1.getDouble("core_angle"))));//@@@@@@
            fiberBean.setCoreOffset(Float.parseFloat(Double.toString(fiber_1.getDouble("core_offset"))));//@@@@@@
            spliceDataBean.setFiberBean(fiberBean);

            // 新增
//            spliceDataBean.setCurrentArcCount(minfo.getString("curArcCnt"));
//            spliceDataBean.setTotalArcCount(minfo.getString("totalArcCnt"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return spliceDataBean;
    }

    /**
     * 解析熔接图片
     *
     * @param context ctx
     * @param spliceDataBean bean
     * @param bleDataBean 熔接机上报的图片数据
     * @return bean
     */
    public static SpliceDataBean parseSpliceImage(Context context, SpliceDataBean spliceDataBean, BleResultBean bleDataBean){
        if (spliceDataBean == null){
            spliceDataBean = new SpliceDataBean();
        }


        String strJson = getAsciiString(bleDataBean.getPayload(),0,bleDataBean.getPayload().length);
        if (strJson == null){
            return spliceDataBean;
        }

//        Log.d("yot132","111@@@@" + spliceDataBean.getId());
//        // 判断是否是同一条熔接记录
//        if (spliceDataBean.getId() == null || !spliceDataBean.getId().equals(bleDataBean.getIdStr())){
//            Log.d("yot132","1122#@@@@@");
//            return spliceDataBean;
//        }

        try {
            byte[] newByteArray = new byte[bleDataBean.getPayload().length];
//            System.arraycopy(bleDataBean.getPayload(), 0, newByteArray, 0, bleDataBean.getPayload().length);
            Log.d("yot132","@@@@@@  img = " + BleHexConvert.bytesToHexString(bleDataBean.getPayload()));
            Bitmap bitmap = Byte2Image.getPicFromBytes(bleDataBean.getPayload(),null);

            // 保存图片到沙盒
            String fileName = bleDataBean.getIdStr() + "_" + String.valueOf(System.currentTimeMillis())+ ".png";
            String imagePath = FileUtil.saveBitmapToSandBox(context, bitmap, fileName);
            if (spliceDataBean.getFiberBean() != null){
                spliceDataBean.getFiberBean().setFuseImagePath(imagePath);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return spliceDataBean;
    }

    public static String getAddress(double Latitude, double Longitude) {
        double latitude = Latitude;
        double longitude = Longitude;
        String address = getCurrentAddress(latitude, longitude);
        return address;
    }

    public static String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
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
//            Toast.makeText(this, "geocoder error(network)", Toast.LENGTH_LONG).show();
            return "geocoder error";
        } catch (IllegalArgumentException illegalArgumentException) {
//            Toast.makeText(this, "gps error", Toast.LENGTH_LONG).show();
            return "gps error";
        }
        if (addresses == null || addresses.size() == 0) {
//            Toast.makeText(this, "No Address", Toast.LENGTH_LONG).show();
            return "No Address";
        }
        Address address = addresses.get(1);
        return address.getAddressLine(0).toString()+"\n";
    }
}
