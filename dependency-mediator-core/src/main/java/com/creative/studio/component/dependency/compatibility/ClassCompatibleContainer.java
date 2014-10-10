package com.creative.studio.component.dependency.compatibility;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public class ClassCompatibleContainer {

    private Map<String, List<Map<String, String>>> container = new TreeMap<String, List<Map<String, String>>>();

    /**
     * @return the container
     */
    public Map<String, List<Map<String, String>>> getContainer() {
        return container;
    }

    /**
     * @param container the container to set
     */
    public void setContainer(Map<String, List<Map<String, String>>> container) {
        this.container = container;
    }

}
