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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.priyankvasa.android.cameraviewex.CameraView;
import com.priyankvasa.android.cameraviewex.Image;
import com.priyankvasa.android.cameraviewex.Modes;

import java.util.Arrays;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;
import kotlin.Unit;

public class MainActivity extends AppCompatActivity {

    private CameraView cameraKitView;

    AsyncHttpClient client;
    SweetAlertDialog loadingDialog;
    MultiStateSwitch multiSwitch;
    int currentState = 0;
    LinearLayout layout_confirm_reset;
    BootstrapButton btnFetchID;
    TextView tvMessage;
    ImageView ivCapturedImage;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraKitView = findViewById(R.id.camera);
        multiSwitch = findViewById(R.id.multiSwitch);
        layout_confirm_reset = findViewById(R.id.layout_confirm_reset);
        btnFetchID = findViewById(R.id.btnFetchID);
        tvMessage = findViewById(R.id.tvMessgae);
        ivCapturedImage = findViewById(R.id.ivCapturedImage);


        multiSwitch.addStatesFromStrings(Arrays.asList("Time In", "Break In/Out", "Time Out"));
        multiSwitch.addStateListener(new StateListener() {
            @Override
            public void onStateSelected(int stateIndex, @NonNull State state) {
                Toast.makeText(MainActivity.this, stateIndex + " ", Toast.LENGTH_LONG).show();
                currentState = stateIndex;
            }
        });
        cameraKitView.setFacing(Modes.Facing.FACING_FRONT);

        //cameraKitView.setFacing(CameraKit.FACING_FRONT);
        //cameraKitView.toggleFacing();
        loadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Working")
                .setContentText("Connecting to server. Please wait...");

        client = new AsyncHttpClient();
//        cameraKitView.setPermissions(CameraKitView.PERMISSION_STORAGE);
//        cameraKitView.setPermissions(CameraKitView.PERMISSION_CAMERA);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_LONG).show();
                        cameraKitView.start();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();

        cameraKitView.addPictureTakenListener((Image image) -> {
            Toast.makeText(MainActivity.this, "Fetching id", Toast.LENGTH_LONG).show();
            get_id(image.getData());


            return Unit.INSTANCE;

        });


    }


    public void confirm(View v) {

    }

    public void reset(View v) {
        layout_confirm_reset.setVisibility(View.INVISIBLE);
        btnFetchID.setVisibility(View.VISIBLE);
        tvMessage.setText(Constants.DEFAULT_MESSAGE);
        ivCapturedImage.setVisibility(View.GONE);
        cameraKitView.setVisibility(View.VISIBLE);

    }

    private void get_id(byte[] capturedImage) {
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
                Toast.makeText(MainActivity.this, new String(responseBody), Toast.LENGTH_LONG).show();
                loadingDialog.dismiss();
                userID = new String(responseBody);
                if(userID.contains("face"))
                {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText(userID)
                            .show();
                }
                else
                {
                    String stateMsg, showMsg;
                    if(currentState == 0)
                    {
                        stateMsg = "timein";
                        showMsg = "Time In";
                    }
                    else if(currentState == 1)
                    {
                        stateMsg = "break";
                        showMsg = "Break In/Out ";
                    }
                    else
                    {
                        stateMsg = "timeout";
                        showMsg = "Time Out";

                    }

                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(stateMsg + " confirmation")
                            .setContentText("Your ID is " + userID  + " and you want to " + showMsg)
                            .setConfirmText("Yes, do it!")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {

                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .show();


                }
                //tvMessage.setText("Success! Your ID is: " + userID);
                //btnFetchID.setVisibility(View.INVISIBLE);
                layout_confirm_reset.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                loadingDialog.dismiss();
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something went wrong!")
                        .show();
            }
        });


    }


    public void fetchID(View v) {


        cameraKitView.capture();


        /***
         cameraKitView.captureImage(new CameraKitView.ImageCallback() {
        @Override public void onImage(CameraKitView cameraKitView, byte[] capturedImage) {
        //File savedPhoto = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
        try {
        cameraKitView.setVisibility(View.GONE);
        ivCapturedImage.setVisibility(View.VISIBLE);
        String imageBase64 = Base64.encodeToString(capturedImage, Base64.DEFAULT);
        String compressed_base64 = Constants.resizeBase64Image(imageBase64);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(capturedImage, 0,capturedImage.length);
        ivCapturedImage.setImageBitmap(decodedByte);
        //Toast.makeText(MainActivity.this, savedPhoto.getPath(), Toast.LENGTH_LONG).show();
        //                    FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
        //                    outputStream.write(capturedImage);
        //                    outputStream.close();
        //
        //
        //                    Bitmap bm = BitmapFactory.decodeFile(savedPhoto.getPath());
        //                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //                    bm.compress(Bitmap.CompressFormat.JPEG, 90, baos); //bm is the bitmap object
        //                    byte[] byteArray = baos.toByteArray();

        //String imgageBase64 = Base64.encodeToString(capturedImage, Base64.DEFAULT);
        String image = "data:image/jpeg;base64," + imageBase64;


        RequestParams params = new RequestParams();
        params.add("image_data",image);
        client.post(Constants.FETCH_ID_URL, params, new AsyncHttpResponseHandler() {
        @Override public void onStart() {
        super.onStart();
        dialog.show();

        }

        @Override public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        Toast.makeText(MainActivity.this, new String(responseBody), Toast.LENGTH_LONG).show();
        dialog.dismiss();
        userID = new String(responseBody);
        tvMessage.setText("Success! Your ID is: " + userID);
        btnFetchID.setVisibility(View.INVISIBLE);
        layout_confirm_reset.setVisibility(View.VISIBLE);
        }

        @Override public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        dialog.dismiss();
        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();

        }
        });



        } catch (Exception e) {
        e.printStackTrace();
        }

        }
        });
         ***/
    }




    @Override
    protected void onPause() {
        cameraKitView.stop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.stop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            cameraKitView.start();
        }
    }
}
