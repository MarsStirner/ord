alter table dms_incoming_documents add `erpNumber` varchar(255); 
alter table dms_outgoing_documents add `erpNumber` varchar(255); 
alter table dms_internal_documents add `erpNumber` varchar(255); 
alter table dms_request_documents add `erpNumber` varchar(255);
alter table dms_tasks add `erpNumber` varchar(255);