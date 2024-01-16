package server;

import server.exceptions.InvalidUserException;
import server.exceptions.UserNotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerData {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Map<String, UserData> userDataMap;

    ServerData() {
        userDataMap = new HashMap<>();
    }

    public void register(String username, UserData userData) throws InvalidUserException {
        if (userDataMap.containsKey(username)) {
            if (!userDataMap.get(username).address().equals(userData.address())) {
                LOGGER.log(Level.SEVERE, "User " + username + " tried to register from another IP.");
                throw new InvalidUserException(username + " has already registered from another address!");
            }
            userDataMap.get(username).addFiles(userData.files());
        } else {
            userDataMap.put(username, userData);
        }
    }

    public void unregister(String username, Set<String> files) throws UserNotFoundException {
        if (userDataMap.containsKey(username)) {
            userDataMap.get(username).removeFiles(files);
        } else {
            LOGGER.log(Level.SEVERE, "Nonexistent user " + username + " tried to unregister.");
            throw new UserNotFoundException(username + " is not registered!");
        }
    }

    public void disconnect(String username) throws UserNotFoundException {
        if (userDataMap.containsKey(username)) {
            userDataMap.remove(username);
        } else {
            LOGGER.log(Level.SEVERE, "Nonexistent user " + username + " tried to disconnect.");
            throw new UserNotFoundException(username + " is not registered!");
        }
    }

    public Set<String> listAddresses() {
        Set<String> set = new HashSet<>();

        for (Map.Entry<String, UserData> entry : userDataMap.entrySet()) {
            set.add(entry.getKey()
                    + " - "
                    + entry.getValue().address());
        }
        return set;
    }

    public Set<String> listFiles() {
        Set<String> set = new HashSet<>();

        for (Map.Entry<String, UserData> entry : userDataMap.entrySet()) {
            for (String file : entry.getValue().files()) {
                set.add(entry.getKey() + " : " + file);
            }
        }
        return set;
    }
}
