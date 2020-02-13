package net.attendancekeeper.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Constants {
    public static final String FETCH_ID_URL = "https://attendancekeeper.net:5002/face_rec/goldenfrog";
    public static final String ATTENDANCE_URL = "https://attendancekeeper.net/goldenfrog/attendance/add";
    public static final String DEFAULT_MESSAGE = "Place your face and click Fetch ID to proceed";


    public static String resizeBase64Image(String base64image){
        byte [] encodeByte= Base64.decode(base64image.getBytes(),Base64.DEFAULT);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPurgeable = true;
        Bitmap image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length,options);


        if(image.getHeight() <= 400 && image.getWidth() <= 400){
            return base64image;
        }
        image = Bitmap.createScaledBitmap(image, 320, 240, false);

        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG,90, baos);

        byte [] b=baos.toByteArray();
        System.gc();
        return Base64.encodeToString(b, Base64.NO_WRAP);

    }


}
