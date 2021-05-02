CREATE TABLE th_user (
  id VARCHAR(255) NOT NULL,
  coins BIGINT NOT NULL,
  date_of_birth DATE NOT NULL,
  name VARCHAR(255) NOT NULL,
  pwd VARCHAR(255) NOT NULL,
  image MEDIUMBLOB NULL,
  CONSTRAINT th_user_pk 		PRIMARY KEY (id)
);