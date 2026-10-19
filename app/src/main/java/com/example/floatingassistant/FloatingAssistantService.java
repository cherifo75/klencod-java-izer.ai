package com.example.floatingassistant;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingAssistantService extends Service {
    private WindowManager manager;
    private TextView bubble;
    private LinearLayout panel;
    private WindowManager.LayoutParams bubbleParams;
    private int dp(float n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, float radius) { GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g; }

    @Override public void onCreate() {
        super.onCreate(); manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) { stopSelf(); return; }
        showBubble();
    }

    private WindowManager.LayoutParams params(int width, int height, int gravity) {
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(width, height, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        p.gravity = gravity; p.x = dp(12); p.y = dp(120); return p;
    }

    private void showBubble() {
        bubble = new TextView(this); bubble.setText("✦"); bubble.setTextSize(25); bubble.setTextColor(Color.WHITE); bubble.setGravity(Gravity.CENTER); bubble.setBackground(bg(Color.rgb(103, 80, 164), 40));
        bubbleParams = params(dp(58), dp(58), Gravity.END | Gravity.CENTER_VERTICAL);
        bubble.setOnClickListener(v -> togglePanel());
        manager.addView(bubble, bubbleParams);
    }

    private void togglePanel() { if (panel == null) showPanel(); else { manager.removeView(panel); panel = null; } }

    private void showPanel() {
        panel = new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(16), dp(14), dp(16), dp(14)); panel.setBackground(bg(Color.rgb(25, 25, 34), 18));
        WindowManager.LayoutParams p = params(dp(330), dp(430), Gravity.CENTER); p.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        TextView title = new TextView(this); title.setText("مساعد البرمجة  ✦"); title.setTextColor(Color.WHITE); title.setTextSize(19); title.setPadding(0, 0, 0, dp(10));
        EditText input = new EditText(this); input.setHint("اكتب الخطأ أو السؤال هنا..."); input.setTextColor(Color.WHITE); input.setHintTextColor(Color.GRAY); input.setGravity(Gravity.TOP | Gravity.RIGHT); input.setMinLines(5); input.setPadding(dp(10), dp(10), dp(10), dp(10)); input.setBackground(bg(Color.rgb(42, 42, 56), 10));
        TextView answer = new TextView(this); answer.setTextColor(Color.rgb(220, 220, 230)); answer.setTextSize(14); answer.setPadding(dp(4), dp(12), dp(4), dp(8)); answer.setText("اكتب سؤالًا مثل: لماذا يظهر NullPointerException؟");
        LinearLayout buttons = new LinearLayout(this); buttons.setGravity(Gravity.CENTER); Button ask = new Button(this); ask.setText("تحليل"); Button copy = new Button(this); copy.setText("نسخ"); Button close = new Button(this); close.setText("إغلاق");
        buttons.addView(ask, new LinearLayout.LayoutParams(0, dp(52), 1)); buttons.addView(copy, new LinearLayout.LayoutParams(0, dp(52), 1)); buttons.addView(close, new LinearLayout.LayoutParams(0, dp(52), 1));
        ask.setOnClickListener(v -> answer.setText(localAnswer(input.getText().toString())));
        copy.setOnClickListener(v -> { ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); cm.setPrimaryClip(ClipData.newPlainText("مساعد البرمجة", answer.getText())); });
        close.setOnClickListener(v -> togglePanel());
        panel.addView(title); panel.addView(input, new LinearLayout.LayoutParams(-1, dp(130))); panel.addView(answer, new LinearLayout.LayoutParams(-1, 0, 1)); panel.addView(buttons); manager.addView(panel, p);
    }

    private String localAnswer(String q) {
        String s = q.toLowerCase();
        if (s.contains("nullpointer") || s.contains("null pointer")) return "NullPointerException يعني أنك تستعمل كائنًا قيمته null. تحقق من مكان إنشاء الكائن، أضف فحصًا قبل الاستعمال، واستعمل السطر الموجود في Stack Trace.";
        if (s.contains("syntax") || s.contains("خطأ نحوي")) return "راجع القوسين والفواصل وعلامات الاقتباس. ابدأ بقراءة أول خطأ في سجل البناء، لأن الأخطاء التالية قد تكون نتيجة له.";
        if (s.contains("flutter") || s.contains("dart")) return "في Flutter جرّب: flutter pub get ثم flutter analyze. راجع build و setState وتأكد من عدم استدعاء setState بعد dispose.";
        if (s.contains("java")) return "في Java افحص نوع المتغير، الاستيرادات، وStack Trace. جرّب تقسيم الكود إلى دوال صغيرة واكتب رسالة الخطأ كاملة.";
        return "لم أفهم السؤال بعد. أرسل رسالة الخطأ كاملة مع 10 أسطر من الكود المحيط بها، وسأساعدك في تحديد السبب والحل.";
    }

    @Override public int onStartCommand(Intent i, int flags, int id) { return START_STICKY; }
    @Override public void onDestroy() { if (bubble != null) manager.removeView(bubble); if (panel != null) manager.removeView(panel); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
