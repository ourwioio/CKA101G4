把完整的建表與假資料 SQL 放在這個資料夾，例如：

01-schema.sql
02-data.sql

MySQL 只有在「資料 volume 第一次建立」時，才會自動執行
/docker-entrypoint-initdb.d 內的 SQL。

若你修改 SQL 後想重新初始化資料庫，請先備份資料，再執行：

docker compose down -v
docker compose up --build
