package de.tudbut.api;

/**
 * Representation of a version of TTC
 *
 * @author TudbuT
 * @since 03 Jun 2022
 */

public class TTCVersion {
    private final String version;
    
    public TTCVersion(String version) {
        this.version = version;
    }
    public TTCVersion(String app, String repo, String version) {
        this.version = app + " " + repo + "@" + version;
    }
    public TTCVersion(String app, String repoOwner, String repoName, String repoBranch, String version) {
        this.version = app + " " + repoOwner + "/" + repoName + ":" + repoBranch + "@" + version;
    }
    
    public boolean isOlderThan(TTCVersion other) {
        String version = getVersion();
        String otherVersion = other.getVersion();
        String[] numbersCurrent = version.substring(1, version.length() - 1).split("[.]");
        String[] numbers = otherVersion.substring(1, version.length() - 1).split("[.]");
        for(int i = 0; i < numbers.length; i++) {
            numbers[i] = numbers[i].replaceAll("[a-zA-Z](.*)", "$1");
            numbersCurrent[i] = numbersCurrent[i].replaceAll("[a-zA-Z](.*)", "$1");
            if(Integer.parseInt(numbers[i]) > Integer.parseInt(numbersCurrent[i]))
                return true;
        }
        return otherVersion.charAt(otherVersion.length() - 1) > version.charAt(version.length() - 1);
    }
    
    public String getVersion() {
        return version.replaceAll(".*@(v.*)$", "$1");
    }
    
    public String getApp() {
        return version.split(" ")[0];
    }
    
    public String getRepo() {
        return version.replaceAll(".* (.*)@.*", "$1");
    }
    
    public String getRepoOwner() {
        return version.replaceAll(".* (.*)/.*", "$1");
    }
    public String getRepoName() {
        return version.replaceAll(".* .*/(.*):.*", "$1");
    }
    public String getRepoBranch() {
        return version.replaceAll(".* .*/.*:(.*)@.*", "$1");
    }
    
    public String toString() {
        return version;
    }

}
