package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new PaintPanel());
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class PaintPanel extends JPanel implements MouseMotionListener, MouseListener {
    private Point startPoint = new Point(-1, -1);
    private final transient BufferedImage backImage;
    private static final TexturePaint TEXTURE = TextureFactory.createCheckerTexture(6, new Color(200, 150, 100, 50));
    private final int[] pixels = new int[320 * 240];
    private final transient MemoryImageSource source = new MemoryImageSource(320, 240, pixels, 0, 320);
    private int penc;

    public PaintPanel() {
        super();
        addMouseMotionListener(this);
        addMouseListener(this);
        backImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = backImage.createGraphics();
        g2.setPaint(TEXTURE);
        g2.fillRect(0, 0, 320, 240);
        g2.dispose();
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.drawImage(backImage, 0, 0, this);
            g2.dispose();
        }
        if (source != null) {
            g.drawImage(createImage(source), 0, 0, null);
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        Point pt = e.getPoint();
        double xDelta = e.getX() - startPoint.getX();
        double yDelta = e.getY() - startPoint.getY();
        double delta = Math.max(Math.abs(xDelta), Math.abs(yDelta));

        double xIncrement = xDelta / delta;
        double yIncrement = yDelta / delta;
        double xStart = startPoint.x;
        double yStart = startPoint.y;
        for (int i = 0; i < delta; i++) {
            Point p = new Point((int) xStart, (int) yStart);
            if (p.x < 0 || p.y < 0 || p.x >= 320 || p.y >= 240) {
                break;
            }
            paintStamp(pixels, p, penc);
            //source.newPixels(p.x - 2, p.y - 2, 4, 4);
            xStart += xIncrement;
            yStart += yIncrement;
        }
        startPoint = pt;
    }
    private void paintStamp(int[] pixels, Point p, int penc) {
        //1x1:
        //pixels[p.x + p.y * 320] = penc;
        //3x3 square:
        for (int n = -1; n <= 1; n++) {
            for (int m = -1; m <= 1; m++) {
                int t = p.x + n + (p.y + m) * 320;
                if (t >= 0 && t < 320 * 240) {
                    pixels[t] = penc;
                }
            }
        }
        repaint(p.x - 2, p.y - 2, 4, 4);
    }
    @Override public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        penc = (e.getButton() == MouseEvent.BUTTON1) ? 0xff000000 : 0x0;
    }
    @Override public void mouseMoved(MouseEvent e)    { /* not needed */ }
    @Override public void mouseExited(MouseEvent e)   { /* not needed */ }
    @Override public void mouseEntered(MouseEvent e)  { /* not needed */ }
    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
    @Override public void mouseClicked(MouseEvent e)  { /* not needed */ }
}

final class TextureFactory {
    private static final Color DEFAULT_COLOR = new Color(100, 100, 100, 100);
    private TextureFactory() { /* Singleton */ }
    public static TexturePaint createCheckerTexture(int cs, Color color) {
        int size = cs * cs;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(color);
        g2.fillRect(0, 0, size, size);
        for (int i = 0; i * cs < size; i++) {
            for (int j = 0; j * cs < size; j++) {
                if ((i + j) % 2 == 0) {
                    g2.fillRect(i * cs, j * cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(img, new Rectangle(0, 0, size, size));
    }
    public static TexturePaint createCheckerTexture(int cs) {
        return createCheckerTexture(cs, DEFAULT_COLOR);
    }
}

// class PaintPanel extends JPanel implements MouseMotionListener, MouseListener {
//     private static final Color ERASER = new Color(0x0, true);
//     private boolean isPen = true;
//     private Point startPoint = new Point(-10, -10);
//     private BufferedImage currentImage = null;
//     private BufferedImage backImage = null;
//     private TexturePaint texture = makeTexturePaint();
//     public PaintPanel() {
//         super();
//         addMouseMotionListener(this);
//         addMouseListener(this);
//         currentImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
//         backImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2 = backImage.createGraphics();
//         g2.setPaint(texture);
//         g2.fillRect(0, 0, 320, 240);
//         g2.dispose();
//     }
//     private static BufferedImage makeBGImage() {
//         Color color = new Color(200, 150, 100, 50);
//         int cs = 6, sz = cs * cs;
//         BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2 = img.createGraphics();
//         g2.setPaint(color);
//         g2.fillRect(0, 0, sz, sz);
//         for (int i = 0; i * cs < sz; i++) {
//             for (int j = 0; j * cs < sz; j++) {
//                 if ((i + j) % 2 == 0) { g2.fillRect(i * cs, j * cs, cs, cs); }
//             }
//         }
//         g2.dispose();
//         return img;
//     }
//     private static TexturePaint makeTexturePaint() {
//         BufferedImage img = makeBGImage();
//         int w = img.getWidth(), h = img.getHeight();
//         Rectangle2D r2d = new Rectangle2D.Float(0, 0, w, h);
//         return new TexturePaint(img, r2d);
//     }
//     @Override public void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         if (backImage != null) {
//             g.drawImage(backImage, 0, 0, this);
//         }
//         if (currentImage != null) {
//             g.drawImage(currentImage, 0, 0, this);
//         }
//     }
//     @Override public void mouseDragged(MouseEvent e) {
//         Point pt = e.getPoint();
//         Graphics2D g2d = currentImage.createGraphics();
//         g2d.setStroke(new BasicStroke(3f));
//         if (isPen) {
//             g2d.setPaint(Color.BLACK);
//         } else {
//             g2d.setComposite(AlphaComposite.Clear);
//             g2d.setPaint(ERASER);
//         }
//         g2d.drawLine(startPoint.x, startPoint.y, pt.x, pt.y);
//         g2d.dispose();
//         startPoint = pt;
//         repaint();
//     }
//     @Override public void mousePressed(MouseEvent e) {
//         startPoint = e.getPoint();
//         isPen = e.getButton() == MouseEvent.BUTTON1;
//     }
//     @Override public void mouseMoved(MouseEvent e) {}
//     @Override public void mouseExited(MouseEvent e) {}
//     @Override public void mouseEntered(MouseEvent e) {}
//     @Override public void mouseReleased(MouseEvent e) {}
//     @Override public void mouseClicked(MouseEvent e) {}
// }
