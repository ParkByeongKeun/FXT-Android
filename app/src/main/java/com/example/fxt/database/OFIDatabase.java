package com.example.fxt.database;//package com.inno.innobluetooth.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.fxt.ble.device.splicer.bean.OFIDataBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OFIDatabase extends BaseDao<OFIDataBean> {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    /**
     * 构造器,初始化数据库
     *
     * @param context 上下文环境
     */
    public OFIDatabase(Context context) {
        super(context);
    }

    private static class SpliceDataDaoInner{
        @SuppressLint("StaticFieldLeak")
        private static final OFIDatabase SPLICE_DATA_DAO = new OFIDatabase(mContext);
    }

    /**
     * 该类采用单例模式,使用该方法创建对象
     *
     * @param context  上下文环境
     * @return  返回该类的对象
     */
    public static OFIDatabase getInstance(Context context) {
        mContext = context;
        return SpliceDataDaoInner.SPLICE_DATA_DAO;
    }


    @Override
    public void close() {
        super.close();
    }
    @Override
    public void insert(OFIDataBean bean) {
        if (bean == null){
            return;
        }
        ContentValues values = new ContentValues();
//        values.put("id", bean.getId());
        values.put("frequency", bean.getFrequency());
        values.put("direction", bean.getDirection());
        values.put("measure", bean.getMeasure());
        values.put("location", bean.getLocation());
        values.put("memo", bean.getMemo());
        values.put("note", bean.getNote());
        values.put("data_time", bean.getDataTime());
        values.put("serial", bean.getSerial());

        db.insert("tb_ofi_data", null, values);

        // 获取最后插入的行ID
        int lastId = -1;
        Cursor cursor = db.rawQuery("select last_insert_rowid() from tb_ofi_data",null);
        if (cursor != null && cursor.moveToFirst()){
            lastId = cursor.getInt(0);
        }

//        ContentValues fiberValues = new ContentValues();
//        fiberValues.put("splice_data_id", lastId);
//        fiberValues.put("loss", bean.getFiberBean().getLoss());
//        fiberValues.put("left_angle", bean.getFiberBean().getLeftAngle());
//        fiberValues.put("right_angle", bean.getFiberBean().getRightAngle());
//        fiberValues.put("core_angle", bean.getFiberBean().getCoreAngle());
//        fiberValues.put("core_offset", bean.getFiberBean().getCoreOffset());
//        fiberValues.put("splice_result", bean.getFiberBean().getSpliceResult());
//        fiberValues.put("error_value", bean.getFiberBean().getErrorValue());

//        if (bean.getFiberBean().getFuseImagePath() != null){
//            fiberValues.put("fuse_image_name", bean.getFiberBean().getFuseImagePath());
//        }

//        db.insert("tb_fnms_data", null, fiberValues);
    }

    @Override
    public void update(OFIDataBean ofiDataBean) {
        //        String sql_ofi_data = "update tb_ofi_data set memo = " + newImagePath +" where _id = " + id;
        ContentValues values = new ContentValues();
//        values.put("id", bean.getId());
        values.put("frequency", ofiDataBean.getFrequency());
        values.put("direction", ofiDataBean.getDirection());
        values.put("measure", ofiDataBean.getMeasure());
        values.put("location", ofiDataBean.getLocation());
        values.put("memo", ofiDataBean.getMemo());
        values.put("note", ofiDataBean.getNote());
        values.put("data_time", ofiDataBean.getDataTime());
        values.put("serial", ofiDataBean.getSerial());

        db.update("tb_ofi_data",values,"_id = ?", new String[] {ofiDataBean.getId()});
//        db.execSQL(sql_ofi_data);
    }

    @Override
    public void deleteById(int id) {
        String sql_ofi_data = "delete from tb_ofi_data where _id = " + id;
        db.execSQL(sql_ofi_data);
//        String sql_splice_fiber_data = "delete from tb_splice_data where _id = " + id;
//        db.execSQL(sql_splice_fiber_data);
    }

    @Override
    public void setImagePathByIDAndSn(int id, String sn, String newImagePath) {

    }

    @Override
    public List<OFIDataBean> selectAllSpliceData() {
        List<OFIDataBean> OFIDataBeanList = new ArrayList<>();
//        List<FiberBean> fiberBeanList = new ArrayList<>();
//        List<String> arrDate = new ArrayList<>();

        Cursor cur = db.rawQuery("SELECT * FROM tb_ofi_data order by _id desc", null);


//        Cursor c = db.rawQuery("SELECT * FROM tb_fnms_data order by _id desc", null);

//        if(c != null) {
//            if(c.moveToFirst()) {
//                do {
//                    if(c.getString(2) != null) {
//
//                        FiberBean fiberValue = new FiberBean();
//                        fiberValue.setLoss(c.getString(2));
//                        fiberValue.setLeftAngle(c.getFloat(3));
//                        fiberValue.setRightAngle(c.getFloat(4));
//                        fiberValue.setCoreAngle(c.getFloat(5));
//                        fiberValue.setCoreOffset(c.getFloat(6));
//                        fiberValue.setSpliceResult(c.getInt(7));
//                        fiberValue.setErrorValue(c.getInt(8));
//                        fiberValue.setFuseImagePath(c.getString(9));
//                        fiberBeanList.add(fiberValue);
//
//                    }
//
////                    arrDate.add(c.getString(10));
////                    spliceValue.setDataTime(c.getString(10));
////                    spliceValue.setFiberBean(fiberValue);
//
////                    spliceDataBeanList.add(spliceValue);
//                } while(c.moveToNext());
//            }
//        }

        if(cur != null) {
            if(cur.moveToFirst()) {
                do {
//                    if(cur.getString(9) != null && cur.getString(10) != null) {
                        OFIDataBean spliceValue = new OFIDataBean();
                        spliceValue.setId(cur.getString(0));
                        spliceValue.setFrequency(cur.getString(1));
                        spliceValue.setDirection(cur.getString(2));
                        spliceValue.setMeasure(cur.getString(3));
                        spliceValue.setLocation(cur.getString(4));
                        spliceValue.setMemo(cur.getString(5));
                        spliceValue.setNote(cur.getString(6));
                        spliceValue.setDataTime(cur.getString(7));
                        spliceValue.setSerial(cur.getString(8));
//                        Log.d("yot132","11 = " + cur.getString(7) + ", 2 = " + cur.getString(8));
//                        spliceValue.setUpdateTime(stringToDate(cur.getString(9)));
//                        spliceValue.setCreateTime(stringToDate(cur.getString(10)));

                        OFIDataBeanList.add(spliceValue);
//                    }


                } while(cur.moveToNext());
            }
        }


        return OFIDataBeanList;
    }

    @Override
    public OFIDataBean selectResultDataByFileName(String sn) {
        return null;
    }

    public Date stringToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 문자열 -> Date
        Date date = null;
//        Log.d("yot132","strDate = " + strDate);
        try {
            date = formatter.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;

    }

}

