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
package org.constellation.portrayal.internal;

import org.constellation.portrayal.PortrayalServiceIF;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display.canvas.control.CanvasMonitor;
import org.geotoolkit.display.canvas.control.NeverFailMonitor;
import org.geotoolkit.display.canvas.control.StopOnErrorMonitor;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.DefaultPortrayalService;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.display2d.service.VisitDef;

import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Service class to portray or work with two dimensional scenes defined by a 
 * scene definition, a view definition, and a canvas definition.
 * <p>
 * <b>Users should *not* call this class directly.</b><br/>
 * Instead, the {@link Cstl.Portrayal} reference should be used.
 * </p>
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @see Cstl.Portrayal
 * @see Portrayal
 */
public final class CstlPortrayalService implements PortrayalServiceIF {

    private static final CstlPortrayalService INSTANCE = new CstlPortrayalService();
    
    /**
     * @return a singleton of cstlPortralyalService
     */
    public static CstlPortrayalService getInstance(){
        return INSTANCE;
    }
        
    private CstlPortrayalService(){}

    /**
     *{@inheritDoc}
     */
    @Override
    public BufferedImage portray( final SceneDef sdef,
                                  final ViewDef vdef,
                                  final CanvasDef cdef) 
    		throws PortrayalException {
    	
        final StopOnErrorMonitor monitor = new StopOnErrorMonitor();
        vdef.setMonitor(monitor);

        try {
            final BufferedImage buffer = DefaultPortrayalService.portray(cdef,sdef,vdef);

            final Exception exp = monitor.getLastException();
            if(exp != null){
                throw exp;
            }

            return buffer;

        } catch(Exception ex) {
            if (ex instanceof PortrayalException) {
                throw (PortrayalException)ex;
            } else {
                throw new PortrayalException(ex);
            }
        } finally {
            sdef.getContext().layers().clear();
        }

    }
    
    /**
     *{@inheritDoc}
     */
    @Override
    public void visit( final SceneDef sdef,
                       final ViewDef vdef,
                       final CanvasDef cdef,
                       final VisitDef visitDef)
            throws PortrayalException {

        try{
            DefaultPortrayalService.visit(cdef,sdef,vdef,visitDef);
        }catch(Exception ex){
            if (ex instanceof PortrayalException) {
                throw (PortrayalException)ex;
            } else {
                throw new PortrayalException(ex);
            }
        }finally{
            visitDef.getVisitor().endVisit();
            sdef.getContext().layers().clear();
        }

    }
    
    /*
     * TODO: document how the size of the text is chosen.
     */
    @Override
    public BufferedImage writeInImage(Exception e, Dimension dim){
        return DefaultPortrayalService.writeException(e, dim);
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public BufferedImage writeBlankImage(Color color, Dimension dim) {
        final BufferedImage img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, dim.width, dim.height);
        g.dispose();
        return img;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public void portray(SceneDef sdef, ViewDef vdef, CanvasDef cdef, OutputDef odef) throws PortrayalException {

        //never stop rendering, we write in the output, we must never.
        final CanvasMonitor monitor = new NeverFailMonitor();
        vdef.setMonitor(monitor);

        try {
            DefaultPortrayalService.portray(cdef,sdef,vdef,odef);
        }catch(PortrayalException ex){
            throw ex;
        } catch(Exception ex) {
            throw new PortrayalException(ex);
        } finally {
            sdef.getContext().layers().clear();
        }
    }
        
}
