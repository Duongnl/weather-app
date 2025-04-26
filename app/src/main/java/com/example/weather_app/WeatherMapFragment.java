package com.example.weather_app;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.Manifest;
import android.content.pm.PackageManager;
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
    private static final LatLng[] CITIES = {
            new LatLng(10.7769, 106.7009), // Ho Chi Minh City
            new LatLng(21.0285, 105.8542), // Hanoi
            new LatLng(16.0471, 108.2068), // Da Nang
            new LatLng(11.9404, 108.4583)  // Da Lat
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
        addWeatherTileOverlay();


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



    private void addWeatherTileOverlay() {
        final String OWM_TILE_URL = "https://tile.openweathermap.org/map/sea_level/%d/%d/%d.png?appid=d26b87ca6c882f50c297a6fed54d2ecf";

        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                try {
                    String url = String.format(OWM_TILE_URL, zoom, x, y);
                    Log.d("TileURL", String.format(OWM_TILE_URL, zoom, x, y));
                    return new URL(url);
                } catch (MalformedURLException e) {
                    Log.e("TileProvider", "Lỗi URL: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
        };


        mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider)
                .zIndex(99) // thêm zIndex để chồng đúng thứ tự
                .transparency(0.1f) // thêm nhẹ độ trong suốt cho đẹp
        );
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