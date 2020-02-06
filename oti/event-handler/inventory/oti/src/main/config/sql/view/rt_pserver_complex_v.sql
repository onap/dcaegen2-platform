CREATE OR REPLACE VIEW dti.rt_pserver_complex_v AS
SELECT
p.HOSTNAME,
p.PTNII_EQUIP_NAME,
p.NUMBER_OF_CPUS,
p.DISK_IN_GIGABYTES,
p.RAM_IN_MEGABYTES,
p.EQUIP_TYPE,
p.EQUIP_VENDOR,
p.EQUIP_MODEL,
p.FQDN,
p.PSERVER_SELFLINK,
p.IPV4_OAM_ADDRESS,
p.SERIAL_NUMBER,
p.PSERVER_ID,
p.IN_MAINT,
p.INTERNET_TOPOLOGY,
p.RESOURCE_VERSION AS resource_version_pserver,
p.VALIDFROM AS validfrom_pserver,
c.PHYSICAL_LOCATION_ID,
c.DATA_CENTER_CODE,
c.COMPLEX_NAME,
c.IDENTITY_URL,
c.RESOURCE_VERSION AS resource_version_complex,
c.PHYSICAL_LOCATION_TYPE,
c.STREET1,
c.STREET2,
c.CITY,
c.STATE,
c.POSTAL_CODE,
c.COUNTRY,
c.REGION,
c.LATITUDE,
c.LONGITUDE,
c.ELEVATION,
c.LATA,
c.VALIDFROM AS validfrom_complex
FROM dti.rt_pserver p
INNER JOIN dti.rt_relationship_list r
ON p.hostname = r.from_node_id
AND r.related_FROM = 'pserver'
INNER JOIN dti.rt_complex c
ON c.physical_location_id = r.to_node_id
AND r.related_to = 'complex'
WHERE p.validto IS NULL
AND   r.validto IS NULL
AND   c.validto IS NULL;
