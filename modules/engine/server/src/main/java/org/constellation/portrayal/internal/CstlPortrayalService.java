/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.portrayal.internal;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.constellation.portrayal.PortrayalServiceIF;

import org.geotoolkit.display.canvas.control.CanvasMonitor;
import org.geotoolkit.display.canvas.control.NeverFailMonitor;
import org.geotoolkit.display.canvas.control.StopOnErrorMonitor;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.DefaultPortrayalService;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.display2d.service.VisitDef;


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
 * @see Cst.Portrayal
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
        
    private CstlPortrayalService(){       
    }

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
