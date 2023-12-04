package com.fiberfox.fxt.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;

public class C_Permission {


    /**
     * TODO [클래스 설명]
     * // -----------------------------------------
     * 1. 퍼미션 관리 클래스
     * // -----------------------------------------
     * 2. 테드 퍼미션 (tedpermission) 사용 : implementation 'gun0912.ted:tedpermission:2.2.2'
     * // -----------------------------------------
     * 3. 호출 방법 : [퍼미션 권한 부여 확인 실시]
     *   C_Permission.checkPermission(A_Main.this);
     * // -----------------------------------------
     * */





    // TODO [전역 변수 선언 실시]
    public static final String AL_TITLE = "Permission Notifycation";

    public static final String AL_NPA = "\n" + "You have not consented to permission."+"\n"
            +"There may be restrictions on the use of some features"+"\n"
            +"Please enable the denied permission in [Settings] > [Permissions]";

    public static final String AL_NQA = "안전한 앱 사용을 위해 보안 상태 확인 권한 동의를 해주세요.";

    public static final String AL_OK = "Ok";
    public static final String AL_SET = "Setting";
    public static final String AL_NO = "No";





    // TODO [퍼미션 체크 메소드]
    public static Context context;
    public static void checkPermission(Context mContext) {
        context = mContext;
        try {
            Log.i("---","---");
            Log.d("//===========//","================================================");
            Log.i("","\n"+"[C_Permission >> checkPermission() :: 퍼미션 부여 확인 실행]");
            Log.d("//===========//","================================================");
            Log.i("---","---");


            // TODO [안들외드 12 버전 / 타겟 31 대응 권한 요청 분기 처리]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){ // [타겟 31 이상]

                TedPermission.create()
                        .setPermissionListener(permissionlistener) // [퍼미션이 부여 체크 이벤트 리스너 지정]

                        // TODO [AndroidManifest.xml 에 등록된 퍼미션 등록]
                        .setPermissions(
                                // -----------------------------------------
                                // [블루투스 권한]
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                // -----------------------------------------
                                // [위치 권한]
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                // -----------------------------------------
                                // TODO [타겟 31 대응 >> 블루투스 권한 추가]
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_ADVERTISE,
                                Manifest.permission.BLUETOOTH_CONNECT
                                // -----------------------------------------
                        )
                        .check();

            }
            else { // [타겟 31 미만]

                TedPermission.create()
                        .setPermissionListener(permissionlistener) // [퍼미션이 부여 체크 이벤트 리스너 지정]

                        // TODO [AndroidManifest.xml 에 등록된 퍼미션 등록]
                        .setPermissions(
                                // -----------------------------------------
                                // [블루투스 권한]
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                // -----------------------------------------
                                // [위치 권한]
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                                // -----------------------------------------
                        )
                        .check();

            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    // TODO [퍼미션이 부여 되었는지 확인 메소드]
    static PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Log.i("---","---");
            Log.w("//===========//","================================================");
            Log.i("","\n"+"[C_Permission >> checkPermission() :: 전체 퍼미션 부여 확인 성공]");
            Log.w("//===========//","================================================");
            Log.i("---","---");
        }
        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Log.i("---","---");
            Log.e("//===========//","================================================");
            Log.i("","\n"+"[C_Permission >> checkPermission() :: 전체 퍼미션 부여 확인 실패]");
            Log.i("","\n"+"[거부된 권한 :: "+String.valueOf(deniedPermissions.toString())+"]");
            Log.e("//===========//","================================================");
            Log.i("---","---");
            // -----------------------------------------
            // [연속적으로 퍼미션을 허용 받기위해 재귀함수 루틴 사용]
            // checkPermission();
            // -----------------------------------------
            // [Alert 팝업창 알림 실시]
//            getAlertDialog(
//                    context,
//                    0, // [설정창 이동 타입 지정]
//                    AL_TITLE,
//                    AL_NPA,
//                    AL_SET,
//                    AL_NO,
//                    "");
            // -----------------------------------------

        }
    };



    //TODO [권한 거부 시 팝업창 호출 부분]
    public static void getAlertDialog(Context mContext, int type, String header, String content, String ok, String no, String normal){
        try {
            // [타이틀 및 내용 표시]
            final String Tittle = header;
            final String Message = content;


            // [버튼 이름 정의]
            String buttonNo = no;
            String buttonYes = ok;
            String buttonNature = normal;


            // [AlertDialog 팝업창 생성]
            new AlertDialog.Builder(mContext)
                    .setTitle(Tittle) // [팝업창 타이틀 지정]
                    //.setIcon(R.drawable.app_icon) // [팝업창 아이콘 지정]
                    .setMessage(Message) // [팝업창 내용 지정]
                    .setCancelable(false) // [외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정]
                    .setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // -----------------------------------------
                            // TODO [애플리케이션 설정창 이동 실시]
                            if (type == 0){
                                // [애플리케이션 설정 창으로 이동한다]
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
                                intent.setData(uri);
                                mContext.startActivity(intent);
                            }
                            // -----------------------------------------
                            // TODO [QueryAllPackagesPermission 앱 설정 창 이동]
                            else if (type == 1){
                                // [Android Q 이상 버전에서 원격 제어앱 탐지를 위해 사용자 정보 접근 권한 필요]
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                Uri uri =  Uri.fromParts("package", mContext.getPackageName(), null);
                                intent.setData(uri);
                                mContext.startActivity(intent);
                            }
                            // -----------------------------------------
                        }
                    })
                    .setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                        }
                    })
                    .setNeutralButton(buttonNature, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                        }
                    })
                    .show();
        }
        catch (Exception e){
            //e.printStackTrace();
            Log.i("---","---");
            Log.e("//===========//","================================================");
            Log.i("","\n"+"[C_Permission >> getAlertDialog() :: 팝업창 호출 실시]");
            Log.i("","\n"+"[catch [에러] :: "+String.valueOf(e.getMessage())+"]");
            Log.e("//===========//","================================================");
            Log.i("---","---");
            try {
                // [토스트 알림 표시]
                Toast.makeText(mContext, String.valueOf(content), Toast.LENGTH_LONG).show();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }


} // TODO [클래스 종료]