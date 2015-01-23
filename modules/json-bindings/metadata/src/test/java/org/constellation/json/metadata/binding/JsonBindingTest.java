
package org.constellation.json.metadata.binding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author guilhem
 */
public class JsonBindingTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void mashallingTest() throws IOException {
        final RootObj root = new RootObj();
        final RootBlock rb = new RootBlock();
        final SuperBlockObj sbo = new SuperBlockObj();
        final SuperBlock sb = new SuperBlock();
        final BlockObj bo = new BlockObj();
        final Block b = new Block();
        
        final Field f = new Field();
        final FieldObj fo = new FieldObj(f);
        final Block b2 = new Block();
        final BlockObj bo2 = new BlockObj(b2);
        final List<ComponentObj> compo = new ArrayList<>();
        compo.add(fo);
        compo.add(bo2);
        b.setChildren(compo);
        bo.setBlock(b);
        sb.setChildren(Arrays.asList(bo));
        sbo.setSuperblock(sb);
        rb.setChildren(Arrays.asList(sbo));
        root.setRoot(rb);
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        //objectMapper.writeValue(System.out, root);
    }
}
