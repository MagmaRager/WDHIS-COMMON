package com.wdhis.common.util;

import org.apache.commons.lang3.StringEscapeUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ALAN on 2018/9/29.
 */
public class DbResultSetUtil {


    public static <T extends Object> T ResultSetToEntity(T entity, ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();

            String[] columnsName = getColumnName(rsmd);
            Class clazz = entity.getClass();
            Object entityObject = null;
            Field[] fields = clazz.getDeclaredFields();

            rs.next();
            entityObject = clazz.newInstance();

            if (rs.getRow() == 1) {
                for (int i = 0; i < columnsName.length; i++) {
                    for (int k = 0; k < fields.length; k++) {
                        if (fields[k].getName().equals(underline2Camel(columnsName[i], true))) {
                            int type = rsmd.getColumnType(i + 1);
                            int prcs = rsmd.getPrecision(i + 1);
                            fields[k].setAccessible(true);
                            String s = rs.getString(columnsName[i]);

                            if (type == Types.VARCHAR) {
                                fields[k].set(entityObject, s);
                            } else if (type == Types.NUMERIC) {
                                int scale = rsmd.getScale(i + 1);
                                if(scale > 0) {
                                    fields[k].set(entityObject, Double.parseDouble(s));
                                }
                                else {
                                    if (prcs >= 12) {
                                        fields[k].set(entityObject, Long.parseLong(s));
                                    } else {
                                        fields[k].set(entityObject, Integer.parseInt(s));
                                    }
                                }
                            } else if (type == Types.TIMESTAMP) {
                                Date d = new Date(rs.getTimestamp(i + 1).getTime());
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                fields[k].set(entityObject, formatter.format(d));
                            }
                            continue;
                        }
                    }
                }
                return (T) entityObject;
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static <T extends Object> String ResultSetToJson(T entity, ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            String[] columnsName = getColumnName(rsmd);

            Class clazz = entity.getClass();
            Field[] fields = clazz.getDeclaredFields();
            StringBuilder json = new StringBuilder("{");

            for (int i = 0; i < columnsName.length; i++) {
                int type = rsmd.getColumnType(i + 1);
                for (int k = 0; k < fields.length; k++) {
                    if (fields[k].getName().equals(underline2Camel(columnsName[i], true))) {
                        fields[k].setAccessible(true);
                        String s = rs.getString(columnsName[i]);

                        if (s == null) {
                            json.append("\"" + underline2Camel(columnsName[i], true) + "\": null,");
                        } else if (type == Types.VARCHAR) {
                            json.append("\"" + underline2Camel(columnsName[i], true) + "\":\"" + StringEscapeUtils.escapeJava(s) + "\",");
                        } else if (type == Types.NUMERIC) {
                            json.append("\"" + underline2Camel(columnsName[i], true) + "\":" + s + ",");
                        } else if (type == Types.TIMESTAMP) {
                            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String tsStr = sdf.format(rs.getTimestamp(i + 1));
                            json.append("\"" + underline2Camel(columnsName[i], true) + "\":\"" + tsStr + "\",");
                        }
                        continue;
                    }
                }
            }
            json.deleteCharAt(json.length() - 1);
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, Object> ResultSetToMapList(ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            String[] columnsName = getColumnName(rsmd);
            Map<String, Object> rowmap = new HashMap<String, Object>();

            for (int i = 0; i < columnsName.length; i++) {
                int type = rsmd.getColumnType(i + 1);
                String s = rs.getString(columnsName[i]);
                if (s == null) {
                    rowmap.put(underline2Camel(columnsName[i], true), null);
                } else if (type == Types.VARCHAR) {
                    rowmap.put(underline2Camel(columnsName[i], true), s);
                } else if (type == Types.NUMERIC) {
                    int scale = rsmd.getScale(i + 1);
                    if(scale > 0) {
                        rowmap.put(underline2Camel(columnsName[i], true), Double.parseDouble(s));
                    }
                    else {
                        int prcs = rsmd.getPrecision(i + 1);
                        if (prcs >= 12) {
                            rowmap.put(underline2Camel(columnsName[i], true), Long.parseLong(s));
                        } else {
                            rowmap.put(underline2Camel(columnsName[i], true), Integer.parseInt(s));
                        }
                    }
                } else if (type == Types.TIMESTAMP) {
                    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String tsStr = sdf.format(rs.getTimestamp(i + 1));
                    rowmap.put(underline2Camel(columnsName[i], true), tsStr);
                }
            }
            return rowmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String[] getColumnName(ResultSetMetaData rsmd) {
        String[] columnName = null;
        try {
            columnName = new String[rsmd.getColumnCount()];
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                columnName[i] = rsmd.getColumnName(i + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columnName;
    }

    private static String underline2Camel(String line, boolean smallCamel) {
        if (line == null || "".equals(line)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("([A-Za-z\\d]+)(_)?");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(smallCamel && matcher.start() == 0 ? Character.toLowerCase(word.charAt(0)) : Character.toUpperCase(word.charAt(0)));
            int index = word.lastIndexOf('_');
            if (index > 0) {
                sb.append(word.substring(1, index).toLowerCase());
            } else {
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
