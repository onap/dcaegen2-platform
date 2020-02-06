CREATE OR REPLACE VIEW dti.aai_complex_v AS 
 SELECT 
    rt_complex.physical_location_id,
    rt_complex.data_center_code,
    rt_complex.complex_name,
    rt_complex.identity_url,
    rt_complex.resource_version,
    rt_complex.physical_location_type,
    rt_complex.street1,
    rt_complex.street2,
    rt_complex.city,
    rt_complex.state,
    rt_complex.postal_code,
    rt_complex.country,
    rt_complex.region,
    rt_complex.latitude,
    rt_complex.longitude,
    rt_complex.elevation,
    rt_complex.lata
   FROM dti.rt_complex;
