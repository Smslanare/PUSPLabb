CREATE TABLE users(
    username VARCHAR(60) PRIMARY KEY,
    displayName VARCHAR(120) NOT NULL,
    hashedPassword VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN'),
    primary key (username)
);

CREATE TABLE projects(
    project_uuid UUID NOT NULL DEFAULT RANDOM_UUID(7),
    projectName VARCHAR(120) NOT NULL,
    description TEXT,
    primary key (project_uuid)
);

CREATE TABLE user_projects(
    username VARCHAR(60) NOT NULL REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE,
    project_uuid UUID NOT NULL REFERENCES projects(project_uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (username, project_uuid),
    foreign key (username) references users(username) on delete cascade on update cascade,
    foreign key (project_uuid) references projects(project_uuid) on delete cascade on update cascade
);

CREATE TABLE notes(
    note_uuid UUID NOT NULL DEFAULT RANDOM_UUID(7),
    username VARCHAR(60) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    primary key (note_uuid),
    foreign key (username) references users(username) on delete cascade on update cascade
);