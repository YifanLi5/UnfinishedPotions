package ScriptClasses;

import Util.CombinationRecipes;
import Util.Margins;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.Script;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScriptPaint extends BotMouseListener implements Painter  {
    private static final Color GRAY = new Color(70, 61, 50, 156);
    private static final Color RED = new Color(255, 0, 0, 156);
    private static final Color GREEN = new Color(0, 255, 0, 156);
    private static final Color YELLOW = new Color(255, 255, 0, 156);
    private static final String IMG_FOLDER = "resources", HERB = "/herb.png", UNF_POTION = "/unf_potion.png", COINS = "/coins.png", UNKNOWN_MARGIN = "----";
    private Script script;
    private BufferedImage herb, unfPot, coins;
    private long startTime;
    private Margins margins;

    private final static Rectangle TOGGLE_PAINT_VISIBILITY = new Rectangle(0, 291, 47, 47);
    private final static Rectangle TOGGLE_GE_OPS = new Rectangle(0, 244, 94, 47);
    private boolean paintVisible = false;
    public static boolean geOpsEnabled = true;

    public ScriptPaint(Script script){
        this.script = script;
        try{
            herb = ImageIO.read(script.getScriptResourceAsStream(IMG_FOLDER + HERB));
            unfPot = ImageIO.read(script.getScriptResourceAsStream(IMG_FOLDER + UNF_POTION));
            coins = ImageIO.read(script.getScriptResourceAsStream(IMG_FOLDER + COINS));
        } catch(IOException e){
            script.log(e);
        }
        script.getBot().addPainter(this);
        script.getBot().addMouseListener(this);
        startTime = System.currentTimeMillis();
        margins = Margins.getInstance(script);
    }

    @Override
    public void onPaint(Graphics2D g) {
        drawShowStatsBtn(g);
        drawToggleGEOpsBtn(g);
        drawMouse(g);
        if(paintVisible){
            drawImgs(g);
            drawHerbLabels(g);
            drawRuntime(g);
            int[] tdfxMargin = margins.getCachedConversionMargin(CombinationRecipes.TOADFLAX);
            drawToadFlaxStats(tdfxMargin[0], tdfxMargin[1], g);
            int[] avanMargin = margins.getCachedConversionMargin(CombinationRecipes.AVANTOE);
            drawAvantoeStats(avanMargin[0], avanMargin[1], g);
            int[] kwrmMargin = margins.getCachedConversionMargin(CombinationRecipes.KWUARM);
            drawKwuarmStats(kwrmMargin[0], kwrmMargin[1], g);
            int[] iritMargin = margins.getCachedConversionMargin(CombinationRecipes.IRIT);
            drawIritStats(iritMargin[0], iritMargin[1], g);
            int[] harrMargin = margins.getCachedConversionMargin(CombinationRecipes.HARRALANDER);
            drawHarralanderStats(harrMargin[0], harrMargin[1], g);
        }
    }

    @Override
    public void checkMouseEvent(MouseEvent mouseEvent) {
        switch (mouseEvent.getID()){
            case MouseEvent.MOUSE_PRESSED:
                Point clickPt = mouseEvent.getPoint();
                if(TOGGLE_PAINT_VISIBILITY.contains(clickPt)){
                    paintVisible = !paintVisible;
                    mouseEvent.consume();
                } else if(TOGGLE_GE_OPS.contains(clickPt)){
                    geOpsEnabled = !geOpsEnabled;
                    mouseEvent.consume();
                }
        }
    }

    private void drawMouse(Graphics2D g){
        Point mP = script.getMouse().getPosition();
        g.drawLine(mP.x - 5, mP.y + 5, mP.x + 5, mP.y - 5);
        g.drawLine(mP.x + 5, mP.y + 5, mP.x - 5, mP.y - 5);
    }

    private void drawToggleGEOpsBtn(Graphics2D g){
        if(geOpsEnabled)
            g.setColor(GREEN);
        else
            g.setColor(RED);
        g.fill(TOGGLE_GE_OPS);
        String geOps = "GE OPS";
        String temp = geOpsEnabled ? "Enabled" : "Disabled";
        int xOffset1 = (TOGGLE_GE_OPS.width - g.getFontMetrics().stringWidth(geOps))/2;
        int xOffset2 = (TOGGLE_GE_OPS.width - g.getFontMetrics().stringWidth(temp))/2;
        g.setColor(Color.WHITE);
        g.drawString(geOps, TOGGLE_GE_OPS.x + xOffset1, TOGGLE_GE_OPS.y + 27);
        g.drawString(temp, TOGGLE_GE_OPS.x + xOffset2, TOGGLE_GE_OPS.y + 35);
    }

    private void drawShowStatsBtn(Graphics2D g){
        g.setColor(GRAY);
        g.fill(TOGGLE_PAINT_VISIBILITY);
        g.setColor(Color.WHITE);
        g.drawString(paintVisible ? "hide" : "show", TOGGLE_PAINT_VISIBILITY.x + 8, TOGGLE_PAINT_VISIBILITY.y + 32);
    }

    private void drawHerbLabels(Graphics2D g){
        final String TOADFLAX = "TDFX", AVANTOE = "AVAN", KWUARM = "KWRM", IRIT = "IRIT", HARRALANDER = "HARR";
        final int X_ORIGIN = 47, Y_ORIGIN = 291, FULL_WIDTH = 472, COLUMN_WIDTH = 94, HEIGHT = 47, TXT_Y_OFFSET = 32, TXT_X_OFFSET = 32;
        g.setColor(GRAY);
        g.fillRect(X_ORIGIN, Y_ORIGIN, FULL_WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString(TOADFLAX, X_ORIGIN + TXT_X_OFFSET, Y_ORIGIN + TXT_Y_OFFSET);
        g.drawString(AVANTOE, X_ORIGIN + COLUMN_WIDTH + TXT_X_OFFSET, Y_ORIGIN + TXT_Y_OFFSET);
        g.drawString(KWUARM, X_ORIGIN + 2*COLUMN_WIDTH + TXT_X_OFFSET, Y_ORIGIN + TXT_Y_OFFSET);
        g.drawString(IRIT, X_ORIGIN + 3*COLUMN_WIDTH + TXT_X_OFFSET, Y_ORIGIN + TXT_Y_OFFSET);
        g.drawString(HARRALANDER, X_ORIGIN + 4*COLUMN_WIDTH + TXT_X_OFFSET, Y_ORIGIN + TXT_Y_OFFSET);

        g.drawLine(X_ORIGIN, Y_ORIGIN, X_ORIGIN, Y_ORIGIN + 185);
        g.drawLine(X_ORIGIN + COLUMN_WIDTH, Y_ORIGIN, X_ORIGIN + COLUMN_WIDTH, Y_ORIGIN + 185);
        g.drawLine(X_ORIGIN + 2*COLUMN_WIDTH, Y_ORIGIN, X_ORIGIN + 2*COLUMN_WIDTH, Y_ORIGIN + 185);
        g.drawLine(X_ORIGIN + 3*COLUMN_WIDTH, Y_ORIGIN, X_ORIGIN + 3*COLUMN_WIDTH, Y_ORIGIN + 185);
        g.drawLine(X_ORIGIN + 4*COLUMN_WIDTH, Y_ORIGIN, X_ORIGIN + 4*COLUMN_WIDTH, Y_ORIGIN + 185);
        g.drawLine(X_ORIGIN + 5*COLUMN_WIDTH, Y_ORIGIN, X_ORIGIN + 5*COLUMN_WIDTH, Y_ORIGIN + 185);
    }

    private void drawImgs(Graphics2D g){
        final int X_ORIGIN = 0, Y_ORIGIN = 338, WIDTH = 47, HEIGHT = 47, IMG_OFFSET_X = 8, IMG_OFFSET_Y = 8;
        g.setColor(GRAY);
        g.fillRect(X_ORIGIN, Y_ORIGIN, WIDTH, HEIGHT);
        g.drawImage(unfPot, null, X_ORIGIN + IMG_OFFSET_X, Y_ORIGIN + IMG_OFFSET_Y);
        g.fillRect(X_ORIGIN, Y_ORIGIN + HEIGHT, WIDTH, HEIGHT);
        g.drawImage(herb, null, X_ORIGIN + IMG_OFFSET_X, Y_ORIGIN + HEIGHT + IMG_OFFSET_Y);
        g.fillRect(X_ORIGIN, Y_ORIGIN + 2*HEIGHT, WIDTH, HEIGHT);
        g.drawImage(coins, null, X_ORIGIN + IMG_OFFSET_X, Y_ORIGIN + 2*HEIGHT + IMG_OFFSET_Y);

        g.setColor(Color.WHITE);
        g.drawLine(X_ORIGIN, Y_ORIGIN, X_ORIGIN + 520, Y_ORIGIN);
        g.drawLine(X_ORIGIN, Y_ORIGIN + HEIGHT, X_ORIGIN + 520, Y_ORIGIN + HEIGHT);
        g.drawLine(X_ORIGIN, Y_ORIGIN + 2*HEIGHT, X_ORIGIN + 520, Y_ORIGIN + 2*HEIGHT);
    }

    private void drawToadFlaxStats(int herbBuyPrice, int unfSellPrice, Graphics2D g){
        final int X_ORIGIN = 47, Y_ORIGIN = 338, COLUMN_WIDTH = 94, FULL_COLUMN_HEIGHT = 142, COLUMN_HEIGHT = 47, TXT_OFFSET = 32;
        g.setColor(GRAY);
        g.fillRect(X_ORIGIN, Y_ORIGIN, COLUMN_WIDTH, FULL_COLUMN_HEIGHT);
        g.setColor(Color.WHITE);
        String unfSellStr, herbBuyStr, delta;
        if(unfSellPrice == Margins.DEFAULT_MARGIN[0] || herbBuyPrice == Margins.DEFAULT_MARGIN[1]){
            unfSellStr = UNKNOWN_MARGIN;
            herbBuyStr = UNKNOWN_MARGIN;
            delta = UNKNOWN_MARGIN;
        } else {
            unfSellStr = String.valueOf(unfSellPrice);
            herbBuyStr = String.valueOf(herbBuyPrice);
            delta = String.valueOf(unfSellPrice - herbBuyPrice);
        }
        g.drawString(unfSellStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + TXT_OFFSET);
        g.drawString(herbBuyStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + COLUMN_HEIGHT + TXT_OFFSET);
        g.drawString(delta, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + 2*COLUMN_HEIGHT + TXT_OFFSET);
    }

    private void drawAvantoeStats(int herbBuyPrice, int unfSellPrice, Graphics2D g){
        final int X_ORIGIN = 141, Y_ORIGIN = 338, COLUMN_WIDTH = 94, FULL_COLUMN_HEIGHT = 142, COLUMN_HEIGHT = 47, TXT_OFFSET = 32;
        g.setColor(GRAY);
        g.fillRect(X_ORIGIN, Y_ORIGIN, COLUMN_WIDTH, FULL_COLUMN_HEIGHT);
        g.setColor(Color.WHITE);
        String unfSellStr, herbBuyStr, delta;
        if(unfSellPrice == Margins.DEFAULT_MARGIN[0] || herbBuyPrice == Margins.DEFAULT_MARGIN[1]){
            unfSellStr = UNKNOWN_MARGIN;
            herbBuyStr = UNKNOWN_MARGIN;
            delta = UNKNOWN_MARGIN;
        } else {
            unfSellStr = String.valueOf(unfSellPrice);
            herbBuyStr = String.valueOf(herbBuyPrice);
            delta = String.valueOf(unfSellPrice - herbBuyPrice);
        }
        g.drawString(unfSellStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + TXT_OFFSET);
        g.drawString(herbBuyStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + COLUMN_HEIGHT + TXT_OFFSET);
        g.drawString(delta, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + 2*COLUMN_HEIGHT + TXT_OFFSET);
    }

    private void drawKwuarmStats(int herbBuyPrice, int unfSellPrice, Graphics2D g){
        final int X_ORIGIN = 235, Y_ORIGIN = 338, COLUMN_WIDTH = 94, FULL_COLUMN_HEIGHT = 142, COLUMN_HEIGHT = 47, TXT_OFFSET = 32;
        g.setColor(GRAY);
        g.fillRect(X_ORIGIN, Y_ORIGIN, COLUMN_WIDTH, FULL_COLUMN_HEIGHT);
        g.setColor(Color.WHITE);
        String unfSellStr, herbBuyStr, delta;
        if(unfSellPrice == Margins.DEFAULT_MARGIN[0] || herbBuyPrice == Margins.DEFAULT_MARGIN[1]){
            unfSellStr = UNKNOWN_MARGIN;
            herbBuyStr = UNKNOWN_MARGIN;
            delta = UNKNOWN_MARGIN;
        } else {
            unfSellStr = String.valueOf(unfSellPrice);
            herbBuyStr = String.valueOf(herbBuyPrice);
            delta = String.valueOf(unfSellPrice - herbBuyPrice);
        }
        g.drawString(unfSellStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + TXT_OFFSET);
        g.drawString(herbBuyStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + COLUMN_HEIGHT + TXT_OFFSET);
        g.drawString(delta, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + 2*COLUMN_HEIGHT + TXT_OFFSET);
    }

    private void drawIritStats(int herbBuyPrice, int unfSellPrice, Graphics2D g){
        final int X_ORIGIN = 329, Y_ORIGIN = 338, COLUMN_WIDTH = 94, FULL_COLUMN_HEIGHT = 142, COLUMN_HEIGHT = 47, TXT_OFFSET = 32;
        g.setColor(GRAY);
        g.fillRect(X_ORIGIN, Y_ORIGIN, COLUMN_WIDTH, FULL_COLUMN_HEIGHT);
        g.setColor(Color.WHITE);
        String unfSellStr, herbBuyStr, delta;
        if(unfSellPrice == Margins.DEFAULT_MARGIN[0] || herbBuyPrice == Margins.DEFAULT_MARGIN[1]){
            unfSellStr = UNKNOWN_MARGIN;
            herbBuyStr = UNKNOWN_MARGIN;
            delta = UNKNOWN_MARGIN;
        } else {
            unfSellStr = String.valueOf(unfSellPrice);
            herbBuyStr = String.valueOf(herbBuyPrice);
            delta = String.valueOf(unfSellPrice - herbBuyPrice);
        }
        g.drawString(unfSellStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + TXT_OFFSET);
        g.drawString(herbBuyStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + COLUMN_HEIGHT + TXT_OFFSET);
        g.drawString(delta, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + 2*COLUMN_HEIGHT + TXT_OFFSET);
    }

    private void drawHarralanderStats(int herbBuyPrice, int unfSellPrice, Graphics2D g){
        final int X_ORIGIN = 423, Y_ORIGIN = 338, COLUMN_WIDTH = 94, FULL_COLUMN_HEIGHT = 142, COLUMN_HEIGHT = 47, TXT_OFFSET = 32;
        g.setColor(GRAY);
        g.fillRect(X_ORIGIN, Y_ORIGIN, COLUMN_WIDTH, FULL_COLUMN_HEIGHT);
        g.setColor(Color.WHITE);
        String unfSellStr, herbBuyStr, delta;
        if(unfSellPrice == Margins.DEFAULT_MARGIN[0] || herbBuyPrice == Margins.DEFAULT_MARGIN[1]){
            unfSellStr = UNKNOWN_MARGIN;
            herbBuyStr = UNKNOWN_MARGIN;
            delta = UNKNOWN_MARGIN;
        } else {
            unfSellStr = String.valueOf(unfSellPrice);
            herbBuyStr = String.valueOf(herbBuyPrice);
            delta = String.valueOf(unfSellPrice - herbBuyPrice);
        }
        g.drawString(unfSellStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + TXT_OFFSET);
        g.drawString(herbBuyStr, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + COLUMN_HEIGHT + TXT_OFFSET);
        g.drawString(delta, X_ORIGIN + TXT_OFFSET, Y_ORIGIN + 2*COLUMN_HEIGHT + TXT_OFFSET);
    }

    private void drawRuntime(Graphics2D g){
        final int X_ORIGIN = 404, Y_ORIGIN = 480, WIDTH = 111, HEIGHT = 22, TXT_X_OFFSET = 31, TXT_Y_OFFSET = 15;
        g.setColor(Color.RED);
        g.fillRect(X_ORIGIN, Y_ORIGIN, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        String runtime = String.valueOf(formatTime(System.currentTimeMillis() - startTime));
        g.drawString(runtime, X_ORIGIN + TXT_X_OFFSET, Y_ORIGIN + TXT_Y_OFFSET);
    }

    private String formatTime(final long ms){
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
