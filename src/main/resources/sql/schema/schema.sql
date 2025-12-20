create schema if not exists sru_library;

Use sru_library;
Set names 'UTF8MB4';
# =============================================  independent tables ===============================================

#========================================================
#crate table user
create table if not exists users(
    userId varchar(100) primary key not null,
    email varchar(50) unique,
    username varchar(50),
    password varchar(255) not null ,
    roles enum('USER', 'ADMIN', 'SUPER_ADMIN') not null
);
#========================================================
# crate table degree level
#Drop table if exists degree_level;
create table if not exists degree_level(
    degree_level_id varchar(10) primary key,
    degree_level varchar(100) not null
);

#========================================================
# crate table collages
#Drop table if exists collages;
create table if not exists colleges(
    college_id varchar(10) primary key,
    college_name varchar(100) not null
);

#========================================================
# crate table language.sql
#Drop table if exists language.sql;
create table if not exists language(
   language_id varchar(5) primary key,
   language_name varchar(20) not null
);

#========================================================
#crate table guest
create table if not exists donator(
  donator_id int primary key auto_increment,
  donator_name varchar(50) not null
);

# =========================================== dependent tables ================================================
#========================================================
# crate table major
#Drop tables if exists majors;
create table if not exists majors(
    major_id varchar(10) primary key,
    major_name varchar(100) not null,
    college_id varchar(10) not null ,
    foreign key (college_id) references colleges(college_id) on delete cascade on update cascade
);

#4
#========================================================
# crate table students
#Drop table if exists students;
create table if not exists students(
    student_id bigint primary key,
    student_name varchar(60) not null,
    gender varchar(10) not null ,
    date_of_birth date not null ,
    degree_level_id varchar(10) not null ,
    major_id varchar(10) not null,
    generation int not null ,
    foreign key (degree_level_id)
       references degree_level(degree_level_id)
       on delete cascade
       on update cascade ,
    foreign key (major_id)
       references majors(major_id)
       on delete cascade
       on update cascade
);

#========================================================
# crate table books
#Drop table if exists books;
create table if not exists books(
    book_id varchar(10) primary key,
    book_title varchar(100) not null ,
    bookQuan int not null,
    language_id varchar(5) not null ,
    college_id varchar(10) not null ,
    author VARCHAR(100) ,
    publication_year INT  ,
    genre varchar(100) not null ,
    received_date DATE null ,
    isActive boolean,
    inactiveDate DATE,
    foreign key (college_id) references colleges(college_id)
        on update cascade
        on delete cascade ,
    foreign key (language_id) references language(language_id)
        on delete cascade
        on update cascade
);


#========================================================
# crate table borrow books
#Drop table if exists borrow_books;
Create table if not exists borrow_books(
    borrow_id bigint primary key auto_increment,
    book_id varchar(10) not null ,
    book_quan int not null ,
    student_id bigint not null ,
    borrow_date date not null ,
    give_back_date date,
    is_bring_back boolean,
    is_extend boolean,
    foreign key (book_id) references books(book_id) on update cascade on delete cascade ,
    foreign key (student_id) references students(student_id) on update cascade
);

#========================================================
create table if not exists donation(
    PRIMARY KEY (book_id, donator_id),
    book_id varchar(10),
    donator_id int not null ,
    donate_date DATE NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(book_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (donator_id) REFERENCES donator(donator_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

#========================================================
# crate table attend and exit
Create table if not exists attend(
    attend_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    visitor_id BIGINT NOT NULL,
    entry_time TIME NOT NULL,
    exit_time TIME NULL,
    attend_date DATE NOT NULL,
    purpose VARCHAR(50) NOT NULL,

    CONSTRAINT fk_attend_visitor
     FOREIGN KEY (visitor_id)
         REFERENCES visitors(visitor_id)
         ON DELETE CASCADE ON UPDATE CASCADE
);

#9
#========================================================
# crate table guest
#Drop table if exists guests;
create table if not exists sru_staff(
    sru_staff_id varchar(10) primary key,
    sru_staff_name varchar(60) not null,
    gender varchar(8) not null ,
    position varchar(30)
);

#
create table if not exists blacklist(
    blacklist_id int primary key auto_increment,
    student_id bigint not null ,
    book_id varchar(10) not null,
    foreign key (student_id) references students(student_id)
);

create table if not exists library_staff(
    staff_id bigint primary key auto_increment,
    staff_name varchar(50) not null ,
    gender varchar(7) not null ,
    position varchar(80) not null ,
    degree_level_id varchar(10) null ,
    major_id varchar(10) null ,
    year int null ,
    shift_work varchar(20) null ,
    is_active bool ,
    constraint fk_staff_degree foreign key (degree_level_id) references degree_level(degree_level_id),
    constraint fk_staff_major foreign key (major_id) references majors(major_id)
);

create table if not exists staff_major(
   staff_id BIGINT,
   major_id VARCHAR(10),
   PRIMARY KEY (staff_id, major_id),
   FOREIGN KEY (staff_id) REFERENCES library_staff(staff_id) on delete cascade on update cascade ,
   FOREIGN KEY (major_id) REFERENCES majors(major_id) on delete cascade on update cascade
);

CREATE TABLE if not exists visitors(
    visitor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    visitor_type ENUM('STUDENT', 'SRU_STAFF') NOT NULL,
    student_id BIGINT NULL,
    sru_staff_id VARCHAR(10) NULL,

    CONSTRAINT fk_visitor_student
        FOREIGN KEY (student_id) REFERENCES students(student_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_visitor_staff
        FOREIGN KEY (sru_staff_id) REFERENCES sru_staff(sru_staff_id)
            ON DELETE CASCADE ON UPDATE CASCADE
);











