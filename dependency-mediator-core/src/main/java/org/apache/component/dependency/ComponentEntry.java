package org.apache.component.dependency;

import java.util.Arrays;
import java.util.jar.JarEntry;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public class ComponentEntry implements Comparable<ComponentEntry> {

    /**
     * Similar file name
     */
    private String   pathName;
    /**
     * Component name ,such as fully-qualified class name or jar name
     */
    private String   name;
    /**
     * Name of the jar which contains this entry,this field may be null
     */
    private String   jarName;
    /**
     * Jar entry meta info,if it is a jar file,this field may be null
     */
    private JarEntry entry;
    /**
     * MD5 info
     */
    private byte[]   digest;

    /**
     * @return the pathName
     */
    public String getPathName() {
        return pathName;
    }

    /**
     * @param pathName the pathName to set
     */
    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the jarName
     */
    public String getJarName() {
        return jarName;
    }

    /**
     * @param jarName the jarName to set
     */
    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    /**
     * @return the entry
     */
    public JarEntry getEntry() {
        return entry;
    }

    /**
     * @param entry the entry to set
     */
    public void setEntry(JarEntry entry) {
        this.entry = entry;
    }

    /**
     * @return the digest
     */
    public byte[] getDigest() {
        return digest;
    }

    /**
     * @param digest the digest to set
     */
    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(digest);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ComponentEntry other = (ComponentEntry) obj;
        if (!Arrays.equals(digest, other.digest))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int compareTo(ComponentEntry o) {
        return equals(o) ? 0 : 1;
    }

}
