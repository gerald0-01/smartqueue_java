package view;

import javax.swing.ImageIcon;
import java.awt.Image;

// Loads the app logo once and provides scaled versions for reuse
public class AppIcon {

    // path relative to the project root where the app is launched from
    private static final String PATH = "public/logo.png";

    // the raw ImageIcon loaded from disk
    private static final ImageIcon ICON = new ImageIcon(PATH);

    // returns the logo scaled to the given width and height
    // SCALE_AREA_AVERAGING is sharper than SCALE_SMOOTH for logos with fine detail
    public static ImageIcon get(int width, int height) {
        Image scaled = ICON.getImage().getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
        return new ImageIcon(scaled);
    }

    // returns the raw Image — used for setIconImage() on JFrames (window title bar icon)
    public static Image getImage() {
        return ICON.getImage();
    }
}
