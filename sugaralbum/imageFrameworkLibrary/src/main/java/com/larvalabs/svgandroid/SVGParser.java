
package com.larvalabs.svgandroid;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

/**
 * Entry point for parsing SVG files for Android. Use one of the various static methods for parsing
 * SVGs by resource, asset or input stream. Optionally, a single color can be searched and replaced
 * in the SVG while parsing. You can also parse an svg path directly.
 * 
 * @see #getSVGFromResource(android.content.res.Resources, int)
 * @see #getSVGFromAsset(android.content.res.AssetManager, String)
 * @see #getSVGFromString(String)
 * @see #getSVGFromInputStream(java.io.InputStream)
 * @see #parsePath(String)
 */
public class SVGParser {
    static final String TAG = SVGParser.class.getSimpleName();

    /**
     * Parse SVG data from an input stream.
     * 
     * @param svgData the input stream, with SVG XML data in UTF-8 character encoding.
     * @return the parsed SVG.
     * @throws SVGParseException if there is an error while parsing.
     */
    public static SVG getSVGFromInputStream(InputStream svgData) throws SVGParseException {
        return SVGParser.parse(svgData);
    }

    /**
     * Parse SVG data from an Android application resource.
     * 
     * @param resources the Android context resources.
     * @param resId the ID of the raw resource SVG.
     * @return the parsed SVG.
     * @throws SVGParseException if there is an error while parsing.
     */
    public static SVG getSVGFromResource(Resources resources, int resId) throws SVGParseException {
        return SVGParser.parse(resources.openRawResource(resId));
    }

    public static SVG getSVGFromFile(String filename) throws SVGParseException,
            FileNotFoundException {
        return SVGParser.parse(new FileInputStream(new File(filename)));
    }

    public static SVG getSVGFromString(String svgString) throws SVGParseException,
            FileNotFoundException {
        return SVGParser.parse(new ByteArrayInputStream(svgString.getBytes()));
    }

    /**
     * Parses a single SVG path and returns it as a <code>android.graphics.Path</code> object. An
     * example path is <code>M250,150L150,350L350,350Z</code>, which draws a triangle.
     * 
     * @param pathString the SVG path, see the specification <a
     *            href="http://www.w3.org/TR/SVG/paths.html">here</a>.
     */
    public static Path parsePath(String pathString) {
        return doPath(pathString);
    }

    private static SVG parse(InputStream in) throws SVGParseException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            SVG result = new SVG();
            SVGHandler handler = new SVGHandler(result);
            xr.setContentHandler(handler);
            xr.parse(new InputSource(in));
            return result;
        } catch(Exception e) {
            throw new SVGParseException(e);
        }
    }

    private static NumberParse parseNumbers(String s) {
        // Util.debug("Parsing numbers from: '" + s + "'");
        int n = s.length();
        int p = 0;
        ArrayList<Float> numbers = new ArrayList<Float>();
        boolean skipChar = false;
        for(int i = 1; i < n; i++) {
            if(skipChar) {
                skipChar = false;
                continue;
            }
            char c = s.charAt(i);
            switch(c) {
            // This ends the parsing, as we are on the next element
                case 'M':
                case 'm':
                case 'Z':
                case 'z':
                case 'L':
                case 'l':
                case 'H':
                case 'h':
                case 'V':
                case 'v':
                case 'C':
                case 'c':
                case 'S':
                case 's':
                case 'Q':
                case 'q':
                case 'T':
                case 't':
                case 'a':
                case 'A':
                case ')': {
                    String str = s.substring(p, i);
                    if(str.trim().length() > 0) {
                        // Util.debug("  Last: " + str);
                        Float f = Float.parseFloat(str);
                        numbers.add(f);
                    }
                    p = i;
                    return new NumberParse(numbers);
                }
                case '\n':
                case '\t':
                case ' ':
                case ',':
                case '-': {
                    String str = s.substring(p, i);
                    // Just keep moving if multiple whitespace
                    if(str.trim().length() > 0) {
                        // Util.debug("  Next: " + str);
                        Float f = Float.parseFloat(str);
                        numbers.add(f);
                        if(c == '-') {
                            p = i;
                        } else {
                            p = i + 1;
                            skipChar = true;
                        }
                    } else {
                        p++;
                    }
                    break;
                }
            }
        }
        String last = s.substring(p);
        if(last.length() > 0) {
            // Util.debug("  Last: " + last);
            try {
                numbers.add(Float.parseFloat(last));
            } catch(NumberFormatException nfe) {
                // Just white-space, forget it
            }
            p = s.length();
        }
        return new NumberParse(numbers);
    }

    private static Matrix parseTransform(String s) {
        if(s == null) {
            return null;
        }
        if(s.startsWith("matrix(")) {
            NumberParse np = parseNumbers(s.substring("matrix(".length()));
            if(np.numbers.size() == 6) {
                Matrix matrix = new Matrix();
                matrix.setValues(new float[] {
                        // Row 1
                        np.numbers.get(0), np.numbers.get(2), np.numbers.get(4),
                        // Row 2
                        np.numbers.get(1), np.numbers.get(3), np.numbers.get(5),
                        // Row 3
                        0, 0, 1,
                });
                return matrix;
            }
        } else if(s.startsWith("translate(")) {
            NumberParse np = parseNumbers(s.substring("translate(".length()));
            if(np.numbers.size() > 0) {
                float tx = np.numbers.get(0);
                float ty = 0;
                if(np.numbers.size() > 1) {
                    ty = np.numbers.get(1);
                }
                Matrix matrix = new Matrix();
                matrix.postTranslate(tx, ty);
                return matrix;
            }
        } else if(s.startsWith("scale(")) {
            NumberParse np = parseNumbers(s.substring("scale(".length()));
            if(np.numbers.size() > 0) {
                float sx = np.numbers.get(0);
                float sy = 0;
                if(np.numbers.size() > 1) {
                    sy = np.numbers.get(1);
                }
                Matrix matrix = new Matrix();
                matrix.postScale(sx, sy);
                return matrix;
            }
        } else if(s.startsWith("skewX(")) {
            NumberParse np = parseNumbers(s.substring("skewX(".length()));
            if(np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                Matrix matrix = new Matrix();
                matrix.postSkew((float)Math.tan(angle), 0);
                return matrix;
            }
        } else if(s.startsWith("skewY(")) {
            NumberParse np = parseNumbers(s.substring("skewY(".length()));
            if(np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                Matrix matrix = new Matrix();
                matrix.postSkew(0, (float)Math.tan(angle));
                return matrix;
            }
        } else if(s.startsWith("rotate(")) {
            NumberParse np = parseNumbers(s.substring("rotate(".length()));
            if(np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                float cx = 0;
                float cy = 0;
                if(np.numbers.size() > 2) {
                    cx = np.numbers.get(1);
                    cy = np.numbers.get(2);
                }
                Matrix matrix = new Matrix();
                matrix.postTranslate(cx, cy);
                matrix.postRotate(angle);
                matrix.postTranslate(-cx, -cy);
                return matrix;
            }
        }
        return null;
    }

    /**
     * This is where the hard-to-parse paths are handled. Uppercase rules are absolute positions,
     * lowercase are relative. Types of path rules:
     * <p/>
     * <ol>
     * <li>M/m - (x y)+ - Move to (without drawing)
     * <li>Z/z - (no params) - Close path (back to starting point)
     * <li>L/l - (x y)+ - Line to
     * <li>H/h - x+ - Horizontal ine to
     * <li>V/v - y+ - Vertical line to
     * <li>C/c - (x1 y1 x2 y2 x y)+ - Cubic bezier to
     * <li>S/s - (x2 y2 x y)+ - Smooth cubic bezier to (shorthand that assumes the x2, y2 from
     * previous C/S is the x1, y1 of this bezier)
     * <li>Q/q - (x1 y1 x y)+ - Quadratic bezier to
     * <li>T/t - (x y)+ - Smooth quadratic bezier to (assumes previous control point is "reflection"
     * of last one w.r.t. to current point)
     * </ol>
     * <p/>
     * Numbers are separate by whitespace, comma or nothing at all (!) if they are self-delimiting,
     * (ie. begin with a - sign)
     * 
     * @param s the path string from the XML
     */
    private static Path doPath(String s) {
        int n = s.length();
        ParserHelper ph = new ParserHelper(s, 0);
        ph.skipWhitespace();
        Path p = new Path();
        float lastX = 0;
        float lastY = 0;
        float lastX1 = 0;
        float lastY1 = 0;
        float subPathStartX = 0;
        float subPathStartY = 0;
        char prevCmd = 0;
        while(ph.pos < n) {
            char cmd = s.charAt(ph.pos);
            switch(cmd) {
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if(prevCmd == 'm' || prevCmd == 'M') {
                        cmd = (char)(prevCmd - 1);
                        break;
                    } else if(prevCmd == 'c' || prevCmd == 'C') {
                        cmd = prevCmd;
                        break;
                    } else if(prevCmd == 'l' || prevCmd == 'L') {
                        cmd = prevCmd;
                        break;
                    }
                default: {
                    ph.advance();
                    prevCmd = cmd;
                }
            }

            boolean wasCurve = false;
            switch(cmd) {
                case 'M':
                case 'm': {
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if(cmd == 'm') {
                        subPathStartX += x;
                        subPathStartY += y;
                        p.rMoveTo(x, y);
                        lastX += x;
                        lastY += y;
                    } else {
                        subPathStartX = x;
                        subPathStartY = y;
                        p.moveTo(x, y);
                        lastX = x;
                        lastY = y;
                    }
                    break;
                }
                case 'Z':
                case 'z': {
                    p.close();
                    p.moveTo(subPathStartX, subPathStartY);
                    lastX = subPathStartX;
                    lastY = subPathStartY;
                    lastX1 = subPathStartX;
                    lastY1 = subPathStartY;
                    wasCurve = true;
                    break;
                }
                case 'L':
                case 'l': {
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if(cmd == 'l') {
                        p.rLineTo(x, y);
                        lastX += x;
                        lastY += y;
                    } else {
                        p.lineTo(x, y);
                        lastX = x;
                        lastY = y;
                    }
                    break;
                }
                case 'H':
                case 'h': {
                    float x = ph.nextFloat();
                    if(cmd == 'h') {
                        p.rLineTo(x, 0);
                        lastX += x;
                    } else {
                        p.lineTo(x, lastY);
                        lastX = x;
                    }
                    break;
                }
                case 'V':
                case 'v': {
                    float y = ph.nextFloat();
                    if(cmd == 'v') {
                        p.rLineTo(0, y);
                        lastY += y;
                    } else {
                        p.lineTo(lastX, y);
                        lastY = y;
                    }
                    break;
                }
                case 'C':
                case 'c': {
                    wasCurve = true;
                    float x1 = ph.nextFloat();
                    float y1 = ph.nextFloat();
                    float x2 = ph.nextFloat();
                    float y2 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if(cmd == 'c') {
                        x1 += lastX;
                        x2 += lastX;
                        x += lastX;
                        y1 += lastY;
                        y2 += lastY;
                        y += lastY;
                    }
                    p.cubicTo(x1, y1, x2, y2, x, y);
                    lastX1 = x2;
                    lastY1 = y2;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'S':
                case 's': {
                    wasCurve = true;
                    float x2 = ph.nextFloat();
                    float y2 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if(cmd == 's') {
                        x2 += lastX;
                        x += lastX;
                        y2 += lastY;
                        y += lastY;
                    }
                    float x1 = 2 * lastX - lastX1;
                    float y1 = 2 * lastY - lastY1;
                    p.cubicTo(x1, y1, x2, y2, x, y);
                    lastX1 = x2;
                    lastY1 = y2;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'A':
                case 'a': {
                    float rx = ph.nextFloat();
                    float ry = ph.nextFloat();
                    float theta = ph.nextFloat();
                    int largeArc = (int)ph.nextFloat();
                    int sweepArc = (int)ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    drawArc(p, lastX, lastY, x, y, rx, ry, theta, largeArc, sweepArc);
                    lastX = x;
                    lastY = y;
                    break;
                }
            }
            if(!wasCurve) {
                lastX1 = lastX;
                lastY1 = lastY;
            }
            ph.skipWhitespace();
        }
        return p;
    }

    private static void drawArc(Path p, float lastX, float lastY, float x, float y, float rx,
            float ry, float theta, int largeArc, int sweepArc) {
        // todo - not implemented yet, may be very hard to do using Android drawing facilities.
    }

    private static NumberParse getNumberParseAttr(String name, Attributes attributes) {
        int n = attributes.getLength();
        for(int i = 0; i < n; i++) {
            if(attributes.getLocalName(i).equals(name)) {
                return parseNumbers(attributes.getValue(i));
            }
        }
        return null;
    }

    private static String getStringAttr(String name, Attributes attributes) {
        int n = attributes.getLength();
        for(int i = 0; i < n; i++) {
            if(attributes.getLocalName(i).equals(name)) {
                return attributes.getValue(i);
            }
        }
        return null;
    }

    private static Float getFloatAttr(String name, Attributes attributes) {
        return getFloatAttr(name, attributes, null);
    }

    private static Float getFloatAttr(String name, Attributes attributes, Float defaultValue) {
        String v = getStringAttr(name, attributes);
        if(v == null) {
            return defaultValue;
        } else {
            if(v.endsWith("px")) {
                v = v.substring(0, v.length() - 2);
            }
            // Log.d(TAG, "Float parsing '" + name + "=" + v + "'");
            return Float.parseFloat(v);
        }
    }

    private static class NumberParse {
        private ArrayList<Float> numbers;

        public NumberParse(ArrayList<Float> numbers) {
            this.numbers = numbers;
        }
    }

    private static class SVGHandler extends DefaultHandler {
        SVG svg;
        // Scratch rect (so we aren't constantly making new ones)
        RectF rect = new RectF();
        RectF limits = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                                 Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        private SVGHandler(SVG svg) {
            this.svg = svg;
        }

        private void doLimits(float x, float y) {
            if(x < limits.left) {
                limits.left = x;
            }
            if(x > limits.right) {
                limits.right = x;
            }
            if(y < limits.top) {
                limits.top = y;
            }
            if(y > limits.bottom) {
                limits.bottom = y;
            }
        }

        private void doLimits(float x, float y, float width, float height) {
            doLimits(x, y);
            doLimits(x + width, y + height);
        }

        private void doLimits(Path path) {
            path.computeBounds(rect, false);
            doLimits(rect.left, rect.top);
            doLimits(rect.right, rect.bottom);
        }

        private Matrix getTransform(Attributes atts) {
            final String transform = getStringAttr("transform", atts);
            return parseTransform(transform);
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName,
                Attributes atts) throws SAXException {
            // Ignore everything but rectangles in bounds mode
            if(localName.equals("svg")) {
                int width = (int)Math.ceil(getFloatAttr("width", atts));
                int height = (int)Math.ceil(getFloatAttr("height", atts));
                svg.setWidth(width);
                svg.setHeight(height);
            } else if(localName.equals("defs")) {
                // Ignore
            } else if(localName.equals("linearGradient")) {
                // Ignore
            } else if(localName.equals("radialGradient")) {
                // Ignore
            } else if(localName.equals("stop")) {
                // Ignore
            } else if(localName.equals("g")) {
                // Ignore
            } else if(localName.equals("rect")) {
                Float x = getFloatAttr("x", atts);
                if(x == null) {
                    x = 0f;
                }
                Float y = getFloatAttr("y", atts);
                if(y == null) {
                    y = 0f;
                }
                Float width = getFloatAttr("width", atts);
                Float height = getFloatAttr("height", atts);
                doLimits(x, y, width, height);
                svg.setTransform(getTransform(atts));
                svg.setType(SVG.TYPE_RECT);
                svg.setRect(x, y, x + width, y + height);
            } else if(localName.equals("line")) {
                // Ignore
            } else if(localName.equals("circle")) {
                Float centerX = getFloatAttr("cx", atts);
                Float centerY = getFloatAttr("cy", atts);
                Float radius = getFloatAttr("r", atts);
                if(centerX != null && centerY != null && radius != null) {
                    doLimits(centerX - radius, centerY - radius);
                    doLimits(centerX + radius, centerY + radius);
                    svg.setTransform(getTransform(atts));
                    svg.setType(SVG.TYPE_CIRCLE);
                    svg.setCircle(centerX, centerY, radius);
                }
            } else if(localName.equals("ellipse")) {
                Float centerX = getFloatAttr("cx", atts);
                Float centerY = getFloatAttr("cy", atts);
                Float radiusX = getFloatAttr("rx", atts);
                Float radiusY = getFloatAttr("ry", atts);
                if(centerX != null && centerY != null && radiusX != null && radiusY != null) {
                    rect.set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY
                            + radiusY);
                    doLimits(centerX - radiusX, centerY - radiusY);
                    doLimits(centerX + radiusX, centerY + radiusY);
                    svg.setTransform(getTransform(atts));
                    svg.setType(SVG.TYPE_OVAL);
                    svg.setOval(rect);
                }
            } else if(localName.equals("polyline")) {
                // Ignore
            } else if(localName.equals("polygon")) {
                NumberParse numbers = getNumberParseAttr("points", atts);
                if(numbers != null) {
                    Path p = new Path();
                    ArrayList<Float> points = numbers.numbers;
                    if(points.size() > 1) {
                        p.moveTo(points.get(0), points.get(1));
                        for(int i = 2; i < points.size(); i += 2) {
                            float x = points.get(i);
                            float y = points.get(i + 1);
                            p.lineTo(x, y);
                        }
                        // Don't close a polyline
                        if(localName.equals("polygon")) {
                            p.close();
                        }
                        doLimits(p);
                        svg.setTransform(getTransform(atts));
                        svg.setType(SVG.TYPE_POLYGON);
                        svg.setPolygone(p);
                    }
                }
            } else if(localName.equals("path")) {
                Path p = doPath(getStringAttr("d", atts));
                doLimits(p);
                svg.setTransform(getTransform(atts));
                svg.setType(SVG.TYPE_PATH);
                svg.setPath(p);
            } else {
                Log.d(TAG, "UNRECOGNIZED SVG COMMAND: " + localName);
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            // no-op
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
            // no-op
        }
    }
}
