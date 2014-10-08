package org.apache.component.dependency.plugins;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

/**
 * @author Von Gosling
 */
public class DependencyMediatorMojoTest extends AbstractMojoTestCase {

    // ----------------------------------------------------------------------
    // Initialization-on-demand
    // ----------------------------------------------------------------------
    private static final class Lazy {
        static {
            final String path = System.getProperty("basedir");
            BASEDIR = null != path ? path : new File("").getAbsolutePath();
        }

        static final String BASEDIR;
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------
    public static String getBasedir() {
        return Lazy.BASEDIR;
    }

    public static File getTestFile(final String path) {
        return getTestFile(getBasedir(), path);
    }

    public static File getTestFile(final String basedir, final String path) {
        File root = new File(basedir);
        if (!root.isAbsolute()) {
            root = new File(getBasedir(), basedir);
        }
        return new File(root, path);
    }

    public static String getTestPath(final String path) {
        return getTestFile(path).getAbsolutePath();
    }

    public static String getTestPath(final String basedir, final String path) {
        return getTestFile(basedir, path).getAbsolutePath();
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //    @Rule
    //    public MojoRule rule = new MojoRule() {
    //                             @Override
    //                             protected void before() throws Throwable {
    //                                super.before();
    //                             }
    //
    //                             @Override
    //                             protected void after() {
    //                                 super.after();
    //                             }
    //                         };

    //    @Test
    //    public void testMojoExecution() throws Exception {
    //        DependencyMediatorMojo myMojo = (DependencyMediatorMojo) rule.lookupConfiguredMojo(
    //                getTestFile("src/test/resources/unit"), "check");
    //        assertNotNull(myMojo);
    //        myMojo.execute();
    //    }

    @Test
    public void testMojoExecution() throws Exception {
        File testPom = getTestFile("src/test/resources/unit/pom.xml");
        assertNotNull(testPom);
        assertTrue(testPom.exists());

        DependencyMediatorMojo myMojo = (DependencyMediatorMojo) lookupMojo("check", testPom);
        assertNotNull(myMojo);
        myMojo.execute();
    }

}
