package com.example.weather_app;

import com.google.android.gms.maps.model.LatLng;

public class City {
        public LatLng location;
        public String name;

        public City(LatLng location, String name) {
            this.location = location;
            this.name = name;
        }
}
