package org.constellation.gui;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.constellation.dto.MetadataLists;
import org.junit.Test;

public class TestJerseyMarshalling {

    @Test
    public void test() {

        // GET
        GenericType<JAXBElement<MetadataLists>> planetType = new GenericType<JAXBElement<MetadataLists>>() {
        };
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080");
        MetadataLists planet = (MetadataLists) target.path("/constellation/api/1/data/metadataCodeLists/fr").request()
                .accept(MediaType.APPLICATION_XML_TYPE).get(planetType).getValue();
        System.out.println("### " + planet.getCategories());

    }

}
