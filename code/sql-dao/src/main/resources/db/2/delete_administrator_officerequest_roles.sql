DELETE FROM dms_system_person_roles WHERE role_id=6 OR role_id=10;
DELETE FROM dms_incoming_documents_role_readers WHERE roleReaders_id=6 OR roleReaders_id=10;
DELETE FROM dms_incoming_documents_role_editors WHERE roleEditors_id=6 OR roleEditors_id=10;
DELETE FROM dms_internal_documents_role_editors WHERE roleEditors_id=6 OR roleEditors_id=10;
DELETE FROM dms_request_documents_role_editors WHERE roleEditors_id=6 OR roleEditors_id=10;
DELETE FROM dms_outgoing_documents_role_editors WHERE roleEditors_id=6 OR roleEditors_id=10;
DELETE FROM dms_system_roles WHERE id=6 OR id=10;