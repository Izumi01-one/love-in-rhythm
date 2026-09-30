package main.model;


public class GameSetting {

    private String fallSpeed = "NORMAL";
    private String backgroundName = "Background 02";
    private String backgroundPath = "/img/background/background02.png";
    private int volume = 70;
    private String code = "";
    private String iconPath = "/img/icons/icon2.png";

    public String getFallSpeed() {
        return fallSpeed;
    }

    public void setFallSpeed(String fallSpeed) {
        this.fallSpeed = fallSpeed;
    }

    public String getBackgroundName() {
        return backgroundName;
    }

    public void setBackgroundName(String backgroundName) {
        this.backgroundName = backgroundName;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}
