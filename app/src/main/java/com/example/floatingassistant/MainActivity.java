package com.example.floatingassistant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int OVERLAY_REQUEST = 10;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 48, 36, 36);
        TextView title = new TextView(this);
        title.setText("مساعد البرمجة العائم"); title.setTextSize(24); title.setPadding(0, 0, 0, 18);
        TextView info = new TextView(this);
        info.setText("فعّل صلاحية الظهور فوق التطبيقات ليظهر زر المساعد فوق Klencod وأي محرر آخر."); info.setTextSize(16); info.setPadding(0, 0, 0, 24);
        Button start = new Button(this); start.setText("تفعيل المساعد العائم");
        start.setOnClickListener(v -> enableAndStart());
        Button stop = new Button(this); stop.setText("إيقاف المساعد");
        stop.setOnClickListener(v -> stopService(new Intent(this, FloatingAssistantService.class)));
        root.addView(title); root.addView(info); root.addView(start); root.addView(stop);
        setContentView(root);
    }

    private void enableAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, OVERLAY_REQUEST);
        } else startService(new Intent(this, FloatingAssistantService.class));
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == OVERLAY_REQUEST && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)))
            startService(new Intent(this, FloatingAssistantService.class));
    }
}