package com.magpi.util;

import com.fazecast.jSerialComm.SerialPort;
import com.magpi.model.Measurement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Manages serial port connections for communicating with Arduino devices
 */
public class  SerialPortManager {
    private SerialPort serialPort;
    private Thread readThread;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Consumer<Measurement> measurementConsumer;
    
    /**
     * Default constructor
     */
    public SerialPortManager() {
    }
    
    /**
     * Detects and returns the port name for an Arduino device
     * @return The system port name or null if not found
     */
    public String detectArduinoPort() {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            // Arduino usually has "USB-SERIAL CH340" or "Arduino" in its name
            if (port.getDescriptivePortName().contains("USB-SERIAL CH340") ||
                    port.getDescriptivePortName().contains("Arduino")) {
                return port.getSystemPortName();
            }
        }

        // If no Arduino port is found, try to find any USB serial port
        for (SerialPort port : ports) {
            if (port.getDescriptivePortName().contains("USB")) {
                return port.getSystemPortName();
            }
        }

        return null; // No suitable port found
    }
    
    /**
     * Opens a connection to the specified port
     * @param portName The system name of the port to open
     * @return true if successful, false otherwise
     */
    public boolean openConnection(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        
        return serialPort.openPort();
    }
    
    /**
     * Starts reading data from the serial port
     * @param consumer A consumer function to handle the measurements
     */
    public void startReading(Consumer<Measurement> consumer) {
        if (serialPort == null || !serialPort.isOpen()) {
            throw new IllegalStateException("Serial port is not open");
        }
        
        this.measurementConsumer = consumer;
        running.set(true);
        
        readThread = new Thread(this::readData);
        readThread.setDaemon(true);
        readThread.start();
    }
    
    private void readData() {
        try (InputStream in = serialPort.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            
            while (running.get()) {
                if (serialPort.bytesAvailable() > 0) {
                    String line = reader.readLine();
                    if (line != null && !line.isEmpty()) {
                        System.out.println("Received: " + line); // For debugging
                        processReading(line);
                    }
                } else {
                    Thread.sleep(50);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error reading from serial port: " + e.getMessage());
        }
    }
    
    private void processReading(String data) {
        try {
            String[] parts = data.split(":");
            if (parts.length == 3) {
                String meter = parts[0].trim();
                double currentValue = Double.parseDouble(parts[1].trim());
                double duration = Double.parseDouble(parts[2].trim());
                
                // Determine meter type
                String meterType = "";
                if (meter.equalsIgnoreCase("Meter 1")) {
                    meterType = "Headshot";
                } else if (meter.equalsIgnoreCase("Meter 2")) {
                    meterType = "Coilshot";
                }
                
                // Create and pass measurement to consumer
                if (!meterType.isEmpty() && measurementConsumer != null) {
                    Measurement measurement = new Measurement(meterType, currentValue, duration);
                    measurementConsumer.accept(measurement);
                }
            } else {
                System.err.println("Invalid data format: " + data);
            }
        } catch (Exception e) {
            System.err.println("Error processing reading: " + e.getMessage());
        }
    }
    
    /**
     * Stops reading from the serial port
     */
    public void stopReading() {
        running.set(false);
        if (readThread != null && readThread.isAlive()) {
            try {
                readThread.interrupt();
                readThread.join(1000); // Wait up to 1 second for thread to finish
            } catch (InterruptedException e) {
                System.err.println("Interrupted while stopping read thread");
            }
        }
    }
    
    /**
     * Closes the serial port connection
     */
    public void closeConnection() {
        if (serialPort != null && serialPort.isOpen()) {
            stopReading();
            serialPort.closePort();
            serialPort = null;
        }
    }
} 