package org.constellation.gui;

import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.apache.commons.codec.binary.Base64;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.LayerList;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Test;

public class TestJerseyMarshalling {

	@Test
	public void testMetadataCodeLists() {
		// GET
		final Client client = ClientBuilder.newClient().register(JacksonFeature.class);
		final WebTarget target = client.target("http://localhost:8080");

		// set authentication on header
		byte[] binaryPassword = "admin:admin".getBytes();
		final String encodedAuthentication = Base64
				.encodeBase64String(binaryPassword);

		// prepare builder before call get
		JerseyInvocation.Builder builder = (JerseyInvocation.Builder) target
				.path("/constellation/api/1/data/metadataCodeLists/fr")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", "Basic " + encodedAuthentication);

		final MetadataLists planet = builder.get(MetadataLists.class);
		System.out.println("### " + planet);
	}

	@Test
	public void testDataSummary() {
		// GET
		final Client client = ClientBuilder.newClient().register(JacksonFeature.class);
		final WebTarget target = client.target("http://localhost:8080");

		// set authentication on header
		byte[] binaryPassword = "admin:admin".getBytes();
		final String encodedAuthentication = Base64
				.encodeBase64String(binaryPassword);

		// prepare builder before call get
		JerseyInvocation.Builder builder = (JerseyInvocation.Builder) target
				.path("/constellation/api/1/data/summary/")
				.request(MediaType.APPLICATION_XML)
				.header("Authorization", "Basic " + encodedAuthentication);

		final ParameterValues pv = new ParameterValues();
		final HashMap<String, String> values = new HashMap<>(0);
        values.put("namespace", "http://geotoolkit.org");
        values.put("name", "SP27GTIF1");
        values.put("providerId", "SP27GTIF");
        pv.setValues(values);
		final Entity<ParameterValues> epv = Entity.entity(pv, MediaType.APPLICATION_XML_TYPE);
		
		final DataBrief planet = builder.put(epv, DataBrief.class);
		System.out.println("### " + planet);
	}
	
	@Test
	public void testGetLayer() {
		// GET
		final Client client = ClientBuilder.newClient().register(JacksonFeature.class);
		final WebTarget target = client.target("http://localhost:8080");

		// set authentication on header
		byte[] binaryPassword = "admin:admin".getBytes();
		final String encodedAuthentication = Base64
				.encodeBase64String(binaryPassword);

		// prepare builder before call get
		JerseyInvocation.Builder builder = (JerseyInvocation.Builder) target
				.path("/constellation/api/1/MAP/wms/default/layer/all")
				.request(MediaType.APPLICATION_XML)
				.header("Authorization", "Basic " + encodedAuthentication);

		final LayerList planet = builder.get(LayerList.class);
		System.out.println("### " + planet);
	}
}
