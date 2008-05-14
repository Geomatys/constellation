/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.observation.xml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import net.seagis.catalog.CatalogException;
import net.seagis.swe.PhenomenonEntry;
import net.seagis.swe.PhenomenonTable;
import net.seagis.gml.UnitOfMeasureEntry;
import net.seagis.gml.UnitOfMeasureTable;
import net.seagis.sampling.SamplingPointEntry;
import net.seagis.sampling.SamplingPointTable;

/**
 * Permet de creer les dictionnaire de données au format XML a partir de la base de données.
 *
 * @author Guilhem Legal
 * @deprecated use JAXB
 */
public class XMLWriter {
    
    /**
     * Creer un writer XML qui lis dans la base de données et ecris dans des fichier XML
     */
    public XMLWriter() {
    }
    
    /**
     * Ecris le fichier dictionnaire des phenomenes.
     *
     * @throws CatalogException, SQLException, IOException.
     */
    public void WritePhenomenonDictionary(String urlFile, PhenomenonTable phenomenons) throws CatalogException, SQLException, IOException{
        Set<PhenomenonEntry> list = phenomenons.getEntries();
        String codeSpace = "urn:x-ogc:tc:arch:doc-rp(05-010)";
        
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + '\n', urlFile);
        write("<gml:Dictionnary xmlns:gml=\"http://www.opengis.net/gml\" xmlns:swe=\"http://www.opengis.net/swe/1.0.1\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"   xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/swe/1.0.1 ./sweCommon/1.0.0/swe.xsd\" " +
                "gml:id=\"phenomene_swe\">" + '\n', urlFile);
        write( '\t' + "<gml:description>description des phénomène</gml:description>" + '\n', urlFile);
        write( '\t' +"<gml:name>Liste des phénomène</gml:name>" + '\n', urlFile);
        
        String decalage = '\t' + "";
        Iterator i = list.iterator();
        while(i.hasNext()){
            PhenomenonEntry p = (PhenomenonEntry)i.next();
            write(decalage + "<gml:dictionaryEntry>" + '\n', urlFile);
            decalage += '\t';
            write(decalage + "<swe:Phenomenon gml:id=\"" + p.getId() + "\">" + '\n', urlFile);
            decalage += '\t';
            write(decalage + "<gml:description>\"" + p.getDescription() + "</gml:description>" + '\n', urlFile);
            write(decalage + "<gml:name codeSpace=\"" + codeSpace + "\">" + p.getName() + "</gml:name>" + '\n', urlFile);
            decalage = decalage.substring(0, decalage.length()-1);
            write(decalage + "</swe:Phenomenon>" + '\n', urlFile);
            decalage = decalage.substring(0, decalage.length()-1);
            write(decalage + "</gml:dictionaryEntry>" + '\n', urlFile);
        }
        decalage = decalage.substring(0, decalage.length()-1);
        write(decalage + "</gml:Dictionary>" + '\n', urlFile);
    }
    
    /**
     * Ecris le fichier dictionnaire des stations.
     */
    public void WriteStationDictionary(String urlFile, SamplingPointTable stations)  throws CatalogException, SQLException, IOException{
        Set<SamplingPointEntry> list = stations.getEntries();
        String codeSpace = "urn:x-brgm:def:samplingStation:bss";
        
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + '\n', urlFile);
        write("<gml:Dictionnary xmlns:gml=\"http://www.opengis.net/gml\" xmlns:swe=\"http://www.opengis.net/swe/1.0.1\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"   xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/swe/1.0.1 ./sweCommon/1.0.0/swe.xsd\" " +
                "gml:id=\"phenomene_swe\">" + '\n', urlFile);
        write( '\t' + "<gml:description>description des stations</gml:description>" + '\n', urlFile);
        write( '\t' +"<gml:name>Liste des stations</gml:name>" + '\n', urlFile);
        
        String decalage = '\t' + "";
        Iterator i = list.iterator();
        while(i.hasNext()){
            SamplingPointEntry sp = (SamplingPointEntry)i.next();
            write(decalage + "<gml:dictionaryEntry>" + '\n', urlFile);
            decalage += '\t';
            write(decalage + "<sa:SamplingPoint gml:id=\"" + sp.getId() + "\">" + '\n', urlFile);
            decalage += '\t';
            write(decalage + "<gml:description>\"" + sp.getDescription() + "</gml:description>" + '\n', urlFile);
            write(decalage + "<gml:name codeSpace=\"" + codeSpace + "\">" + sp.getName() + "</gml:name>" + '\n', urlFile);
            write(decalage + "<gml:sampledFeature xlink:href=\"" + sp.getSampledFeatures().iterator().next() + "\"/>" + '\n', urlFile);
            write(decalage + "<sa:position>" + '\n', urlFile);
            decalage += '\t';
            write(decalage + "<gml:Point gml:id=\"" + sp.getPosition().getId() + "\">" + '\n', urlFile);
            decalage += '\t';
            write(decalage + "<gml:pos srsName=\"" + sp.getPosition().getPos().getSrsName() + "\" srsDimension=\"" + sp.getPosition().getPos().getDimension() +
                    "\">" + sp.getPosition().getPos().getValue() + "</gml:pos>" + '\n', urlFile);
            decalage = decalage.substring(0, decalage.length()-1);
            
            write(decalage + "</gml:Point>" + '\n', urlFile);
            decalage = decalage.substring(0, decalage.length()-1);
            write(decalage + "</sa:position>" + '\n', urlFile);
            decalage = decalage.substring(0, decalage.length()-1);
            write(decalage + "</sa:SamplingPoint>" + '\n', urlFile);
            decalage = decalage.substring(0, decalage.length()-1);
            write(decalage + "</gml:dictionaryEntry>" + '\n', urlFile);
        }
        decalage = decalage.substring(0, decalage.length()-1);
        write(decalage + "</gml:Dictionary>" + '\n', urlFile);
        
    }
    
    /**
     * Ecris le fichier dictionnaire des unites de mesure.
     */
    public void WriteUOMDictionary(String urlFile, UnitOfMeasureTable units) throws CatalogException, SQLException, IOException{
        Set<UnitOfMeasureEntry> list = units.getEntries();
        String codeSpace = "urn:x-ogc:tc:arch:doc-rp(05-010)";
        
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + '\n', urlFile);
        write("<gml:Dictionnary xmlns:gml=\"http://www.opengis.net/gml\" xmlns:swe=\"http://www.opengis.net/swe/1.0.1\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"   xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/swe/1.0.1 ./sweCommon/1.0.0/swe.xsd\" " +
                "gml:id=\"phenomene_swe\">" + '\n', urlFile);
        write( '\t' + "<gml:description>description des unités de mesure</gml:description>" + '\n', urlFile);
        write( '\t' +"<gml:name>Liste des unité de mesure</gml:name>" + '\n', urlFile);
        
        String decalage = '\t' + "";
        Iterator i = list.iterator();
        while(i.hasNext()){
            UnitOfMeasureEntry uom = (UnitOfMeasureEntry)i.next();
            write(decalage + "<gml:dictionaryEntry>" + '\n', urlFile);
            decalage += '\t';
            write(decalage + "<gml:BaseUnit gml:id=\"" + uom.getId() + "\">" + '\n', urlFile);
            decalage += '\t';
            write(decalage + "<gml:name codeSpace=\"" + codeSpace + "\">" + uom.getName() + "</gml:name>" + '\n', urlFile);
            write(decalage + "<gml:quantityType\">" + uom.getQuantityType() + "</gml:quantity>" + '\n', urlFile);
            write(decalage + "<gml:unitsSystem\">" + uom.getUnitsSystem() + "</gml:unitsSystem>" + '\n', urlFile);
            decalage = decalage.substring(0, decalage.length()-1);
            write(decalage + "</gml:BaseUnit>" + '\n', urlFile);
            decalage = decalage.substring(0, decalage.length()-1);
            write(decalage + "</gml:dictionaryEntry>" + '\n', urlFile);
        }
        decalage = decalage.substring(0, decalage.length()-1);
        write(decalage + "</gml:Dictionary>" + '\n', urlFile);
    }
    
    
    /**
     * Write at the end of the XML file with the specified number of tabulation.
     *
     * @param text The text to append to the file.
     * @param urlFile The url file.
     *
     * @throws IOException
     */
    private void write(String text, String urlFile) throws IOException {
        
        //true means we append a the end of the file
        FileWriter fw = new FileWriter(urlFile, true);
        BufferedWriter output = new BufferedWriter(fw);
        
        output.write(text);
        output.newLine();
        output.flush();
    }
    
    /**
     * Empty a the XML file.
     *
     * @param urlFile The url file.
     *
     * @throws IOException
     */
    private void emptyFile(String urlFile) throws IOException {
        
        //false means we overwrite
        FileWriter fw = new FileWriter(urlFile, false);
        BufferedWriter output = new BufferedWriter(fw);
        
        output.write("");
        output.flush();
        output.close();
    }
    
}

