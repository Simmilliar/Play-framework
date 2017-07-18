# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table session (
  token                         varchar(255) not null,
  user_email                    varchar(255) not null,
  expiration_date               bigint not null,
  constraint pk_session primary key (token)
);

create table users (
  email                         varchar(255) not null,
  confirmed                     boolean default false not null,
  confirmation_key              varchar(255) not null,
  name                          varchar(255) not null,
  password_hash                 varchar(255) not null,
  constraint pk_users primary key (email)
);

alter table session add constraint fk_session_user_email foreign key (user_email) references users (email) on delete restrict on update restrict;
create index ix_session_user_email on session (user_email);


# --- !Downs

alter table if exists session drop constraint if exists fk_session_user_email;
drop index if exists ix_session_user_email;

drop table if exists session cascade;

drop table if exists users cascade;

