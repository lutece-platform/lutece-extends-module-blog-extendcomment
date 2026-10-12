-- liquibase formatted sql
-- changeset blog-extendcomment:init_core_blog-extendcomment.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'BLOG_EXTENDCOMMENT_MANAGEMENT'
--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'BLOG_EXTENDCOMMENT_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('BLOG_EXTENDCOMMENT_MANAGEMENT','module.blog.extendcomment.adminFeature.ManageBlogExtendcomment.name',1,'jsp/admin/plugins/blog/modules/extendcomment/ManageBlogExtendcomment.jsp','module.blog.extendcomment.adminFeature.ManageBlogExtendcomment.description',0,'blog-extendcomment',NULL,NULL,NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'BLOG_EXTENDCOMMENT_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('BLOG_EXTENDCOMMENT_MANAGEMENT',1);


-- changeset blog-extendcomment:init_core_blog-extendcomment-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_admin_right SET id_feature_group = 'APPLICATIONS', icon_url = 'ti ti-messages', level_right = 2 WHERE id_right = 'BLOG_EXTENDCOMMENT_MANAGEMENT';
