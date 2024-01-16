package server;

import server.exceptions.InvalidUserException;
import server.exceptions.UserNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerDataTest {

    private static ServerData serverData;
    private final Set<String> files1 = Set.of(
            "/home/ivanP/pictures/123.png",
            "C:\\Users\\ivanP\\music\\song.mp3",
            "D:\\Games\\Battlefront\\scores\\ivan1234\\score.txt");
    private final Set<String> files2 = Set.of(
            "/home/ivanP/pictures/tree.jpeg",
            "C:\\Users\\ivanP\\video\\cats.mp4",
            "D:\\Games\\Battlefront\\scores\\ivan1234\\score.txt");

    @Before
    public void setServerData() {
        serverData = new ServerData();
    }

    @Test
    public void testRegister() throws InvalidUserException {
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", files1));

        Set<String> files = serverData.listFiles();

        // Testing if files are registered correctly
        assertTrue(files.contains("ivan1234 : /home/ivanP/pictures/123.png"));
        assertTrue(files.contains("ivan1234 : C:\\Users\\ivanP\\music\\song.mp3"));
        assertTrue(files.contains("ivan1234 : D:\\Games\\Battlefront\\scores\\ivan1234\\score.txt"));

        // Testing if user address is registered correctly
        assertTrue(serverData.listAddresses().contains("ivan1234 - 127.0.0.1:1234"));
    }

    @Test
    public void testRegisterMultiple() throws InvalidUserException {
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", new HashSet<>(files1)));
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", files2));

        Set<String> files = serverData.listFiles();

        // Testing if files are registered correctly
        assertEquals(5, files.size());
        assertTrue(files.contains("ivan1234 : /home/ivanP/pictures/123.png"));
        assertTrue(files.contains("ivan1234 : C:\\Users\\ivanP\\music\\song.mp3"));
        assertTrue(files.contains("ivan1234 : D:\\Games\\Battlefront\\scores\\ivan1234\\score.txt"));
        assertTrue(files.contains("ivan1234 : /home/ivanP/pictures/tree.jpeg"));
        assertTrue(files.contains("ivan1234 : C:\\Users\\ivanP\\video\\cats.mp4"));

        // Testing if user address is registered correctly
        assertTrue(serverData.listAddresses().contains("ivan1234 - 127.0.0.1:1234"));
    }

    @Test (expected = InvalidUserException.class)
    public void testRegisterOneUserTwoAddresses() throws InvalidUserException {
        // Testing exception throw when one user tries to join from multiple addresses
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", new HashSet<>(files1)));
        serverData.register("ivan1234", new UserData("127.0.0.2:1234", files2));
    }

    @Test
    public void testUnregister() throws InvalidUserException, UserNotFoundException {
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", new HashSet<>(files1)));
        serverData.unregister("ivan1234", Set.of("C:\\Users\\ivanP\\music\\song.mp3"));

        Set<String> files = serverData.listFiles();

        // Testing if files are unregistered correctly
        assertEquals(2, files.size());
        assertTrue(files.contains("ivan1234 : /home/ivanP/pictures/123.png"));
        assertTrue(files.contains("ivan1234 : D:\\Games\\Battlefront\\scores\\ivan1234\\score.txt"));
    }

    @Test (expected = UserNotFoundException.class)
    public void testUnregisterInvalidUser() throws InvalidUserException, UserNotFoundException {
        // Testing exception throw when unregister is invoked with nonexistent user
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", new HashSet<>(files1)));
        serverData.unregister("petko1234", Set.of("C:\\Users\\ivanP\\music\\song.mp3"));
    }

    @Test
    public void testDisconnect() throws InvalidUserException, UserNotFoundException {
        final int ONE_USER = 1;
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", files1));
        serverData.register("petko1234", new UserData("127.0.0.2:1234", files2));
        serverData.disconnect("ivan1234");

        assertEquals("Testing unregistering of the user files after disconnect",
                serverData.listFiles().size(), files2.size());
        assertEquals("Testing unregistering of the user address after disconnect",
                ONE_USER, serverData.listAddresses().size());
    }

    @Test (expected = UserNotFoundException.class)
    public void testDisconnectInvalidUser() throws InvalidUserException, UserNotFoundException {
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", files1));
        // Testing exception throw when disconnect is invoked with invalid user
        serverData.disconnect("petko1234");
    }

    @Test
    public void testListFiles() throws InvalidUserException {
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", files1));
        serverData.register("petko1234", new UserData("127.0.0.2:1234", files2));

        Set<String> files = serverData.listFiles();

        // Testing if files are listed correctly
        assertEquals(6, files.size());
        assertTrue(files.contains("ivan1234 : /home/ivanP/pictures/123.png"));
        assertTrue(files.contains("ivan1234 : C:\\Users\\ivanP\\music\\song.mp3"));
        assertTrue(files.contains("ivan1234 : D:\\Games\\Battlefront\\scores\\ivan1234\\score.txt"));
        assertTrue(files.contains("petko1234 : /home/ivanP/pictures/tree.jpeg"));
        assertTrue(files.contains("petko1234 : C:\\Users\\ivanP\\video\\cats.mp4"));
        assertTrue(files.contains("petko1234 : D:\\Games\\Battlefront\\scores\\ivan1234\\score.txt"));
    }

    @Test
    public void testListAddresses() throws InvalidUserException {
        serverData.register("ivan1234", new UserData("127.0.0.1:1234", files1));
        serverData.register("petko1234", new UserData("127.0.0.2:1234", files2));
        serverData.register("kris1234", new UserData("127.0.0.2:1234", files2));

        Set<String> addresses = serverData.listAddresses();

        //Testing if addresses are listed correctly
        assertEquals(3, addresses.size());
        assertTrue(addresses.contains("ivan1234 - 127.0.0.1:1234"));
        assertTrue(addresses.contains("petko1234 - 127.0.0.2:1234"));
        assertTrue(addresses.contains("kris1234 - 127.0.0.2:1234"));
    }
}
