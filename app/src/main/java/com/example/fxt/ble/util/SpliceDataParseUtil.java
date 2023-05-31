package com.example.fxt.ble.util;

import static com.example.fxt.ble.api.util.ByteUtil.getAsciiString;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.api.util.BleHexConvert;
import com.example.fxt.ble.device.splicer.bean.FiberBean;
import com.example.fxt.ble.device.splicer.bean.InfoBean;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.example.fxt.utils.Byte2Image;
import com.example.fxt.utils.FileUtil;

import org.json.JSONObject;

import java.util.Date;

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

    public static SpliceDataBean parseSpliceData(SpliceDataBean spliceDataBean, BleResultBean bleDataBean) {
        if (spliceDataBean == null){
            spliceDataBean = new SpliceDataBean();
        }

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

            Log.d("yot132","123 = " + object);
            // 通过MINFO字段获取其所包含的JSONObject对象
//            JSONObject minfo = object.getJSONObject("MINFO");
            spliceDataBean.setSn(object.getString("serial_num"));
            spliceDataBean.setAppVer(object.getString("module"));//@@@@@@
            spliceDataBean.setFpgaVer(object.getString("module"));//@@@@@@
            spliceDataBean.setManufacturer(object.getString("cur_arc_cnt"));
            spliceDataBean.setBrand(object.getString("total_arc_cnt"));
            spliceDataBean.setModel(object.getString("splice_mode"));//@@@@@@

            FiberBean fiberBean = new FiberBean();
            // 通过RESULT字段获取其所包含的JSONObject对象
            JSONObject result = object.getJSONObject("fiber_1");
            fiberBean.setSpliceResult(0);//@@@@@@
            fiberBean.setErrorValue(result.getInt("err"));

            // 通过FUSION字段获取其所包含的JSONObject对象
            JSONObject fiber_1 = object.getJSONObject("fiber_1");
            spliceDataBean.setSpliceName(object.getString("serial_num"));
            spliceDataBean.setDataTime(object.getString("splice_time"));
            fiberBean.setLoss(fiber_1.getString("loss"));
            fiberBean.setLeftAngle(Float.parseFloat(Double.toString(fiber_1.getDouble("left_angle"))));
            fiberBean.setRightAngle(Float.parseFloat(Double.toString(fiber_1.getDouble("right_angle"))));
            fiberBean.setCoreAngle(Float.parseFloat(Double.toString(fiber_1.getDouble("right_angle"))));//@@@@@@
            fiberBean.setCoreOffset(Float.parseFloat(Double.toString(fiber_1.getDouble("right_angle"))));//@@@@@@
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
}
