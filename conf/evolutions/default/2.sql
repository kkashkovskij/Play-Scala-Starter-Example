

#--- !Ups

CREATE TABLE articles (

  id SERIAL PRIMARY KEY ,
  shortName VARCHAR(255),
  fullName VARCHAR(255),
  parentChapterId INTEGER REFERENCES chapters(id)

);

# --- !Downs

DROP TABLE articles;
