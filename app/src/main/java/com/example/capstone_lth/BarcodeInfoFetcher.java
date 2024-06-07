package com.example.capstone_lth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BarcodeInfoFetcher extends Thread {

    private static final String BASE_URL = "https://retaildb.or.kr/service/product_info/search/";
    private String barcode;
    private Handler handler;

    public BarcodeInfoFetcher(String barcode, Handler handler) {
        this.barcode = barcode;
        this.handler = handler;
    }

    @Override
    public void run() {
        String result = null;
        try {
            String _url = BASE_URL + barcode; //URL 바코드 쿼리
            // Open the connection
            URL url = new URL(_url); // URL에 바코드정보 추가해서 검색
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream(); // JSON 데이터 string으로 가져옴

            // Get the stream
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            // Set the result
            result = builder.toString(); // 바코드 검색 결과
            Log.d("result : ", result);

            // To JsonObject
            JSONObject jsonObj = new JSONObject(result);
            JSONArray baseItems = jsonObj.getJSONArray("baseItems");

            String _target_value = "";

            for (int i = 0; i < baseItems.length(); i++) {
                JSONObject tmp = (JSONObject) baseItems.get(i);
                String _name = tmp.getString("name");
                String _name_value = tmp.getString("value");

                if (_name.equalsIgnoreCase("상품명(국문)")) {
                    _target_value = _name_value;

                }
            }

            Log.d("json test: ", "target value : " + _target_value);

            Bundle bundle = new Bundle();
            bundle.putString("foodName", _target_value);

            Message message = new Message();
            message.setData(bundle);
            handler.sendMessage(message);

        } catch (Exception e) {
            Log.e("REST_API", "GET method failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
