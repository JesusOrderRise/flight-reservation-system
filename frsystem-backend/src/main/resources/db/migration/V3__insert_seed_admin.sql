INSERT INTO users (first_name,
                   last_name,
                   email,
                   password_hash,
                   role)
VALUES ('System',
        'Admin',
        'admin@frsystem.com',
        '$2a$10$dOTx4yhz/KkLbaKuoEiJ1OW43zYR8TWQbZU5JN4xNPhRkUVFoJb8q',
        'ADMIN'
       )ON CONFLICT (email) DO NOTHING;

--password is Admin123!