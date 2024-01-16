package server;

import java.util.Set;

public record UserData(String address, Set<String> files) {
    public void addFiles(Set<String> filesToAdd) {
        files.addAll(filesToAdd);
    }

    public void removeFiles(Set<String> filesToRemove) {
        files.removeAll(filesToRemove);
    }
}
