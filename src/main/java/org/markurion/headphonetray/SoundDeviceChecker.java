package org.markurion.headphonetray;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SoundDeviceChecker {
    private ArrayList<String> availableDevices = new ArrayList<>();
    private String targetDeviceName;

    public SoundDeviceChecker(String targetDeviceName) {
        this.targetDeviceName = targetDeviceName;
    }

    public SoundDeviceChecker() {
    }

    public boolean isTargetDeviceAvailable() {
        if(targetDeviceName == null) {
            return false;
        }

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfo) {
            if (info.getName().equals(targetDeviceName)) {
                return true;
            }
        }
        return false;
    }

    public void setTargetDeviceName(String targetDeviceName) {
        this.targetDeviceName = targetDeviceName;
    }

    public String getTargetDeviceName() {
        return targetDeviceName;
    }

    public ArrayList<String> getListOfDevices() {
        availableDevices.clear();
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfo) {
            availableDevices.add(info.getName());
        }

        // Filter all the devices that starts from "Microphone" or "Port Microphone"
        availableDevices.removeIf(device -> device.startsWith("Microphone"));
        availableDevices.removeIf(device -> device.startsWith("Port Microphone"));

        //Filter all devices that does not contain "Port"
        availableDevices.removeIf(device -> !device.contains("Port"));

        return availableDevices;
    }

    /** Returns current active sound device */
    public String getActiveDevice() {
        return runJar();

        // This code was sent to the separate jar to execute in parallel. As separate thread.
//        String device = "";
//        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
//        for(Mixer.Info info : mixerInfo){
//            Mixer mixer = AudioSystem.getMixer(info);
//            if(mixer.isLineSupported(javax.sound.sampled.Port.Info.SPEAKER)){
//                device = info.getName();
//                break;
//            }
//        }
//        return device;
    }

    public String runJar(){
        String output = "";
        try {
            // Command to run the JAR file
            String command = "java -jar lib/audioDevice.jar";

            // Start the process
            Process process = Runtime.getRuntime().exec(command);

            // Capture the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                output = line;
            }

            // Wait for the process to complete
            process.waitFor();

            // Destroy the process
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    // Switch to target device by using svcl.exe
    public void switchToTargetDevice() {
        try {
            // get the name inside the ()
            String properDeviceName = targetDeviceName.substring(targetDeviceName.indexOf("(") + 1, targetDeviceName.indexOf(")"));

            // Command to switch the default audio device using nircmd
            String command ="svcl.exe /SetDefault \""+ properDeviceName +"\"";

            Process process = Runtime.getRuntime().exec(command);

            // Wait for the process to complete
            process.waitFor();

            // Destroy the process
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //Switch to target device
        SoundDeviceChecker soundDeviceChecker = new SoundDeviceChecker("Speakers (Avantree DG60)");
        soundDeviceChecker.switchToTargetDevice();

    }
}
