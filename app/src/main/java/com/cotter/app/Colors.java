package com.cotter.app;

public class Colors {

    public String ColorPrimary;
    public String ColorPrimaryLight;
    public String ColorAccent;
    public String ColorDanger;
    public String ColorDangerLight;
    public String ColorBackground;
    public String ColorBlack;
    public String ColorSuperLightGrey;
    public int SuccessImage;
    public int ErrorImage;
    public int NetworkErrorImage;
    public int HttpErrorImage;
    public int Logo;
    public int Tap;

    public Colors() {
        ColorBackground = "#FFFFFF";
        ColorBlack = "#000000";
        ColorPrimary = "#21ce99";
        ColorAccent = "#21ce99";
        ColorDanger = "#D92C59";
        ColorDangerLight = "#F7E5E8";
        ColorPrimaryLight = "#DAEDC6";
        ColorSuperLightGrey = "#F5F5F5";
        SuccessImage = R.drawable.check;
        NetworkErrorImage = R.drawable.ic_cloud_off_black_24dp;
        HttpErrorImage = R.drawable.ic_error_outline_black_24dp;
        Logo = R.drawable.cotter_logo;
        Tap = R.drawable.tap_device;
        ErrorImage = R.drawable.warning;
    }

    public void setColorBackground(String color) {
        ColorBackground = color;
    }
    public void setColorBlack(String color) {
        ColorBlack = color;
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
    public void setColorDangerLight(String color) {
        ColorDangerLight = color;
    }
    public void setColorPrimaryLight(String color) {
        ColorPrimaryLight = color;
    }
    public void setColorSuperLightGrey(String color) {
        ColorSuperLightGrey = color;
    }


    public void setSuccessImage(int img) {
        SuccessImage = img;
    }
    public void setErrorImage(int img) { ErrorImage = img; }
    public void setHttpErrorImage(int img) { HttpErrorImage = img; }
    public void setNetworkErrorImage(int img) { NetworkErrorImage = img; }
    public void setLogo(int img) { Logo = img; }
    public void setTap(int img) { Tap = img; }
}
