package com.example.fxt.login;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthService {
    @POST("/api/auth/join")
    Call<JoinResponse> join(@Body JoinRequest request);
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @PUT("/api/auth/updatePassword")
    Call<UpdatePasswordResponse> updatePassword(@Body UpdatePasswordRequest request);
    @PUT("/api/auth/updateName")
    Call<UpdateDispNameResponse> updateDispName(@Body UpdateDispNameRequest request);
    @HTTP(method = "DELETE", path = "/api/auth/delete", hasBody = true)
    Call<DeleteResponse> delete(@Body DeleteRequest request);
    @POST("/api/auth/resend")
    Call<ResendResponse> resend(@Body ResendRequest request);
    @POST("/api/auth/check")
    Call<CheckResponse> check(@Body CheckRequest request);
    @POST("/api/auth/userlist")
    Call<UserListItem> userList(@Body UserListRequest request);
    @POST("/api/auth/getName")
    Call<NameResponse> getName(@Body NameRequest request);
    @POST("/api/auth/verifyJoin")
    Call<VerifyJoinResponse> verifyJoin(@Body VerifyJoinRequest request);
    @POST("/api/auth/checkUserId")
    Call<CheckUserIdResponse> checkUserId(@Body CheckUserIdRequest request);
    @POST("/api/auth/resetPasswordRequest")
    Call<ResetPasswordResponse> resetPasswordRequest(@Body ResetPasswordRequest request);
    @POST("/api/auth/resetPassword")
    Call<ResetPasswordAuthResponse> resetPassword(@Body ResetPasswordAuthRequest request);
    @POST("/api/auth/resetPasswordCheckCode")
    Call<ResetPasswordCheckResponse> resetPassword(@Body ResetPasswordCheckRequest request);
    @PUT("/api/auth/updateToken")
    Call<UpdateTokenResponse> updateToken(@Body UpdateTokenRequest request);
    @POST("/api/event/get/velocity")
    Call<GetVelocityLogResponse> getVelocityLog(@Body GetVelocityLogRequest request);
    @POST("/api/event/del/velocity")
    Call<DelVelocityLogResponse> delVelocityLog(@Body DelVelocityLogRequest request);
}