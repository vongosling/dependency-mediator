package com.creative.studio.component.dependency;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public class ComponentContainer {
    /**
     * Component reactor container
     */
    public static HashMap<String, TreeSet<ComponentEntry>> compMaps = new HashMap<String, TreeSet<ComponentEntry>>();

    public static void put(String keyName, ComponentEntry cEntry) {
        if (compMaps.containsKey(keyName)) {
            Set<ComponentEntry> cSets = compMaps.get(keyName);
            //Digest compare
            if (!cSets.contains(cEntry)) {
                cSets.add(cEntry);
            }
        } else {
            TreeSet<ComponentEntry> entries = new TreeSet<ComponentEntry>();
            entries.add(cEntry);
            compMaps.put(keyName, entries);
        }
    }

}
