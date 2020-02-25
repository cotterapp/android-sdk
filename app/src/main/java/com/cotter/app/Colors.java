package com.cotter.app;

public class Colors {

    public String ColorPrimary;
    public String ColorAccent;
    public String ColorDanger;
    public String ColorBackground;
    public int SuccessImage;
    public int NetworkErrorImage;
    public int HttpErrorImage;
    public int Logo;
    public int Tap;

    public Colors() {
        ColorBackground = "#FFFFFF";
        ColorPrimary = "#21ce99";
        ColorAccent = "#21ce99";
        ColorDanger = "#D92C59";
        SuccessImage = R.drawable.check;
        NetworkErrorImage = R.drawable.ic_cloud_off_black_24dp;
        HttpErrorImage = R.drawable.ic_error_outline_black_24dp;
        Logo = R.drawable.cotter_logo;
        Tap = R.drawable.tap_device;
    }

    public void setColorPrimary(String color) {
        ColorPrimary = color;
    }

    public void setColorAccent(String color) {
        ColorAccent = color;
    }

    public void setColorDanger(String color) {
        ColorDanger = color;
    }

    public void setColorBackground(String color) {
        ColorBackground = color;
    }

    public void setSuccessImage(int img) {
        SuccessImage = img;
    }

    public void setNetworkErrorImage(int img) { NetworkErrorImage = img; }

    public void setLogo(int img) { Logo = img; }
}
