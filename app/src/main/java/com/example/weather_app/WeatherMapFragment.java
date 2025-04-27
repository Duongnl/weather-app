package com.example.weather_app;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeatherMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private Spinner tileTypeSpinner;
    private TileOverlay tileOverlay; // Thêm dòng này ở đầu class để lưu tileOverlay cũ
    private static final City[] CITIES = {
            new City(new LatLng(21.3043, 105.5630), "Vĩnh Phúc"),
            new City(new LatLng(21.1361, 106.0853), "Bắc Ninh"),
            new City(new LatLng(21.0560, 107.1911), "Quảng Ninh"),
            new City(new LatLng(20.8443, 106.6881), "Hải Phòng"),

            new City(new LatLng(20.9355, 106.1714), "Hưng Yên"),
            new City(new LatLng(20.4469, 106.3314), "Thái Bình"),
            new City(new LatLng(20.5896, 105.9042), "Hà Nam"),
            new City(new LatLng(20.4167, 106.1639), "Nam Định"),
            new City(new LatLng(20.2505, 105.9749), "Ninh Bình"),
            new City(new LatLng(22.1511, 104.9865), "Hà Giang"),
            new City(new LatLng(22.6637, 106.2857), "Cao Bằng"),
            new City(new LatLng(22.0856, 105.6351), "Bắc Kạn"),
            new City(new LatLng(21.8221, 105.4465), "Tuyên Quang"),
            new City(new LatLng(22.3366, 103.9147), "Lào Cai"),
            new City(new LatLng(21.7355, 104.2651), "Yên Bái"),
            new City(new LatLng(21.5941, 105.8549), "Thái Nguyên"),
            new City(new LatLng(21.8619, 106.4196), "Lạng Sơn"),
            new City(new LatLng(21.2780, 106.1889), "Bắc Giang"),
            new City(new LatLng(21.4143, 105.2641), "Phú Thọ"),
            new City(new LatLng(18.6820, 105.6733), "Vinh"),
            new City(new LatLng(21.9847, 103.0217), "Điện Biên"),
            new City(new LatLng(22.3390, 103.2251), "Lai Châu"),
            new City(new LatLng(21.5682, 103.9382), "Sơn La"),
            new City(new LatLng(19.2800, 104.0945), "Nghệ An"),
            new City(new LatLng(16.7425, 107.3220), "Quảng Trị"),
            new City(new LatLng(17.4700, 106.5953), "Đồng Hới"),
            new City(new LatLng(15.8801, 108.3380), "Hội An"),
            new City(new LatLng(15.5754, 108.4699), "Tam Kỳ"),

            new City(new LatLng(16.4635, 107.5510), "Huế"),
            new City(new LatLng(16.0471, 108.2068), "Đà Nẵng"),
            new City(new LatLng(15.1425, 108.7928), "Quảng Ngãi"),
            new City(new LatLng(13.9922, 109.1821), "Bình Định"),
            new City(new LatLng(13.0370, 108.0750), "Kon Tum"),
            new City(new LatLng(13.4292, 108.0215), "Gia Lai"),
            new City(new LatLng(10.9332, 106.2054), "Tây Ninh"),
            new City(new LatLng(10.9557, 106.6323), "Bình Dương"),
            new City(new LatLng(10.3671, 107.0834), "Vũng Tàu"),
            new City(new LatLng(10.7769, 106.7009), "Thành Phố Hồ Chí Minh"),
            new City(new LatLng(10.2014, 106.0637), "Long An"),
            new City(new LatLng(10.2288, 106.3497), "Bến Tre"),
            new City(new LatLng(9.9481, 106.3245), "Trà Vinh"),
            new City(new LatLng(9.9876, 105.9249), "Vĩnh Long"),
            new City(new LatLng(10.5133, 105.3910), "An Giang"),
            new City(new LatLng(10.0455, 105.7468), "Cần Thơ"),
            new City(new LatLng(9.9923, 105.9155), "Hậu Giang"),
            new City(new LatLng(9.6022, 105.9733), "Sóc Trăng"),
            new City(new LatLng(9.2900, 105.6980), "Bạc Liêu"),
            new City(new LatLng(9.1751, 104.8261), "Cà Mau")
    };


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WeatherMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WeatherMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeatherMapFragment newInstance(String param1, String param2) {
        WeatherMapFragment fragment = new WeatherMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        tileTypeSpinner = view.findViewById(R.id.tileTypeSpinner);
        // Các loại lớp tile (overlay) bạn muốn hỗ trợ
        String[] tileTypes = { "clouds_new", "precipitation_new", "temp_new", "wind_new", "pressure_new", "snow"};
        String[] tileTypesDisplay = { "Mây", "Lượng mưa", "Nhiệt độ", "Gió", "Áp suất", "Tuyết"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item, tileTypesDisplay);
        adapter.setDropDownViewResource(R.layout.spinner_item);


        tileTypeSpinner.setAdapter(adapter);
        tileTypeSpinner.setSelection(0);

//        lắng nghe
        tileTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTile = tileTypes[position];
                updateWeatherTileOverlay(selectedTile); // Gọi function để đổi layer
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });





        // Khởi tạo FusedLocationProviderClient để lấy vị trí
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Kiểm tra quyền truy cập vị trí
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    private Bitmap createCustomMarker(String cityName) {
        int width = 200;
        int height = 100;
        int cornerRadius = 20; // Đặt bán kính góc bo tròn

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Tạo paint cho nền
        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // màu nền
        paint.setAntiAlias(true); // Bật anti-aliasing để làm mềm các góc bo tròn

        // Vẽ hình chữ nhật với góc bo tròn
        RectF rectF = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        // Tạo paint cho chữ
        paint.setColor(Color.BLACK); // màu chữ
        paint.setTextSize(30f);
        paint.setTextAlign(Paint.Align.CENTER);

        // Vẽ text vào giữa hình
        canvas.drawText(cityName, width / 2, height / 2 + 10, paint);

        return bitmap;
    }

    // Lưu các Marker để quản lý việc show/hide
    private final List<Marker> cityMarkers = new ArrayList<>();

    private void addCityMarkers() {
        for (City city : CITIES) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(city.location)
                    .title(city.name)
                    .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(city.name)))); // dùng hình vuông chứa tên

            // Lưu marker vào list
            if (marker != null) {
                cityMarkers.add(marker);
            }
        }

        // Bắt sự kiện click (chỉ cần set 1 lần)
        mMap.setOnMarkerClickListener(marker -> {
            fetchWeatherForCity(marker);
            return false;
        });

        // Ban đầu update visibility luôn
        updateMarkerVisibility(mMap.getCameraPosition().zoom);

        // Nghe sự kiện zoom
        mMap.setOnCameraIdleListener(() -> {
            float zoom = mMap.getCameraPosition().zoom;
            updateMarkerVisibility(zoom);
        });
    }

    private void updateMarkerVisibility(float zoom) {
        boolean shouldShow = zoom > 8.0f; // Zoom > 10 mới hiện

        for (Marker marker : cityMarkers) {
            marker.setVisible(shouldShow);
        }
    }



    private void fetchWeatherForCity(Marker marker) {
        String city = marker.getTitle(); // Lấy tên thành phố từ marker

        String apiKey = "d26b87ca6c882f50c297a6fed54d2ecf";
        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&lang=vi&appid=%s&units=metric", city, apiKey);

        new Thread(() -> {
            try {
                // Gửi yêu cầu đến OpenWeather API
                java.net.URL apiUrl = new URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");

                // Đọc dữ liệu trả về
                java.io.InputStream inputStream = connection.getInputStream();
                java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
                final String response = scanner.hasNext() ? scanner.next() : "";

                // Parse JSON trả về
                org.json.JSONObject jsonObject = new org.json.JSONObject(response);

                // Lấy thông tin thành phố
                String cityName = jsonObject.getJSONObject("city").getString("name");

                // Lấy thông tin nhiệt độ (độ C)
                double temp = jsonObject.getJSONArray("list").getJSONObject(0).getJSONObject("main").getDouble("temp");

                // Lấy thông tin mô tả thời tiết
                String weather = jsonObject.getJSONArray("list")
                        .getJSONObject(0).getJSONArray("weather")
                        .getJSONObject(0).getString("description");

                // Lấy lượng mưa (nếu có)
                double rain = jsonObject.getJSONArray("list").getJSONObject(0).optJSONObject("rain") != null ?
                        jsonObject.getJSONArray("list").getJSONObject(0).getJSONObject("rain").optDouble("3h", 0) : 0;

                // Lấy tỷ lệ mây (clouds)
                int clouds = jsonObject.getJSONArray("list").getJSONObject(0).getJSONObject("clouds").getInt("all");

                // Tạo thông tin thời tiết để hiển thị
                String info = String.format("%s\n%.1f°C\nMưa: %.1f mm\nMây: %d%%\nThời tiết: %s", cityName, temp, rain, clouds, weather);

                // Cập nhật thông tin lên UI thread
                requireActivity().runOnUiThread(() -> {
                    marker.setTitle(cityName);
                    marker.setSnippet(info);
                    marker.showInfoWindow();
                });

            } catch (Exception e) {
                Log.e("WeatherFetch", "Lỗi khi fetch thời tiết", e);
            }
        }).start();
    }



    private void updateWeatherTileOverlay(final String layerName) {
        if (mMap == null) return;

        // Nếu có tileOverlay cũ thì remove trước
        if (tileOverlay != null) {
            tileOverlay.remove();
        }

        final String OWM_TILE_URL = "https://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=d26b87ca6c882f50c297a6fed54d2ecf";

        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                try {
                    String url = String.format(OWM_TILE_URL, layerName, zoom, x, y);
                    Log.d("TileURL", url);
                    return new URL(url);
                } catch (MalformedURLException e) {
                    Log.e("TileProvider", "Lỗi URL: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
        };

        tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider)
                .zIndex(99)
                .transparency(0.1f)
        );
    }

    private Marker currentMarker = null; // Dùng để lưu trữ marker hiện tại

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null; // dùng mặc định
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate layout tùy chỉnh
                View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                TextView title = view.findViewById(R.id.title);
                TextView snippet = view.findViewById(R.id.snippet);

                title.setText(marker.getTitle());
                snippet.setText(marker.getSnippet());

                return view;
            }
        });

        // Lắng nghe sự kiện nhấn vào bản đồ
        // Khi người dùng nhấn giữ vào bản đồ
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // Nếu có marker cũ, xóa đi
                if (currentMarker != null) {
                    currentMarker.remove();
                }

                // Thêm dấu đỏ tại vị trí người dùng nhấn
                currentMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Địa điểm bạn nhấn giữ")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


                // Thực hiện request dữ liệu thời tiết tại vị trí này
                getWeatherData(latLng);
            }
        });

        // Hiển thị vị trí hiện tại nếu đã có quyền
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getUserLocation(); // Lấy vị trí người dùng
        }

        addCityMarkers();

    }


    private void getWeatherData(LatLng latLng) {
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=d26b87ca6c882f50c297a6fed54d2ecf", latLng.latitude, latLng.longitude);

        // Gọi API trong một thread mới
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL weatherApiUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) weatherApiUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(15000); // 15 seconds timeout
                    connection.setReadTimeout(15000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        // Parse dữ liệu JSON (nếu cần)
                        String response = stringBuilder.toString();
                        parseWeatherData(response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseWeatherData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject main = jsonObject.getJSONObject("main");
            double temperatureKelvin = main.getDouble("temp");  // Nhiệt độ (Kelvin)
            double temperatureCelsius = temperatureKelvin - 273.15;  // Chuyển từ Kelvin sang độ C

            JSONObject clouds = jsonObject.getJSONObject("clouds");
            int cloudiness = clouds.getInt("all");  // Mức độ mây

            JSONObject rain = jsonObject.optJSONObject("rain");  // Lượng mưa
            final double finalRainAmount = (rain != null) ? rain.optDouble("1h", 0) : 0;

            final double finalTemperatureCelsius = temperatureCelsius;
            final int finalCloudiness = cloudiness;

            // Update UI
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String weatherInfo = String.format(
                            "Nhiệt độ: %.2f°C\nMây: %d%%\nMưa: %.2f mm",
                            finalTemperatureCelsius, finalCloudiness, finalRainAmount
                    );
//                    Toast.makeText(getContext(), weatherInfo, Toast.LENGTH_LONG).show();
                    showPopup(weatherInfo);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showPopup(String weatherInfo) {
        // Tạo layout cho PopupWindow
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_notification, null);

        // Cập nhật nội dung TextView trong popup
        TextView notificationText = popupView.findViewById(R.id.notification_text);
        notificationText.setText(weatherInfo);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true); // Cho phép đóng Popup khi click ngoài vùng popup
        popupWindow.setOutsideTouchable(true);

        // Đóng popup khi nhấn nút đóng
//        ImageButton closeButton = popupView.findViewById(R.id.close_button);
//        closeButton.setOnClickListener(v -> popupWindow.dismiss());

        // Hiển thị PopupWindow
        popupWindow.showAtLocation(getActivity().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }








    private void getUserLocation() {
        // Kiểm tra quyền trước khi lấy vị trí
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Lấy vị trí hiện tại
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                            mMap.addMarker(new MarkerOptions().position(userLocation).title("Vị trí của bạn"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 5));
                        }
                    }
                });
    }

    // Xử lý kết quả yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }
    }
}