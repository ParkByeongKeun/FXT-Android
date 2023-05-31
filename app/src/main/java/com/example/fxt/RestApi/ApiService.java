package com.example.fxt.RestApi;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("/web/login")
    Call<ResponseLogin> login(@Field("phone") String phone, @Field("password") String password);
    @FormUrlEncoded
    @POST("/web/register")
    Call<ResponseRegister> register(@Field("phone") String phone, @Field("password") String password);
    @FormUrlEncoded
    @POST("/web/sendVerCode")
    Call<ResponseSendVerCode> sendVerCode(@Field("phone") String phone, @Field("action") String action, @Field("type") String type);
    @FormUrlEncoded
    @POST("/web/validateVerCode")
    Call<ResponseValidateVerCode> validateVerCode(@Field("smsId") int smsId, @Field("phone") String phone, @Field("verCode") String verCode);
    @FormUrlEncoded
    @POST("/web/updatePassword")
    Call<ResponseUpdatePassword> updatePassword(@Field("phone") String phone, @Field("password") String password);
    @FormUrlEncoded
    @POST("/web/getUserInfo")
    Call<ResponseGetUserInfo> getUserInfo(@Field("token") String token);
    @FormUrlEncoded
    @POST("/web/updateUser")
    Call<ResponseUpdateUser> updateUser(@Field("token") String token, @Field("name") String name, @Field("sex") String sex, @Field("company") String company);
    @FormUrlEncoded
    @POST("/web/validateToken")
    Call<ResponseValidateToken> validateToken(@Field("token") String token);
    @FormUrlEncoded
    @POST("/web/getBasic")
    Call<ResponseGetBasic> getBasic(@Field("type") String type);
    @FormUrlEncoded
    @POST("/web/uploadHead")
    Call<ResponseUploadHead> uploadHead(@Field("image") String image, @Field("token") String token);
    @FormUrlEncoded
    @POST("/web/getVideoList")
    Call<ResponseGetVideoList> getVideoList(@Field("type") String type, @Field("title") String title);
    @FormUrlEncoded
    @POST("/web/getMacCount")
    Call<ResponseGetMacCount> getMacCount(@Field("project") String project, @Field("token") String token);
    @FormUrlEncoded
    @POST("/web/getMacList")
    Call<ResponseGetMacList> getMacList(@Field("project") String project, @Field("token") String token);
    @FormUrlEncoded
    @POST("/web/getMacInfo")
    Call<ResponseGetMacInfo> getMacInfo(@Field("macSerial") String macSerial, @Field("project") String project);
    @FormUrlEncoded
    @POST("/web/setLocMacInfo")
    Call<ResponseSetLocMacInfo> setLocMacInfo(@Field("macSerial") String macSerial,@Field("project") String project, @Field("loc") String loc, @Field("dloc") String dloc);
    @FormUrlEncoded
    @POST("/web/setFusedInfo")
    Call<ResponseSetFusedInfo> setFusedInfo(@Field("id") int id, @Field("loc") String loc, @Field("dloc") String dloc, @Field("remark") String remark);
    @FormUrlEncoded
    @POST("/web/getFusedList")
    Call<ResponseGetFusedList> getFusedList(@Field("macSerial") String macSerial, @Field("project") String project, @Field("offset") String offset, @Field("getType") String getType);
    @FormUrlEncoded
    @POST("/web/getFusedInfo")
    Call<ResponseGetFusedInfo> getFusedInfo(@Field("id") int id);
    @FormUrlEncoded
    @POST("/web/getImageList")
    Call<ResponseGetImageList> getImageList(@Field("id") int id);
    @FormUrlEncoded
    @POST("/web/bind")
    Call<ResponseBind> bind(@Field("token") String token, @Field("macSerial") String macSerial, @Field("project") String project);
    @FormUrlEncoded
    @POST("/web/unBind")
    Call<ResponseUnBind> unBind(@Field("token") String token, @Field("macSerial") String macSerial, @Field("project") String project);
    @FormUrlEncoded
    @POST("/web/bindStatus")
    Call<ResponseBindStatus> bindStatus(@Field("macSerial") String macSerial, @Field("project") String project);
    @FormUrlEncoded
    @POST("/web/generatePdf")
    Call<ResponseGeneratePdf> generatePdf(@Field("token") String token, @Field("ids") String ids, @Field("remark") String remark, @Field("dloc") String dloc, @Field("loc") String loc, @Field("type") int type);
    @FormUrlEncoded
    @POST("/web/excelPreview")
    Call<ResponseExcelPreview> excelPreview(@Field("token") String token, @Field("id") String id);
    @FormUrlEncoded
    @POST("/web/deletePdf")
    Call<ResponseDeletePdf> deletePdf(@Field("token") String token, @Field("ids") String ids, @Field("type") int type);
    @FormUrlEncoded
    @POST("/web/downloadFile")
    Call<ResponseDownloadFile> downloadFile(@Field("fileUrl") String fileUrl);
    @FormUrlEncoded
    @POST("/web/deleteRecord")
    Call<ResponseDeleteRecord> deleteRecord(@Field("token") String token, @Field("ids") String ids);
}
