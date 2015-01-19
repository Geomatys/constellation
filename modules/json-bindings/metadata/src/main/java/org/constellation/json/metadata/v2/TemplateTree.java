/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.constellation.json.metadata.v2;

import com.sun.org.apache.bcel.internal.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.constellation.json.metadata.binding.Block;
import org.constellation.json.metadata.binding.Field;
import org.constellation.json.metadata.binding.RootBlock;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.json.metadata.binding.SuperBlock;

/**
 *
 * @author guilhem
 */
public class TemplateTree {
    
    private List<ValueNode> nodes = new ArrayList<>();
    
    public ValueNode getNodeByPath(String path) {
        for (ValueNode node : nodes) {
            if (node.path.equals(path)) {
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
        final String numeratedPath = updateLastOrdinal(node.getNumeratedPath(), i);
        ValueNode exist = getNodeByNumeratedPath(numeratedPath);
        if (exist == null) {
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

    public List<ValueNode> getNodesByPathAndParent(String path, ValueNode parent) {
        final List<ValueNode> results = new ArrayList<>();
        for (ValueNode node : nodes) {
            if (node.path.equals(path) && node.hashParent(parent)) {
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
    
    private void addNode(ValueNode node, ValueNode ancestor) {
        
        // else for a new Node to add, we create all the missing parent nodes
        nodes.add(node);

        ValueNode child = node;
        String path     = node.path;
        while (path.indexOf('.') != -1) {
            path = path.substring(0, path.lastIndexOf('.'));

            List<ValueNode> parents = getNodesByPath(path);
            if (parents.isEmpty()) {
                ValueNode parent = new ValueNode(path, null, 0, null, null);
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
                    ValueNode parent = new ValueNode(path, null, 0, null, null);
                    nodes.add(parent);
                    parent.addChild(child);
                    child = parent;
                } else {
                    break;
                }
            }
        }
        
    }
    
    public static TemplateTree getTreeFromRootObj(RootObj template) {
        final TemplateTree tree = new TemplateTree();
        for (SuperBlock sb : template.getRoot().getSuperBlocks()) {
            
            Map<String, Integer> blockPathOrdinal = new HashMap<>();
            for (Block block : sb.getBlocks()) {
                
                // Multiple Block
                ValueNode ancestor = null;
                if (block.getPath() != null) {
                    int blockOrdinal = updateOrdinal(blockPathOrdinal, block.getPath());
                    ancestor = new ValueNode(block, blockOrdinal);
                    tree.addNode(ancestor, null);
                }
                
                // Fields
                Map<String, Integer> fieldPathOrdinal = new HashMap<>();
                for (Field field : block.getFields()) {
                    int fieldOrdinal = updateOrdinal(fieldPathOrdinal, field.getPath());
                    final ValueNode node = new ValueNode(field, fieldOrdinal);
                    tree.addNode(node, ancestor);
                }
            }
        }
        
        return tree;
    }
    
    private static int updateOrdinal(Map<String, Integer> pathOrdinal, final String path) {
        int ordinal = 0;
        if (pathOrdinal.containsKey(path)) {
            ordinal = pathOrdinal.get(path) + 1;
        }
        pathOrdinal.put(path, ordinal);
        return ordinal;
    }
    
    public static RootObj getRootObjFromTree(final RootObj rootobj, final TemplateTree tree) {
        final RootBlock root = rootobj.getRoot();
        
        for (SuperBlock sb : root.getSuperBlocks()) {
            final List<Block> children = new ArrayList<>(sb.getBlocks());
            int count = 0;
            for (Block block : children) {
                Block origBlock = new Block(block);
                
                if (block.getPath() != null) {
                    List<ValueNode> nodes = tree.getNodesByBlockName(block.getName());
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
