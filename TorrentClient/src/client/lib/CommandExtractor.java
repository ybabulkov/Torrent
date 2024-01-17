package client.lib;

public class CommandExtractor {

    public static String extractCommandPrefix(String command) {
        int spaceIndex = command.trim().indexOf(' ');
        return (spaceIndex != -1) ? command.substring(0, spaceIndex) : command;
    }

}
