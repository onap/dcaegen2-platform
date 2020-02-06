CREATE OR REPLACE VIEW dti.aai_relationship_list_v AS 
 SELECT 
        from_node_id,
        to_node_id,
        related_from, 
        related_to,
        related_link
   FROM dti.rt_relationship_list;
