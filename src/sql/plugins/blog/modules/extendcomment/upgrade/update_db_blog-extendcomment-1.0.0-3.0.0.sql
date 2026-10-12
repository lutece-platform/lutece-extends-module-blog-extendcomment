-- liquibase formatted sql
-- changeset blog-extendcomment:update_db_blog-extendcomment-1.0.0-3.0.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_admin_right SET id_feature_group = 'APPLICATIONS', icon_url = 'ti ti-messages', level_right = 2 WHERE id_right = 'BLOG_EXTENDCOMMENT_MANAGEMENT';
