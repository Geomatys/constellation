/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.coverage.catalog.sql;

// J2SE
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

// Geotools
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.resources.Arguments;

// Sicade
import net.sicade.coverage.catalog.CatalogException;
import net.sicade.coverage.catalog.Observations;
import net.sicade.coverage.catalog.Layer;


/**
 * Ajoute des entrées dans une table {@link WritableGridCoverageTable}.
 * Elle peut être appelée par un script shell pour automatiser l'ajout d'image dans la table 
 * {@link GridCoverageTable GridCoverage}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 *
 * @todo Envisager de déplacer ce code vers {@link WritableGridCoverageTable}.
 */
@Deprecated
public class UpdateGridCoverageTable {
    /**
     * Interdit les instantiations de cette classe.
     */
    private UpdateGridCoverageTable() {
    }
    
    /**
     * Retourne un objet {@link Date} correspondant a la chaîne de caractère {@code date} fournie 
     * en paramètre.
     * 
     * @param   date    La chaîne de caractère à "traduire" en objet {@link Date}.
     */
    private static Date getDate(String date) {
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Calendar calendar = format.getCalendar();
        int an;
        int mois;
        int jour;
        switch (date.length()) {
            case 7 :    an      = Integer.parseInt(date.substring(0, 4));
                        mois    = 0;
                        jour    = Integer.parseInt(date.substring(4, date.length()));
                        break;
            case 8 :    final int dateComplete = Integer.parseInt(date.substring(0, date.length()));
                        an      = dateComplete/10000;
                        jour    = dateComplete - (an*10000);
                        mois    = jour/100;
                        jour   -= mois*100;
                        break;
            default :   System.err.print("Date invalide : " + date  + ". ");
                        System.err.println("Erreur lors du calcul des dates...");
                        System.exit(1);
                        return null;
        }
        calendar.clear();
        mois = (mois != 0) ? (mois - 1) : mois;
        calendar.set(an, mois, jour);
        return calendar.getTime();
    }
    
    /**
     * La méthode principale attend comme argument sur la ligne de commande : 
     *  - la couche pour laquelle rajouter une image, 
     *  - le nom de l'image à rajouter (sans son chemin ni son extension), 
     *  - la date de début, 
     *  - la date de fin, 
     *  - xmin, ymin, xmax, ymax coordonnées géographiques, 
     *  - largeur et hauteur de l'image.
     * Les dates peuvent être au format : aaaammjj ou aaaajjj.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws CatalogException, SQLException {
        if (args.length == 0) {
            help();
            return;
        }
        final Arguments     arguments    = new Arguments(args);
        final Observations  observations = Observations.getDefault();
        final Layer         layer        = observations.getLayer(arguments.getRequiredString("-layers"));
        final String        filename     = arguments.getRequiredString("-f");
        final Date          startTime    = getDate(arguments.getRequiredString("-startTime"));
        final Date          endTime      = getDate(arguments.getRequiredString("-endTime"));
        final Double        xmin         = arguments.getRequiredDouble ("-xmin" );
        final Double        xmax         = arguments.getRequiredDouble ("-xmax" );
        final Double        ymin         = arguments.getRequiredDouble ("-ymin" );
        final Double        ymax         = arguments.getRequiredDouble ("-ymax" );
        final Rectangle2D   bbox         = new Rectangle2D.Double(xmin, ymin, (xmax-xmin), (ymax-ymin));
        final Dimension     size         = new Dimension(arguments.getRequiredInteger("-width"), 
                                                         arguments.getRequiredInteger("-height"));
        final WritableGridCoverageTable wgt = observations.getDatabase().getTable(WritableGridCoverageTable.class);
        wgt.setLayer(layer);
        wgt.addEntry(filename, startTime, endTime, new GeographicBoundingBoxImpl(bbox), size);
    }
    
    /**
     * Affiche l'aide.
     */
    private static void help() {
        System.out.println("Usage: -layer=layer -f=filename -startTime=aaaammjj -endTime=aaaammjj " +
                "-xmin=xmin -xmax=xmax -ymin=ymin -ymax=ymax -width=width -height=height");
        System.out.println();
        System.out.println("  -layer        La couche pour laquelle il faut rajouter une entrée dans la table.");
        System.out.println("  -f            Le nom de l'image, sans son chemin ni son extension.");
        System.out.println("  -startTime    Le début de la plage de temps qui concerne l'image.");
        System.out.println("  -endTime      La fin de la plage de temps qui concerne l'image.");
        System.out.println("  -xmin         Le xmin de la bounding box de l'image.");
        System.out.println("  -xmax         Le xmax de la bounding box de l'image.");
        System.out.println("  -ymin         Le ymin de la bounding box de l'image.");
        System.out.println("  -ymax         Le ymax de la bounding box de l'image.");
        System.out.println();
        System.out.println("Les cinq paramètres sont obligatoires.");
        System.out.println();
        System.out.println("Le format de la date peut être : aaaammjj ou bien aaaajjj.");
        System.out.println();
        System.out.println("L'exemple suivant rajoute dans la table \"GridCoverages\" : ");
        System.out.println("    - l'image                   \"PP20070102\" ");
        System.out.println("    - de la couche              \"Potentiel de pêche (Calédonie) BET-optimal\" ");
        System.out.println("    - dont la date de début est \"20070101\" ");
        System.out.println("    - la date de fin est        \"20070102\" ");
        System.out.println("    - la bounding box est définie par : ");
        System.out.println("        - xmin                  \"155\"");
        System.out.println("        - ymin                  \"-29.6666666666667\"");
        System.out.println("        - xmax                  \"175\"");
        System.out.println("        - ymax                  \"-14\"");
        System.out.println("    - les dimensions sont définie par : ");
        System.out.println("        - width                 \"400\"");
        System.out.println("        - height                \"313\"");
        System.out.println();
        System.out.println("java net.sicade.observation.coverage.sql.UpdateGridCoverageTable "  +
                                "-layer=\"Potentiel de pêche (Calédonie) BET-optimal\" "       +
                                "-f=PP20070102 -startTime=20070101 -endTime=20070102 "          +
                                "-xmin=155 -xmax=175 -ymin=-29.6666666666667 -ymax=-14 "        +
                                "-width=400 -height=height");
        System.out.flush();
    }
}
