
package org.constellation.json.metadata.v2;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.sis.measure.Angle;
import org.apache.sis.metadata.AbstractMetadata;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.util.CharSequences;
import org.apache.sis.util.iso.Types;
import static org.constellation.json.JsonMetadataConstants.DATE_FORMAT;
import static org.constellation.json.JsonMetadataConstants.DATE_HOUR_FORMAT;
import static org.constellation.json.JsonMetadataConstants.DATE_READ_ONLY;
import org.constellation.json.metadata.ParseException;
import org.constellation.json.metadata.binding.Block;
import org.constellation.json.metadata.binding.Field;
import org.constellation.json.metadata.binding.RootBlock;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.json.metadata.binding.SuperBlock;
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
     * @return 
     */
    public RootObj writeTemplate(final RootObj template, final Object metadata) throws ParseException {
        final TemplateTree tree  = TemplateTree.getTreeFromRootObj(template);
        
        fillValueWithMetadata(tree, tree.getRoot(), metadata);
        
        return getRootObjFromTree(template, tree);
    }
    
    private void fillValueWithMetadata(final TemplateTree tree, final ValueNode root, final Object metadata) throws ParseException {
        final List<ValueNode> children = new ArrayList<>(root.children);
        for (ValueNode node : children) {
            final ValueNode origNode = new ValueNode(node);
            final Object obj = getValue(node, metadata);
            if (obj instanceof Collection && !((Collection)obj).isEmpty())  {
                final Iterator it = ((Collection)obj).iterator();
                int i = node.ordinal;
                while (it.hasNext()) {
                    Object child = it.next();
                    node = tree.duplicateNode(origNode, i);
                    if (node.isField()) {
                        node.value = valueToString(node, child);
                    } else {
                        fillValueWithMetadata(tree, node, child);
                    }
                    i++;
                }
            } else {
                if (node.isField()) {
                    node.value = valueToString(node, obj);
                } else {
                    fillValueWithMetadata(tree, node, obj);
                }
            }
        }
    }
    
    private Object getValue(final ValueNode node, Object metadata) throws ParseException {
        if (metadata instanceof AbstractMetadata) {
            Object obj = asFullMap(metadata).get(node.name);
            
           /*
            * if the node has a type we verify that the values correspound to the declared type.
            * For a collection, we return a sub-collection with only the matching instance
            */
            if (node.type != null) {
                Class type;
                try {
                    type = Class.forName(node.type);
                } catch (ClassNotFoundException ex) {
                    throw new ParseException("Unable to find a class for type : " + node.type);
                }
        
                if (obj instanceof Collection) {
                    final Collection result     = new ArrayList<>(); 
                    final Collection collection = (Collection) obj;
                    final Iterator it           = collection.iterator();
                    while (it.hasNext()) {
                        final Object o = it.next();
                        if (type.isInstance(o)) {
                            result.add(o);
                        }
                    }
                    return result;
                } else if (type.isInstance(obj) ) {
                    return obj;
                }
                return null;
            } else {
                return obj;
            }
        } else {
            // TODO try via getter
            return null;
        }
    }
    
    private static String valueToString(final ValueNode n, final Object value) {
        final String p;
        if (value == null) {
            p = n.defaultValue;
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
    
    private RootObj getRootObjFromTree(final RootObj rootobj, final TemplateTree tree) {
        final RootBlock root = rootobj.getRoot();
        
        for (SuperBlock sb : root.getSuperBlocks()) {
            final List<Block> children = new ArrayList<>(sb.getBlocks());
            int count = 0;
            for (Block block : children) {
                Block origBlock = new Block(block);
                
                if (block.getPath() != null) {
                    List<ValueNode> nodes = tree.getNodesByPathAndType(block.getPath(), block.getType());
                    for (int i = 0; i < nodes.size(); i++) {
                        final ValueNode node = nodes.get(i);
                        if (i > 0) {
                            block = new Block(origBlock);
                            sb.addBlock(count + 1, block);
                        }
                        final List<Field> childrenField = new ArrayList<>(block.getFields());
                        int countField = 0;
                        for (Field field : childrenField) {
                            final List<ValueNode> childNodes = tree.getNodesByPathAndParent(field.getPath(), node);
                            for (int j = 0; j < childNodes.size(); j++) {
                                final ValueNode childNode = childNodes.get(j);
                                if (j > 0) {
                                    field = new Field(field);
                                    block.addField(countField + 1, field);
                                }
                                field.setValue(childNode.value);
                            }
                            countField++;
                        }
                    }
                    
                } else {
                
                    final List<Field> childrenField = new ArrayList<>(block.getFields());
                    int countField = 0;
                    for (Field field : childrenField) {
                        final List<ValueNode> nodes = tree.getNodesByPath(field.getPath());
                        for (int i = 0; i < nodes.size(); i++) {
                            final ValueNode node = nodes.get(i);
                            if (i > 0) {
                                field = new Field(field);
                                block.addField(countField + 1, field);
                            }
                            field.setValue(node.value);
                        }
                        countField++;
                    }
                }
                count++;
            }
        }
        
        return rootobj;
    }
    
}
