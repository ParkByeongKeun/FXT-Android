package com.fiberfox.fxt.database;//package com.inno.innobluetooth.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fiberfox.fxt.ble.device.splicer.bean.FiberBean;
import com.fiberfox.fxt.ble.device.splicer.bean.SpliceDataBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpliceDataDao extends BaseDaoSplice<SpliceDataBean> {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    /**
     * 构造器,初始化数据库
     *
     * @param context 上下文环境
     */
    public SpliceDataDao(Context context) {
        super(context);
    }

    private static class SpliceDataDaoInner{
        @SuppressLint("StaticFieldLeak")
        private static final SpliceDataDao SPLICE_DATA_DAO = new SpliceDataDao(mContext);
    }

    /**
     * 该类采用单例模式,使用该方法创建对象
     *
     * @param context  上下文环境
     * @return  返回该类的对象
     */
    public static SpliceDataDao getInstance(Context context) {
        mContext = context;
        return SpliceDataDaoInner.SPLICE_DATA_DAO;
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void insert(SpliceDataBean bean) {
        if (bean == null){
            return;
        }
        ContentValues values = new ContentValues();
        values.put("id", bean.getId());
        values.put("sn", bean.getSn());
        values.put("app_ver", bean.getAppVer());
        values.put("fpga_ver", bean.getFpgaVer());
        values.put("manufacturer", bean.getManufacturer());
        values.put("model", bean.getModel());
        values.put("brand", bean.getBrand());
        values.put("splice_name", bean.getSpliceName());
        values.put("data_time", bean.getDataTime());
        values.put("user", bean.getUser());
//        values.put("createTime", bean.getCreateTime());

        db.insert("tb_splice_data", null, values);

        if (bean.getFiberBean() == null){
            return;
        }

        // 获取最后插入的行ID
        int lastId = -1;
        Cursor cursor = db.rawQuery("select last_insert_rowid() from tb_splice_data",null);
        if (cursor != null && cursor.moveToFirst()){
            lastId = cursor.getInt(0);
        }

        ContentValues fiberValues = new ContentValues();
        fiberValues.put("splice_data_id", lastId);
        fiberValues.put("loss", bean.getFiberBean().getLoss());
        fiberValues.put("left_angle", bean.getFiberBean().getLeftAngle());
        fiberValues.put("right_angle", bean.getFiberBean().getRightAngle());
        fiberValues.put("core_angle", bean.getFiberBean().getCoreAngle());
        fiberValues.put("core_offset", bean.getFiberBean().getCoreOffset());
        fiberValues.put("splice_result", bean.getFiberBean().getSpliceResult());
        fiberValues.put("error_value", bean.getFiberBean().getErrorValue());

        if (bean.getFiberBean().getFuseImagePath() != null){
            fiberValues.put("fuse_image_name", bean.getFiberBean().getFuseImagePath());
        }
//        fiberValues.put("updateTime", bean.getDataTime());
//        fiberValues.put("createTime", bean.getDataTime());

        db.insert("tb_splice_fiber_data", null, fiberValues);
    }

    @Override
    public void deleteById(int id) {
        String sql_splice_data = "delete from tb_splice_fiber_data where _id = " + id;
        db.execSQL(sql_splice_data);
        String sql_splice_fiber_data = "delete from tb_splice_data where _id = " + id;
        db.execSQL(sql_splice_fiber_data);
    }

    @Override
    public void setImagePathByIDAndSn(int id, String sn, String newImagePath) {

    }

    @Override
    public List<SpliceDataBean> selectAllSpliceData() {
        List<SpliceDataBean> spliceDataBeanList = new ArrayList<>();
        List<FiberBean> fiberBeanList = new ArrayList<>();
//        List<String> arrDate = new ArrayList<>();

        Cursor cur = db.rawQuery("SELECT * FROM tb_splice_data order by _id desc", null);


        Cursor c = db.rawQuery("SELECT * FROM tb_splice_fiber_data order by _id desc", null);

        if(c != null) {
            if(c.moveToFirst()) {
                do {
                    if(c.getString(2) != null) {

                        FiberBean fiberValue = new FiberBean();
                        fiberValue.setLoss(c.getString(2));
                        fiberValue.setLeftAngle(c.getFloat(3));
                        fiberValue.setRightAngle(c.getFloat(4));
                        fiberValue.setCoreAngle(c.getFloat(5));
                        fiberValue.setCoreOffset(c.getFloat(6));
                        fiberValue.setSpliceResult(c.getString(7));
                        fiberValue.setErrorValue(c.getString(8));
                        fiberValue.setFuseImagePath(c.getString(9));
                        fiberBeanList.add(fiberValue);

                    }

//                    arrDate.add(c.getString(10));
//                    spliceValue.setDataTime(c.getString(10));
//                    spliceValue.setFiberBean(fiberValue);

//                    spliceDataBeanList.add(spliceValue);
                } while(c.moveToNext());
            }
        }

        if(cur != null) {
            if(cur.moveToFirst()) {
                do {
                    if(cur.getString(9) != null && cur.getString(10) != null) {
                        SpliceDataBean spliceValue = new SpliceDataBean();

                        spliceValue.setId(cur.getString(0));
                        spliceValue.setSn(cur.getString(2));
                        spliceValue.setAppVer(cur.getString(3));
                        spliceValue.setFpgaVer(cur.getString(4));
                        spliceValue.setManufacturer(cur.getString(5));
                        spliceValue.setModel(cur.getString(6));
                        spliceValue.setBrand(cur.getString(7));
                        spliceValue.setSpliceName(cur.getString(8));
                        spliceValue.setUpdateTime(stringToDate(cur.getString(9)));
                        spliceValue.setCreateTime(stringToDate(cur.getString(11)));
                        spliceValue.setUser(cur.getString(10));

                        spliceDataBeanList.add(spliceValue);
//                        Log.d("yot132","123");
                    }


//                    Log.d("yot132","asd = " +
//                            cur.getString(0) + // id
//                            "\n" + cur.getString(1) + // ?? 1.62653e+06
//                            "\n" + cur.getString(2) + // sn
//                            "\n" + cur.getString(3) + // swver
//                            "\n" + cur.getString(4) + // ?? 1.00
//                            "\n" + cur.getString(5) + // 1 ??
//                            "\n" + cur.getString(6) + // Model
//                            "\n" + cur.getString(7) + // Model
//                            "\n" + cur.getString(8) + // splice mode
//                            "\n" + cur.getString(9) + //update date
//                            "\n" + cur.getString(10) //create date
//                             );


                } while(cur.moveToNext());
            }
        }

        Log.d("yot132","1 = " + fiberBeanList.size() + ", 2 = " + spliceDataBeanList.size());
//        for(int i = 0 ; i < fiberBeanList.size() ; i ++) {
//            Log.d("yot132","i = " + fiberBeanList.get(i).get);
//        }
//        if(fiberBeanList.size() == spliceDataBeanList.size()) {
            for(int i = 0 ; i < spliceDataBeanList.size() ; i ++) {
                spliceDataBeanList.get(i).setFiberBean(fiberBeanList.get(i));
            }


        return spliceDataBeanList;
    }

    @Override
    public SpliceDataBean selectResultDataByFileName(String sn) {
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

