package com.example.weather_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvCity;
    private TextView alertView;
    private TextView tvTime;
    private TextView tvTemperature;
    private TextView tvDescription;
    private TextView tvPressure;
    private TextView tvHumidity;
    private TextView tvWind;
    private RecyclerView rvHourlyForecast;
    private RecyclerView rvDailyForecast;
    private DailyForecastAdapter dailyForecastAdapter;
    private static final String API_KEY = "90264afa2dd5fa5943b9c718e812ac0f";
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private FusedLocationProviderClient fusedLocationClient;

    private ForecastAdapter forecastAdapter;
    private List<WeatherForecast> forecastByDay = new ArrayList<>(); // List chứa dữ liệu dự báo

    private List<WeatherForecast> forecastList = new ArrayList<>(); // List chứa dữ liệu dự báo

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvHourlyForecast = view.findViewById(R.id.rvHourlyForecast);
        rvHourlyForecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // Kết nối với Adapter (dữ liệu sẽ được truyền vào adapter)
        forecastAdapter = new ForecastAdapter(forecastList);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        tvCity = view.findViewById(R.id.tvCity);
        tvTime = view.findViewById(R.id.tvTime);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvPressure = view.findViewById(R.id.tvPressure);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWind = view.findViewById(R.id.tvWind);
        alertView = view.findViewById(R.id.alertView); // ánh xạ TextView cảnh báo


        rvHourlyForecast = view.findViewById(R.id.rvHourlyForecast); // RecyclerView for hourly forecast

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Set up RecyclerView
        rvHourlyForecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        forecastAdapter = new ForecastAdapter(forecastList);
        rvHourlyForecast.setAdapter(forecastAdapter);

        rvDailyForecast = view.findViewById(R.id.rvDailyForecast);
        rvDailyForecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        dailyForecastAdapter = new DailyForecastAdapter(forecastByDay);
        rvDailyForecast.setAdapter(dailyForecastAdapter);

        // Check for permission before proceeding to get location
        if (checkPermission()) {
            getCurrentLocation();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            // You can show a dialog explaining why permission is needed
        }
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                fetchWeatherByLocation(latitude, longitude);
                                fetchForecastByLocation(latitude, longitude);
                            }
                        }
                    });
        } catch (SecurityException e) {
            Log.e("MainFragment", "SecurityException: Permission not granted", e);
        }
    }

    private void fetchWeatherByLocation(double latitude, double longitude) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&lang=vi&appid=" + API_KEY + "&units=metric");
//                Log.e("url", "url : "+ url );
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.e("response", "response : "+ response );

                parseWeather(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchForecastByLocation(double latitude, double longitude) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude + "&lon=" + longitude + "&lang=vi&appid=" + API_KEY + "&units=metric");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                parseForecast(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void parseWeather(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            String cityName = jsonObject.getString("name");
            JSONObject main = jsonObject.getJSONObject("main");
            double temperature = main.getDouble("temp");
            int pressure = main.getInt("pressure");
            int humidity = main.getInt("humidity");

            JSONObject wind = jsonObject.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");

            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            String description = weatherArray.getJSONObject(0).getString("description");

            // Format time
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            // Cập nhật giao diện trên luồng chính
            requireActivity().runOnUiThread(() -> {
                tvCity.setText(cityName);
                tvTime.setText("Today " + time);
                tvTemperature.setText(String.format(Locale.getDefault(), "%.0f°", temperature));
                tvDescription.setText(description);
                tvPressure.setText(pressure + " hPa");
                tvHumidity.setText("Độ ẩm " + humidity + "%");
                tvWind.setText(windSpeed + " km/h");

                // Ví dụ hiển thị cảnh báo
                if (temperature >= 35) {
                    alertView.setText("Cảnh báo: Trời rất nóng!");
                    alertView.setVisibility(View.VISIBLE);
                }
                else if (temperature >= 30) {
                    alertView.setText("Cảnh báo: Trời khá nóng");
                    alertView.setVisibility(View.VISIBLE);
                }

                else if (temperature <= 10) {
                    alertView.setText("Cảnh báo: Trời rất lạnh!");
                    alertView.setVisibility(View.VISIBLE);
                } else {
                    alertView.setVisibility(View.GONE);
                }
                if (windSpeed > 10) {
                    alertView.setText("Cảnh báo: Gió mạnh!");
                }
                if (humidity < 30) {
                    alertView.setText("Cảnh báo: Độ ẩm thấp, không khí khô! ");

                } else if (humidity > 80) {
                    alertView.setText("Cảnh báo: Độ ẩm cao, không khí ẩm ướt! ");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void parseForecast(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray listArray = jsonObject.getJSONArray("list");
            JSONObject cityObject = jsonObject.getJSONObject("city");
            int timezoneOffset = cityObject.getInt("timezone"); // Lấy múi giờ của thành phố (giây)

            List<String> addedDates = new ArrayList<>();
            forecastByDay.clear();
            forecastList.clear();

            long currentTimeMillis = System.currentTimeMillis();
            Date currentDate = new Date(currentTimeMillis + timezoneOffset * 1000L);
            SimpleDateFormat sdfCurrentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayString = sdfCurrentDate.format(currentDate);
            for (int i = 0; i < listArray.length(); i++) {
                JSONObject forecastObject = listArray.getJSONObject(i);

                long dt = forecastObject.getLong("dt");
                long localTimeMillis = dt * 1000L + timezoneOffset * 1000L;
                Date forecastDate = new Date(localTimeMillis);

                String forecastDateString = sdfCurrentDate.format(forecastDate);

                if (forecastDateString.equals(todayString)) continue;

                if (!addedDates.contains(forecastDateString)) {
                    addedDates.add(forecastDateString);

                    JSONObject mainObject = forecastObject.getJSONObject("main");
                    double temp = mainObject.getDouble("temp");

                    JSONArray weatherArray = forecastObject.getJSONArray("weather");
                    String description = weatherArray.getJSONObject(0).getString("description");

                    SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE", new Locale("vi"));
                    String dayName = sdfDay.format(forecastDate);
                    String icon = weatherArray.getJSONObject(0).getString("icon");

                    forecastByDay.add(new WeatherForecast(dayName, temp, description, icon));

                    if (forecastByDay.size() >= 6) break;
                }
            }

            for (int i = 0; i < 5; i++) {
                JSONObject forecastObject = listArray.getJSONObject(i);
                String dtTxt = forecastObject.getString("dt_txt");

                String hour = dtTxt.substring(11, 16);

                JSONObject mainObject = forecastObject.getJSONObject("main");
                double temp = mainObject.getDouble("temp");

                JSONArray weatherArray = forecastObject.getJSONArray("weather");
                String description = weatherArray.getJSONObject(0).getString("description");

                String icon = weatherArray.getJSONObject(0).getString("icon");

                forecastList.add(new WeatherForecast(hour, temp, description, icon));
            }

            requireActivity().runOnUiThread(() -> {
                forecastAdapter.notifyDataSetChanged();
                dailyForecastAdapter.notifyDataSetChanged();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Adapter classes
    public static class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
        private List<WeatherForecast> forecastList;

        public ForecastAdapter(List<WeatherForecast> forecastList) {
            this.forecastList = forecastList;
        }

        @NonNull
        @Override
        public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
            return new ForecastViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
            WeatherForecast forecast = forecastList.get(position);

            holder.tvTime.setText(forecast.getTime());
            holder.tvTemperature.setText(String.format("%.1f°C", forecast.getTemperature()));
//            holder.tvDescription.setText(forecast.getDescription());
            Glide.with(holder.itemView.getContext()).load("https://openweathermap.org/img/wn/" + forecast.getIcon() + "@2x.png").into(holder.ivIcon);
        }

        @Override
        public int getItemCount() {
            return forecastList.size();
        }

        public static class ForecastViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime;
            TextView tvTemperature;
//            TextView tvDescription;
            ImageView ivIcon;

            public ForecastViewHolder(View itemView) {
                super(itemView);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvTemperature = itemView.findViewById(R.id.tvTemperature);
//                tvDescription = itemView.findViewById(R.id.tvDescription);
                ivIcon = itemView.findViewById(R.id.imageViewWeatherIcon);
                if (tvTime == null || tvTemperature == null ||  ivIcon == null) {
                    Log.e("ForecastViewHolder", "Một hoặc nhiều views không được khởi tạo đúng cách");
                }
            }
        }
    }

    // Adapter for daily forecast
    public static class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DailyForecastViewHolder> {
        private List<WeatherForecast> forecastList;

        public DailyForecastAdapter(List<WeatherForecast> forecastList) {
            this.forecastList = forecastList;
        }

        @NonNull
        @Override
        public DailyForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_forecast, parent, false);
            return new DailyForecastViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DailyForecastViewHolder holder, int position) {
            WeatherForecast forecast = forecastList.get(position);

            holder.tvDay.setText(forecast.getTime());
            holder.tvTemperature.setText(String.format("%.1f°C", forecast.getTemperature()));
//            holder.tvDescription.setText(forecast.getDescription());
            Glide.with(holder.itemView.getContext()).load("https://openweathermap.org/img/wn/" + forecast.getIcon() + "@2x.png").into(holder.ivIcon);
        }

        @Override
        public int getItemCount() {
            return forecastList.size();
        }

        public static class DailyForecastViewHolder extends RecyclerView.ViewHolder {
            TextView tvDay;
            TextView tvTemperature;
//            TextView tvDescription;
            ImageView ivIcon;

            public DailyForecastViewHolder(View itemView) {
                super(itemView);
                tvDay = itemView.findViewById(R.id.tvDay);
                tvTemperature = itemView.findViewById(R.id.tvTemp);
//                tvDescription = itemView.findViewById(R.id.tvDescription);
                ivIcon = itemView.findViewById(R.id.imageViewWeatherIcon);
            }
        }
    }
}