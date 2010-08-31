

1. Configuration Directory

The files found here should be placed in the user's home directory in a folder
named .constellation.


2. Modify configuration

Datas are divided by provider, one for each 'kind'.
Shapefile, postgis, coverage ...

Each data source configuration file can contain several <Source>...</Source>

To set the source namespace use the tag :
Parameter name="namespace">namespace</Parameter>
If you do not wish a namespace then use the value
Parameter name="namespace">no namespace</Parameter>


3. Reloading configuration

To reload the configuration after the server has started,
log in the administration panel and reload sources from the 'service' page.
