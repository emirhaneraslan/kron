package com.example.kroncustomer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CustomerService {

    @Autowired
    private DataSource dataSource;

    private static final Logger log =
            LoggerFactory.getLogger(CustomerService.class);

    @RequestMapping(value = "/customer/add", method = RequestMethod.POST)
    public ResponseEntity<String> add(@RequestBody CustomerDTO customerDTO) {

        log.info("Request came");
        Connection connection=null;

        try {

            validateRequest(customerDTO);

            connection =this.dataSource.getConnection();

            List<Long> tagIdList = getTagIds(connection, customerDTO.getTags());

            Long customerId = insertCustomer(connection,customerDTO);

            insertCustomerTags(connection ,customerId,tagIdList);

            log.info("Customer saved successfully: " + customerDTO.getUsername());

        } catch (IllegalArgumentException e) {
            log.error("Validation Error: ", e);
            return new ResponseEntity<>(e.getLocalizedMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (SQLException e) {
            log.error("Database Error: ", e);
            return new ResponseEntity<>(e.getLocalizedMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return new ResponseEntity<>(e.getLocalizedMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            closeConnection(connection);
        }

        return new ResponseEntity<>("Customer saved successfully.",
                HttpStatus.OK);
    }

    /**
     * This method gives tag IDs.
     *
     * @param connection
     * @param tags
     * @return tag id list.
     * @throws Exception
     */
    private List<Long> getTagIds(Connection connection, List<String> tags)
            throws Exception {

        PreparedStatement tagIdStatement = connection.prepareStatement
                ("SELECT id FROM tag WHERE name = ?");

        List<Long> tagIdList = new ArrayList<>();
        for (String tag : tags) {
            tagIdStatement.setString(1, tag);
            ResultSet resultSet = tagIdStatement.executeQuery();
            if (resultSet.next()) {
                tagIdList.add(resultSet.getLong(1));
            } else {
                throw new IllegalArgumentException("Undefined tag: " + tag);
            }
        }
        return tagIdList;

    }

    /**
     *This method adds customer and tag relationships to the customer_tags table.
     *
     * @param connection
     * @param customerId
     * @param tagIdList
     */
    private void insertCustomerTags(Connection connection,
                                    Long customerId,
                                    List<Long> tagIdList) {

        try (PreparedStatement insertCustomerTagStatement =
                     connection.prepareStatement
                             ("INSERT INTO customer_tags (customer_id, tag_id) " +
                                     "VALUES (?, ?)")) {
            for (Long tagId : tagIdList) {
                insertCustomerTagStatement.setLong(1, customerId);
                insertCustomerTagStatement.setLong(2, tagId);
                try {
                    insertCustomerTagStatement.executeUpdate();
                } catch (SQLException e) {
                    log.error("CustomerID or TagID didn't not be found: ", e);
                }
            }
        } catch (SQLException e) {
            log.error("Database Error: ", e);
        }
    }

    /**
     * This method closes connection.
     *
     * @param connection
     */
    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }


    /**
     * This method validates the request.
     *
     * @param customerDTO
     */
    private void validateRequest(CustomerDTO customerDTO) {

        if (customerDTO.getUsername() == null
                || customerDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a username.");
        }
        if (customerDTO.getName() == null
                || customerDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a name.");
        }
        if (customerDTO.getSurname() == null
                || customerDTO.getSurname().trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a surname.");
        }
        if (customerDTO.getEmail() == null
                || customerDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a email.");
        }
        if (customerDTO.getTags() == null
                || customerDTO.getTags().isEmpty()) {
            throw new IllegalArgumentException("Please provide a tag or tags.");
        }
    }

    /**
     * This method inserts customers.
     *
     * @param connection
     * @param customerDTO
     * @return customerId
     * @throws Exception
     */
    private Long insertCustomer(Connection connection
            , CustomerDTO customerDTO) throws Exception {

        PreparedStatement selectByUsernameStatement = connection.prepareStatement
                ("SELECT * FROM customer WHERE username = ?");
        selectByUsernameStatement.setString(1, customerDTO.getUsername());
        ResultSet resultSet = selectByUsernameStatement.executeQuery();
        if (resultSet.next()) {
            throw new IllegalArgumentException
                    ("Username already exists: " + customerDTO.getUsername());
        }

        PreparedStatement selectByEmailStatement = connection.prepareStatement
                ("SELECT * FROM customer WHERE email = ?");
        selectByEmailStatement.setString(1, customerDTO.getEmail());
        resultSet = selectByEmailStatement.executeQuery();
        if (resultSet.next()) {
            throw new IllegalArgumentException
                    ("Email already exists: " + customerDTO.getEmail());
        }

        PreparedStatement insertCustomerStatement =
                connection.prepareStatement
                        ("INSERT INTO customer (username, name, surname, email)" +
                                " VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        insertCustomerStatement.setString(1, customerDTO.getUsername());
        insertCustomerStatement.setString(2, customerDTO.getName());
        insertCustomerStatement.setString(3, customerDTO.getSurname());
        insertCustomerStatement.setString(4, customerDTO.getEmail());
        int affectedRows = insertCustomerStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new IllegalArgumentException("Insert customer failed: " + customerDTO.getUsername());
        }

        PreparedStatement selectCustomerIdStatement = connection.prepareStatement
                        ("SELECT id FROM customer WHERE username = ?");
        selectCustomerIdStatement.setString(1, customerDTO.getUsername());
        resultSet = selectCustomerIdStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            throw new IllegalArgumentException("Insert customer failed: " + customerDTO.getUsername());
        }
    }
}



