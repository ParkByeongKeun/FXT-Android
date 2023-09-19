package com.example.fxt.ble.util;

import static com.example.fxt.ble.api.util.ByteUtil.getAsciiString;

import android.content.Context;

import com.example.fxt.CustomApplication;
import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.device.splicer.bean.OFIDataBean;

import net.ijoon.auth.UserRequest;
import net.ijoon.auth.UserResponse;

import org.json.JSONObject;

import java.util.Date;

/**
 * 熔接机上报数据解析工具类
 */
public class OfiDataParseUtil {

    /**
     * 解析数据
     *
     * @param OFIDataBean bean
     * @param bleDataBean 熔接机上报的数据
     * @return bean
     */
    private static CustomApplication customApplication;


    public static OFIDataBean parseSpliceData(Context context, OFIDataBean OFIDataBean, BleResultBean bleDataBean) {
        customApplication = (CustomApplication) context.getApplicationContext();

        if (OFIDataBean == null){
            OFIDataBean = new OFIDataBean();
        }

        String strJson = getAsciiString(bleDataBean.getPayload(),0,bleDataBean.getPayload().length);
        if (strJson == null){
            return OFIDataBean;
        }

        OFIDataBean.setId(bleDataBean.getIdStr());
        // 设置接收时间 更新时间
        OFIDataBean.setCreateTime(new Date());
        OFIDataBean.setUpdateTime(new Date());

        try {
            // 最外层的JSONObject对象
            JSONObject object = new JSONObject(strJson);

            String sql_splice_data = "create table tb_ofi_data (" +
                    "_id INTEGER primary key autoincrement, " +
                    "frequency TEXT,direction TEXT,measure TEXT,location TEXT,memo TEXT,note TEXT," +
                    "createTime TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP)";

            String sql_splice_fiber_data = "create table tb_fnms_data (" +
                    "_id INTEGER primary key autoincrement, " +
                    "id TEXT,salt TEXT,hash TEXT,note TEXT," +
                    "createTime TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP)";
            // 通过MINFO字段获取其所包含的JSONObject对象
            JSONObject minfo = object.getJSONObject("MINFO");
            OFIDataBean.setFrequency(minfo.getString("SN"));
            OFIDataBean.setDirection(minfo.getString("APPVER"));
            OFIDataBean.setMeasure(minfo.getString("FPGAVER"));
            OFIDataBean.setLocation(minfo.getString("curArcCnt"));
            OFIDataBean.setMemo(minfo.getString("totalArcCnt"));
//            spliceDataBean.setManufacturer(minfo.getString("curArcCnt"));
//            spliceDataBean.setBrand(minfo.getString("totalArcCnt"));
            OFIDataBean.setNote(minfo.getString("model"));
            OFIDataBean.setDataTime(minfo.getString("data_time"));
            OFIDataBean.setUser(minfo.getString("user"));
//            FiberBean fiberBean = new FiberBean();
//            // 通过RESULT字段获取其所包含的JSONObject对象
//            JSONObject result = object.getJSONObject("RESULT");
//            fiberBean.setSpliceResult(result.getInt("splice_result"));
//            fiberBean.setErrorValue(result.getInt("error_value"));
//
//            // 通过FUSION字段获取其所包含的JSONObject对象
//            JSONObject fusion = object.getJSONObject("FUSION");
//            spliceDataBean.setSpliceName(fusion.getString("splice_name"));
//            spliceDataBean.setDataTime(fusion.getString("data_time"));
//            fiberBean.setLoss(fusion.getString("loss"));
//            fiberBean.setLeftAngle(Float.parseFloat(Double.toString(fusion.getDouble("left_angle"))));
//            fiberBean.setRightAngle(Float.parseFloat(Double.toString(fusion.getDouble("right_angle"))));
//            fiberBean.setCoreAngle(Float.parseFloat(Double.toString(fusion.getDouble("core_angle"))));
//            fiberBean.setCoreOffset(Float.parseFloat(Double.toString(fusion.getDouble("core_offset"))));
//            spliceDataBean.setFiberBean(fiberBean);

            // 新增
//            spliceDataBean.setCurrentArcCount(minfo.getString("curArcCnt"));
//            spliceDataBean.setTotalArcCount(minfo.getString("totalArcCnt"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return OFIDataBean;
    }

    /**
     * 解析熔接图片
     *
     * @param context ctx
     * @param spliceDataBean bean
     * @param bleDataBean 熔接机上报的图片数据
     * @return bean
     */
//    public static SpliceDataBean parseSpliceImage(Context context, SpliceDataBean spliceDataBean, BleResultBean bleDataBean){
//        if (spliceDataBean == null){
//            spliceDataBean = new SpliceDataBean();
//        }
//
//        String strJson = getAsciiString(bleDataBean.getPayload(),0,bleDataBean.getPayload().length);
//        if (strJson == null){
//            return spliceDataBean;
//        }
//
//        // 判断是否是同一条熔接记录
//        if (spliceDataBean.getId() == null || !spliceDataBean.getId().equals(bleDataBean.getIdStr())){
//            return spliceDataBean;
//        }
//
////        try {
////            Bitmap bitmap = Byte2Image.getPicFromBytes(bleDataBean.getPayload(),null);
////
////            // 保存图片到沙盒
//////            String fileName = bleDataBean.getIdStr() + "_" + String.valueOf(System.currentTimeMillis())+ ".png";
//////            String imagePath = FileUtil.saveBitmapToSandBox(context, bitmap, fileName);
//////            if (spliceDataBean.getFiberBean() != null){
//////                spliceDataBean.getFiberBean().setFuseImagePath(imagePath);
//////            }
////        }catch (Exception e){
////            e.printStackTrace();
////        }
//
//        return spliceDataBean;
//    }
}
