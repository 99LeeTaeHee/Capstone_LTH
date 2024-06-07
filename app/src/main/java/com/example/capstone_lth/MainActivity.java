package com.example.capstone_lth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private String myTestBarcode = "8801097235014";
    private String myScanBarcode;
    private TextView textView;
    private Handler handler;
    private DBHelper dbHelper;

    private final ActivityResultLauncher<Intent> barcodeScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    myScanBarcode = result.getData().getStringExtra("SCAN_RESULT");
                    if (myScanBarcode != null) {
                        textView.setText(myScanBarcode);
                        fetchBarcodeInfo();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scan_btn = findViewById(R.id.scanner);
        textView = findViewById(R.id.text1);

        scan_btn.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) // 카메라 권한이 없을경우 요청
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else { // 이미 카메라권한이 있을경우 바코드스캔 진행
                startBarcodeScannerActivity();
            }
        });

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                textView.setText(bundle.getString("foodName"));
            }
        };
    }

    private void startBarcodeScannerActivity() { // 바코드 스캔
        Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
        barcodeScannerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScannerActivity();
            } else {
                // 권한이 거부되었을 때의 처리
                textView.setText("카메라 권한이 필요합니다.");
            }
        }
    }

    private void fetchBarcodeInfo() {
        if (myScanBarcode == null) {
            myScanBarcode = myTestBarcode;
        }

        String productName = dbHelper.getBarcodeInfo(myScanBarcode);
        if (productName != null) {
            textView.setText(productName);
        } else {
            BarcodeInfoFetcher fetcher = new BarcodeInfoFetcher(myScanBarcode, handler);
            fetcher.start();
        }
    }

    Handler handler2 = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            String productName = bundle.getString("foodName");
            textView.setText(productName);
            dbHelper.addProductName(productName); // 제품명 데이터베이스에 저장
        }
    };

    private void displayProductNames() {
        List<String> productNames = dbHelper.getAllProductNames();
        StringBuilder stringBuilder = new StringBuilder();

        for (String productName : productNames) {
            stringBuilder.append(productName).append("\n");
        }

        textView.setText(stringBuilder.toString());
    }

}
