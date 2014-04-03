/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.json.binding;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.type;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class ChannelSelection implements StyleElement<org.opengis.style.ChannelSelection> {

    private SelectedChannelType greyChannel   = null;
    private SelectedChannelType[] rgbChannels = null;

    public ChannelSelection() {
    }

    public ChannelSelection(final org.opengis.style.ChannelSelection channelSelection) {
        ensureNonNull("channelSelection", channelSelection);
        if (channelSelection.getGrayChannel() != null) {
            greyChannel = new SelectedChannelType(channelSelection.getGrayChannel());
        }
        if (channelSelection.getRGBChannels() != null && channelSelection.getRGBChannels().length > 2) {
            rgbChannels = new SelectedChannelType[3];
            rgbChannels[0] = new SelectedChannelType(channelSelection.getRGBChannels()[0]);
            rgbChannels[1] = new SelectedChannelType(channelSelection.getRGBChannels()[1]);
            rgbChannels[2] = new SelectedChannelType(channelSelection.getRGBChannels()[2]);
        }
    }

    public SelectedChannelType getGreyChannel() {
        return greyChannel;
    }

    public void setGreyChannel(final SelectedChannelType greyChannel) {
        this.greyChannel = greyChannel;
    }

    public SelectedChannelType[] getRgbChannels() {
        return rgbChannels;
    }

    public void setRgbChannels(final SelectedChannelType[] RGBChannels) {
        this.rgbChannels = RGBChannels;
    }

    @Override
    public org.opengis.style.ChannelSelection toType() {
        if (greyChannel != null) {
            return SF.channelSelection(type(greyChannel));
        }
        if (rgbChannels != null && rgbChannels.length > 2) {
            return SF.channelSelection(type(rgbChannels[0]), type(rgbChannels[1]), type(rgbChannels[2]));
        }
        return null; // no channel selection
    }
}
