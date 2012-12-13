

package com.sound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;



public abstract class Encoder implements Runnable {
    
    private PipedInputStream rawData;
    private PipedOutputStream encodedData;
    private boolean isStopped;
    private FileOutputStream encoderOutput;
    private FileOutputStream encoderInput;
    private boolean mediaDebug;
    private String peersHome;
    private CountDownLatch latch;

    public Encoder(PipedInputStream rawData, PipedOutputStream encodedData,
            boolean mediaDebug, String peersHome,
            CountDownLatch latch) {
        this.rawData = rawData;
        this.encodedData = encodedData;
        this.mediaDebug = mediaDebug;
        this.peersHome = peersHome;
        this.latch = latch;
        isStopped = false;
    }
    
    public void run() {
        byte[] buffer;
        if (mediaDebug) {
            SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String date = simpleDateFormat.format(new Date());
            String dir = peersHome + File.separator + SoundManager.MEDIA_DIR
                + File.separator;
            String fileName = dir + date + "_g711_encoder.output";
            try {
                encoderOutput = new FileOutputStream(fileName);
                fileName = dir + date + "_g711_encoder.input";
                encoderInput = new FileOutputStream(fileName);
            } catch (FileNotFoundException e) {
                System.out.println(e.toString());
                return;
            }
        }
        int ready;
        while (!isStopped) {
            try {
                ready = rawData.available();
                while (ready == 0 && !isStopped) {
                    try {
                        Thread.sleep(2);
                        ready = rawData.available();
                    } catch (InterruptedException e) {
                        System.out.println(e.toString());
                    }
                }
                if (isStopped) {
                    break;
                }
                buffer = new byte[ready];
                rawData.read(buffer);
                if (mediaDebug) {
                    try {
                        encoderInput.write(buffer);
                    } catch (IOException e) {
                        System.out.println(e.toString());
                    }
                }
            } catch (IOException e) {
                System.out.println(e.toString());
                return;
            }
            
            byte[] ulawData = process(buffer);
            if (mediaDebug) {
                try {
                    encoderOutput.write(ulawData);
                } catch (IOException e) {
                    System.out.println(e.toString());
                    break;
                }
            }
            try {
                encodedData.write(ulawData);
                encodedData.flush();
            } catch (IOException e) {
                System.out.println(e.toString());
                return;
            }
        }
        if (mediaDebug) {
            try {
                encoderOutput.close();
                encoderInput.close();
            } catch (IOException e) {
                System.out.println(e.toString());
                return;
            }
        }
        latch.countDown();
        if (latch.getCount() != 0) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public abstract byte[] process(byte[] media);

}
