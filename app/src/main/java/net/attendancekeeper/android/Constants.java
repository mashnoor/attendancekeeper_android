package net.attendancekeeper.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Base64;

import com.orhanobut.hawk.Hawk;

import java.io.ByteArrayOutputStream;

public class Constants {

    public static final String DEVICE_VERIFY_URL = "https://attendancekeeper.net/verifydevice.php?device_id=";


    public static String getFetchIdUrl()
    {
        return "https://attendancekeeper.net:5009/face_rec/" + Hawk.get("company_name");
    }
    public static String getAttendanceUrl()
    {
        return "https://attendancekeeper.net/" +  Hawk.get("company_name") +"/attendance/add";
    }
    public static boolean haveNetworkConnection(Activity activity) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static String getDeviceUniqueID(Activity activity){
        return Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);

    }


}
