package main.checkVLC;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

public class findVLCJ {

    @Deprecated(forRemoval = true)
    @SuppressWarnings("unused")
    private static void testVlcj() {
        try {
            MediaPlayerFactory factory = new MediaPlayerFactory();
            System.out.println("LibVLC path = " + factory.nativeLibraryPath());
            factory.release();
        } catch (LinkageError e) {
            System.out.println("VLCJ không tìm thấy LibVLC: " + e.getMessage());
        }
    }

}
