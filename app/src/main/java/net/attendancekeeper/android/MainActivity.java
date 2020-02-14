package net.attendancekeeper.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.davidmiguel.multistateswitch.MultiStateSwitch;
import com.davidmiguel.multistateswitch.State;
import com.davidmiguel.multistateswitch.StateListener;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;


import java.util.Arrays;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;
import kotlin.Unit;

public class MainActivity extends AppCompatActivity {

    private CameraView cameraKitView;

    AsyncHttpClient client;
    SweetAlertDialog errorDialog;
    MultiStateSwitch multiSwitch;
    int currentState = 0;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraKitView = findViewById(R.id.camera);
        multiSwitch = findViewById(R.id.multiSwitch);


        multiSwitch.addStatesFromStrings(Arrays.asList("Time In", "Break In/Out", "Time Out"));
        multiSwitch.addStateListener(new StateListener() {
            @Override
            public void onStateSelected(int stateIndex, @NonNull State state) {
                Toast.makeText(MainActivity.this, stateIndex + " ", Toast.LENGTH_LONG).show();
                currentState = stateIndex;
            }
        });


//        loadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
//                .setTitleText("Working")
//                .setContentText("Connecting to server. Please wait...");

        errorDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText("Something went wrong!");


        client = new AsyncHttpClient();

        CameraView camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);


        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();


        camera.setFacing(Facing.FRONT);

        camera.addCameraListener(new CameraListener() {


            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                get_id(result.getData());
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePictureSnapshot();
            }
        });


    }

    private void makeAttendance(String type) {
        SweetAlertDialog loadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Working")
                .setContentText("Connecting to server. Please wait...");
        RequestParams params = new RequestParams();
        params.put("idno", userID);
        params.put("type", type);
        client.post(Constants.ATTENDANCE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                loadingDialog.show();
                userID = "";
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                loadingDialog.dismissWithAnimation();

                String response = new String(responseBody);
                Gson gson = new Gson();
                AttendanceResponse attendanceResponse = gson.fromJson(response, AttendanceResponse.class);

                if (attendanceResponse.getError() == null || attendanceResponse.getError().isEmpty()) {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Yaay!.")
                            .setContentText(attendanceResponse.getSuccess())
                            .show();

                } else {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Opps...")
                            .setContentText(attendanceResponse.getError())
                            .show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                loadingDialog.dismissWithAnimation();
                errorDialog.show();


            }
        });

    }

    private void get_id(byte[] capturedImage) {
        SweetAlertDialog loadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Working")
                .setContentText("Connecting to server. Please wait...");
        String imageBase64 = Base64.encodeToString(capturedImage, Base64.DEFAULT);
        RequestParams params = new RequestParams();
        params.add("image_data", imageBase64);
        client.post(Constants.FETCH_ID_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();

                loadingDialog.show();

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //Toast.makeText(MainActivity.this, new String(responseBody), Toast.LENGTH_LONG).show();
                loadingDialog.dismissWithAnimation();
                userID = new String(responseBody);
                if (userID.contains("face")) {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText(userID)

                            .show();
                } else {
                    String stateMsg, showMsg;
                    if (currentState == 0) {
                        stateMsg = "timein";
                        showMsg = "Time In";
                    } else if (currentState == 1) {
                        stateMsg = "break";
                        showMsg = "Break In/Out ";
                    } else {
                        stateMsg = "timeout";
                        showMsg = "Time Out";

                    }

                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(showMsg + " Confirmation")
                            .setContentText("Your ID is " + userID + " and you want to " + showMsg)
                            .setConfirmText("Yes, do it!")
                            .setCancelButton("Nope!", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    sDialog.cancel();


                                    makeAttendance(stateMsg);
                                }
                            })
                            .show();


                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                loadingDialog.dismissWithAnimation();
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something went wrong!")
                        .show();
            }
        });


    }


    public void takePhoto(View v) {
        cameraKitView.takePictureSnapshot();
    }


    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
