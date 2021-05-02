package tudbut.mod.client.ttc.utils;

import de.tudbut.tools.FileRW;
import de.tudbut.tools.Tools;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.parsing.TCN;

import java.io.IOException;

public class FakeSkidUtils {
    
    public static final String name;
    public static final GuiTTC.ITheme theme;
    
    static {
        String theName = "TTC Client";
        GuiTTC.ITheme theTheme = null;
        try {
            TCN tcn = TCN.readMap(Tools.stringToMap(new FileRW("").getContent().join("\n")));
            theName = tcn.getString("name");
            int gbc = Integer.parseInt(tcn.getString("buttonColor"), 16);
            int gsbc = Integer.parseInt(tcn.getString("subButtonColor"), 16);
            int gtc = Integer.parseInt(tcn.getString("textColor"), 16);
            boolean hs = tcn.getBoolean("textShadow");
            theTheme = new GuiTTC.ITheme() {
                @Override
                public int getButtonColor() {
                    return gbc;
                }
    
                @Override
                public int getSubButtonColor() {
                    return gsbc;
                }
    
                @Override
                public int getTextColor() {
                    return gtc;
                }
    
                @Override
                public boolean hasShadow() {
                    return hs;
                }
            };
            
        }
        catch (Exception ignored) {
        }
        name = theName;
        theme = theTheme;
    }
    
}
