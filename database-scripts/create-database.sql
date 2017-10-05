create table AUDIT_REVISION (rev bigint generated by default as identity, REV_TIMESTAMP bigint not null, USER_NAME varchar(255) not null, primary key (rev));
create table LOOKUP_VALUE (LOOKUP_VALUE_ID bigint generated by default as identity, ACTIVE boolean not null, EFFECTIVE_FROM date, EFFECTIVE_TO date, DISPLAY_VALUE varchar(255) not null, LOOKUP_LIST_NAME varchar(100) not null, version bigint, PARENT bigint, primary key (LOOKUP_VALUE_ID));
create table LOOKUP_VALUE_AUD (LOOKUP_VALUE_ID bigint not null, REV bigint not null, REVTYPE smallint, ACTIVE boolean, EFFECTIVE_FROM date, EFFECTIVE_TO date, DISPLAY_VALUE varchar(255), LOOKUP_LIST_NAME varchar(255), PARENT bigint, primary key (LOOKUP_VALUE_ID, REV));
create unique index UC_LOOKUP_LIST_VALUE on LOOKUP_VALUE (LOOKUP_LIST_NAME, DISPLAY_VALUE, PARENT);
alter table LOOKUP_VALUE add constraint FK_LOOKUP_VALUE_PARENT foreign key (PARENT) references LOOKUP_VALUE;
alter table LOOKUP_VALUE_AUD add constraint FK14l1crqdd17eimwe9dr8oo88v foreign key (REV) references AUDIT_REVISION;
