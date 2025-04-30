package com.example.weather_app;

public class ForecastByDay {
    private String time; // Có thể là giờ (HH:mm) hoặc tên ngày (Thứ Hai)
    private double temperature;
    private String description;
    private boolean isDaily; // Phân biệt dữ liệu ngày/giờ

    // Constructor
    public ForecastByDay(String time, double temperature, String description, boolean isDaily) {
        this.time = time;
        this.temperature = temperature;
        this.description = description;
        this.isDaily = isDaily;
    }

    // Getter methods
    public String getTime() { return time; }
    public double getTemperature() { return temperature; }
    public String getDescription() { return description; }
    public boolean isDaily() { return isDaily; }
}
