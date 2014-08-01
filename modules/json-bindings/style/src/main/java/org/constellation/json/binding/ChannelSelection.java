/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        /*
         * A channel selection cannot contains both gray channel and rgb channel because it is exclusive in geotk.
         */
        if (channelSelection.getGrayChannel() != null) {
            greyChannel = new SelectedChannelType(channelSelection.getGrayChannel());
            rgbChannels = null;
        }else if (channelSelection.getRGBChannels() != null && channelSelection.getRGBChannels().length > 2) {
            rgbChannels = new SelectedChannelType[3];
            rgbChannels[0] = new SelectedChannelType(channelSelection.getRGBChannels()[0]);
            rgbChannels[1] = new SelectedChannelType(channelSelection.getRGBChannels()[1]);
            rgbChannels[2] = new SelectedChannelType(channelSelection.getRGBChannels()[2]);
            greyChannel = null;
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
