package org.constellation.test;

import org.constellation.test.pages.IndexPage;
import org.junit.Test;

/**
 * @author bgarcia
 */
public class IndexPageIT extends AbstractIT{

    @Test
    public void testWeSeeIndexPage() {
        getDrv().get(getSiteBase().toString());
        new IndexPage(getDrv(), getSiteBase());
    }

}
