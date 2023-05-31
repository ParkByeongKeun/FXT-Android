package com.example.fxt.ble.util;

import static com.example.fxt.ble.api.util.ByteUtil.getAsciiString;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.device.splicer.bean.FiberBean;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.example.fxt.utils.Byte2Image;
import com.example.fxt.utils.FileUtil;
import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;

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

        try {
            // 最外层的JSONObject对象
            JSONObject object = new JSONObject(strJson);

            // 通过MINFO字段获取其所包含的JSONObject对象
            JSONObject minfo = object.getJSONObject("MINFO");
            spliceDataBean.setSn(minfo.getString("SN"));
            spliceDataBean.setAppVer(minfo.getString("APPVER"));
            spliceDataBean.setFpgaVer(minfo.getString("FPGAVER"));
            spliceDataBean.setManufacturer(minfo.getString("curArcCnt"));
            spliceDataBean.setBrand(minfo.getString("totalArcCnt"));
//            spliceDataBean.setManufacturer(minfo.getString("curArcCnt"));
//            spliceDataBean.setBrand(minfo.getString("totalArcCnt"));
            spliceDataBean.setModel(minfo.getString("model"));

            FiberBean fiberBean = new FiberBean();
            // 通过RESULT字段获取其所包含的JSONObject对象
            JSONObject result = object.getJSONObject("RESULT");
            fiberBean.setSpliceResult(result.getInt("splice_result"));
            fiberBean.setErrorValue(result.getInt("error_value"));

            // 通过FUSION字段获取其所包含的JSONObject对象
            JSONObject fusion = object.getJSONObject("FUSION");
            spliceDataBean.setSpliceName(fusion.getString("splice_name"));
            spliceDataBean.setDataTime(fusion.getString("data_time"));
            fiberBean.setLoss(fusion.getString("loss"));
            fiberBean.setLeftAngle(Float.parseFloat(Double.toString(fusion.getDouble("left_angle"))));
            fiberBean.setRightAngle(Float.parseFloat(Double.toString(fusion.getDouble("right_angle"))));
            fiberBean.setCoreAngle(Float.parseFloat(Double.toString(fusion.getDouble("core_angle"))));
            fiberBean.setCoreOffset(Float.parseFloat(Double.toString(fusion.getDouble("core_offset"))));
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

        // 判断是否是同一条熔接记录
        if (spliceDataBean.getId() == null || !spliceDataBean.getId().equals(bleDataBean.getIdStr())){
            return spliceDataBean;
        }

        try {
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
