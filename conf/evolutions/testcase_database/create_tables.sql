
#---!Ups

CREATE SEQUENCE articles_ids;

CREATE TABLE articles (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('articles_ids'),
  short_name VARCHAR(255),
  full_name VARCHAR(255),
  full_text VARCHAR(255),
  parent_chapter_id INTEGER
);

CREATE SEQUENCE chapters_ids;

CREATE TABLE chapters (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('chapters_ids'),
  short_name VARCHAR(255),
  full_name VARCHAR(255),
  parent_chapter_id INTEGER
)

