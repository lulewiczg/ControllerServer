package com.github.lulewiczg.controller.audio;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.common.Common;
import com.github.lulewiczg.controller.server.ExceptionLoggingService;

@Lazy
@Service
@Scope("prototype")
public class AudioService implements Closeable {

    private static final Logger log = LogManager.getLogger(AudioService.class);

    private OutputStream out;
    private ExecutorService exec;

    @Autowired
    private ExceptionLoggingService exceptionService;

    private TargetDataLine line;

    public AudioService(OutputStream out) {
        this.out = out;
        exec = Executors.newSingleThreadExecutor();
    }

    public void stream() throws LineUnavailableException {
        // AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
        // DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        //
        // // checks if system supports the data line
        // if (!AudioSystem.isLineSupported(info)) {
        // log.error("Sound not supported");
        // throw new AudioException();
        // }
        // line = (TargetDataLine) AudioSystem.getLine(info);
        // line.open(format);
        // line.start();
        exec.submit(() -> {
            AudioInputStream ais;
            try {
                ais = AudioSystem.getAudioInputStream(new File("D:/Muzyka/Inne/Simon & Garkfunkel - The Sound of Silence.wav"));
            } catch (UnsupportedAudioFileException | IOException e1) {
                e1.printStackTrace();
                return;
            }
            try {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, out);
            } catch (IOException e) {
                exceptionService.info(log, "Error during streaming sound", e);
            }
            // AudioInputStream ais = new AudioInputStream(line);
            // int bytesRead;
            // byte[] buff = new byte[16384];
            // try {
            // while ((bytesRead = ais.read(buff)) != -1) {
            // out.write(buff, 0, bytesRead);
            // }
            // } catch (IOException e) {
            // exceptionService.info(log, "Error during streaming sound", e);
            // }
        });
    }

    @Override
    public void close() throws IOException {
        if (line != null) {
            line.close();
        }
        Common.close(out);
        exec.shutdownNow();
    }
}
