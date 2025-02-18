package org.markurion.headphonetray;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class SoundActiveCheck {
    public static void main(String[] args) {
        String device = "";
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for(Mixer.Info info : mixerInfo){
            Mixer mixer = AudioSystem.getMixer(info);
            if(mixer.isLineSupported(javax.sound.sampled.Port.Info.SPEAKER)){
                device = info.getName();
                break;
            }
        }
        System.out.println(device);
    }

    public void runJar() {

    }
}
