-- =====================================================================
-- Sample / demonstration data for the Campus Lost and Found system
-- ---------------------------------------------------------------------
-- OPTIONAL SCRIPT. The application works with an empty database.
-- Every row below is demonstration data created only so that the
-- console output can be shown without entering records manually.
-- Run it after database/schema.sql, as:
--     mysql -u root -p campus_lost_found < database/sample_data.sql
-- =====================================================================

USE campus_lost_found;

INSERT INTO items (item_name, category, description, report_type, location, report_date, contact_info, status) VALUES
('Blue Wallet',        'Documents', 'Blue leather wallet with student ID inside',            'LOST',  'Central Library, 2nd floor',       '2026-09-28', 'rahul@student.campus.edu',  'ACTIVE'),
('Black Water Bottle', 'Bottles',   'Steel bottle with a name sticker "Asha"',                'FOUND', 'Block C, Classroom 204',           '2026-10-01', 'asha@student.campus.edu',   'ACTIVE'),
('USB Pendrive 64GB',  'Electronics','Silver pen drive containing project files',             'LOST',  'Computer Lab 1',                  '2026-10-02', NULL,                        'ACTIVE'),
('College ID Card',    'Documents', 'ID card of student from the ECE department',             'FOUND', 'Canteen, near counter 2',          '2026-10-03', 'helpdesk@campus.edu',       'RECOVERED'),
('Black Umbrella',     'Accessories','Compact folding umbrella with a red handle',            'LOST',  'Bus stop, main gate',              '2026-10-04', NULL,                        'ACTIVE'),
('Calculator',         'Electronics','Casio scientific calculator, slightly scratched',        'FOUND', 'Lecture Hall 3, last bench',       '2026-10-05', 'vinay@student.campus.edu',  'ACTIVE');

-- Marking demonstration rows: the ID card was returned to its owner,
-- so it is RECOVERED but keeps report_type = FOUND.
