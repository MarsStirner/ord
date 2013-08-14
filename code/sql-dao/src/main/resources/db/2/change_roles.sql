update ord2.dms_internal_documents_role_editors set roleEditors_id=14 where roleEditors_id=6;
update ord2.dms_internal_documents_role_editors set roleEditors_id=19 where roleEditors_id=10;

update ord2.dms_internal_documents_role_readers set roleReaders_id=14 where roleReaders_id=6;
update ord2.dms_internal_documents_role_readers set roleReaders_id=19 where roleReaders_id=10;

update ord2. dms_incoming_documents_role_editors set roleEditors_id=14 where roleEditors_id=6;
update ord2. dms_incoming_documents_role_editors set roleEditors_id=19 where roleEditors_id=10;

update ord2.dms_incoming_documents_role_readers set roleReaders_id=14 where roleReaders_id=6;
update ord2.dms_incoming_documents_role_readers set roleReaders_id=19 where roleReaders_id=10;

update ord2. dms_request_documents_role_editors set roleEditors_id=14 where roleEditors_id=6;
update ord2. dms_request_documents_role_editors set roleEditors_id=19 where roleEditors_id=10;

update ord2.dms_request_documents_role_readers set roleReaders_id=14 where roleReaders_id=6;
update ord2.dms_request_documents_role_readers set roleReaders_id=19 where roleReaders_id=10;

update ord2. dms_outgoing_documents_role_editors set roleEditors_id=14 where roleEditors_id=6;
update ord2. dms_outgoing_documents_role_editors set roleEditors_id=19 where roleEditors_id=10;

update ord2.dms_outgoing_documents_role_readers set roleReaders_id=14 where roleReaders_id=6;
update ord2.dms_outgoing_documents_role_readers set roleReaders_id=19 where roleReaders_id=10;

update ord2.dms_system_person_roles set role_id=14 where role_id=6 and 
person_id not in( select person_id from (select person_id from ord2.dms_system_person_roles where role_id=14) as sub);

delete from ord2.dms_system_person_roles where role_id = 6;

update ord2.dms_system_person_roles set role_id=19 where role_id=10 and 
person_id not in( select person_id from (select person_id from ord2.dms_system_person_roles where role_id=19) as sub);

delete from ord2.dms_system_person_roles where role_id = 10;

delete from ord2.dms_system_roles where id=6;
delete from ord2.dms_system_roles where id=10;