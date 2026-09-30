package main.model;

public class IconItem {

    private String name;
    private String previewPath;
    private String iconPath;

    public String getName() {
        return name == null || name.isBlank() ? "Chủ Đề Icon" : name;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public String getIconPath() {
        return iconPath;
    }
}