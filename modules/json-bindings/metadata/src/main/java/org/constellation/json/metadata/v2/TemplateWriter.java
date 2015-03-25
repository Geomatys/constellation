
package org.constellation.json.metadata.v2;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import org.apache.sis.measure.Angle;
import org.apache.sis.metadata.AbstractMetadata;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.util.CharSequences;
import org.apache.sis.util.iso.Types;
import static org.constellation.json.JsonMetadataConstants.DATE_FORMAT;
import static org.constellation.json.JsonMetadataConstants.DATE_HOUR_FORMAT;
import static org.constellation.json.JsonMetadataConstants.DATE_READ_ONLY;
import org.constellation.json.metadata.ParseException;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.util.ReflectionUtilities;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.util.Enumerated;

/**
 *  Metadata Object ===> RootObj
 * 
 * @author guilhem
 */
public class TemplateWriter extends AbstractTemplateHandler {
    
    public TemplateWriter(final MetadataStandard standard) {
        super(standard);
    }
    
    /**
     * Write a metadata Object into a template
     * 
     * @param template
     * @param metadata
     * @param prune
     * @return 
     */
    public RootObj writeTemplate(final RootObj template, final Object metadata, final boolean prune) throws ParseException {
        final TemplateTree tree  = TemplateTree.getTreeFromRootObj(template);
        
        fillValueWithMetadata(tree, tree.getRoot(), metadata, new HashMap<String, Set<Object>>(), prune);
        if (prune) {
            TemplateTree.pruneTree(tree, tree.getRoot());
        }
        return TemplateTree.getRootObjFromTree(template, tree, prune);
    }
    
    private void fillValueWithMetadata(final TemplateTree tree, final ValueNode root, final Object metadata, final  Map<String, Set<Object>> excluded, final boolean prune) throws ParseException {
        final List<ValueNode> children = new ArrayList<>(root.children);
        for (ValueNode node : children) {
            final ValueNode origNode = new ValueNode(node);
            final Object obj = getValue(node, metadata, excluded);
            if (obj instanceof Collection && !((Collection)obj).isEmpty())  {
                final Iterator it = ((Collection)obj).iterator();
                int i = node.ordinal;
                while (it.hasNext()) {
                    Object child = it.next();
                    node = tree.duplicateNode(origNode, i);
                    if (node.isField()) {
                        node.value = valueToString(node, child, !prune);
                    } else {
                        fillValueWithMetadata(tree, node, child, excluded, prune);
                    }
                    i++;
                }
            } else {
                if (node.isField()) {
                    node.value = valueToString(node, obj, !prune);
                } else {
                    fillValueWithMetadata(tree, node, obj, excluded, prune);
                }
            }
        }
    }
    
    private ValueNode extractSubTreeFromMetadata(final ValueNode root, final Object metadata, final  Map<String, Set<Object>> excluded) throws ParseException {
        if (root.isField()) {
            root.value = valueToString(root, metadata, false);
            return root;
        }
        final List<ValueNode> children = new ArrayList<>(root.children);
        for (ValueNode node : children) {
            final ValueNode origNode = new ValueNode(node);
            final Object obj = getValue(node, metadata, excluded);
            if (obj instanceof Collection && !((Collection)obj).isEmpty())  {
                final Iterator it = ((Collection)obj).iterator();
                int i = node.ordinal;
                boolean first = true;
                while (it.hasNext()) {
                    Object child = it.next();
                    if (!first) {
                        node = new ValueNode(origNode, root, i);
                    }
                    if (node.isField()) {
                        node.value = valueToString(node, child, false);
                    } else {
                        extractSubTreeFromMetadata(node, child, excluded);
                    }
                    first = false;
                    i++;
                }
            } else {
                if (node.isField()) {
                    node.value = valueToString(node, obj, false);
                } else {
                    extractSubTreeFromMetadata(node, obj, excluded);
                }
            }
        }
        return root;
    }
    
    private Object getValue(final ValueNode node, Object metadata,  Map<String, Set<Object>> excluded) throws ParseException {
        if (metadata instanceof AbstractMetadata && !(metadata instanceof Period) && !(metadata instanceof Instant)) {
            Object obj = asFullMap(metadata).get(node.name);
            if (obj instanceof Collection) {
                final Collection result = new ArrayList<>(); 
                final Iterator it       = ((Collection)obj).iterator();
                while (it.hasNext()) {
                    final Object o = getSingleValue(node, it.next(), excluded);
                    if (o != null) result.add(o);
                }
                return result;
            } else {
                return getSingleValue(node, obj, excluded);
            }
        } else if (metadata instanceof Collection && ((Collection)metadata).isEmpty()) {
            return null;
            
        } else if (metadata != null) {
            final Method getter = ReflectionUtilities.getGetterFromName(node.name, metadata.getClass());
            if (getter != null) {
                return ReflectionUtilities.invokeMethod(metadata, getter);
            }
            LOGGER.warning("Unable to find:" + metadata.getClass().getName() + " getter for:" + node.name);
            return null;
            
        } else {
            return null;
        }
    }
    
    private Object getSingleValue(final ValueNode node, Object metadata, Map<String, Set<Object>> excluded) throws ParseException {
        if (isExcluded(excluded, node, metadata)) return null;
        
        /*
         * In strict mode, we want that the sub-tree of the object correspound exactly the node tree.
         * For a collection, we return a sub-collection with only the matching instance
         *
         * The matching point are read-only fields and types.
         */
        if (node.strict) {
            final ValueNode candidate = extractSubTreeFromMetadata(new ValueNode(node), metadata, new HashMap<String, Set<Object>>());
            if (matchNode(node, candidate)) {
                exclude(excluded, node, metadata);
                return metadata;
            }
            return null;

       /*
        * if the node has a type we verify that the values correspound to the declared type.
        * For a collection, we return a sub-collection with only the matching instance
        */
        } else if (node.type != null) {
            final Class type = readType(node);
            if (type.isInstance(metadata) ) {
                exclude(excluded, node, metadata);
                return metadata;
            }
            return null;
        /*
         * else return simply the object
         */
        } else {
            // exclude ??
            return metadata;
        }
    }
    
    private static boolean matchNode(final ValueNode origin, final ValueNode candidate) {
        if (Objects.equals(origin.type, candidate.type)) {
            if (origin.render != null && origin.render.contains("readonly") && !Objects.equals(origin.defaultValue, candidate.value)) {
                return false;
            } else if (!origin.getPredefinedValues().isEmpty() && !origin.getPredefinedValues().contains(candidate.value)) {
                return false;
            }
            for (ValueNode originChild : origin.children) {
                final List<ValueNode> candidateChildren = candidate.getChildrenByName(originChild.name);
                if (!originChild.multiple && candidateChildren.size() > 1) {
                    return false;
                }
                for (ValueNode candidateChild : candidateChildren) {
                    if (!matchNode(originChild, candidateChild)) {
                        return false;
                    }
                }
            }
            return true;
            
        }
        return false;
    }
    
    private static String valueToString(final ValueNode n, final Object value, final boolean applyDefault) {
        final String p;
        // Null or empty collection
        if (value == null || value instanceof Collection) { 
            if (applyDefault) {
                p = n.defaultValue;
            } else {
                p = null;
            }
        } else if (value instanceof Number) {
            p = value.toString();
        } else if (value instanceof Angle) {
            p = Double.toString(((Angle) value).degrees());
        } else {
            /*
             * Above were unquoted cases. Below are texts to quote.
             */
            
            if (value instanceof Enumerated) {
                p = Types.getStandardName(value.getClass()) + '.' + Types.getCodeName((Enumerated) value);
            } else if (value instanceof Date) {
                if (DATE_READ_ONLY.equals(n.render)) {
                    synchronized (DATE_HOUR_FORMAT) {
                        p = DATE_HOUR_FORMAT.format(value);
                    }
                } else {
                    synchronized (DATE_FORMAT) {
                        p = DATE_FORMAT.format(value);
                    }
                }
            } else if (value instanceof Locale) {
                String language;
                try {
                    language = ((Locale) value).getISO3Language();
                } catch (MissingResourceException e) {
                    language = ((Locale) value).getLanguage();
                }
                p = "LanguageCode." + language;
            } else if (value instanceof Charset) {
                p = ((Charset) value).name();
            } else {
                CharSequence cs = value.toString();
                cs = CharSequences.replace(cs, "\"", "\\\"");
                cs = CharSequences.replace(cs, "\t", "\\t");
                cs = CharSequences.replace(cs, "\n", "\\n");
                p = cs.toString();
            }
        }
        return p;
    }
    
    private static void exclude(final Map<String, Set<Object>> excluded, final ValueNode node, final Object obj) {
        if (excluded.containsKey(node.path)) {
            excluded.get(node.path).add(obj);
        } else {
            final HashSet<Object> set = new HashSet<>();
            set.add(obj);
            excluded.put(node.path, set);
        }
    }
    
    private static boolean isExcluded(final Map<String, Set<Object>> excluded, final ValueNode node, final Object obj) {
        return excluded.containsKey(node.path) && excluded.get(node.path).contains(obj);
    }
    
    private static class NumeratedCollectionElement {
        public int index;
        public Object obj;
        
        public NumeratedCollectionElement(int index, Object obj) {
            this.index = index;
            this.obj   = obj;
        }
        
    }
}
