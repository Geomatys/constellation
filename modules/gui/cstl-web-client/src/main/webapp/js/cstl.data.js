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

/**
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */

/**
 * Constellation UI datas manager.
 *
 * @type {object}
 */
CSTL.Data = {

    createMetadataTree : function(metadatas, parentDivId){
        for(var i=0; i<metadatas.length; i++){
            var key = metadatas[i];
            var name = key.name;
            var nameWithoutWhiteSpace = key.nameNoWhiteSpace;
            var value = key.value;
            var childrenExist = key.childrenExist;
            var parentNode = key.parentName;
            var depthSpan = key.depthSpan;

            if(childrenExist){
                //root node
                if(parentNode == "null" || parentNode == ""){
                    var htmlElement =   "<a data-toggle='collapse' data-target='#"+nameWithoutWhiteSpace+"Div' class='span"+depthSpan+"'>"+name+"</a>" +
                        "<div class='collapse span"+depthSpan+"' id='"+nameWithoutWhiteSpace+"Div'><table id='"+nameWithoutWhiteSpace+"' class=     'table table-striped'></table></div>";
                    jQuery(parentDivId).append(htmlElement);
                }else{
                    var htmlElement =   "<a data-toggle='collapse' data-target='#"+nameWithoutWhiteSpace+"Div' class='span"+depthSpan+"'>"+name+"</a>" +
                        "<div class='collapse span"+depthSpan+"' id='"+nameWithoutWhiteSpace+"Div'><table id='"+nameWithoutWhiteSpace+"' class='table table-striped'></table></div>";
                    jQuery("#"+parentNode+"Div").append(htmlElement);
                }
            }else{
                var htmlElement = "<tr><td>"+name+"</td><td>"+value+"</td></tr>";
                jQuery("#"+parentNode).append(htmlElement);
            }
        }
    }
}