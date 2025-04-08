package com.pos.services;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class BarcodeScannerService implements NativeKeyListener {


    private static final Logger LOGGER = LoggerFactory.getLogger(BarcodeScannerService.class);

    @Value("${app.barcode-reader.timeout-ms:250}")
    private long barcodeScanTimeout;

    private final StringBuilder buffer = new StringBuilder();
    private ScheduledExecutorService executor;
    private Consumer<String> barcodeCallback;
    private boolean scanning = false;
    private long lastInputTime = 0;

    @PostConstruct
    public void init() {
        try {
            // Registrar el hook global para capturar eventos de teclado
            GlobalScreen.registerNativeHook();

            // Añadir este objeto como listener
            GlobalScreen.addNativeKeyListener(this);

            // Crear un executor para manejar el timeout
            executor = Executors.newSingleThreadScheduledExecutor();

            //log.info("Escáner de código de barras inicializado correctamente");
        } catch (NativeHookException e) {
            LOGGER.error("Error al inicializar el hook global para el escáner: ", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            GlobalScreen.unregisterNativeHook();
            GlobalScreen.removeNativeKeyListener(this);
            executor.shutdown();
        } catch (NativeHookException e) {
            LOGGER.error("Error al desregistrar el hook global", e);
        }
    }

    /**
     * Establece el callback que se llamará cuando se complete un escaneo de código de barras
     */
    public void setBarcodeCallback(Consumer<String> callback) {
        this.barcodeCallback = callback;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        long currentTime = System.currentTimeMillis();

        // Si ha pasado mucho tiempo desde la última entrada, consideramos que es un nuevo escaneo
        if (currentTime - lastInputTime > barcodeScanTimeout) {
            buffer.setLength(0);  // Limpiar buffer
            scanning = true;
        }

        lastInputTime = currentTime;

        if (scanning) {
            int keyCode = e.getKeyCode();

            // Verificar si es un Enter (fin de escaneo)
            if (keyCode == NativeKeyEvent.VC_ENTER) {
                String barcode = buffer.toString().trim();

                // Solo procesar si hay algo en el buffer
                if (!barcode.isEmpty() && barcodeCallback != null) {
                    // Programar la ejecución del callback en un momento futuro
                    // Esto permite que el evento de teclado termine su procesamiento
                    executor.schedule(() -> {
                        barcodeCallback.accept(barcode);
                        LOGGER.debug("Código de barras detectado: {}", barcode);
                    }, 10, TimeUnit.MILLISECONDS);
                }

                buffer.setLength(0);  // Limpiar buffer
                scanning = false;
            } else {
                // Convertir el código de tecla a carácter y añadirlo al buffer
                char keyChar = e.getKeyChar();
                if (keyChar != NativeKeyEvent.CHAR_UNDEFINED) {
                    buffer.append(keyChar);
                }
            }

            // Si el buffer se hace muy grande, descartarlo (probablemente no es un código de barras)
            if (buffer.length() > 50) {
                buffer.setLength(0);
                scanning = false;
            }
        }
    }
}