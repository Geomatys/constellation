package org.constellation.coverage;

import java.net.MalformedURLException;

import junit.framework.Assert;

import org.apache.sis.storage.DataStoreException;
import org.junit.Test;

public class PyramidCoverageHelperTestCase {

	@Test
	public void testSimple() throws MalformedURLException, DataStoreException {
		PyramidCoverageHelper helper = PyramidCoverageHelper.builder("name")
				.inputFormat("PNG").fromImage("path/to/a/geo.tiff")
				.toMemoryStore().build();
		Assert.assertNotNull(helper);

	}

}
