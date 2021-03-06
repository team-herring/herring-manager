package org.herring.manager.auth;

import org.codehaus.jackson.map.ObjectMapper;
import org.herring.core.cluster.ClusterSharedStorage;
import org.herring.core.cluster.zookeeper.ZookeeperClient;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author Chiwan Park
 * @since 0.1
 */
@Service
public class ZookeeperAuthenticationStorage implements AuthenticationStorage {

    private ClusterSharedStorage storage;
    private static final UUID AUTH_UUID = UUID.fromString("60136F30-8E26-4698-99A3-E3559C817072");

    public ZookeeperAuthenticationStorage() {
        storage = null;
    }

    public void setZookeeperClient(ZookeeperClient zkClient) {
        storage = new ClusterSharedStorage(zkClient, AUTH_UUID);
    }

    @Override
    public List<String> getListOfUsername() throws IOException {
        return storage.getKeyList();
    }

    @Override
    public User getUserByName(String name) throws IOException, UsernameNotFoundException {
        if (!storage.containsKey(name))
            throw new UsernameNotFoundException("User name '" + name + "' is not found.");

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(storage.get(name), User.class);
    }

    @Override
    public void addUser(User user) throws IOException, IllegalArgumentException {
        validateUser(user);

        if (storage.containsKey(user.getUsername()))
            throw new IllegalArgumentException("Given username already exists.");

        // Password Encoding
        PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        ObjectMapper mapper = new ObjectMapper();
        String jsonified = mapper.writeValueAsString(user);

        storage.put(user.getUsername(), jsonified);
    }

    @Override
    public void deleteUser(String username) throws IOException, IllegalArgumentException {
        if (username.length() <= 0)
            throw new IllegalArgumentException("Username is needed.");
        if (!storage.containsKey(username))
            throw new IllegalArgumentException("Given username doesn't exists.");

        storage.remove(username);
    }

    @Override
    public void editUser(User user) throws IOException, IllegalArgumentException {
        validateUser(user);

        if (!storage.containsKey(user.getUsername()))
            throw new IllegalArgumentException("Given username doesn't exists.");

        // Password Encoding
        PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        ObjectMapper mapper = new ObjectMapper();
        String jsonified = mapper.writeValueAsString(user);

        storage.put(user.getUsername(), jsonified);
    }

    private void validateUser(User user) throws IllegalArgumentException {
        if (user.getRoles().length <= 0)
            throw new IllegalArgumentException("User must have least one role.");
        if (user.getUsername().length() <= 0)
            throw new IllegalArgumentException("Username is needed.");
        if (user.getPassword().length() <= 0)
            throw new IllegalArgumentException("Password is needed.");
    }
}
