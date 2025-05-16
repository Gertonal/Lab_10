# [C] Create
# insert into library (title, author, publisher, year, pages) values ("Война и мир","Толстой","М", 1999, 750);

# [R] Read
SELECT * FROM my_db.library;

# [U] Update
update my_db.library set title='Маски', author='Метельский', publisher = 'СПБ', year = 2019, pages= 254 where id = 10;

# [D] Delete
# delete from library where id = 3;