--Users schema

#--- !Ups
CREATE SEQUENCE articles_ids;

CREATE TABLE articles (

  id INTEGER PRIMARY KEY DEFAULT NEXTVAL(articles_ids),
  short_name VARCHAR(255)
);

#--- !Downs

DROP TABLE articles;