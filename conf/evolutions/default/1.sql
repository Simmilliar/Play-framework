# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table sessions (
  token                         varchar(255) not null,
  user_email                    varchar(255) not null,
  expiration_date               bigint not null,
  constraint pk_sessions primary key (token)
);

create table user (
  email                         varchar(255) not null,
  confirmed                     boolean default false not null,
  confirmation_key              varchar(255) not null,
  name                          varchar(255) not null,
  password_hash                 varchar(255) not null,
  constraint pk_user primary key (email)
);

alter table sessions add constraint fk_sessions_user_email foreign key (user_email) references user (email) on delete restrict on update restrict;
create index ix_sessions_user_email on sessions (user_email);


# --- !Downs

alter table sessions drop constraint if exists fk_sessions_user_email;
drop index if exists ix_sessions_user_email;

drop table if exists sessions;

drop table if exists user;

