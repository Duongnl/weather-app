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
        rvHourlyForecast.setAdapter(forecastAdapter);

        // Giả sử bạn đã có một hàm để lấy dữ liệu dự báo từ API hoặc đâu đó
        // parseForecast(json);  // Dữ liệu sẽ được nạp vào từ parseForecast(json)

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
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

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

    private void parseWeather(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject mainObject = jsonObject.getJSONObject("main");

            double temp = mainObject.getDouble("temp");

            String cityName = jsonObject.getString("name");

            // Update UI with weather data
            requireActivity().runOnUiThread(() -> {
                tvCity.setText(cityName);
                tvTemperature.setText(String.format("%.1f°C", temp));
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
            // Xóa danh sách cũ nếu có
            forecastList.clear();

            long currentTimeMillis = System.currentTimeMillis();
            Date currentDate = new Date(currentTimeMillis + timezoneOffset * 1000L);
            SimpleDateFormat sdfCurrentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayString = sdfCurrentDate.format(currentDate);

            for (int i = 0; i < listArray.length(); i++) {
                JSONObject forecastObject = listArray.getJSONObject(i);

                // Chuyển đổi thời gian dự báo sang múi giờ địa phương
                long dt = forecastObject.getLong("dt");
                long localTimeMillis = dt * 1000L + timezoneOffset * 1000L;
                Date forecastDate = new Date(localTimeMillis);

                // Định dạng ngày của dự báo
                String forecastDateString = sdfCurrentDate.format(forecastDate);

                // Bỏ qua nếu là ngày hôm nay
                if (forecastDateString.equals(todayString)) continue;

                // Kiểm tra xem ngày đã được thêm chưa
                if (!addedDates.contains(forecastDateString)) {
                    addedDates.add(forecastDateString);

                    // Lấy thông tin thời tiết
                    JSONObject mainObject = forecastObject.getJSONObject("main");
                    double temp = mainObject.getDouble("temp");
                    

                    JSONArray weatherArray = forecastObject.getJSONArray("weather");
                    String description = weatherArray.getJSONObject(0).getString("description");

                    // Định dạng tên ngày (VD: "Thứ Hai")
                    SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE", new Locale("vi"));
                    String dayName = sdfDay.format(forecastDate);

                    forecastByDay.add(new WeatherForecast(dayName, temp, description));

                    // Dừng khi đủ 5 ngày
                    if (forecastByDay.size() >= 6) break;
                }
            }

            Log.e("forecastDay", "Size: " + forecastByDay.size());
            for (WeatherForecast forecast : forecastByDay) {
                Log.e("forecastDay", "forecastDay: " + forecast.getTime() + ", " + forecast.getTemperature() + ", " + forecast.getDescription());
            }


            for (int i = 0; i < 5; i++) {  // Lấy 5 dự báo tiếp theo
                JSONObject forecastObject = listArray.getJSONObject(i);
                String dtTxt = forecastObject.getString("dt_txt");

                // Cắt chuỗi dtTxt để lấy phần giờ và phút
                String hour = dtTxt.substring(11, 16);  // Lấy phần từ 11 đến 16 (HH:mm)

                JSONObject mainObject = forecastObject.getJSONObject("main");
                double temp = mainObject.getDouble("temp");

                JSONArray weatherArray = forecastObject.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                String description = weatherObject.getString("description");

                // Tạo đối tượng WeatherForecast với giờ, nhiệt độ và mô tả
                WeatherForecast forecast = new WeatherForecast(hour, temp, description);
                // Thêm vào danh sách
                forecastList.add(forecast);

                // In ra dữ liệu của từng dự báo
                Log.e("WeatherForecast", "Hour: " + hour + ", Temperature: " + temp + ", Description: " + description);
            }

            // In ra dữ liệu của forecastList sau khi đã thêm các dự báo
            Log.e("forecastList", "Size: " + forecastList.size());
            for (WeatherForecast forecast : forecastList) {
                Log.e("forecastList", "Forecast: " + forecast.getTime() + ", " + forecast.getTemperature() + ", " + forecast.getDescription());
            }

            // Cập nhật lại Adapter khi có dữ liệu mới
            requireActivity().runOnUiThread(() -> {
                forecastAdapter.notifyDataSetChanged(); // cập nhật hourly forecast
                dailyForecastAdapter.notifyDataSetChanged(); // cập nhật daily forecast
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {
        private List<WeatherForecast> dailyForecasts;

        public DailyForecastAdapter(List<WeatherForecast> dailyForecasts) {
            this.dailyForecasts = dailyForecasts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_daily_forecast, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeatherForecast forecast = dailyForecasts.get(position);
            holder.tvDay.setText(forecast.getTime());
            holder.tvTemp.setText(String.format("%.1f°C", forecast.getTemperature()));
            holder.tvDesc.setText(forecast.getDescription());
        }

        @Override
        public int getItemCount() {
            return dailyForecasts.size();
        }

        public  class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDay, tvTemp, tvDesc;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDay = itemView.findViewById(R.id.tvDay);
                tvTemp = itemView.findViewById(R.id.tvTemp);
                tvDesc = itemView.findViewById(R.id.tvDesc);
            }
        }
    }




    @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Log.e("MainFragment", "Permission denied by user");
            }
        }
    }

    public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

        private List<WeatherForecast> forecastList;

        public ForecastAdapter(List<WeatherForecast> forecastList) {
            this.forecastList = forecastList;

        }

        @NonNull
        @Override
        public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_forecast, parent, false);
            return new ForecastViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
            WeatherForecast forecast = forecastList.get(position);
            holder.tvTime.setText(forecast.getTime());
            holder.tvTemperature.setText(String.format("%.1f°C", forecast.getTemperature()));
            holder.tvDescription.setText(forecast.getDescription());

        }

        @Override
        public int getItemCount() {
            return forecastList.size();
        }

        class ForecastViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime;
            TextView tvTemperature;
            TextView tvDescription;

            public ForecastViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvTemperature = itemView.findViewById(R.id.tvTemperature);
                tvDescription = itemView.findViewById(R.id.tvDescription);
            }
        }
    }



}



