INSERT INTO usuario(username,password,email,enabled,eliminado,create_at) VALUES('fmuller','$2a$10$DJbNY7Dk1NA/UzLd7DGn5.eDzHM1eZZK3ivYrZ2dXdyYu6BS8dGiy','fmuller@gmail.com',1,0,NOW());
INSERT INTO usuario(username,password,email,enabled,eliminado,create_at) VALUES('rosa','$2a$10$DJbNY7Dk1NA/UzLd7DGn5.eDzHM1eZZK3ivYrZ2dXdyYu6BS8dGiy','rosa@gmail.com',1,0,NOW());

INSERT INTO role(nombre) VALUES('ROLE_ADMIN');
INSERT INTO role(nombre) VALUES('ROLE_USER');

INSERT INTO usuario_role(usuario_id,role_id) VALUES(1,1);
INSERT INTO usuario_role(usuario_id,role_id) VALUES(1,2);
INSERT INTO usuario_role(usuario_id,role_id) VALUES(2,2);
