
package org.constellation.json.metadata.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.json.JsonMetadataConstants;
import static org.constellation.json.JsonMetadataConstants.getLastOrdinal;
import static org.constellation.json.JsonMetadataConstants.isNumeratedPath;
import org.constellation.json.metadata.binding.Block;
import org.constellation.json.metadata.binding.BlockObj;
import org.constellation.json.metadata.binding.ComponentObj;
import org.constellation.json.metadata.binding.Field;
import org.constellation.json.metadata.binding.FieldObj;
import org.constellation.json.metadata.binding.IBlock;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.json.metadata.binding.SuperBlock;

/**
 *
 * @author guilhem
 */
public class TemplateTree {
    
    private static final Logger LOGGER = Logging.getLogger(TemplateTree.class);
    
    private final List<ValueNode> nodes = new ArrayList<>();
    
    public ValueNode getNodeByPath(String path) {
        for (ValueNode node : nodes) {
            if (node.path.equals(path)) {
                return node;
            }
        }
        return null;
    }
    
    public ValueNode getNodeByNumeratedPath(String numeratedPath, String blockName) {
        for (ValueNode node : nodes) {
            if (node.getNumeratedPath().equals(numeratedPath)
             && Objects.equals(node.blockName, blockName)) {
                return node;
            }
        }
        return null;
    }
    
    public ValueNode getNodeByNumeratedPath(String numeratedPath) {
        for (ValueNode node : nodes) {
            if (node.getNumeratedPath().equals(numeratedPath)) {
                return node;
            }
        }
        return null;
    }
    
    public List<ValueNode> getNodesByPath(String path) {
        final List<ValueNode> results = new ArrayList<>();
        for (ValueNode node : nodes) {
            if (node.path.equals(path)) {
                results.add(node);
            }
        }
        return results;
    }
    
    public List<ValueNode> getNodesByPathAndType(String path, String type) {
        final List<ValueNode> results = new ArrayList<>();
        for (ValueNode node : nodes) {
            if (node.path.equals(path) && Objects.equals(node.type, type)) {
                results.add(node);
            }
        }
        return results;
    }
    
    public List<ValueNode> getNodesByBlockName(String blockName) {
        final List<ValueNode> results = new ArrayList<>();
        for (ValueNode node : nodes) {
            if (blockName.equals(node.blockName)) {
                results.add(node);
            }
        }
        return results;
    }
    
    public List<ValueNode> getNodesForBlock(Block block) {
        final List<ValueNode> results = new ArrayList<>();
        if (block.getPath() != null) {
            for (ValueNode node : nodes) {
                if (block.getName().equals(node.blockName)) {
                    results.add(node);
                }
            }
            return results;
        } else {
            results.add(null);
        }
        return results;
    }
    
    
    public ValueNode getRoot() {
        for (ValueNode node : nodes) {
            if (node.parent == null) {
                return node;
            }
        }
        return null;
    }

    
    /**
     * Duplicate a node only it does not exist
     * @param node
     * @return 
     */
    public ValueNode duplicateNode(ValueNode node, int i) {
        String numeratedPath = updateLastOrdinal(node.getNumeratedPath(), i);
        ValueNode exist = getNodeByNumeratedPath(numeratedPath, node.blockName); // issue here
        if (exist == null) {
            int j = i;
            ValueNode n = getNodeByNumeratedPath(numeratedPath);
            while (n != null) {
                j++;
                numeratedPath = updateLastOrdinal(numeratedPath, j);
                ValueNode tmp = getNodeByNumeratedPath(numeratedPath);
                n.updateOrdinal(j);
                n = tmp;
            }
            exist = new ValueNode(node, node.parent, i);
            nodes.add(exist);
            for (ValueNode child : node.children) {
                duplicateNode(child, exist);
                
            }
        }
        return exist;
    }
    
    private void duplicateNode(ValueNode node, ValueNode parent) {
        ValueNode newNode = new ValueNode(node, parent, node.ordinal);
        nodes.add(newNode);
        for (ValueNode child : node.children) {
            duplicateNode(child, newNode);
        }
    }

    public List<ValueNode> getNodesByFieldAndParent(String fieldName, ValueNode parent) {
        final List<ValueNode> results = new ArrayList<>();
        for (ValueNode node : nodes) {
            if (node.blockName != null && node.blockName.equals(fieldName) && (parent == null || node.hashParent(parent))) {
                results.add(node);
            }
        }
        return results;
    }
    
    private String updateLastOrdinal(final String numeratedPath, int ordinal) {
        int i = numeratedPath.lastIndexOf('[');
        if (i != -1) {
            return numeratedPath.substring(0, i + 1) + ordinal +"]";
        }
        throw new IllegalArgumentException(numeratedPath + " does not contain numerated value");
    }
    
    private void addNode(ValueNode node, ValueNode ancestor, final RootObj template, String numPath) {
        nodes.add(node);
        
        // for a new Node to add, we create all the missing parent nodes
        ValueNode child = node;
        String path     = node.path;
        while (path.indexOf('.') != -1) {
            path    = path.substring(0, path.lastIndexOf('.'));
            numPath = numPath.substring(0, numPath.lastIndexOf('.'));
            int i   = getLastOrdinal(numPath);

            List<ValueNode> parents;
            if (isNumeratedPath(numPath)) {
                parents = new ArrayList<>();
                ValueNode v = getNodeByNumeratedPath(numPath);
                if (v != null) {
                    parents.add(v);
                }
            } else {
                parents = getNodesByPath(path);
            }
            if (parents.isEmpty()) {
                ValueNode parent = new ValueNode(path, template.getTypeForPath(path), i, null, null, false);
                nodes.add(parent);
                parent.addChild(child);
                child = parent;
            } else {
                boolean found = false;
                for (ValueNode parent : parents) {
                    if (ancestor == null || parent.hashParent(ancestor) || parent.simpleEquals(ancestor)) {
                        parent.addChild(child);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ValueNode parent = new ValueNode(path, template.getTypeForPath(path), i, null, null,false);
                    nodes.add(parent);
                    parent.addChild(child);
                    child = parent;
                } else {
                    break;
                }
            }
        }
        
    }
    
    private void moveFollowingNumeratedPath(String path, int ordinal) {
        path = JsonMetadataConstants.cleanNumeratedPath(path);
        List<ValueNode> toUpdate = getNodesByPath(path);
        for (ValueNode n : toUpdate) {
            if (n.ordinal >= ordinal) {
                n.updateOrdinal(n.ordinal++);
            }
        }
    }
    
    public static TemplateTree getTreeFromRootObj(RootObj template) {
        final TemplateTree tree = new TemplateTree();
        for (SuperBlock sb : template.getSuperBlocks()) {
            final Map<String, Integer> blockPathOrdinal = new HashMap<>();
            for (BlockObj block : sb.getChildren()) {
                updateTreeFromBlock(block, tree, template, blockPathOrdinal);
            }
        }
        
        return tree;
    }
    
    public static void updateTreeFromBlock(BlockObj blockO, TemplateTree tree, final RootObj template, Map<String, Integer> blockPathOrdinal) {
        final Block block = blockO.getBlock();
        
        // Multiple Block
        ValueNode ancestor = null;
        if (block.getPath() != null) {
            int ordinal = updateOrdinal(blockPathOrdinal, block.getPath());
            ancestor = new ValueNode(block, ordinal);
            if (block.getPath().endsWith("+")) {
                block.updatePath(ordinal);
                template.moveFollowingNumeratedPath(block, ordinal);
            }
            tree.addNode(ancestor, null, template, block.getPath());
        } 

        // Fields
        final Map<String, Integer> fieldPathOrdinal = new HashMap<>();
        for (ComponentObj child : block.getChildren()) {
            if (child instanceof FieldObj) {
                updateTreeFromField((FieldObj)child, tree, ancestor, template, fieldPathOrdinal);
            } else {
                updateTreeFromBlock((BlockObj)child, tree, template, blockPathOrdinal);
            }
        }
    }
    
    public static void updateTreeFromField(FieldObj fieldObj, TemplateTree tree, final ValueNode ancestor, final RootObj template, Map<String, Integer> fieldPathOrdinal) {
        final Field field = fieldObj.getField();
        int fieldOrdinal = updateOrdinal(fieldPathOrdinal, field.getPath());
        final ValueNode node = new ValueNode(field, fieldOrdinal);
        tree.addNode(node, ancestor, template, field.getPath());
    }
    
    private static int updateOrdinal(Map<String, Integer> pathOrdinal, String path) {
        path = JsonMetadataConstants.removeLastNumeratedPathPart(path);
        int ordinal = 0;
        if (pathOrdinal.containsKey(path)) {
            ordinal = pathOrdinal.get(path) + 1;
        }
        pathOrdinal.put(path, ordinal);
        return ordinal;
    }
    
    public static RootObj getRootObjFromTree(final RootObj rootobj, final TemplateTree tree, final boolean prune) {
        final RootObj result = new RootObj(rootobj);
        
        for (SuperBlock sb : result.getSuperBlocks()) {
            final List<BlockObj> children = new ArrayList<>(sb.getChildren());
            int blockCount = 0;
            for (BlockObj block : children) {
                blockCount = updateRootObjFromTree(block, sb, tree, blockCount);
                blockCount++;
                if (prune && block.getBlock().childrenEmpty()) {
                    sb.removeBlock(block);
                    blockCount--;
                }
            }
            if (sb.childrenEmpty()) {
                result.getRoot().remove(sb);
            }
        }
        
        return result;
    }
    
    private static int updateRootObjFromTree(final BlockObj blockObj, final IBlock owner, final TemplateTree tree, int blockCount) {
        Block block = blockObj.getBlock();
        final Block origBlock = new Block(block);
        final List<ValueNode> blockNodes = tree.getNodesForBlock(block);

        if (blockNodes.isEmpty()) {
            owner.removeBlock(blockObj);
            blockCount--;
        }
        
        for (int i = 0; i < blockNodes.size(); i++) {
            final ValueNode node = blockNodes.get(i);
            if (i > 0) {
                block = owner.addBlock(blockCount + 1, new Block(origBlock));
                blockCount++;
            }
            if (node != null) {
                block.setPath(node.getNumeratedPath());
            }

            final List<ComponentObj> blockChildren = new ArrayList<>(block.getChildren());
            int fieldCount = 0;
            for (ComponentObj child : blockChildren) {
                if (child instanceof FieldObj) {
                    fieldCount = updateRootObjFromTree((FieldObj) child, block, tree, node, fieldCount);
                } else {
                    fieldCount = updateRootObjFromTree((BlockObj) child, block, tree, fieldCount);
                }
                fieldCount++;
            }
        }
        return blockCount;
    }
    
    private static int updateRootObjFromTree(final FieldObj fieldObj, final Block owner, final TemplateTree tree, final ValueNode node, int fieldCount) {
        Field field = fieldObj.getField();
        final List<ValueNode> fieldNodes = tree.getNodesByFieldAndParent(field.getName(), node);
        
        if (fieldNodes.isEmpty()) {
            owner.removeField(fieldObj);
        }
        
        for (int j = 0; j < fieldNodes.size(); j++) {
            final ValueNode childNode = fieldNodes.get(j);
            if (j > 0) {
                if (field.getMultiplicity() > 1) {
                    field = owner.addField(fieldCount + 1, new Field(field));
                    fieldCount++;
                } else {
                    LOGGER.info("field value excluded for multiplicity purpose");
                    continue;
                }
            }
            field.setPath(childNode.getNumeratedPath());
            field.setValue(childNode.value);
        }
        return fieldCount;
    }
    
    public static void pruneTree(TemplateTree tree, final ValueNode node) {
        List<ValueNode> toRemove = new ArrayList<>();
        for (ValueNode child : node.children) {
            pruneTree(tree, child);
            if (child.isField()) {
                if (child.value == null || child.value.isEmpty()) {
                    toRemove.add(child);
                }
            }
        }
        if (!toRemove.isEmpty()) {
            node.children.removeAll(toRemove);
            tree.nodes.removeAll(toRemove);
        }
    }
}
