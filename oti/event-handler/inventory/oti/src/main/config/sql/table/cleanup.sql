\echo "Clean up relationship-list for generic-vnf related stale data"
DELETE FROM dti.rt_relationship_list WHERE from_node_id IN (select vnf_id FROM dti.rt_generic_vnf WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY vnf_name ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_generic_vnf ) t WHERE t.row_num > 1 ));

DELETE FROM dti.rt_relationship_list WHERE to_node_id IN (select vnf_id FROM dti.rt_generic_vnf WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY vnf_name ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_generic_vnf ) t WHERE t.row_num > 1 ));

\echo "Clean up relationship-list for vce related stale data"
DELETE FROM dti.rt_relationship_list WHERE from_node_id IN (select vnf_id FROM dti.rt_vce WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY vnf_name ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_vce ) t WHERE t.row_num > 1 ));

DELETE FROM dti.rt_relationship_list WHERE to_node_id IN (select vnf_id FROM dti.rt_vce WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY vnf_name ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_vce ) t WHERE t.row_num > 1 ));

\echo "Clean up relationship-list for vserver related stale data"
DELETE FROM dti.rt_relationship_list WHERE from_node_id IN (select concat(cloud_owner, '|', cloud_region_id, '|', tenant_id, '|', vserver_id) FROM dti.rt_vserver WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY cloud_owner, cloud_region_id, tenant_id, vserver_name ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_vserver ) t WHERE t.row_num > 1 ));

DELETE FROM dti.rt_relationship_list WHERE to_node_id IN (select concat(cloud_owner, '|', cloud_region_id, '|', tenant_id, '|', vserver_id) FROM dti.rt_vserver WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY cloud_owner, cloud_region_id, tenant_id, vserver_name ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_vserver ) t WHERE t.row_num > 1 ));

\echo "Clean up rt_generic_vnf for generic-vnf related stale data"
DELETE FROM dti.rt_generic_vnf WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY vnf_name ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_generic_vnf ) t WHERE t.row_num > 1 );

\echo "Clean up rt_vserver for vserver related stale data"
DELETE FROM dti.rt_vserver WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY concat(cloud_owner, '|', cloud_region_id, '|', tenant_id, '|', vserver_name) ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_vserver ) t WHERE t.row_num > 1 );

\echo "Clean up rt_vce for vce related stale data"
DELETE FROM dti.rt_vce WHERE resource_version IN (SELECT resource_version FROM (SELECT resource_version, ROW_NUMBER() OVER( PARTITION BY vnf_name ORDER BY  resource_version DESC ) AS row_num FROM dti.rt_vce ) t WHERE t.row_num > 1 );
