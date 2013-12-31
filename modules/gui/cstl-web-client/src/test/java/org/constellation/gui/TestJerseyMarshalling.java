package org.constellation.gui;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.apache.commons.codec.binary.Base64;
import org.constellation.dto.MetadataLists;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Test;

public class TestJerseyMarshalling {

	@Test
	public void test() {
		// GET
		GenericType<JAXBElement<MetadataLists>> planetType = new GenericType<JAXBElement<MetadataLists>>() {
		};

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

}
