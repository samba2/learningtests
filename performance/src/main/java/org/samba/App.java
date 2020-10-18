package org.samba;

import java.util.ArrayList;
import java.util.List;

// compile
// change to target/classes
// run
// java  -XX:+FlightRecorder -XX:StartFlightRecording=duration=20s,filename=/tmp/recording2.jfr -cp . org.samba.App
// open the file with JMC (taken from AdoptOpenJDK)
//
// JMC on Ubuntu
// -  sudo apt-get install libgtk-3-dev
// - sudo apt install libwebkit2gtk-4.0-dev
public class App {
    public static void main(String[] args) {
        List<Object> items = new ArrayList<>(1);
        try {
            while (true){
                items.add(new Object());
            }
        } catch (OutOfMemoryError e){
            System.out.println(e.getMessage());
        }
        assert items.size() > 0;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
