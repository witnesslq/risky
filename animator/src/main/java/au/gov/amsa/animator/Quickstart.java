package au.gov.amsa.animator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WebMapServer;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.event.MapMouseAdapter;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.wms.WMSLayerChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a
 * map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and
 * tutorials. *
 */
public class Quickstart {

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile
     * and displays its contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        // System.setProperty("http.proxyHost", "proxy.amsa.gov.au");
        // System.setProperty("http.proxyPort", "8080");
        // System.setProperty("https.proxyHost", "proxy.amsa.gov.au");
        // System.setProperty("https.proxyPort", "8080");
        File file = new File(
                "/home/dxm/Downloads/shapefile-australia-coastline-polygon/cstauscd_r.shp");

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        final MapContent map = new MapContent();

        map.setTitle("Animator");
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
        map.addLayer(createExtraFeatures());
        // addWms(map);

        GTRenderer renderer = new StreamingRenderer() {

            @Override
            public void paint(Graphics2D g, Rectangle paintArea, ReferencedEnvelope mapArea,
                    AffineTransform worldToScreen) {
                super.paint(g, paintArea, mapArea, worldToScreen);
                System.out.println("drawing");
                Point2D.Float d = new Point2D.Float();
                worldToScreen.transform(new Point2D.Float(149.1244f, -35.3075f), d);
                g.drawString("Canberra", d.x, d.y);
            }

        };

        final AtomicReference<AnimatorMapFrame> mapFrame = new AtomicReference<AnimatorMapFrame>();
        // Now display the map
        SwingUtilities.invokeLater(() -> {
            final AnimatorMapFrame frame = new AnimatorMapFrame(map, renderer);
            frame.enableStatusBar(true);
            frame.enableToolBar(true);
            frame.initComponents();
            frame.setSize(800, 600);
            JPanel glass = (JPanel) frame.getGlassPane();
            glass.setLayout(null);
            glass.add(new JButton("boo"));
            mapFrame.set(frame);
            frame.getMapPane().addMouseListener(new MapMouseAdapter() {

                @Override
                public void onMouseClicked(MapMouseEvent event) {
                    DirectPosition2D p = event.getWorldPos();
                    System.out.println(p);
                }

            });
            frame.setVisible(true);
            glass.setVisible(true);

        });

    }

    private static Layer createExtraFeatures() throws SchemaException {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("Location");
        b.setCRS(DefaultGeographicCRS.WGS84);
        // picture location
        b.add("geom", Point.class);
        final SimpleFeatureType TYPE = b.buildFeatureType();

        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
        Point point = gf.createPoint(new Coordinate(149.1244, -35.3075));

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(TYPE);
        builder.add(point);
        SimpleFeature feature = builder.buildFeature("Canberra");
        DefaultFeatureCollection features = new DefaultFeatureCollection(null, null);
        features.add(feature);

        Style style = SLD.createPointStyle("Star", Color.BLUE, Color.BLUE, 0.3f, 10);

        return new FeatureLayer(features, style);
    }

    private static void addWms(MapContent map) {
        // URL wmsUrl = WMSChooser.showChooseWMS();

        WebMapServer wms;
        try {
            wms = new WebMapServer(new URL("http://sarapps.amsa.gov.au:8080/cts-gis/wms"));
        } catch (ServiceException | IOException e) {
            throw new RuntimeException(e);
        }
        List<org.geotools.data.ows.Layer> wmsLayers = WMSLayerChooser.showSelectLayer(wms);
        for (org.geotools.data.ows.Layer wmsLayer : wmsLayers) {
            System.out.println("adding " + wmsLayer.getTitle());
            WMSLayer displayLayer = new WMSLayer(wms, wmsLayer);
            map.addLayer(displayLayer);
        }
    }

}