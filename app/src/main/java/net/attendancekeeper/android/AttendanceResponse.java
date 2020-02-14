package net.attendancekeeper.android;

import com.google.gson.annotations.SerializedName;

public class AttendanceResponse {
    @SerializedName("error")
    private String error;

    @SerializedName("success")
    private String success;

    public String getSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}
