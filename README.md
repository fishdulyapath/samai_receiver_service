CREATE TABLE public.krc_permission
(
  roworder serial,
  user_code character varying(255) NOT NULL,
  receive_screen smallint NOT NULL DEFAULT 0,
  admin_screen smallint NOT NULL DEFAULT 0,
  history_screen smallint NOT NULL DEFAULT 0,
  create_datetime timestamp without time zone DEFAULT timezone('asia/bangkok'::text, now()),
  CONSTRAINT krc_permission_pkey PRIMARY KEY (user_code)
)


CREATE TABLE public.krc_trans
(
  roworder serial,
  doc_no character varying(255) NOT NULL,
  doc_date date DEFAULT timezone('Asia/Bangkok'::text, now()),
  doc_time time without time zone DEFAULT timezone('Asia/Bangkok'::text, now()),
  wh_code character varying(255),
  location_code character varying(255),
  creator_code character varying(255),
  remark text,
  create_datetime timestamp without time zone DEFAULT timezone('Asia/Bangkok'::text, now()),
  status smallint DEFAULT 0,
  is_approve smallint DEFAULT 0,
  user_approve character varying(255),
  approve_date_time timestamp without time zone DEFAULT timezone('Asia/Bangkok'::text, now()),
  branch_code character varying(255),
  doc_ref character varying(255),
  is_close character varying(255),
  user_close character varying(255),
  close_date_time timestamp without time zone DEFAULT timezone('Asia/Bangkok'::text, now()),
  cust_code character varying(255),
  sale_code character varying(255),
  CONSTRAINT krc_trans_pk PRIMARY KEY (doc_no)
)


CREATE TABLE public.krc_trans_detail
(
  roworder serial,
  doc_no character varying(255),
  barcode character varying(255),
  item_year character varying(255),
  item_code character varying(255),
  unit_code character varying(255),
  wh_code character varying(255),
  location_code character varying(255),
  qty numeric DEFAULT 0,
  so_qty numeric DEFAULT 0,
  create_datetime timestamp without time zone DEFAULT timezone('Asia/Bangkok'::text, now()),
  remark text,
  line_number integer NOT NULL DEFAULT 0,
  CONSTRAINT krc_trans_detail_pk PRIMARY KEY (roworder)
)
