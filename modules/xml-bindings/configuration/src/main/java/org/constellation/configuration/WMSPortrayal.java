/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.configuration;

import org.apache.sis.util.ArraysExt;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.display2d.GO2Hints;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.PortrayalExtension;
import org.geotoolkit.factory.Hints;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
@XmlRootElement(name="WMSPortrayal")
@XmlAccessorType(XmlAccessType.FIELD)
public class WMSPortrayal {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.configuration");

    private static final String INTERPOLATION_NEAREST = "nearest";
    private static final String INTERPOLATION_BILINEAR = "bilinear";
    private static final String INTERPOLATION_BICUBIC = "bicubic";

    private static final String RENDERING_DEFAULT = "default";
    private static final String RENDERING_QUALITY = "quality";
    private static final String RENDERING_SPEED = "speed";

    private static final String RENDERING_ORDER_FEATURE = "feature";
    private static final String RENDERING_ORDER_SYMBOLIZER = "symbolizer";

    @XmlTransient
    private static boolean emptyExtension = false;

    @XmlTransient
    private final static Map<String,ImageWriterSpi> nativewriterspi = new HashMap<String, ImageWriterSpi>();

    /**
     * AntiAliasing.
     */
    @XmlElement(name="Antialiasing")
    private boolean antialiasing = true;

    /**
     * Interpolation is used when rendering images, values are : {@link #INTERPOLATION_NEAREST}, {@link #INTERPOLATION_BILINEAR} or {@link #INTERPOLATION_BICUBIC}.
     */
    @XmlElement(name="Interpolation")
    private String interpolation ;

    /**
     * Global Java2D rendering quality, values are : {@link #RENDERING_QUALITY}, {@link #RENDERING_SPEED} or {@link #RENDERING_DEFAULT}.
     */
    @XmlElement(name="Rendering")
    private String rendering;

    /**
     * Set the rendering order,order by symbolizer is longer but gives nicer results for vector
     * datas, values are : {@link #RENDERING_ORDER_FEATURE} or {@link #RENDERING_ORDER_SYMBOLIZER}.
     */
    @XmlElement(name="Rendering-order")
    private String renderingOrder;

    /**
     * Generalization of geometries.
     */
    @XmlElement(name="Generalize")
    private boolean generalize;

    /**
     * Generalization factor for geometries.
     */
    @XmlElement(name="Generalize-factor")
    private Float generalizeFactor;

    /**
     * MultiThreaded : experimental, may consume more memory but may provide much better
     * performance for several layers at the same time.
     */
    @XmlElement(name="Multithread")
    private boolean multithread;

    /**
     * Direct coverage writer : experimental, when possible allow the portrayal service
     * to directly write coverages in the response stream.
     */
    @XmlElement(name="Coverage-writer")
    private boolean coverageWriter;

    /**
     * Parallal Buffer : experimental.
     */
    @XmlElement(name="Parallal-buffer")
    private boolean parallalBuffer;

    /**
     * Set the Native Java Advanced Imaging readers that are allowed to be used. Default : none.
     */
    @XmlElement(name="JAI-native-reader")
    private List<String> nativeReader = new ArrayList<String>();

    /**
     * Set the Native Java Advanced Imaging writers that are allowed to be used. Default : png and jpeg allowed.
     */
    @XmlElement(name="JAI-native-writer")
    private List<String> nativeWriter = new ArrayList<String>();

    /**
     * Compressions level based on the mime type.
     */
    @XmlElement(name="Compressions")
    private Compressions compressions;

    /**
     * Legend template used by default.
     */
    @XmlElement(name="LegendTemplate")
    private LegendTemplate legendTemplate;

    /**
     * Portrayal decoration like Grid, Text, Images, Compas, ...
     */
    @XmlElement(name="Decorations")
    private Decorations decorations;


    public WMSPortrayal() {

        //default rendering hints
        this.antialiasing = true;
        this.interpolation = INTERPOLATION_NEAREST;
        this.rendering = RENDERING_DEFAULT;
        this.renderingOrder = RENDERING_ORDER_FEATURE;
        this.generalize = true;
        this.generalizeFactor = 1.3f;
        this.multithread = false;
        this.coverageWriter = false;
        this.parallalBuffer = false;

        this.legendTemplate = new LegendTemplate();
        this.decorations = new Decorations();
        this.compressions = new Compressions();
    }

     /**
     * @return the hints defined in the wms portrayal configuration file.
     */
    public Hints getHints() {

        final Hints hints = new Hints();

        //antialiasing
        hints.put(RenderingHints.KEY_ANTIALIASING, (antialiasing) ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

        //interpolation
        if (interpolation != null && !interpolation.isEmpty()) {
            if (INTERPOLATION_BICUBIC.equalsIgnoreCase(interpolation)) {
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            } else if (INTERPOLATION_BILINEAR.equalsIgnoreCase(interpolation)) {
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            } else if (INTERPOLATION_NEAREST.equalsIgnoreCase(interpolation)) {
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            } else {
                LOGGER.log(Level.WARNING, "Interpolation value not valid : {0}. Can be \"nearest\", \"bilinear\" or \"bicubic\".", interpolation);
            }
        }

        //rendering
        if (rendering != null && !rendering.isEmpty()) {
            if (RENDERING_DEFAULT.equalsIgnoreCase(rendering)) {
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
            } else if (RENDERING_QUALITY.equalsIgnoreCase(rendering)) {
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            } else if (RENDERING_SPEED.equalsIgnoreCase(rendering)) {
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            } else {
                LOGGER.log(Level.WARNING, "Rendering value not valid : {0}. Can be \"default\", \"quality\" or \"speed\".", rendering);
            }
        }

        //renderingOrder
        if (renderingOrder != null && !renderingOrder.isEmpty()) {
            if (RENDERING_ORDER_SYMBOLIZER.equalsIgnoreCase(renderingOrder)) {
                hints.put(GO2Hints.KEY_SYMBOL_RENDERING_ORDER, GO2Hints.SYMBOL_RENDERING_PRIME);
            } else if (RENDERING_ORDER_FEATURE.equalsIgnoreCase(renderingOrder)) {
                hints.put(GO2Hints.KEY_SYMBOL_RENDERING_ORDER, GO2Hints.SYMBOL_RENDERING_SECOND);
            } else {
                LOGGER.log(Level.WARNING, "Rendering order value not valid : {0}. Can be \"feature\" or \"symbolizer\".", renderingOrder);
            }
        }

        //generalize
        hints.put(GO2Hints.KEY_GENERALIZE, generalize);
        //generalize factor
        hints.put(GO2Hints.KEY_GENERALIZE_FACTOR, (generalizeFactor != null) ? generalizeFactor : GO2Hints.GENERALIZE_FACTOR_DEFAULT);

        //multithread
        hints.put(GO2Hints.KEY_MULTI_THREAD, multithread);

        //coverageWriter
        hints.put(GO2Hints.KEY_COVERAGE_WRITER, coverageWriter);

        //parallalBuffer
        hints.put(GO2Hints.KEY_PARALLAL_BUFFER, parallalBuffer);
        return hints;
    }

    public void setHints(final Hints hints) {
        //antialiasing
        if (hints.containsKey(RenderingHints.KEY_ANTIALIASING)) {
            antialiasing = hints.get(RenderingHints.KEY_ANTIALIASING).equals(RenderingHints.VALUE_ANTIALIAS_ON) ? true : false;
        }

        //interpolation
        if (hints.containsKey(RenderingHints.KEY_INTERPOLATION)) {
            final Object inter = hints.get(RenderingHints.KEY_INTERPOLATION);
            if (inter.equals(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)) {
                interpolation = INTERPOLATION_NEAREST;
            } else if (inter.equals(RenderingHints.VALUE_INTERPOLATION_BICUBIC)) {
                interpolation = INTERPOLATION_BICUBIC;
            } else if (inter.equals(RenderingHints.VALUE_INTERPOLATION_BILINEAR)) {
                interpolation = INTERPOLATION_BILINEAR;
            }
        }

        //rendering
        if (hints.containsKey(RenderingHints.KEY_RENDERING)) {
            final Object render = hints.get(RenderingHints.KEY_RENDERING);
            if (render.equals(RenderingHints.VALUE_RENDER_DEFAULT)) {
                rendering = RENDERING_DEFAULT;
            } else if (render.equals(RenderingHints.VALUE_RENDER_QUALITY)) {
                rendering = RENDERING_QUALITY;
            } else if (render.equals(RenderingHints.VALUE_RENDER_SPEED)) {
                rendering = RENDERING_SPEED;
            }
        }

        //rendering order
        if (hints.containsKey(GO2Hints.KEY_SYMBOL_RENDERING_ORDER)) {
            final Object renderOrder = hints.get(GO2Hints.KEY_SYMBOL_RENDERING_ORDER);
            if (renderOrder.equals(GO2Hints.SYMBOL_RENDERING_PRIME)) {
                renderingOrder = RENDERING_ORDER_SYMBOLIZER;
            } else if (renderOrder.equals(GO2Hints.SYMBOL_RENDERING_SECOND)) {
                renderingOrder = RENDERING_ORDER_FEATURE;
            }
        }

        //generalize
        if (hints.containsKey(GO2Hints.KEY_GENERALIZE)) {
            generalize = (Boolean) hints.get(GO2Hints.KEY_GENERALIZE);
        }

        //generalize factor
        if (hints.containsKey(GO2Hints.KEY_GENERALIZE_FACTOR)) {
            generalizeFactor = (Float) hints.get(GO2Hints.KEY_GENERALIZE_FACTOR);
        }

        //multithread
        if (hints.containsKey(GO2Hints.KEY_MULTI_THREAD)) {
            multithread = (Boolean) hints.get(GO2Hints.KEY_MULTI_THREAD);
        }

        //coverageWriter
        if (hints.containsKey(GO2Hints.KEY_COVERAGE_WRITER)) {
            coverageWriter = (Boolean) hints.get(GO2Hints.KEY_COVERAGE_WRITER);
        }

        //parallalBuffer
        if (hints.containsKey(GO2Hints.KEY_PARALLAL_BUFFER)) {
            parallalBuffer = (Boolean) hints.get(GO2Hints.KEY_PARALLAL_BUFFER);
        }
    }

    public void setLegendTemplate(LegendTemplate legendTemplate) {
        this.legendTemplate = legendTemplate;
    }


    /**
     * Returns the default legend template.
     */
    public org.geotoolkit.display2d.ext.legend.LegendTemplate getDefaultLegendTemplate(){
        if (legendTemplate != null) {
            return legendTemplate.toDisplayLegendTemplate();
        }
        return null;
    }

    /**
     * First call to this method will parse the configuration file if there is one.
     *
     * @return PortrayalExtension
     */
    public PortrayalExtension getExtension() {
        if (emptyExtension) return new DecorationExtension();

        return decorations.getExtension();
    }

    /**
     * Disable any read extension (test purpose).
     *
     * @return PortrayalExtension
     */
    public static synchronized void setEmptyExtension(final boolean emptyExt) {
        emptyExtension = emptyExt;
    }


    public Map<String,Float> getCompressions(){
        final Map<String, Float> compMap = new HashMap<String, Float>();
        if (compressions != null) {
            for (Compression comp : compressions.getCompressions()) {
                compMap.put(comp.getMimeType(), comp.getCompression());
            }
        }
        return compMap;
    }

    public Float getCompression(String mime){
        if (compressions != null) {
            for (Compression comp : compressions.getCompressions()) {
                if (comp.getMimeType().equals(mime)) {
                    return comp.getCompression();
                }
            }
        }
        return null;
    }

    public void setCompression(List<Compression> compression) {
        if (compressions != null) {
            this.compressions.setCompressions(compression);
        }
    }

    public void addCompression(Compression compression) {
        if (compressions != null) {
            this.compressions.getCompressions().add(compression);
        }
    }

    /**
     * Create an output definition for the given mime type
     * Compression rate, type and optimal writer spi.
     *
     * @param mime
     * @return
     */
    public OutputDef getOutputDef(String mime) {
        final OutputDef odef = new OutputDef(mime, new Object());
        odef.setCompression(getCompression(mime));

        if(nativeWriter != null){
            for(String str : nativeWriter){
                if(mime.equalsIgnoreCase(str)){
                    odef.setSpi(getNativeWriterSpi(mime));
                }
            }
        }
        if (odef.getSpi() == null) {
            final ServiceRegistry registry = IIORegistry.getDefaultInstance();
            for (final Iterator<ImageWriterSpi> it = registry.getServiceProviders(ImageWriterSpi.class, false); it.hasNext();) {
                ImageWriterSpi spi = it.next();
                final String classname = spi.getClass().getName();
                if (!classname.startsWith("com.sun.media.")) {
                    if(ArraysExt.contains(spi.getMIMETypes(),mime)){
                        odef.setSpi(spi);
                        break;
                    }
                }
            }
        }
        return odef;
    }

    private synchronized static ImageWriterSpi getNativeWriterSpi(final String mime){
        ImageWriterSpi spi = nativewriterspi.get(mime);
        if(spi != null) return spi;

        final ServiceRegistry registry = IIORegistry.getDefaultInstance();
        for (final Iterator<ImageWriterSpi> it = registry.getServiceProviders(ImageWriterSpi.class, false); it.hasNext();) {
            spi = it.next();
            final String classname = spi.getClass().getName();
            if (classname.startsWith("com.sun.media.")) {
                if(ArraysExt.contains(spi.getMIMETypes(),mime)){
                    nativewriterspi.put(mime, spi);
                    return spi;
                }
            }
        }
        return null;
    }


    public boolean isAntialiasing() {
        return antialiasing;
    }

    public void setAntialiasing(boolean antialiasing) {
        this.antialiasing = antialiasing;
    }

    public String getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(String interpolation) {
        this.interpolation = interpolation;
    }

    public String getRendering() {
        return rendering;
    }

    public void setRendering(String rendering) {
        this.rendering = rendering;
    }

    public String getRenderingOrder() {
        return renderingOrder;
    }

    public void setRenderingOrder(String renderingOrder) {
        this.renderingOrder = renderingOrder;
    }

    public boolean isGeneralize() {
        return generalize;
    }

    public void setGeneralize(boolean generalize) {
        this.generalize = generalize;
    }

    public Float getGeneralizeFactor() {
        return generalizeFactor;
    }

    public void setGeneralizeFactor(Float generalizeFactor) {
        this.generalizeFactor = generalizeFactor;
    }

    public boolean isMultithread() {
        return multithread;
    }

    public void setMultithread(boolean multithread) {
        this.multithread = multithread;
    }

    public boolean isCoverageWriter() {
        return coverageWriter;
    }

    public void setCoverageWriter(boolean coverageWriter) {
        this.coverageWriter = coverageWriter;
    }

    public boolean isParallalBuffer() {
        return parallalBuffer;
    }

    public void setParallalBuffer(boolean parallalBuffer) {
        this.parallalBuffer = parallalBuffer;
    }

    public List<String> getNativeReader() {
        return nativeReader;
    }

    public void setNativeReader(List<String> nativeReader) {
        this.nativeReader = nativeReader;
    }

    public List<String> getNativeWriter() {
        return nativeWriter;
    }

    public void setNativeWriter(List<String> nativeWriter) {
        this.nativeWriter = nativeWriter;
    }

    public List<AbstractDecoration> getDecorations() {
        return decorations.getDecorations();
    }

    public void setDecorations(List<AbstractDecoration> decorations) {
        this.decorations.setDecorations(decorations);
    }


}
