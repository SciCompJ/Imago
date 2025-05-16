/**
 * 
 */
package imago.util.imagej;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import imago.util.imagej.ImagejRoi.SubType;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.MultiPoint2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.geom.geom2d.polygon.Box2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.geom.geom2d.polygon.Polyline2D;

/**
 * 
 */
public class ImagejRoiDecoder
{
    // offsets
    private static final int TOP = 8;
    private static final int LEFT = 10;
    private static final int BOTTOM = 12;
    private static final int RIGHT = 14;
    private static final int N_COORDINATES = 16;
    private static final int X1 = 18;
    private static final int Y1 = 22;
    private static final int X2 = 26;
    private static final int Y2 = 30;
    private static final int XD = 18;
    private static final int YD = 22;
    private static final int WIDTHD = 26;
    private static final int HEIGHTD = 30;
//    private static final int SIZE = 18;
    private static final int STROKE_WIDTH = 34;
    public static final int SHAPE_ROI_SIZE = 36;
    private static final int STROKE_COLOR = 40;
    private static final int FILL_COLOR = 44;
//    private static final int ARROW_STYLE = 52;
    private static final int FLOAT_PARAM = 52; //ellipse ratio or rotated rect width
    private static final int POINT_TYPE= 52;
//    private static final int ARROW_HEAD_SIZE = 53;
//    private static final int ROUNDED_RECT_ARC_SIZE = 54;
//    private static final int POSITION = 56;
    private static final int HEADER2_OFFSET = 60;
    private static final int COORDINATES = 64;
    // header2 offsets
//    private static final int C_POSITION = 4;
//    private static final int Z_POSITION = 8;
//    private static final int T_POSITION = 12;
    private static final int NAME_OFFSET = 16;
    private static final int NAME_LENGTH = 20;
//    private static final int OVERLAY_LABEL_COLOR = 24;
//    private static final int OVERLAY_FONT_SIZE = 28; //short
//    private static final int GROUP = 30;  //byte
//    private static final int IMAGE_OPACITY = 31;  //byte
//    private static final int IMAGE_SIZE = 32;  //int
    private static final int FLOAT_STROKE_WIDTH = 36;  //float
    private static final int ROI_PROPS_OFFSET = 40;
    private static final int ROI_PROPS_LENGTH = 44;
//    private static final int COUNTERS_OFFSET = 48;

    // types
    private static final int polygon=0, rect=1, oval=2, line=3, freeline=4, polyline=5, // noRoi=6,
        freehand=7, traced=8, angle=9, point=10;

    // binary masks for options
    public static final int SPLINE_FIT = 1;
    public static final int DOUBLE_HEADED = 2;
    public static final int OUTLINE = 4;
    public static final int OVERLAY_LABELS = 8;
    public static final int OVERLAY_NAMES = 16;
    public static final int OVERLAY_BACKGROUNDS = 32;
    public static final int OVERLAY_BOLD = 64;
    public static final int SUB_PIXEL_RESOLUTION = 128;
    public static final int DRAW_OFFSET = 256;
    public static final int ZERO_TRANSPARENT = 512;
    public static final int SHOW_LABELS = 1024;
    public static final int SCALE_LABELS = 2048;
    public static final int PROMPT_BEFORE_DELETING = 4096; //points
    public static final int SCALE_STROKE_WIDTH = 8192;

    /**
     * Decodes an ImageJ ROI from a byte array, typically obtained from a ROI
     * file or from meta data within a TIFF file.
     * 
     * @param byteArray
     *            the byte array containing encoded ROI information.
     * @return an instance of ImagejRoi
     */
    public static final ImagejRoi decode(byte[] byteArray)
    {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return new ImagejRoiDecoder().readRoi(buffer);
    }
    
    private ImagejRoiDecoder()
    {
    }
    
    private ImagejRoi readRoi(ByteBuffer buffer)
    {
        // read header
        if (buffer.get() != 73 || buffer.get() != 111)
        {
            throw new RuntimeException("Buffer does not start with ImageJ Roi identifier");
        }
        
        Info info = Info.read(buffer);
        int hdr2Offset = buffer.getInt(HEADER2_OFFSET);

        // additional meta data, not yet supported
//        int channel = 0, slice = 0, frame = 0;
//        int overlayLabelColor = 0;
//        int overlayFontSize = 0;
//        int group = 0;
//        int imageOpacity = 0;
//        int imageSize = 0;
//        if (hdr2Offset > 0 && hdr2Offset + IMAGE_SIZE + 4 <= buffer.capacity())
//        {
//            channel = buffer.getInt(hdr2Offset + C_POSITION);
//            slice = buffer.getInt(hdr2Offset + Z_POSITION);
//            frame = buffer.getInt(hdr2Offset + T_POSITION);
//            overlayLabelColor = buffer.getInt(hdr2Offset + OVERLAY_LABEL_COLOR);
//            overlayFontSize = buffer.getShort(hdr2Offset + OVERLAY_FONT_SIZE);
//            imageOpacity = buffer.get(hdr2Offset + IMAGE_OPACITY) & 0x00FF;
//            imageSize = buffer.getInt(hdr2Offset + IMAGE_SIZE);
//            group = buffer.get(hdr2Offset + GROUP) & 0x00FF;
//        }
        
        
        boolean isComposite = buffer.getInt(SHAPE_ROI_SIZE) > 0;
        if (isComposite) 
        {
            throw new RuntimeException("Composite ROI are not supported");
//            roi = getShapeRoi();
//            if (version>=218)
//                getStrokeWidthAndColor(roi, hdr2Offset, scaleStrokeWidth);
//            roi.setPosition(position);
//            if (channel>0 || slice>0 || frame>0)
//                roi.setPosition(channel, slice, frame);
//            decodeOverlayOptions(roi, version, options, overlayLabelColor, overlayFontSize);
//            if (version>=224) {
//                String props = getRoiProps();
//                if (props!=null)
//                    roi.setProperties(props);
//            }
//            if (version>=228 && group>0)
//                roi.setGroup(group);
//            return roi;
        }
        
        ImagejRoi roi = readRoiGeometry(buffer, info);
        if (roi == null) return null;
        
        roi.setName(readName(buffer, "no-name"));

        
        // read stroke width, stroke color and fill color (1.43i or later)
        if (info.version >= 218)
        {
            boolean scaleStrokeWidth = true;
            if (info.version >= 228) scaleStrokeWidth = (info.options & SCALE_STROKE_WIDTH) != 0;
            
            readDrawOptions(buffer, roi, hdr2Offset, scaleStrokeWidth);
            if (info.type == point) roi.setStrokeWidth(0);
            // boolean splineFit = (options&SPLINE_FIT)!=0;
            // if (splineFit && roi instanceof PolygonRoi)
            // ((PolygonRoi)roi).fitSpline();
        }
        
//        if (version>=218 && subtype==TEXT)
//            roi = getTextRoi(roi, version);

//        if (version>=221 && subtype==IMAGE)
//            roi = getImageRoi(roi, imageOpacity, imageSize, options);

        if (info.version >= 224) 
        {
            String props = getRoiProps(buffer);
            if (props!=null)
                roi.setProperties(props);
        }

//        roi.setPosition(position);
//        if (channel>0 || slice>0 || frame>0)
//            roi.setPosition(channel, slice, frame);

//        if (version>=227) {
//            int[] counters = getPointCounters(n);
//            if (counters!=null && (roi instanceof PointRoi))
//                ((PointRoi)roi).setCounters(counters);  //must be after roi.setPosition()
//        }
        
//        // set group (1.52t or later)
//        if (version>=228 && group>0)
//            roi.setGroup(group);

//        decodeOverlayOptions(roi, version, options, overlayLabelColor, overlayFontSize);

        return roi;
    }
    
    private ImagejRoi readRoiGeometry(ByteBuffer buffer, Info info)
    {
        switch (info.type)
        {
            case rect:
                return new ImagejRoi(info.subPixelRect ? readSubPixelBounds(buffer) : readPixelBounds(buffer));
                
            case oval:
                Box2D box = info.subPixelRect ? readSubPixelBounds(buffer) : readPixelBounds(buffer);
                Point2D center = box.centroid();
                double boxWidth = box.bounds().xExtent();
                double boxHeight = box.bounds().yExtent();
                return new ImagejRoi(new Ellipse2D(center, boxWidth * 0.5, boxHeight * 0.5, 0));
                
            case line:
            {
                double x1 = buffer.getFloat(X1);
                double y1 = buffer.getFloat(Y1);
                double x2 = buffer.getFloat(X2);
                double y2 = buffer.getFloat(Y2);
//                if (subtype==ARROW) {
//                    roi = new Arrow(x1, y1, x2, y2);        
//                    ((Arrow)roi).setDoubleHeaded((options&DOUBLE_HEADED)!=0);
//                    ((Arrow)roi).setOutline((options&OUTLINE)!=0);
//                    int style = getByte(ARROW_STYLE);
//                    if (style>=Arrow.FILLED && style<=Arrow.BAR)
//                        ((Arrow)roi).setStyle(style);
//                    int headSize = getByte(ARROW_HEAD_SIZE);
//                    if (headSize>=0 && style<=30)
//                        ((Arrow)roi).setHeadSize(headSize);
//                } else {
                LineSegment2D seg = new LineSegment2D(new Point2D(x1, y1), new Point2D(x2, y2));
                return new ImagejRoi(seg);
            }
            
            case point:
            {
                ArrayList<Point2D> coords = readCoordinates(buffer, info.subPixelResolution);
                MultiPoint2D points = MultiPoint2D.create(coords);
                ImagejRoi roi = new ImagejRoi(points);
                
                if (info.version>=226)
                {
                    roi.setPointType(buffer.get(POINT_TYPE) & 0xFF);
                    roi.setPointSizeIndex(buffer.getShort(STROKE_WIDTH) & 0xFF);
                }
                
//                if ((options&SHOW_LABELS)!=0 && !ij.Prefs.noPointLabels)
//                    ((PointRoi)roi).setShowLabels(true);
//                if ((options&PROMPT_BEFORE_DELETING)!=0)
//                    ((PointRoi)roi).promptBeforeDeleting(true);
                return roi;
            }
            
            case freehand:
            {
                if (info.subtype == SubType.ELLIPSE || info.subtype == SubType.ROTATED_RECT)
                {
                    double ex1 = buffer.getFloat(X1);
                    double ey1 = buffer.getFloat(Y1);
                    double ex2 = buffer.getFloat(X2);
                    double ey2 = buffer.getFloat(Y2);
                    double param = buffer.getFloat(FLOAT_PARAM);
                    
                    if (info.subtype == SubType.ROTATED_RECT)
                    {
                        throw new RuntimeException("No support for RotatedRect ROI");
                        // roi = new RotatedRectRoi(ex1,ey1,ex2,ey2,param);
                    }
                    else
                    {
                        Ellipse2D elli = makeEllipse(ex1, ey1, ex2, ey2, param);
                        return new ImagejRoi(elli);
                    }
                }
            }
                
            case polygon:
            {
                ArrayList<Point2D> coords = readCoordinates(buffer, info.subPixelResolution);
                Polygon2D poly = Polygon2D.create(coords);
                return new ImagejRoi(poly);
            }
            
            case polyline, traced, freeline, angle:
            {
                ArrayList<Point2D> coords = readCoordinates(buffer, info.subPixelResolution);
                Polyline2D poly = Polyline2D.create(coords, false);
                return new ImagejRoi(poly);
            }
            
            default:
                throw new RuntimeException("Unrecognized ROI type: " + info.type);
        }
    }
    
    private Box2D readPixelBounds(ByteBuffer buffer)
    {
        int left = buffer.getShort(LEFT);
        int top = buffer.getShort(TOP);
        int bottom = buffer.getShort(BOTTOM);
        int right = buffer.getShort(RIGHT);
        return new Box2D(left, right, top, bottom);
    }
    
    private Box2D readSubPixelBounds(ByteBuffer buffer)
    {
        double xd = buffer.getFloat(XD);
        double yd = buffer.getFloat(YD);
        double widthd = buffer.getFloat(WIDTHD);
        double heightd = buffer.getFloat(HEIGHTD);
        return new Box2D(xd, xd+widthd, yd, yd+heightd);
    }
    
    private ArrayList<Point2D> readCoordinates(ByteBuffer buffer, boolean subPixelResolution)
    {
        int n = buffer.getShort(N_COORDINATES) & 0x00FFFF;
        ArrayList<Point2D> coords = new ArrayList<Point2D>(n);
        
        if (n == 0 || n < 0) return coords;
        
        if (subPixelResolution)
        {
            int base1 = COORDINATES + 4 * n;
            int base2 = base1 + 4 * n;
            
            for (int i = 0; i < n; i++)
            {
                float xf = buffer.getFloat(base1 + i * 4);
                float yf = buffer.getFloat(base2 + i * 4);
                coords.add(new Point2D(xf, yf));
            }
        }
        else
        {
            int left = buffer.getShort(LEFT);
            int top = buffer.getShort(TOP);
            
            // offset to coordinates
            int base1 = COORDINATES;
            int base2 = base1+2*n;
            
            int xtmp, ytmp;

            // read integer coordinates
            for (int i = 0; i < n; i++)
            {
                xtmp = Math.max(buffer.getShort(base1 + i * 2), 0);
                ytmp = Math.max(buffer.getShort(base2 + i * 2), 0);
                coords.add(new Point2D(left + xtmp, top + ytmp));
            }
        }
        
        return coords;
    }
    
    private String readName(ByteBuffer buffer, String defaultName)
    {
        int hdr2Offset = buffer.getInt(HEADER2_OFFSET);
        if (hdr2Offset == 0) return defaultName;
        int offset = buffer.getInt(hdr2Offset + NAME_OFFSET);
        int length = buffer.getInt(hdr2Offset + NAME_LENGTH);
        if (offset == 0 || length == 0) return defaultName;
        if (offset + length * 2 > buffer.capacity()) return defaultName;
        
        return readString(buffer, offset, length);
    }
    
    private void readDrawOptions(ByteBuffer buffer, ImagejRoi roi, int hdr2Offset, boolean scaleStrokeWidth)
    {
        double strokeWidth = buffer.getShort(STROKE_WIDTH);
        if (hdr2Offset > 0)
        {
            double strokeWidthD = buffer.getFloat(hdr2Offset + FLOAT_STROKE_WIDTH);
            if (strokeWidthD > 0.0) strokeWidth = strokeWidthD;
        }
        if (strokeWidth > 0.0)
        {
            roi.setStrokeWidth(strokeWidth);
            roi.scaleStrokeWidth = scaleStrokeWidth;
        }
        
        int fillColor = buffer.getInt(FILL_COLOR);
        if (fillColor != 0)
        {
            int alpha = (fillColor >> 24) & 0xff;
            roi.setFillColor(new Color(fillColor, alpha != 255));
        }
        
        int strokeColor = buffer.getInt(STROKE_COLOR);
        if (strokeColor != 0)
        {
            int alpha = (strokeColor >> 24) & 0xff;
            roi.setStrokeColor(new Color(strokeColor, alpha != 255));
        }
    }

    private String getRoiProps(ByteBuffer buffer)
    {
        int hdr2Offset = buffer.getInt(HEADER2_OFFSET);
        if (hdr2Offset == 0) return null;
        int offset = buffer.getInt(hdr2Offset + ROI_PROPS_OFFSET);
        int length = buffer.getInt(hdr2Offset + ROI_PROPS_LENGTH);
        if (offset == 0 || length == 0) return null;
        return readString(buffer, offset, length);
    }

    private static final String readString(ByteBuffer buffer, int offset, int length)
    {
        int nBytes = length * 2;
        if (offset + nBytes > buffer.capacity()) return null;
        
        ByteBuffer buffer2 = ByteBuffer.allocate(nBytes);
        buffer.get(buffer2.array(), 0, nBytes);
        return buffer2.order(buffer.order()).asCharBuffer().toString();
    }
    
    private static final Ellipse2D makeEllipse(double x1, double y1, double x2, double y2, double aspectRatio) 
    {
        double centerX = (x1 + x2)/2.0;
        double centerY = (y1 + y2)/2.0;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double major = Math.hypot(dx, dy) * 0.5;
        double minor = major*aspectRatio;
        double phiB = Math.atan2(dy, dx);
        return new Ellipse2D(new Point2D(centerX, centerY), major, minor, Math.toDegrees(phiB));
    }

    static class Info
    {
        public static final int VERSION_OFFSET = 4;
        public static final int TYPE = 6;
        public static final int SUBTYPE = 48;
        public static final int OPTIONS = 50;

        public static final Info read(ByteBuffer buffer)
        {
            Info info = new Info();
            
            info.version = buffer.getShort(VERSION_OFFSET);
            info.type = buffer.get(TYPE) & 0xFF;
            info.subtype = SubType.fromValue(buffer.getShort(SUBTYPE));
            
            info.options = buffer.getShort(OPTIONS);
            
            info.subPixelResolution = (info.options & SUB_PIXEL_RESOLUTION) != 0 && info.version >= 222;
            info.subPixelRect = info.version >= 223 && info.subPixelResolution && (info.type == rect || info.type == oval);
            
            return info;
        }
     
        int version;
        int type;
        SubType subtype;
        
        int options;
        
        boolean subPixelResolution;
        boolean subPixelRect;
        
        private Info()
        {
        }
    }
}
