package org.apache.component.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * <p>
 * 1. Support directory scan,including classpath
 * <p>
 * 2. Support component scan,including jar,war,ear and sar
 * <p>
 * 3. Support duplicate classes scan,duplicate means the same fully-qualified
 * class name, but not the same digest or incompatible class(details see <a
 * href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-13.html">jls</a>
 * and <a href=
 * "http://www.oracle.com/technetwork/java/javase/compatibility-137541.html"
 * >class compatibility</a>)
 * <p>
 * 4. Check who using this duplicate classes
 * 
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public class DependencyMediator {

    /**
     * Whether to check <code>.jar</code> inside files
     */
    private static boolean      checkJars        = true;

    /**
     * Whether to check class compatible
     */
    private static boolean      checkCompatible  = true;

    public static final String  CLASS_SUFFIX     = ".class";
    public static final Pattern JAR_FILE_PATTERN = Pattern.compile("^.+\\.(jar|JAR)$");

    /**
     * Recursively finds class files and process
     * 
     * @param file Directory full of class files or jar files (in which case all
     *            of them are processed recursively), or a class file (in which
     *            case that single class is processed), or a jar file (in which
     *            case all the classes in this jar file are processed.)
     */
    public static void process(File file) throws IOException {
        List<File> files = new ArrayList<File>();
        if (file.isDirectory()) {
            files = processDirectory(file);
            for (File f : files) {
                doProcess(f);
            }
        } else {
            doProcess(file);
        }
        if (checkCompatible) {
            //processCompatible(file, jarFile, classMap);
        }

    }

    private static String getFileExtension(String fullName) {
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    protected static void doProcess(File file) throws IOException {
        String fileFormat = getFileExtension(file.getName());
        ComponentFormat compFormat = ComponentFormat.fromString(fileFormat);
        if (null == compFormat) {
            System.err.printf("Not support file format [%s] now !", file.getName());
            System.exit(-1);
        }
        switch (compFormat) {
            case WAR:
            case EAR:
            case SAR:
            case ZIP:
            case GZIP:
            case JAR:
                processJarFile(file, checkJars);
                break;
            case CLASS:
                processClassFile(file);
                break;
            default:
                break;
        }
    }

    protected static List<File> processDirectory(File dir) throws IOException {
        List<File> totalFiles = new ArrayList<File>();
        listFiles(dir, totalFiles);
        //Ensure that outer classes are visited before inner classes
        Collections.sort(totalFiles, new Comparator<File>() {
            public int compare(File file1, File file2) {
                String n1 = file1.getName();
                String n2 = file2.getName();
                int diff = n1.length() - n2.length();
                return diff != 0 ? diff : n1.compareTo(n2);
            }
        });
        return totalFiles;
    }

    protected static void listFiles(File dir, List<File> totalFiles) {
        //Performance problems: using Files.newDirectoryStream
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    listFiles(f, totalFiles);
                } else {
                    if (JAR_FILE_PATTERN.matcher(f.getName()).matches()
                            || f.getName().endsWith(CLASS_SUFFIX)) {
                        totalFiles.add(f);
                    }
                }
            }
        }
    }

    /**
     * Nothing to do about the Class-Path property in MANIFEST.MF file now
     * 
     * @param file
     * @param checkJars
     * @throws IOException
     */
    public static void processJarFile(File file, boolean checkJars) throws IOException {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);
            if (checkJars) {
                Enumeration<JarEntry> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry jarEntry = jarEntries.nextElement();
                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }
                    //Check whether  the same class
                    String keyName = jarEntry.getName()
                            .substring(0, jarEntry.getName().length() - 6).replace("/", ".");
                    ComponentEntry cEntry = new ComponentEntry();
                    cEntry.setPathName(jarFile.getName() + ":" + jarEntry.getName());
                    cEntry.setJarName(jarFile.getName());
                    cEntry.setName(keyName);
                    cEntry.setEntry(jarEntry);
                    cEntry.setDigest(getDigest(jarFile.getInputStream(jarEntry)));
                }
            } else {
                //Handle MANIFEST 
                String name = jarFile.getName().substring(jarFile.getName().lastIndexOf("/") + 1);
                Attributes attr = jarFile.getManifest().getMainAttributes();
                String buildJdk = attr.getValue("Build-Jdk");
                String builtBy = attr.getValue("Built-By");
                String keyName = name;
                if (!buildJdk.isEmpty()) {
                    keyName = keyName + ":" + buildJdk;
                }
                if (!builtBy.isEmpty()) {
                    keyName = keyName + ":" + builtBy;
                }
                ComponentEntry cEntry = new ComponentEntry();
                cEntry.setName(name);
                cEntry.setPathName(jarFile.getName());
                cEntry.setDigest(getDigest(new FileInputStream(new File(jarFile.getName()))));

                ComponentContainer.put(keyName, cEntry);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (null != jarFile) {
                jarFile.close();
            }
        }
    }

    public static void output(HashMap<String, TreeSet<ComponentEntry>> classMap) {
        System.out.println("Output component reactor info......");
        int count = 0;
        for (Entry<String, TreeSet<ComponentEntry>> entry : classMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                count++;
                System.out.printf("Duplicated component  [%s] was founded in the  path : \n",
                        entry.getKey());
                for (ComponentEntry jar : entry.getValue()) {
                    System.out.printf(" \t%s\n", jar.getPathName());
                }
            }
        }
        if (count == 0) {
            System.out.println("Congratulations,no component conflict or incompatible !");
        }
    }

    //    private static void processCompatible(File file, JarFile jarFile,
    //                                          HashMap<String, TreeSet<ComponentEntry>> classMap)
    //            throws IOException {
    //        Iterator<Entry<String, TreeSet<ComponentEntry>>> iter = classMap.entrySet().iterator();
    //        while (iter.hasNext()) {
    //            Entry<String, TreeSet<ComponentEntry>> jarEntryInfoEntry = iter.next();
    //            Set<ComponentEntry> jarEntryInfos = jarEntryInfoEntry.getValue();
    //            Iterator<ComponentEntry> jarEntryInfoIter = jarEntryInfos.iterator();
    //            while (jarEntryInfoIter.hasNext()) {
    //                JarEntry jarEntry = jarEntryInfoIter.next().getEntry();
    //                InputStream is = jarFile.getInputStream(jarEntry);
    //                loadByteCode(file.getPath() + ":" + jarEntry.getName(), is);
    //            }
    //        }
    //    }

    private static void loadByteCode(final String fileName, final InputStream is)
            throws IOException {
        {
            try {
                FileInputStream fis = new FileInputStream(new File(fileName));
                final byte[] dd = getDigest(fis);
                ClassReader cr = new ClassReader(is);
                cr.accept(new ClassVisitor(Opcodes.ASM5) {
                    public void visit(int version, int access, String name, String signature,
                                      String superName, String[] interfaces) {
                        //Check whether  the same class
                        String keyName = name.replace('/', '.');
                        ComponentEntry cEntry = new ComponentEntry();
                        cEntry.setPathName(fileName);
                        cEntry.setName(keyName);

                        cEntry.setDigest(dd);

                        ComponentContainer.put(keyName, cEntry);
                    }
                }, 0);

            } catch (ArrayIndexOutOfBoundsException e) {
                // MANIMALSNIFFER-9 it is a pity that ASM does not throw a nicer error on encountering a malformed class file.
                IOException ioException = new IOException("Bad class file " + fileName);
                ioException.initCause(e);
                throw ioException;
            }
        }
    }

    private static byte[] getDigest(InputStream is) {
        DigestInputStream dis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(is, md);
            byte[] bytes = new byte[1024];
            int numBytes = -1;
            while ((numBytes = is.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
            return md.digest();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    protected static void processClassFile(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            loadByteCode(file.getPath(), in);
        } finally {
            in.close();
        }
    }

    private static Collection<URLClassLoader> getClassLoaders(ClassLoader baseClassLoader) {
        Collection<URLClassLoader> loaders = new ArrayList<URLClassLoader>();
        ClassLoader loader = baseClassLoader;
        while (loader != null) {
            //Ignore 
            if ("sun.misc.Launcher$ExtClassLoader".equals(loader.getClass().getName())) {
                break;
            }
            if (loader instanceof URLClassLoader) {
                loaders.add((URLClassLoader) loader);
            }
            loader = loader.getParent();
        }
        return loaders;
    }

    public static void scanClassPath() {
        Set<URLClassLoader> loaders = new LinkedHashSet<URLClassLoader>();
        loaders.addAll(getClassLoaders(Thread.currentThread().getContextClassLoader()));
        loaders.addAll(getClassLoaders(DependencyMediator.class.getClassLoader()));

        for (URLClassLoader cl : loaders) {
            for (URL url : cl.getURLs()) {
                String file = url.getFile();
                File dir = new File(file);
                try {
                    process(dir);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void main(String args[]) {
        File dir = null;
        boolean scanClasspath = SystemPropertyUtils.getBoolean("scanClasspath", false);
        if (args.length == 0) {
            if (scanClasspath) {
                scanClassPath();
            }
        } else {
            dir = new File(args[0]);
            try {
                process(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        output(ComponentContainer.compMaps);
    }
}
