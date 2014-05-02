package com.cometproject.server.storage.queries.catalog;

import com.cometproject.server.game.catalog.types.CatalogItem;
import com.cometproject.server.game.catalog.types.CatalogPage;
import com.cometproject.server.storage.SqlHelper;
import javolution.util.FastMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CatalogDao {
    public static FastMap<Integer, CatalogPage> getPages() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        FastMap<Integer, CatalogPage> data = new FastMap<>();

        try {
            sqlConnection = SqlHelper.getConnection();

            preparedStatement = SqlHelper.prepare("SELECT * FROM catalog_pages", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                data.put(resultSet.getInt("id"), new CatalogPage(resultSet, getItemsByPage(resultSet.getInt("id"))));
            }
        } catch (SQLException e) {
            SqlHelper.handleSqlException(e);
        } finally {
            SqlHelper.closeSilently(resultSet);
            SqlHelper.closeSilently(preparedStatement);
            SqlHelper.closeSilently(sqlConnection);
        }

        return data;
    }

    private static FastMap<Integer, CatalogItem> getItemsByPage(int pageId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        FastMap<Integer, CatalogItem> data = new FastMap<>();

        try {
            sqlConnection = SqlHelper.getConnection();

            preparedStatement = SqlHelper.prepare("SELECT * FROM catalog_items WHERE page_id = ?", sqlConnection);
            preparedStatement.setInt(1, pageId);

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                data.put(resultSet.getInt("id"), new CatalogItem(resultSet));
            }

        } catch (SQLException e) {
            SqlHelper.handleSqlException(e);
        } finally {
            SqlHelper.closeSilently(resultSet);
            SqlHelper.closeSilently(preparedStatement);
            SqlHelper.closeSilently(sqlConnection);
        }

        return data;
    }
}
