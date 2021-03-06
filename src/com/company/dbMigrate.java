package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.sql.*;
import java.util.Calendar;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.*;



public abstract class dbMigrate {

    private static final Logger logger = LogManager.getLogger();

    private static Connection setConnection(String url, String user, String password){
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, user, password);
            logger.info("Connection established!");
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static void addItemDB(Element eElement, Connection con, String query) throws SQLException {
        PreparedStatement preparedStmt = con.prepareStatement(query);
        Random rand = new Random();
        int rnd = rand.nextInt();
        preparedStmt.setInt(1, rnd > 0 ? rnd : rnd * -1);
        preparedStmt.setString(2, eElement.getElementsByTagName("firstName").item(0).getTextContent());
        preparedStmt.setString(3, eElement.getElementsByTagName("middleName").item(0).getTextContent());
        preparedStmt.setString(4, eElement.getElementsByTagName("secondName").item(0).getTextContent());
        preparedStmt.setInt(5, new Integer(eElement.getElementsByTagName("uId").item(0).getTextContent()));
        preparedStmt.setString(6, eElement.getElementsByTagName("count").item(0).getTextContent());
        preparedStmt.execute();
    }

    public static boolean migrateDB(String xmlPath, String xsdPath, String url, String user, String password){

        Connection con = setConnection(url, user, password);
        if (con == null)
            return false;

        try {
            File fXmlFile = new File(xmlPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("user");

            Calendar calendar = Calendar.getInstance();
            java.sql.Date startDate = new java.sql.Date(calendar.getTime().getTime());
            String query = " insert into users ( idusers, firstName, secondName, middleName, uId, count)"
                    + " values (?, ?, ?, ?, ?, ?)";

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    addItemDB(eElement, con, query);
                    logger.info("Item added to DB");
                }

            }
            con.close();
            logger.info("Connection closed!");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
