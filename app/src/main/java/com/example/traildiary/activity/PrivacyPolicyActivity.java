package com.example.traildiary.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.traildiary.R;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        TextView tvContent = findViewById(R.id.tv_content);
        tvContent.setText(readPrivacyPolicy());
    }

    private String readPrivacyPolicy() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.privacy_policy);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            inputStream.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "无法读取隐私政策内容";
        }
    }
}