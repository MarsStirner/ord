UPDATE dms_tasks SET status_id = 16 where status_id = 1;
UPDATE dms_tasks SET status_id = 10 where status_id = 2;
UPDATE dms_tasks SET status_id = 23 where status_id = 3;
UPDATE dms_tasks SET status_id = 15 where status_id = 4;

UPDATE wf_history SET from_status_id = 16 where from_status_id = 1 and docType = "Task";
UPDATE wf_history SET from_status_id = 10 where from_status_id = 2 and docType = "Task";
UPDATE wf_history SET from_status_id = 23 where from_status_id = 3 and docType = "Task";
UPDATE wf_history SET from_status_id = 15 where from_status_id = 4 and docType = "Task";

UPDATE wf_history SET to_status_id = 16 where to_status_id = 1 and docType = "Task";
UPDATE wf_history SET to_status_id = 10 where to_status_id = 2 and docType = "Task";
UPDATE wf_history SET to_status_id = 23 where to_status_id = 3 and docType = "Task";
UPDATE wf_history SET to_status_id = 15 where to_status_id = 4 and docType = "Task";

UPDATE wf_history SET action_id = 10 where action_id = 1 and docType = "Task";
UPDATE wf_history SET action_id = 19 where action_id = 2 and docType = "Task";
UPDATE wf_history SET action_id = 18 where action_id = 25 and docType = "Task";
UPDATE wf_history SET action_id = 9 where action_id = 0 and docType = "Task";

UPDATE dms_request_documents SET status_id = 9 where status_id = 1;
UPDATE dms_request_documents SET status_id = 2 where status_id = 2;
UPDATE dms_request_documents SET status_id = 10 where status_id = 80;
UPDATE dms_request_documents SET status_id = 11 where status_id = 90;
UPDATE dms_request_documents SET status_id = 3 where status_id = 100;

UPDATE wf_history SET from_status_id = 9 where from_status_id = 1 and docType = "RequestDocument";
UPDATE wf_history SET from_status_id = 2 where from_status_id = 2 and docType = "RequestDocument";
UPDATE wf_history SET from_status_id = 10 where from_status_id = 80 and docType = "RequestDocument";
UPDATE wf_history SET from_status_id = 11 where from_status_id = 90 and docType = "RequestDocument";
UPDATE wf_history SET from_status_id = 3 where from_status_id = 100 and docType = "RequestDocument";

UPDATE wf_history SET to_status_id = 9 where to_status_id = 1 and docType = "RequestDocument";
UPDATE wf_history SET to_status_id = 2 where to_status_id = 2 and docType = "RequestDocument";
UPDATE wf_history SET to_status_id = 10 where to_status_id = 80 and docType = "RequestDocument";
UPDATE wf_history SET to_status_id = 11 where to_status_id = 90 and docType = "RequestDocument";
UPDATE wf_history SET to_status_id = 3 where to_status_id = 100 and docType = "RequestDocument";

UPDATE wf_history SET action_id = 2 where action_id = 1 and docType = "RequestDocument";
UPDATE wf_history SET action_id = 10 where action_id = 2 and docType = "RequestDocument";
UPDATE wf_history SET action_id = 11 where action_id = 80 and docType = "RequestDocument";
UPDATE wf_history SET action_id = 3 where action_id = 90 and docType = "RequestDocument";
UPDATE wf_history SET action_id = 4 where action_id = 100 and docType = "RequestDocument";
UPDATE wf_history SET action_id = 1 where action_id = 110 and docType = "RequestDocument";
UPDATE wf_history SET action_id = 6 where action_id = 135 and docType = "RequestDocument";
UPDATE wf_history SET action_id = 9 where action_id = 0 and docType = "RequestDocument";


UPDATE dms_paper_copy_documents SET status_id = 1 where status_id = 1;
UPDATE dms_paper_copy_documents SET status_id = 2 where status_id = 2;
UPDATE dms_paper_copy_documents SET status_id = 3 where status_id = 99;
UPDATE dms_paper_copy_documents SET status_id = 4 where status_id = 110;
UPDATE dms_paper_copy_documents SET status_id = 5 where status_id = 120;
UPDATE dms_paper_copy_documents SET status_id = 6 where status_id = 130;
UPDATE dms_paper_copy_documents SET status_id = 7 where status_id = 200;
UPDATE dms_paper_copy_documents SET status_id = 8 where status_id = 210;

UPDATE wf_history SET from_status_id = 1 where from_status_id = 1 and docType = "PaperCopyDocument";
UPDATE wf_history SET from_status_id = 2 where from_status_id = 2 and docType = "PaperCopyDocument";
UPDATE wf_history SET from_status_id = 3 where from_status_id = 99 and docType = "PaperCopyDocument";
UPDATE wf_history SET from_status_id = 4 where from_status_id = 110 and docType = "PaperCopyDocument";
UPDATE wf_history SET from_status_id = 5 where from_status_id = 120 and docType = "PaperCopyDocument";
UPDATE wf_history SET from_status_id = 6 where from_status_id = 130 and docType = "PaperCopyDocument";
UPDATE wf_history SET from_status_id = 7 where from_status_id = 200 and docType = "PaperCopyDocument";
UPDATE wf_history SET from_status_id = 8 where from_status_id = 210 and docType = "PaperCopyDocument";

UPDATE wf_history SET to_status_id = 1 where to_status_id = 1 and docType = "PaperCopyDocument";
UPDATE wf_history SET to_status_id = 2 where to_status_id = 2 and docType = "PaperCopyDocument";
UPDATE wf_history SET to_status_id = 3 where to_status_id = 99 and docType = "PaperCopyDocument";
UPDATE wf_history SET to_status_id = 4 where to_status_id = 110 and docType = "PaperCopyDocument";
UPDATE wf_history SET to_status_id = 5 where to_status_id = 120 and docType = "PaperCopyDocument";
UPDATE wf_history SET to_status_id = 6 where to_status_id = 130 and docType = "PaperCopyDocument";
UPDATE wf_history SET to_status_id = 7 where to_status_id = 200 and docType = "PaperCopyDocument";
UPDATE wf_history SET to_status_id = 8 where to_status_id = 210 and docType = "PaperCopyDocument";

UPDATE wf_history SET action_id = 2 where action_id = 1 and docType = "PaperCopyDocument";
UPDATE wf_history SET action_id = 3 where action_id = 99 and docType = "PaperCopyDocument";
UPDATE wf_history SET action_id = 4 where action_id = 100 and docType = "PaperCopyDocument";
UPDATE wf_history SET action_id = 1 where action_id = 110 and docType = "PaperCopyDocument";
UPDATE wf_history SET action_id = 5 where action_id = 120 and docType = "PaperCopyDocument";
UPDATE wf_history SET action_id = 6 where action_id = 130 and docType = "PaperCopyDocument";
UPDATE wf_history SET action_id = 7 where action_id = 200 and docType = "PaperCopyDocument";
UPDATE wf_history SET action_id = 8 where action_id = 210 and docType = "PaperCopyDocument";
UPDATE wf_history SET action_id = 9 where action_id = 0 and docType = "PaperCopyDocument";


UPDATE dms_outgoing_documents SET status_id = 12 where status_id = 1;
UPDATE dms_outgoing_documents SET status_id = 13 where status_id = 2;
UPDATE dms_outgoing_documents SET status_id = 14 where status_id = 3;
UPDATE dms_outgoing_documents SET status_id = 2 where status_id = 80;
UPDATE dms_outgoing_documents SET status_id = 11 where status_id = 90;
UPDATE dms_outgoing_documents SET status_id = 3 where status_id = 100;

UPDATE wf_history SET from_status_id = 12 where from_status_id = 1 and docType = "OutgoingDocument";
UPDATE wf_history SET from_status_id = 13 where from_status_id = 2 and docType = "OutgoingDocument";
UPDATE wf_history SET from_status_id = 14 where from_status_id = 3 and docType = "OutgoingDocument";
UPDATE wf_history SET from_status_id = 2 where from_status_id = 80 and docType = "OutgoingDocument";
UPDATE wf_history SET from_status_id = 11 where from_status_id = 90 and docType = "OutgoingDocument";
UPDATE wf_history SET from_status_id = 3 where from_status_id = 100 and docType = "OutgoingDocument";

UPDATE wf_history SET to_status_id = 12 where to_status_id = 1 and docType = "OutgoingDocument";
UPDATE wf_history SET to_status_id = 13 where to_status_id = 2 and docType = "OutgoingDocument";
UPDATE wf_history SET to_status_id = 14 where to_status_id = 3 and docType = "OutgoingDocument";
UPDATE wf_history SET to_status_id = 2 where to_status_id = 80 and docType = "OutgoingDocument";
UPDATE wf_history SET to_status_id = 11 where to_status_id = 90 and docType = "OutgoingDocument";
UPDATE wf_history SET to_status_id = 3 where to_status_id = 100 and docType = "OutgoingDocument";

UPDATE wf_history SET action_id = 14 where action_id = 2 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 15 where action_id = 3 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 2 where action_id = 80 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 2 where action_id = 81 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 2 where action_id = 82 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 11 where action_id = 90 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 3 where action_id = 99 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 9 where action_id = 0 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 17 where action_id = -10000 and docType = "OutgoingDocument";
UPDATE wf_history SET action_id = 13 where action_id = 1002 and docType = "OutgoingDocument";

UPDATE dms_office_keeping_volumes SET status_id = 21 where status_id = 2;
UPDATE dms_office_keeping_volumes SET status_id = 22 where status_id = 1;
UPDATE dms_office_keeping_volumes SET status_id = 20 where status_id = 10;
UPDATE dms_office_keeping_volumes SET status_id = 19 where status_id = 110;

UPDATE wf_history SET from_status_id = 21 where from_status_id = 2 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET from_status_id = 22 where from_status_id = 1 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET from_status_id = 20 where from_status_id = 10 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET from_status_id = 19 where from_status_id = 110 and docType = "OfficeKeepingVolume";

UPDATE wf_history SET to_status_id = 21 where to_status_id = 2 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET to_status_id = 22 where to_status_id = 1 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET to_status_id = 20 where to_status_id = 10 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET to_status_id = 19 where to_status_id = 110 and docType = "OfficeKeepingVolume";

UPDATE wf_history SET action_id = 2 where action_id = 1 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET action_id = 20 where action_id = 10 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET action_id = 21 where action_id = 100 and docType = "OfficeKeepingVolume";
UPDATE wf_history SET action_id = 22 where action_id = 110 and docType = "OfficeKeepingVolume";

UPDATE dms_office_keeping_files SET status_id = 17 where status_id = 1;
UPDATE dms_office_keeping_files SET status_id = 18 where status_id = 2;

UPDATE wf_history SET from_status_id = 17 where from_status_id = 1 and docType = "OfficeKeepingFile";
UPDATE wf_history SET from_status_id = 18 where from_status_id = 2 and docType = "OfficeKeepingFile";

UPDATE wf_history SET to_status_id = 17 where to_status_id = 1 and docType = "OfficeKeepingFile";
UPDATE wf_history SET to_status_id = 18 where to_status_id = 2 and docType = "OfficeKeepingFile";

UPDATE wf_history SET action_id = 2 where action_id = 1 and docType = "OfficeKeepingFile";
UPDATE wf_history SET action_id = null where action_id = 2 and docType = "OfficeKeepingFile";

UPDATE dms_internal_documents SET status_id = 12 where status_id = 1;
UPDATE dms_internal_documents SET status_id = 13 where status_id = 2;
UPDATE dms_internal_documents SET status_id = 14 where status_id = 3;
UPDATE dms_internal_documents SET status_id = 2 where status_id = 5;
UPDATE dms_internal_documents SET status_id = 10 where status_id = 80;
UPDATE dms_internal_documents SET status_id = 11 where status_id = 90;
UPDATE dms_internal_documents SET status_id = 3 where status_id = 100;
UPDATE dms_internal_documents SET status_id = 4 where status_id = 110;
UPDATE dms_internal_documents SET status_id = 6 where status_id = 130;
UPDATE dms_internal_documents SET status_id = 15 where status_id = 150;

UPDATE wf_history SET from_status_id = 12 where from_status_id = 1 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 13 where from_status_id = 2 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 14 where from_status_id = 3 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 2 where from_status_id = 5 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 10 where from_status_id = 80 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 11 where from_status_id = 90 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 3 where from_status_id = 100 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 4 where from_status_id = 110 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 5 where from_status_id = 130 and docType = "InternalDocument";
UPDATE wf_history SET from_status_id = 15 where from_status_id = 150 and docType = "InternalDocument";

UPDATE wf_history SET to_status_id = 12 where to_status_id = 1 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 13 where to_status_id = 2 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 14 where to_status_id = 3 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 2 where to_status_id = 5 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 10 where to_status_id = 80 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 11 where to_status_id = 90 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 3 where to_status_id = 100 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 4 where to_status_id = 110 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 5 where to_status_id = 130 and docType = "InternalDocument";
UPDATE wf_history SET to_status_id = 15 where to_status_id = 150 and docType = "InternalDocument";

UPDATE wf_history SET action_id = 14 where action_id = 1 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 15 where action_id = 3 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 2 where action_id = 5 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 2 where action_id = 6 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 2 where action_id = 55 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 10 where action_id = 80 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 3 where action_id = 90 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 4 where action_id = 100 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 1 where action_id = 110 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 6 where action_id = 135 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 16 where action_id = 150 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 9 where action_id = 0 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 17 where action_id = -10000 and docType = "InternalDocument";
UPDATE wf_history SET action_id = 13 where action_id = 1002 and docType = "InternalDocument";


UPDATE dms_incoming_documents SET status_id = 9 where status_id = 1;
UPDATE dms_incoming_documents SET status_id = 2 where status_id = 2;
UPDATE dms_incoming_documents SET status_id = 10 where status_id = 80;
UPDATE dms_incoming_documents SET status_id = 11 where status_id = 90;
UPDATE dms_incoming_documents SET status_id = 3 where status_id = 100;
UPDATE dms_incoming_documents SET status_id = 4 where status_id = 110;
UPDATE dms_incoming_documents SET status_id = 5 where status_id = 120;
UPDATE dms_incoming_documents SET status_id = 6 where status_id = 130;

UPDATE wf_history SET from_status_id = 9 where from_status_id = 1 and docType = "IncomingDocument";
UPDATE wf_history SET from_status_id = 2 where from_status_id = 2 and docType = "IncomingDocument";
UPDATE wf_history SET from_status_id = 10 where from_status_id = 80 and docType = "IncomingDocument";
UPDATE wf_history SET from_status_id = 11 where from_status_id = 90 and docType = "IncomingDocument";
UPDATE wf_history SET from_status_id = 3 where from_status_id = 100 and docType = "IncomingDocument";
UPDATE wf_history SET from_status_id = 4 where from_status_id = 110 and docType = "IncomingDocument";
UPDATE wf_history SET from_status_id = 5 where from_status_id = 120 and docType = "IncomingDocument";
UPDATE wf_history SET from_status_id = 6 where from_status_id = 130 and docType = "IncomingDocument";

UPDATE wf_history SET to_status_id = 9 where to_status_id = 1 and docType = "IncomingDocument";
UPDATE wf_history SET to_status_id = 2 where to_status_id = 2 and docType = "IncomingDocument";
UPDATE wf_history SET to_status_id = 10 where to_status_id = 80 and docType = "IncomingDocument";
UPDATE wf_history SET to_status_id = 11 where to_status_id = 90 and docType = "IncomingDocument";
UPDATE wf_history SET to_status_id = 3 where to_status_id = 100 and docType = "IncomingDocument";
UPDATE wf_history SET to_status_id = 4 where to_status_id = 110 and docType = "IncomingDocument";
UPDATE wf_history SET to_status_id = 5 where to_status_id = 120 and docType = "IncomingDocument";
UPDATE wf_history SET to_status_id = 6 where to_status_id = 130 and docType = "IncomingDocument";


UPDATE wf_history SET action_id = 2 where action_id = 1 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 10 where action_id = 2 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 11 where action_id = 80 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 3 where action_id = 90 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 4 where action_id = 100 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 1 where action_id = 110 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 5 where action_id = 125 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 6 where action_id = 135 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 9 where action_id = 0 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 12 where action_id = 1001 and docType = "IncomingDocument";
UPDATE wf_history SET action_id = 13 where action_id = 1002 and docType = "IncomingDocument";