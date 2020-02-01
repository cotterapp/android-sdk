package com.cotter.app;

public class Colors {

    public String ColorPrimary;
    public String ColorAccent;
    public String ColorDanger;

    public Colors() {
        ColorPrimary = "#21ce99";
        ColorAccent = "#21ce99";
        ColorDanger = "#D92C59";
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
}
