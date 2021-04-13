UPDATE metadata SET data = replace(data, '>WWW:LINK-1.0-http--samples<', '>WWW:LINK<') WHERE data LIKE '%WWW:LINK-1.0-http--samples%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>WWW:LINK-1.0-http--rss<', '>WWW:LINK<') WHERE data LIKE '%WWW:LINK-1.0-http--rss%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>WWW:LINK-1.0-http--related<', '>WWW:LINK<') WHERE data LIKE '%WWW:LINK-1.0-http--related%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>WWW:LINK-1.0-http--partners<', '>WWW:LINK<') WHERE data LIKE '%WWW:LINK-1.0-http--partners%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>WWW:LINK-1.0-httpâ€”link<', '>WWW:LINK<') WHERE data LIKE '%WWW:LINK-1.0-httpâ€”link%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>WWW:LINK-1.0-http--link<', '>WWW:LINK<') WHERE data LIKE '%WWW:LINK-1.0-http--link%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>WWW:LINK-1.0-http—link<', '>WWW:LINK<') WHERE data LIKE '%WWW:LINK-1.0-http—link%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>WWW:DOWNLOAD-1.0-http--download<', '>WWW:DOWNLOAD-URL<') WHERE data LIKE '%WWW:DOWNLOAD-1.0-http--download%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>WWW:DOWNLOAD-1.0-ftp--download<', '>WWW:DOWNLOAD-URL<') WHERE data LIKE '%WWW:DOWNLOAD-1.0-ftp--download%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>V_51 CHTOPO:specialised-geoportal<', '>CHTOPO:specialised-geoportal<') WHERE data LIKE '%V_51 CHTOPO:specialised-geoportal%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>V_45 WWW:DOWNLOAD-1.0-http--download<', '>WWW:DOWNLOAD-URL<') WHERE data LIKE '%V_45 WWW:DOWNLOAD-1.0-http--download%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>V_37 OGC:WMS-http-get-capabilities<', '>OGC:WMS<') WHERE data LIKE '%V_37 OGC:WMS-http-get-capabilities%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>V_32 WWW:DOWNLOAD-URL<', '>WWW:DOWNLOAD-URLWWW:DOWNLOAD-URL<') WHERE data LIKE '%V_32 WWW:DOWNLOAD-URL%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>V_27 WWW:LINK-1.0-http--link<', '>WWW:LINK<') WHERE data LIKE '%V_27 WWW:LINK-1.0-http--link%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>text/html<', '>WWW:LINK<') WHERE data LIKE '%text/html%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>OGC:WMTS-http-get-capabilities<', '>OGC:WMTS<') WHERE data LIKE '%OGC:WMTS-http-get-capabilities%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>OGC:WMS-http-get-map<', '>OGC:WMS<') WHERE data LIKE '%OGC:WMS-http-get-map%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>OGC:WMS-http-get-capabilities<', '>OGC:WMS<') WHERE data LIKE '%OGC:WMS-http-get-capabilities%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>OGC:WMC-http-get-capabilities<', '>OGC:WMC<') WHERE data LIKE '%OGC:WMC-http-get-capabilities%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>OGC:WFS-http-get-capabilities<', '>OGC:WFS<') WHERE data LIKE '%OGC:WFS-http-get-capabilities%'  AND isharvested = 'n';
UPDATE metadata SET data = replace(data, '>ESRI:REST Preview<', '>ESRI:REST<') WHERE data LIKE '%ESRI:REST Preview%'  AND isharvested = 'n';


