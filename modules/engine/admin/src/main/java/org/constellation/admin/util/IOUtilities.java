/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.admin.util;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.sis.util.Static;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.geotoolkit.xml.parameter.ParameterValueWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.slf4j.Logger;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class IOUtilities extends Static {

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IOUtilities.class);

	/**
	 * Transform a {@link MutableStyle} instance into a {@link String} instance.
	 *
	 * @param style
	 *            the style to be written
	 * @return a {@link String} instance
	 * @throws IOException
	 *             on error while writing {@link MutableStyle} XML
	 */
	public static String writeStyle(final MutableStyle style)
			throws IOException {
		ensureNonNull("style", style);
		final StyleXmlIO util = new StyleXmlIO();
		try {
			final StringWriter sw = new StringWriter();
			util.writeStyle(sw, style, StyledLayerDescriptor.V_1_1_0);
			return sw.toString();
		} catch (JAXBException ex) {
			throw new IOException(
					"An error occurred while writing MutableStyle XML.", ex);
		}
	}

	/**
	 * Transform a {@link GeneralParameterValue} instance into a {@link String}
	 * instance.
	 *
	 * @param parameter
	 *            the parameter to be written
	 * @return a {@link String} instance
	 * @throws IOException
	 *             on error while writing {@link GeneralParameterValue} XML
	 */
	public static String writeParameter(final GeneralParameterValue parameter)
			throws IOException {
		ensureNonNull("parameter", parameter);
		try {
			final StringWriter sw = new StringWriter();
			final ParameterValueWriter writer = new ParameterValueWriter();
			writer.setOutput(sw);
			writer.write(parameter);
			return sw.toString();
		} catch (XMLStreamException ex) {
			throw new IOException(
					"An error occurred while writing ParameterDescriptorGroup XML.",
					ex);
		}
	}

	/**
	 * Reads an {@link InputStream} to build a {@link GeneralParameterValue}
	 * instance according the specified {@link ParameterDescriptorGroup}.
	 *
	 * @param stream
	 *            the stream to read
	 * @param descriptor
	 *            the parameter descriptor
	 * @return a {@link GeneralParameterValue} instance
	 * @throws IOException
	 *             on error while reading {@link GeneralParameterValue} XML
	 */
	public static GeneralParameterValue readParameter(final InputStream stream,
			final ParameterDescriptorGroup descriptor) throws IOException {
		ensureNonNull("stream", stream);
		ensureNonNull("descriptor", descriptor);
		try {
			final ParameterValueReader reader = new ParameterValueReader(
					descriptor);
			reader.setInput(stream);
			return reader.read();
		} catch (XMLStreamException ex) {
			throw new IOException(
					"An error occurred while parsing ParameterDescriptorGroup XML.",
					ex);
		} finally {
			try {
				stream.close();
			} catch (IOException ex) {
				LOGGER.warn("Error while closing stream", ex);
			}
		}
	}

	public static GeneralParameterValue readParameter(final InputStream stream,
			final GeneralParameterDescriptor descriptor) throws IOException {
		ensureNonNull("stream", stream);
		ensureNonNull("descriptor", descriptor);
		try {
			final ParameterValueReader reader = new ParameterValueReader(
					descriptor);
			reader.setInput(stream);
			return reader.read();
		} catch (XMLStreamException ex) {
			throw new IOException(
					"An error occurred while parsing ParameterDescriptorGroup XML.",
					ex);
		} finally {
			try {
				stream.close();
			} catch (IOException ex) {
				LOGGER.warn("Error while closing stream", ex);
			}
		}
	}

	/**
	 * Reads a {@link String} instance from the specified {@link InputStream}.
	 *
	 * @return a {@link String} instance
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static String readString(final InputStream stream)
			throws IOException {
		ensureNonNull("stream", stream);
		try {
			final StringWriter writer = new StringWriter();
			IOUtils.copy(stream, writer);
			return writer.toString();
		} finally {
			try {
				stream.close();
			} catch (IOException ex) {
				LOGGER.warn("Error while closing stream", ex);
			}
		}
	}
}
