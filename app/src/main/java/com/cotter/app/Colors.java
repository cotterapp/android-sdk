package com.cotter.app;

public class Colors {

    public String ColorPrimary;
    public String ColorAccent;
    public String ColorDanger;
    public String ColorBackground;
    public int SuccessImage;

    public Colors() {
        ColorBackground = "#FFFFFF";
        ColorPrimary = "#21ce99";
        ColorAccent = "#21ce99";
        ColorDanger = "#D92C59";
        SuccessImage = R.drawable.check;
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
}
