package com.example.repository;

import com.example.model.Order;
import com.example.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@SuppressWarnings("rawtypes")
public class UserRepository extends MainRepository<User> {

    @Value("${spring.application.userDataPath}")
    private String userDataPath;

    /**
     * Tells MainRepository where to find the users.json file.
     */
    @Override
    protected String getDataPath() {
        return userDataPath;
    }

    /**
     * Tells MainRepository how to deserialize an array of Users.
     */
    @Override
    protected Class<User[]> getArrayType() {
        return User[].class;
    }

    /**
     * 1) Get Users
     *    Returns all users from the JSON file.
     */
    public ArrayList<User> getUsers() {
        return findAll(); // findAll() is inherited from MainRepository
    }

    /**
     * 2) Get User By ID
     *    Fetch a user by its unique ID.
     */
    public User getUserById(UUID userId) {
        for (User user : getUsers()) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    /**
     * 3) Add User
     *    Adds a new user to the JSON file.
     */
    public User addUser(User user) {
        save(user); // save() is inherited from MainRepository
        return user;
    }

    /**
     * 4) Get the Orders of a User
     *    Retrieve all orders for a given user ID.
     */
    public List<Order> getOrdersByUserId(UUID userId) {
        User user = getUserById(userId);
        return (user != null) ? user.getOrders() : new ArrayList<>();
    }

    /**
     * 5) Add Order to the User
     *    Lets the user add an order to their orders list.
     */
    public void addOrderToUser(UUID userId, Order order) {
        ArrayList<User> users = getUsers();
        for (User u : users) {
            if (u.getId().equals(userId)) {
                u.getOrders().add(order);
                overrideData(users); // overwrite the JSON file with the updated list
                return;
            }
        }
    }

    /**
     * 6) Remove Order from User
     *    Removes a specific order from the user's orders list.
     */
    public void removeOrderFromUser(UUID userId, UUID orderId) {
        ArrayList<User> users = getUsers();
        for (User u : users) {
            if (u.getId().equals(userId)) {
                u.getOrders().removeIf(o -> o.getId().equals(orderId));
                overrideData(users);
                return;
            }
        }
    }

    /**
     * 7) Delete User
     *    Deletes a user by passing his/her ID.
     */
    public void deleteUserById(UUID userId) {
        ArrayList<User> users = getUsers();
        boolean removed = users.removeIf(u -> u.getId().equals(userId));
        if (removed) {
            overrideData(users);
        }
    }
}
