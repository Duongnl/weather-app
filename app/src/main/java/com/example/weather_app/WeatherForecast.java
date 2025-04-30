package com.example.weather_app;
public class WeatherForecast {
    private String time;
    private double temperature;
    private String description;
    private String icon;

    public WeatherForecast(String time, double temperature, String description) {
        this.time = time;
        this.temperature = temperature;
        this.description = description;
//        this.icon;

    }



    public String getTime() {
        return time;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }
}

