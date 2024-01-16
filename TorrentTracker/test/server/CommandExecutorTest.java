package server;

import server.exceptions.InvalidUserException;
import server.exceptions.UserNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CommandExecutorTest {
    private static final String SINGLE_LINE_PREFIX = "1" + System.lineSeparator();

    @Mock
    private ServerData serverData;
    @Mock
    private SocketChannel sc;
    @Mock
    private InetAddress ip;

    private CommandExecutor commandExecutor;


    @Before
    public void setCommandExecutor() {
        sc = mock(SocketChannel.class);
        serverData = mock(ServerData.class);
        ip = mock(InetAddress.class);
        commandExecutor = new CommandExecutor(serverData);
    }

    @Test
    public void testRegister() throws InvalidUserException {
        when(ip.getHostAddress()).thenReturn("127.0.0.1");

        String command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals("Testing reply when register is invoked",
                SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(sc, command, ip));

        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");
        verify(serverData, times(1))
                .register("ivan1234", new UserData("127.0.0.1:1234", expectedSet));
    }

    @Test
    public void testRegisterNoPort() {
        when(ip.getHostAddress()).thenReturn("127.0.0.1");

        String command = "register ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals("Testing reply when port is missing",
                SINGLE_LINE_PREFIX + "Could not retrieve port.",
                commandExecutor.execute(sc, command, ip));
    }

    @Test
    public void testRegisterInvalidUser() throws InvalidUserException {
        when(ip.getHostAddress()).thenReturn("127.0.0.1");
        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");

        doThrow(new InvalidUserException("ivan1234 has already registered from another address!"))
                .when(serverData).register("ivan1234", new UserData("127.0.0.1:1234", expectedSet));

        String command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        String expectedResult = SINGLE_LINE_PREFIX + "ivan1234 has already registered from another address!";
        assertEquals("Testing reply when invalidUserException is thrown",
                expectedResult, commandExecutor.execute(sc, command, ip));

    }

    @Test
    public void testRegisterAnotherUserSameChannel() throws InvalidUserException {
        when(ip.getHostAddress()).thenReturn("127.0.0.1");

        String command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(sc, command, ip));

        command = "register 1234 petko1234 /home/ivan/pictures/123.jpg";
        assertEquals("Testing reply when register is invoked with another user",
                SINGLE_LINE_PREFIX + "This session is associated with another user.",
                commandExecutor.execute(sc, command, ip));

        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");
        verify(serverData, times(1))
                .register("ivan1234", new UserData("127.0.0.1:1234", expectedSet));
    }

    @Test
    public void testRegisterSameUserSameChannel() throws InvalidUserException {
        when(ip.getHostAddress()).thenReturn("127.0.0.1");

        String command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(sc, command, ip));

        command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(sc, command, ip));

        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");
        verify(serverData, times(2))
                .register("ivan1234", new UserData("127.0.0.1:1234", expectedSet));
    }

    @Test
    public void testRegisterSameUserAnotherChannel() throws InvalidUserException {
        when(ip.getHostAddress()).thenReturn("127.0.0.1");

        String command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(sc, command, ip));

        SocketChannel otherSc = mock(SocketChannel.class);
        command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(otherSc, command, ip));

        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");
        verify(serverData, times(2))
                .register("ivan1234", new UserData("127.0.0.1:1234", expectedSet));
    }

    @Test
    public void testUnregister() throws UserNotFoundException {
        String command = "unregister ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully unregistered!",
                commandExecutor.execute(sc, command, ip));

        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");
        verify(serverData, times(1))
                .unregister("ivan1234", expectedSet);
    }

    @Test
    public void testUnregisterUserNotFound() throws UserNotFoundException {
        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");
        doThrow(new UserNotFoundException("ivan1234 is not registered!"))
                .when(serverData).unregister("ivan1234", expectedSet);

        String command = "unregister ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals("Testing unregister when userNotFoundException is thrown",
                SINGLE_LINE_PREFIX + "ivan1234 is not registered!",
                commandExecutor.execute(sc, command, ip));
    }

    @Test
    public void testUnregisterAnotherUserSameChannel() throws UserNotFoundException {
        when(ip.getHostAddress()).thenReturn("127.0.0.1");

        String command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(sc, command, ip));

        command = "unregister petko1234 /home/ivan/pictures/123.jpg";
        assertEquals("Testing reply when unregister is invoked with another user",
                SINGLE_LINE_PREFIX + "This session is associated with another user.",
                commandExecutor.execute(sc, command, ip));

        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");
        verify(serverData, times(0))
                .unregister("ivan1234", expectedSet);
    }

    @Test
    public void disconnect() throws InvalidUserException {
        when(ip.getHostAddress()).thenReturn("127.0.0.1");

        String command = "register 1234 ivan1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(sc, command, ip));

        commandExecutor.disconnect(sc);

        command = "register 1234 petko1234 /home/ivan/pictures/123.jpg";
        assertEquals(SINGLE_LINE_PREFIX + "File(s) successfully registered!",
                commandExecutor.execute(sc, command, ip));

        Set<String> expectedSet = Set.of("/home/ivan/pictures/123.jpg");
        verify(serverData, times(1))
                .register("ivan1234", new UserData("127.0.0.1:1234", expectedSet));

        verify(serverData, times(1))
                .register("petko1234", new UserData("127.0.0.1:1234", expectedSet));
    }

    @Test
    public void listFiles() {
        String file1 = "ivan1234 : /home/ivan/pictures/123.jpg";
        String file2 = "petar5678 : D:\\music\\imperialMarch.mp3";
        when(serverData.listFiles()).thenReturn(new HashSet<>(Set.of(file1, file2)));

        String expected = "2" + System.lineSeparator()
                + "ivan1234 : /home/ivan/pictures/123.jpg"
                + System.lineSeparator()
                + "petar5678 : D:\\music\\imperialMarch.mp3"
                + System.lineSeparator();
        assertEquals(expected, commandExecutor.execute(sc, "list-files", ip));
    }

    @Test
    public void listAddresses() {
        String address1 = "ivan1234 - 127.0.0.1:1234";
        String address2 = "petar5678 - 127.0.0.2:1234";
        when(serverData.listAddresses()).thenReturn(new HashSet<>(Set.of(address1, address2)));

        String expected = "2" + System.lineSeparator()
                + "petar5678 - 127.0.0.2:1234"
                + System.lineSeparator()
                + "ivan1234 - 127.0.0.1:1234"
                + System.lineSeparator();
        assertEquals(expected, commandExecutor.execute(sc, "list-addresses", ip));
    }

    @Test
    public void unknownCommand() {
        assertEquals(SINGLE_LINE_PREFIX + "Unknown command!", commandExecutor.execute(sc, "gfssadger", ip));
    }
}
