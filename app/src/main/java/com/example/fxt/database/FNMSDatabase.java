package com.example.fxt.database;//package com.inno.innobluetooth.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.fxt.ble.device.splicer.bean.FNMSDataBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FNMSDatabase extends BaseDao<FNMSDataBean> {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    /**
     * 构造器,初始化数据库
     *
     * @param context 上下文环境
     */
    public FNMSDatabase(Context context) {
        super(context);
    }

    private static class SpliceDataDaoInner{
        @SuppressLint("StaticFieldLeak")
        private static final FNMSDatabase SPLICE_DATA_DAO = new FNMSDatabase(mContext);
    }

    /**
     * 该类采用单例模式,使用该方法创建对象
     *
     * @param context  上下文环境
     * @return  返回该类的对象
     */
    public static FNMSDatabase getInstance(Context context) {
        mContext = context;
        return SpliceDataDaoInner.SPLICE_DATA_DAO;
    }

    @Override
    public void close() {
        super.close();
    }
    @Override
    public void insert(FNMSDataBean bean) {
        if (bean == null){
            return;
        }
        ContentValues values = new ContentValues();
        values.put("id", bean.getId());
        values.put("user", bean.getUser());
        values.put("salt", bean.getSalt());
        values.put("hash", bean.getHash());
        values.put("note", bean.getNote());
        values.put("data_time", bean.getData_time());

        db.insert("tb_fnms_data", null, values);

        // 获取最后插入的行ID
        int lastId = -1;
        Cursor cursor = db.rawQuery("select last_insert_rowid() from tb_fnms_data",null);
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
    public void update(FNMSDataBean fnmsDataBean) {
//        ContentValues values = new ContentValues();
////        values.put("id", bean.getId());
//        values.put("frequency", ofiDataBean.getFrequency());
//        values.put("direction", ofiDataBean.getDirection());
//        values.put("measure", ofiDataBean.getMeasure());
//        values.put("location", ofiDataBean.getLocation());
//        values.put("memo", ofiDataBean.getMemo());
//        values.put("note", ofiDataBean.getNote());
//        values.put("data_time", ofiDataBean.getDataTime());
//
//        db.update("tb_ofi_data",values,"_id = ?", new String[] {ofiDataBean.getId()});
    }

    @Override
    public void deleteById(int id) {
        String sql_fnms_data = "delete from tb_fnms_data where _id = " + id;
        db.execSQL(sql_fnms_data);
//        String sql_splice_fiber_data = "delete from tb_splice_data where _id = " + id;
//        db.execSQL(sql_splice_fiber_data);
    }

    @Override
    public void setImagePathByIDAndSn(int id, String sn, String newImagePath) {

    }

    @Override
    public List<FNMSDataBean> selectAllSpliceData() {
        List<FNMSDataBean> spliceDataBeanList = new ArrayList<>();
//        List<FiberBean> fiberBeanList = new ArrayList<>();
//        List<String> arrDate = new ArrayList<>();

        Cursor cur = db.rawQuery("SELECT * FROM tb_fnms_data order by _id desc", null);


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
                    if(cur.getString(9) != null && cur.getString(10) != null) {
                        FNMSDataBean spliceValue = new FNMSDataBean();
                        spliceValue.setId(cur.getString(0));
                        spliceValue.setUser(cur.getString(2));
                        spliceValue.setSalt(cur.getString(3));
                        spliceValue.setHash(cur.getString(4));
                        spliceValue.setNote(cur.getString(5));
                        spliceValue.setData_time(cur.getString(6));

                        spliceDataBeanList.add(spliceValue);
                    }


                } while(cur.moveToNext());
            }
        }


        return spliceDataBeanList;
    }

    @Override
    public FNMSDataBean selectResultDataByFileName(String sn) {
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

