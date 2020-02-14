package net.attendancekeeper.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Constants {

    private static final String COMPANY_NAME = "westacebd";
    public static final String FETCH_ID_URL = "https://attendancekeeper.net:5009/face_rec/" + COMPANY_NAME;
    public static final String ATTENDANCE_URL = "https://attendancekeeper.net/" +  COMPANY_NAME +"/attendance/add";
    public static final String DEFAULT_MESSAGE = "Place your face and click Fetch ID to proceed";




}
