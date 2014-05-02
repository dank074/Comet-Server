package com.cometproject.server.storage.queries.rooms;

import com.cometproject.server.game.rooms.items.FloorItem;
import com.cometproject.server.game.rooms.items.WallItem;
import com.cometproject.server.storage.SqlHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RoomItemDao {

    public static void getItems(int roomId, ConcurrentLinkedQueue<FloorItem> floorItems, ConcurrentLinkedQueue<WallItem> wallItems) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            sqlConnection = SqlHelper.getConnection();

            preparedStatement = SqlHelper.prepare("SELECT * FROM items WHERE room_id = ?", sqlConnection);
            preparedStatement.setInt(1, roomId);

            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                if (resultSet.getString("wall_pos").equals(""))
                    floorItems.add(new FloorItem(resultSet.getInt("id"), resultSet.getInt("base_item"), roomId, resultSet.getInt("user_id"), resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getDouble("z"), resultSet.getInt("rot"), resultSet.getString("extra_resultSet")));
                else
                    wallItems.add(new WallItem(resultSet.getInt("id"), resultSet.getInt("base_item"), roomId, resultSet.getInt("user_id"), resultSet.getString("wall_pos"), resultSet.getString("extra_resultSet")));
            }
        } catch (SQLException e) {
            SqlHelper.handleSqlException(e);
        } finally {
            SqlHelper.closeSilently(resultSet);
            SqlHelper.closeSilently(preparedStatement);
            SqlHelper.closeSilently(sqlConnection);
        }
    }
}