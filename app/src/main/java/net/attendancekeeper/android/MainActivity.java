package net.attendancekeeper.android;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.davidmiguel.multistateswitch.MultiStateSwitch;
import com.davidmiguel.multistateswitch.State;
import com.davidmiguel.multistateswitch.StateListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private CameraKitView cameraKitView;

    AsyncHttpClient client;
    ProgressDialog dialog;
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

        cameraKitView.setFacing(CameraKit.FACING_FRONT);
        //cameraKitView.toggleFacing();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Fetching ID. Please wait...");
        client = new AsyncHttpClient();
        cameraKitView.setPermissions(CameraKitView.PERMISSION_STORAGE);
        cameraKitView.setPermissions(CameraKitView.PERMISSION_CAMERA);

    }


    public void confirm(View v)
    {

    }

    public void reset(View v)
    {
        layout_confirm_reset.setVisibility(View.INVISIBLE);
        btnFetchID.setVisibility(View.VISIBLE);
        tvMessage.setText(Constants.DEFAULT_MESSAGE);
        ivCapturedImage.setVisibility(View.GONE);
        cameraKitView.setVisibility(View.VISIBLE);

    }


    public void fetchID(View v)
    {
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] capturedImage) {
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
                        @Override
                        public void onStart() {
                            super.onStart();
                            dialog.show();

                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Toast.makeText(MainActivity.this, new String(responseBody), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            userID = new String(responseBody);
                            tvMessage.setText("Success! Your ID is: " + userID);
                            btnFetchID.setVisibility(View.INVISIBLE);
                            layout_confirm_reset.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                           dialog.dismiss();
                           Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();

                        }
                    });



                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
