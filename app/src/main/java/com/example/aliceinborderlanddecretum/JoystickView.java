package com.example.aliceinborderlanddecretum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

// NOTA: Usamos View. Podríamos usar SurfaceView para mejor rendimiento,
// pero para empezar, View es más simple.
public class JoystickView extends View {

    // Variables para dibujar
    private Paint outerCirclePaint;
    private Paint innerCirclePaint;

    // Variables de posición
    private float centerX;
    private float centerY;
    private float outerCircleRadius;
    private float innerCircleRadius;

    // Posición actual del "stick" (la rueda pequeña)
    private float hatX;
    private float hatY;

    // Interface para comunicar el movimiento a nuestra Activity
    public interface JoystickListener {
        // El ángulo (en radianes) y la "fuerza" (0 a 1)
        void onMove(float angle, float strength);
    }

    private JoystickListener joystickListener;

    // --- Constructores (necesarios para que Android use esta Vista) ---
    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    // --- Fin de Constructores ---

    private void init() {
        // Configura cómo se dibujarán los círculos
        outerCirclePaint = new Paint();
        outerCirclePaint.setColor(Color.GRAY);
        outerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        outerCirclePaint.setAlpha(128); // Semi-transparente

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(Color.DKGRAY);
        innerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    // Método para que la Activity se "registre" a los eventos
    public void setJoystickListener(JoystickListener listener) {
        this.joystickListener = listener;
    }

    // Se llama cuando Android sabe el tamaño de esta Vista
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Centra el joystick
        centerX = w / 2f;
        centerY = h / 2f;
        // El círculo exterior será la mitad del tamaño más pequeño
        outerCircleRadius = Math.min(w, h) / 2f * 0.8f; // 80% del tamaño
        innerCircleRadius = outerCircleRadius / 2f; // La rueda es la mitad del joystick

        // Posición inicial de la rueda (centrada)
        hatX = centerX;
        hatY = centerY;
    }

    // Se llama para dibujar la vista
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 1. Dibuja el círculo exterior
        canvas.drawCircle(centerX, centerY, outerCircleRadius, outerCirclePaint);
        // 2. Dibuja la rueda interior
        canvas.drawCircle(hatX, hatY, innerCircleRadius, innerCirclePaint);
    }

    // --- ¡LA PARTE MÁS IMPORTANTE! ---
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        // Calcula la distancia y el ángulo desde el centro
        float deltaX = touchX - centerX;
        float deltaY = touchY - centerY;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        // atan2 nos da el ángulo en radianes
        float angle = (float) Math.atan2(deltaY, deltaX);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Si el toque está DENTRO del círculo exterior
                if (distance < outerCircleRadius) {
                    hatX = touchX;
                    hatY = touchY;
                } else {
                    // Si se sale, "fija" la rueda al borde
                    hatX = centerX + outerCircleRadius * (float) Math.cos(angle);
                    hatY = centerY + outerCircleRadius * (float) Math.sin(angle);
                }

                // Calcula la "fuerza" (0 = centro, 1 = borde)
                float strength = distance / outerCircleRadius;
                if (strength > 1) strength = 1;

                // ¡Avisa a la Activity!
                if (joystickListener != null) {
                    joystickListener.onMove(angle, strength);
                }
                break;

            case MotionEvent.ACTION_UP:
                // Cuando sueltas el dedo, resetea la rueda al centro
                hatX = centerX;
                hatY = centerY;

                // Avisa que el movimiento paró
                if (joystickListener != null) {
                    joystickListener.onMove(angle, 0); // Fuerza 0
                }
                break;
        }

        invalidate(); // Vuelve a dibujar la vista
        return true; // Hemos manejado el evento
    }
}