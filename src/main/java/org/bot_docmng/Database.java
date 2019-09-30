package org.bot_docmng;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class Database {
    private static String dbPath = Setup.getInstance().getDbPath();
    private static String dbFile = "db.sqlite";
    private static File db = new File(dbPath, dbFile);
    private Connection connection;

    Database() {
        this.connection = null;
        if (!db.exists()) {
            try {
                db.createNewFile();
                try {
                    connect();
                    Statement statement = this.connection.createStatement();
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS files (" +
                            "file_uid TEXT," +
                            "telegram_id TEXT," +
                            "PRIMARY KEY(file_uid))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS tasks (" +
                            "task_uid TEXT," +
                            "performer_uid TEXT," +
                            "message_id TEXT," +
                            "chat_id TEXT," +
                            "PRIMARY KEY(message_id,chat_id) )");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS add_task_message (" +
                            "author_uid TEXT," +
                            "performer_uid TEXT," +
                            "message_id TEXT," +
                            "PRIMARY KEY(message_id,author_uid) )");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS performers (" +
                            "user_id TEXT," +
                            "chat_id TEXT," +
                            "name TEXT," +
                            "PRIMARY KEY(user_id) )");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS times (" +
                            "time TEXT," +
                            "function TEXT," +
                            "PRIMARY KEY(function) )");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS messages ( " +
                            "task_uid TEXT, " +
                            "performer_uid TEXT, " +
                            "message_id TEXT, " +
                            "chat_id TEXT, " +
                            "PRIMARY KEY(message_id,chat_id) )");
                } catch (SQLException e) {
                    BotLogger.error(e.getMessage());
                } finally {
                    close();
                }
            } catch (IOException e) {
                BotLogger.error(e.getMessage());
            }
        }

    }

    private void connect() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
        }
    }

    private void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                BotLogger.error(e.getMessage());
            }
        }
    }

    void putFile(String fileUid, String telegramUid) {
        try {
            connect();
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("INSERT INTO files(file_uid,telegram_id) VALUES(?,?)");
            preparedStatement.setString(1, fileUid);
            preparedStatement.setString(2, telegramUid);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
        } finally {
            close();
        }
    }

    String getFile(String fileUid) {
        try {
            connect();
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("SELECT telegram_id FROM files WHERE file_uid = ?");
            preparedStatement.setString(1, fileUid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getString(1);
            return null;
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
            return null;
        } finally {
            close();
        }
    }

    void putPerformer(HashMap<String, String> map) {
        try {
            connect();
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("INSERT INTO performers(" +
                            "name, " +
                            "chat_id," +
                            "user_id)" +
                            "VALUES (?,?,?)");
            preparedStatement.setString(1, map.get("name"));
            preparedStatement.setString(2, map.get("chat_id"));
            preparedStatement.setString(3, map.get("user_id"));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
        } finally {
            close();
        }
    }

    HashMap<String, String> getPerformer(String userId) {
        HashMap<String, String> map = new HashMap<>();
        try {
            connect();
            PreparedStatement preparedStatement;
            if (userId.length() > 15) {
                preparedStatement =
                        this.connection.prepareStatement("SELECT user_id, " +
                                "chat_id, " +
                                "name " +
                                "FROM performers " +
                                "WHERE user_id = ?");
            } else {
                preparedStatement =
                        this.connection.prepareStatement("SELECT user_id, " +
                                "chat_id, " +
                                "name " +
                                "FROM performers " +
                                "WHERE chat_id = ?");
            }
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                map.put("name", resultSet.getString(3));
                map.put("user_id", resultSet.getString(1));
                map.put("chat_id", resultSet.getString(2));
                return map;
            }
            return map;
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
            return map;
        } finally {
            close();
            return map;
        }
    }

    void putAddTask(HashMap<String, String> map) {
        try {
            connect();
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("INSERT INTO add_task_message(" +
                            "author_uid, " +
                            "performer_uid," +
                            "message_id)" +
                            "VALUES (?,?,?)");
            preparedStatement.setString(1, map.get("author_uid"));
            preparedStatement.setString(2, map.get("performer_uid"));
            preparedStatement.setString(3, map.get("message_id"));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
        } finally {
            close();
        }
    }

    String getAddTask(String authorUid, String messageId) {
        String response = "";
        try {
            connect();
            PreparedStatement preparedStatement;
            preparedStatement =
                    this.connection.prepareStatement("SELECT " +
                            "performer_uid " +
                            "FROM add_task_message " +
                            "WHERE message_id = ?" +
                            "AND author_uid = ?");
            preparedStatement.setString(1, messageId);
            preparedStatement.setString(2, authorUid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                response = resultSet.getString(1);
                return response;
            }
            return response;
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
            return response;
        } finally {
            close();
        }
    }

    void putTask(HashMap<String, String> map) {
        try {
            connect();
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("INSERT INTO messages(" +
                            "task_uid, " +
                            "performer_uid," +
                            "message_id," +
                            "chat_id)" +
                            "VALUES (?,?,?,?)");
            preparedStatement.setString(1, map.get("task_uid"));
            preparedStatement.setString(2, map.get("performer_uid"));
            preparedStatement.setString(3, map.get("message_id"));
            preparedStatement.setString(4, map.get("chat_id"));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
        } finally {
            close();
        }
    }

    ArrayList<HashMap<String, String>> getTasksFromId(String taskId) {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        try {
            connect();
            PreparedStatement preparedStatement;
            preparedStatement =
                    this.connection.prepareStatement("SELECT " +
                            "message_id, " +
                            "chat_id " +
                            "FROM messages " +
                            "WHERE task_uid = ?");
            preparedStatement.setString(1, taskId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("message_id", resultSet.getString(1));
                map.put("chat_id", resultSet.getString(2));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
            return list;
        } finally {
            close();
        }
    }

    String getTaskUidFromMessage(String messageId, String chatId) {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        String result = "";
        try {
            connect();
            PreparedStatement preparedStatement;
            preparedStatement =
                    this.connection.prepareStatement("SELECT " +
                            "task_uid " +
                            "FROM messages " +
                            "WHERE chat_id = ?" +
                            "AND message_id = ?");
            preparedStatement.setString(1, chatId);
            preparedStatement.setString(2, messageId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
            return result;
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
            return result;
        } finally {
            close();
        }
    }

    public HashMap<String, String> getTaskFromMessage(String chatId, String messageId) {
        HashMap<String, String> map = new HashMap<>();
        try {
            connect();
            PreparedStatement preparedStatement;
            preparedStatement =
                    this.connection.prepareStatement("SELECT " +
                            "task_uid, " +
                            "performer_uid, " +
                            "message_id," +
                            "chat_id " +
                            "FROM messages " +
                            "WHERE message_id = ?" +
                            "AND chat_id = ?");
            preparedStatement.setString(1, messageId);
            preparedStatement.setString(2, chatId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                map.put("task_uid", resultSet.getString(1));
                map.put("performer_uid", resultSet.getString(2));
                map.put("message_id", resultSet.getString(3));
                map.put("chat_id", resultSet.getString(4));
                return map;
            }
            return map;
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
            return map;
        } finally {
            close();
        }
    }

    public String getTimeByFunction(String function) {
        try {
            connect();
            PreparedStatement preparedStatement;
            preparedStatement =
                    this.connection.prepareStatement("SELECT " +
                            "time " +
                            "FROM times " +
                            "WHERE function = ?");
            preparedStatement.setString(1, function);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                if (function.equals("scheduler_timestamp")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    String end = dateFormat.format(new Date());
                    putTimeByFunction(function, end);
                }
            } else {
                return resultSet.getString(1);
            }
            return "";
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
            return "";
        } finally {
            close();
        }
    }

    void putTimeByFunction(String function, String time) {
        try {
            connect();
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("INSERT OR REPLACE INTO times(" +
                            "function, " +
                            "time)" +
                            "VALUES (?,?)");
            preparedStatement.setString(1, function);
            preparedStatement.setString(2, time);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            BotLogger.error(e.getMessage());
        } finally {
            close();
        }
    }
}