package org.constellation.ws.rest;

import org.constellation.dto.StyleBean;
import org.constellation.dto.StyleListBean;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.style.MutableStyle;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Service to acces to styles
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/style/")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class Style {


    /**
     * Give style list available
     * @return a {@link StyleBean} {@link List}
     */
    @GET
    @Path("/")
    public StyleListBean getLayerStyle() {
        List<StyleBean> styles = new ArrayList<>(0);
        StyleListBean stylesList = new StyleListBean();
        final Collection<StyleProvider> providers = StyleProviderProxy.getInstance().getProviders();
        for (StyleProvider provider : providers) {
            Set<String> keys = provider.getKeys();
            for (String key : keys) {
                MutableStyle style = provider.get(key);
                if (style != null) {
                    StyleBean styleBean = new StyleBean();
                    styleBean.setName(style.getName());
                    styleBean.setProviderId(provider.getId());
                    styles.add(styleBean);
                }
            }

        }
        stylesList.setStyles(styles);
        return stylesList;
    }
}
