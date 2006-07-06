/*
 * Sicade - Systèmes intégrés de connaissances
 *          pour l'aide à la décision en environnement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.resources;


/**
 * Temporary wrapper around {@link org.geotools.resources.XArray}
 * leveraging generic type safety. This temporary wrapper will
 * be removed when generic type will be available in JDK 1.5.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("unchecked")
public final class XArray {
    /**
     * Toute construction d'objet de cette classe est interdites.
     */
    private XArray() {
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à {@code null}
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static <Element> Element[] resize(final Element[] array, final int length) {
        return (Element[]) org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static double[] resize(final double[] array, final int length) {
        return org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static float[] resize(final float[] array, final int length) {
        return org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static long[] resize(final long[] array, final int length) {
        return org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static int[] resize(final int[] array, final int length) {
        return org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static short[] resize(final short[] array, final int length) {
        return org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static byte[] resize(final byte[] array, final int length) {
        return org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static char[] resize(final char[] array, final int length) {
        return org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que {@code array} mais avec la longueur {@code length}
     * spécifiée. Si la longueur désirée {@code length} est plus grande que la longueur initiale du tableau {@code array},
     * alors le tableau retourné contiendra tous les éléments de {@code array} avec en plus des éléments initialisés à {@code false}
     * à la fin du tableau. Si au contraire la longueur désirée {@code length} est plus courte que la longueur initiale du tableau
     * {@code array}, alors le tableau sera tronqué (c'est à dire que les éléments en trop de {@code array} seront oubliés).
     * Si la longueur de {@code array} est égale à {@code length}, alors {@code array} sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que {@code array}, de longueur {@code length} et contenant les données de {@code array}.
     */
    public static boolean[] resize(final boolean[] array, final int length) {
        return org.geotools.resources.XArray.resize(array, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static <Element> Element[] remove(final Element[] array, final int index, final int length) {
        return (Element[]) org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static double[] remove(final double[] array, final int index, final int length) {
        return org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static float[] remove(final float[] array, final int index, final int length) {
        return org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static long[] remove(final long[] array, final int index, final int length) {
        return org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static int[] remove(final int[] array, final int index, final int length) {
        return org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static short[] remove(final short[] array, final int index, final int length) {
        return org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static byte[] remove(final byte[] array, final int index, final int length) {
        return org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static char[] remove(final char[] array, final int index, final int length) {
        return org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans {@code array} du premier élément à retirer.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de {@code array} avec des éléments retirés.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static boolean[] remove(final boolean[] array, final int index, final int length) {
        return org.geotools.resources.XArray.remove(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués d'élements nuls.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} dont l'index est
     *                égal ou supérieur à {@code index} seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static <Element> Element[] insert(final Element[] array, final int index, final int length) {
        return (Element[]) org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} dont l'index est
     *                égal ou supérieur à {@code index} seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static double[] insert(final double[] array, final int index, final int length) {
        return org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static float[] insert(final float[] array, final int index, final int length) {
        return org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static long[] insert(final long[] array, final int index, final int length) {
        return org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} dont l'index est
     *                égal ou supérieur à {@code index} seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static int[] insert(final int[] array, final int index, final int length) {
        return org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} dont l'index est
     *                égal ou supérieur à {@code index} seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static short[] insert(final short[] array, final int index, final int length) {
        return org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} dont l'index est
     *                égal ou supérieur à {@code index} seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static byte[] insert(final byte[] array, final int index, final int length) {
        return org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} dont l'index est
     *                égal ou supérieur à {@code index} seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static char[] insert(final char[] array, final int index, final int length) {
        return org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de {@code false}.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de {@code array} où insérer les espaces.
     *                Tous les éléments de {@code array} dont l'index est
     *                égal ou supérieur à {@code index} seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de {@code array} avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement {@code dst}, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static boolean[] insert(final boolean[] array, final int index, final int length) {
        return org.geotools.resources.XArray.insert(array, index, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static <Element> Element[] insert(final Element[] src, final int src_pos, final Element[] dst, final int dst_pos, final int length) {
        return (Element[]) org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static double[] insert(final double[] src, final int src_pos, final double[] dst, final int dst_pos, final int length) {
        return org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static float[] insert(final float[] src, final int src_pos, final float[] dst, final int dst_pos, final int length) {
        return org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static long[] insert(final long[] src, final int src_pos, final long[] dst, final int dst_pos, final int length) {
        return org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static int[] insert(final int[] src, final int src_pos, final int[] dst, final int dst_pos, final int length) {
        return org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static short[] insert(final short[] src, final int src_pos, final short[] dst, final int dst_pos, final int length) {
        return org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static byte[] insert(final byte[] src, final int src_pos, final byte[] dst, final int dst_pos, final int length) {
        return org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static char[] insert(final char[] src, final int src_pos, final char[] dst, final int dst_pos, final int length) {
        return org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau {@code src}
     * sera inséré en totalité ou en partie dans le tableau {@code dst}.
     *
     * @param src     Tableau à insérer dans {@code dst}.
     * @param src_pos Index de la première donnée de {@code src} à insérer dans {@code dst}.
     * @param dst     Tableau dans lequel insérer des données de {@code src}.
     * @param dst_pos Index de {@code dst} où insérer les données de {@code src}.
     *                Tous les éléments de {@code dst} dont l'index est
     *                égal ou supérieur à {@code dst_pos} seront décalés.
     * @param length  Nombre de données de {@code src} à insérer.
     * @return        Tableau qui contient la combinaison de {@code src} et {@code dst}. Cette
     *                méthode peut retourner directement {@code dst}, mais jamais {@code src}.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static boolean[] insert(final boolean[] src, final int src_pos, final boolean[] dst, final int dst_pos, final int length) {
        return org.geotools.resources.XArray.insert(src, src_pos, dst, dst_pos, length);
    }
}
