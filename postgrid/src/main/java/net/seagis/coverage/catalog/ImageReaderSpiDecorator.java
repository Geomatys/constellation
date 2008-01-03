/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.catalog;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.spi.ImageReaderSpi;


/**
 * Delegates {@link ImageReader} creation to an other provider.
 *
 * @author Martin Desruisseaux
 */
class ImageReaderSpiDecorator extends ImageReaderSpi {
    /**
     * The wrapped provider.
     */
    private final ImageReaderSpi provider;

    /**
     * Wraps the given provider. Every fields are left to {@code null},
     * which doesn't matter since we override every methods.
     */
    public ImageReaderSpiDecorator(final ImageReaderSpi provider) {
        super();
        this.provider = provider;
    }

    // Below this point are methods from IIOServiceProvider

    @Override
    public String getVendorName() {
        return provider.getVendorName();
    }

    @Override
    public String getVersion() {
        return provider.getVersion();
    }

    public String getDescription(Locale locale) {
        return provider.getDescription(locale);
    }

    // Below this point are methods from ImageReaderWriterSpi

    @Override
    public String[] getFormatNames() {
        return provider.getFormatNames();
    }

    @Override
    public String[] getFileSuffixes() {
        return provider.getFileSuffixes();
    }

    @Override
    public String[] getMIMETypes() {
        return provider.getMIMETypes();
    }

    @Override
    public String getPluginClassName() {
        return provider.getPluginClassName();
    }

    @Override
    public boolean isStandardStreamMetadataFormatSupported() {
        return provider.isStandardStreamMetadataFormatSupported();
    }

    @Override
    public String getNativeStreamMetadataFormatName() {
        return provider.getNativeStreamMetadataFormatName();
    }

    @Override
    public String[] getExtraStreamMetadataFormatNames() {
        return provider.getExtraStreamMetadataFormatNames();
    }

    @Override
    public boolean isStandardImageMetadataFormatSupported() {
        return provider.isStandardImageMetadataFormatSupported();
    }

    @Override
    public String getNativeImageMetadataFormatName() {
        return provider.getNativeImageMetadataFormatName();
    }

    @Override
    public String[] getExtraImageMetadataFormatNames() {
        return provider.getExtraImageMetadataFormatNames();
    }

    @Override
    public IIOMetadataFormat getStreamMetadataFormat(String formatName) {
        return provider.getStreamMetadataFormat(formatName);
    }

    @Override
    public IIOMetadataFormat getImageMetadataFormat(String formatName) {
        return provider.getImageMetadataFormat(formatName);
    }

    // Below this point are methods from ImageReaderSpi

    @Override
    public Class[] getInputTypes() {
        return provider.getInputTypes();
    }

    public boolean canDecodeInput(Object source) throws IOException {
        return provider.canDecodeInput(source);
    }

    @Override
    public ImageReader createReaderInstance() throws IOException {
        return provider.createReaderInstance();
    }

    public ImageReader createReaderInstance(Object extension) throws IOException {
        return provider.createReaderInstance(extension);
    }

    @Override
    public boolean isOwnReader(ImageReader reader) {
        return provider.isOwnReader(reader);
    }

    @Override
    public String[] getImageWriterSpiNames() {
        return provider.getImageWriterSpiNames();
    }
}
